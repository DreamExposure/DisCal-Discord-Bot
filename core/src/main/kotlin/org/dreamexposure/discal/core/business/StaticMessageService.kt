package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.`object`.component.LayoutComponent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.discordjson.json.MessageCreateRequest
import discord4j.discordjson.json.MessageEditRequest
import discord4j.rest.http.client.ClientException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.StaticMessageCache
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.database.StaticMessageData
import org.dreamexposure.discal.core.database.StaticMessageRepository
import org.dreamexposure.discal.core.exceptions.NotFoundException
import org.dreamexposure.discal.core.`object`.new.StaticMessage
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class StaticMessageService(
    private val settingsService: GuildSettingsService,
    private val staticMessageRepository: StaticMessageRepository,
    private val staticMessageCache: StaticMessageCache,
    private val calendarService: CalendarService,
    private val rsvpService: RsvpService,
    private val embedService: EmbedService,
    private val componentService: ComponentService,
    private val metricService: MetricService,
    private val beanFactory: BeanFactory,
) {
    private val discordClient: DiscordClient
        get() = beanFactory.getBean()
    private val OVERVIEW_EVENT_COUNT = Config.CALENDAR_OVERVIEW_DEFAULT_EVENT_COUNT.getInt()
        private val MAX_CUTOFF_DAYS = Config.CALENDAR_OVERVIEW_DEFAULT_CUTOFF_DAYS.getInt()

    suspend fun getStaticMessageCount() = staticMessageRepository.count().awaitSingle()

    suspend fun getStaticMessage(guildId: Snowflake, messageId: Snowflake): StaticMessage? {
        var message = staticMessageCache.get(guildId, key = messageId)
        if (message != null) return message

        message = staticMessageRepository.findByGuildIdAndMessageId(guildId.asLong(), messageId.asLong())
            .map(::StaticMessage)
            .awaitSingleOrNull()

        if (message != null) staticMessageCache.put(guildId, key = messageId, message)
        return message
    }

    suspend fun getStaticMessagesForCalendar(guildId: Snowflake, calendarNumber: Int): List<StaticMessage> {
        // TODO: I'm hoping one day I figure out how to do this with caching more easily
        return staticMessageRepository.findAllByGuildIdAndCalendarNumber(guildId.asLong(), calendarNumber)
            .map(::StaticMessage)
            .collectList()
            .awaitSingle()
    }

    suspend fun getStaticMessagesForShard(shardIndex: Int, shardCount: Int): List<StaticMessage> {
        return staticMessageRepository.findAllByShardIndex(shardIndex, shardCount)
            .map(::StaticMessage)
            .collectList()
            .awaitSingle()
    }

    suspend fun getEnabledStaticMessagesForShard(shardIndex: Int, shardCount: Int): List<StaticMessage> {
        return staticMessageRepository.findAllEnabledByShardIndex(shardIndex, shardCount)
            .map(::StaticMessage)
            .collectList()
            .awaitSingle()
    }

    suspend fun createStaticMessage(
        guildId: Snowflake,
        channelId: Snowflake,
        calendarNumber: Int,
        type: StaticMessage.Type,
        updateHour: Long
    ): StaticMessage {
        // Gather everything we need
        val settings = settingsService.getSettings(guildId)
        val calendar = calendarService.getCalendar(guildId, calendarNumber) ?: throw NotFoundException("Calendar not found")
        val channel = discordClient.getChannelById(channelId)
        val nextUpdate = ZonedDateTime.now(calendar.timezone)
            .truncatedTo(ChronoUnit.DAYS)
            .plusHours(updateHour + 24)
            .toInstant()

        val embed: EmbedCreateSpec
        val additionalComponents = mutableListOf<LayoutComponent>()
        var forcedUpdate: Instant? = null

        // Handle type specific behavior and rendering
        when (type) {
            StaticMessage.Type.CALENDAR_OVERVIEW -> {
                val events = calendarService.getUpcomingEvents(guildId, calendarNumber, OVERVIEW_EVENT_COUNT)
                embed = embedService.calendarOverviewEmbed(calendar, events, showUpdate = true)
            }
            StaticMessage.Type.CALENDAR_WEEKLY -> {
                val events = calendarService.getEventsInNextNDays(guildId, calendarNumber, 7)
                embed = embedService.calendarWeekOverviewEmbed(calendar, events, showUpdate = true)
            }
            StaticMessage.Type.NEXT_EVENT -> {
                val event = calendarService.getUpcomingEvents(guildId, calendarNumber, 1).firstOrNull()
                if (event != null) {
                    additionalComponents.addAll(componentService.getEventRsvpComponents(event, settings))
                    forcedUpdate = event.end
                }

                embed = embedService.nextUpcomingEventEmbed(event, null, settings, includeRsvp = false, showUpdate = true)
            }
            StaticMessage.Type.NEXT_EVENT_WITH_RSVP -> {
                val event = calendarService.getUpcomingEvents(guildId, calendarNumber, 1).firstOrNull()
                val rsvp = if (event == null) null else rsvpService.getRsvp(guildId, event.id)
                if (event != null) {
                    additionalComponents.addAll(componentService.getEventRsvpComponents(event, settings, true))
                    forcedUpdate = event.end
                }

                embed = embedService.nextUpcomingEventEmbed(event, rsvp, settings, includeRsvp = true, showUpdate = true)
            }
        }


        // Finally create the message
        val message = channel.createMessage(
            MessageCreateRequest.builder()
                .addEmbed(embed.asRequest())
                .components((additionalComponents + componentService.getStaticMessageComponents()).map { it.data })
                .build()
        ).awaitSingle()
        val saved = staticMessageRepository.save(
            StaticMessageData(
                guildId = guildId.asLong(),
                messageId = message.id().asLong(),
                channelId = channelId.asLong(),
                type = type.value,
                lastUpdate = Instant.now(),
                scheduledUpdate = nextUpdate,
                forcedUpdate = forcedUpdate,
                enabled = true,
                calendarNumber = calendarNumber,
            )
        ).map(::StaticMessage).awaitSingle()

        staticMessageCache.put(guildId, key = saved.messageId, saved)
        return saved
    }

    suspend fun updateStaticMessage(guildId: Snowflake, messageId: Snowflake) {
        val taskTimer = StopWatch()
        taskTimer.start()

        val old = getStaticMessage(guildId, messageId) ?: throw NotFoundException("Static message not found")

        // While we don't need the message data, we do want to make sure it exists
        val existingData = discordClient.getMessageById(old.channelId, old.messageId)
            .data.onErrorResume(ClientException.isStatusCode(403, 404)) { Mono.empty() }
            .awaitSingleOrNull()

        if (existingData == null) {
            // Message or channel was deleted OR access was revoked, treat this as deleted
            deleteStaticMessage(guildId, old.messageId)
            return
        }

        // Check if the message is in a thread that is archived
        val channelData = discordClient.getChannelById(old.channelId)
            .data.onErrorResume(ClientException.isStatusCode(403, 404)) { Mono.empty() }
                .awaitSingleOrNull()

        if (channelData == null) {
            // Somehow the message exists but the channel doesn't? this code should never be called, but just in case lol
            deleteStaticMessage(guildId, old.messageId)
            return
        }

        // Check if channel is archived - set as disabled
         if (channelData.threadMetadata().isPresent && channelData.threadMetadata().get().archived()) {
             val updated = old.copy(enabled = false)

             staticMessageRepository.updateByGuildIdAndMessageId(
                 guildId = updated.guildId.asLong(),
                 messageId = updated.messageId.asLong(),
                 channelId = updated.channelId.asLong(),
                 type = updated.type.value,
                 lastUpdate = updated.lastUpdate,
                 scheduledUpdate = updated.scheduledUpdate,
                 forcedUpdate = updated.forcedUpdate,
                 enabled = updated.enabled,
                 calendarNumber = updated.calendarNumber,
             ).awaitSingleOrNull()

             staticMessageCache.put(guildId, key = updated.messageId, updated)
             return
         }

        val calendar = calendarService.getCalendar(guildId, old.calendarNumber) ?: throw NotFoundException("Calendar not found")
        val settings = settingsService.getSettings(guildId)

        // Finally update the message
        var forcedUpdate: Instant? = null
        val additionalComponents = mutableListOf<LayoutComponent>()
        val embed: EmbedCreateSpec

        // Handle type specific behavior and rendering
        when (old.type) {
            StaticMessage.Type.CALENDAR_OVERVIEW -> {
                val events = calendarService.getUpcomingEvents(guildId, old.calendarNumber, OVERVIEW_EVENT_COUNT, MAX_CUTOFF_DAYS)
                embed = embedService.calendarOverviewEmbed(calendar, events, showUpdate = true)
            }
            StaticMessage.Type.CALENDAR_WEEKLY -> {
                val events = calendarService.getEventsInNextNDays(guildId, old.calendarNumber, 7)
                embed = embedService.calendarWeekOverviewEmbed(calendar, events, showUpdate = true)
            }
            StaticMessage.Type.NEXT_EVENT -> {
                val event = calendarService.getUpcomingEvents(guildId, old.calendarNumber, 1).firstOrNull()
                if (event != null) {
                    additionalComponents.addAll(componentService.getEventRsvpComponents(event, settings))
                    forcedUpdate = event.end
                }

                embed = embedService.nextUpcomingEventEmbed(event, null, settings, includeRsvp = false, showUpdate = true)
            }
            StaticMessage.Type.NEXT_EVENT_WITH_RSVP -> {
                val event = calendarService.getUpcomingEvents(guildId, old.calendarNumber, 1).firstOrNull()
                val rsvp = if (event == null) null else rsvpService.getRsvp(guildId, event.id)
                if (event != null) {
                    additionalComponents.addAll(componentService.getEventRsvpComponents(event, settings, true))
                    forcedUpdate = event.end
                }

                embed = embedService.nextUpcomingEventEmbed(event, rsvp, settings, includeRsvp = true, showUpdate = true)
            }
        }

        discordClient.getMessageById(old.channelId, old.messageId).edit(
            MessageEditRequest.builder()
                .addEmbed(embed.asRequest())
                .componentsOrNull((additionalComponents + componentService.getStaticMessageComponents()).map { it.data })
                .build()
        ).awaitSingleOrNull()

        val updated = old.copy(
            lastUpdate = Instant.now(),
            scheduledUpdate = if (old.scheduledUpdate.isBefore(Instant.now())) old.scheduledUpdate.plus(1, ChronoUnit.DAYS) else old.scheduledUpdate,
            forcedUpdate = forcedUpdate,
            enabled = true,
        )
        staticMessageRepository.updateByGuildIdAndMessageId(
            guildId = updated.guildId.asLong(),
            messageId = updated.messageId.asLong(),
            channelId = updated.channelId.asLong(),
            type = updated.type.value,
            lastUpdate = updated.lastUpdate,
            scheduledUpdate = updated.scheduledUpdate,
            forcedUpdate = updated.forcedUpdate,
            enabled = updated.enabled,
            calendarNumber = updated.calendarNumber,
        ).awaitSingleOrNull()

        staticMessageCache.put(guildId, key = updated.messageId, updated)

        taskTimer.stop()
        metricService.recordStaticMessageTaskDuration("single", taskTimer.totalTimeMillis)
        metricService.incrementStaticMessagesUpdated(updated.type)
    }

    suspend fun updateStaticMessages(guildId: Snowflake, calendarNumber: Int, eventOnly: Boolean = false) {
        val taskTimer = StopWatch()
        taskTimer.start()

        val oldVersions = getStaticMessagesForCalendar(guildId, calendarNumber)
            .filter { it.enabled }
            .filter { if (eventOnly) it.type.isEventSpecific() else true }
        val calendar = calendarService.getCalendar(guildId, calendarNumber) ?: throw NotFoundException("Calendar not found")
        val settings = settingsService.getSettings(guildId)

        oldVersions.forEach { old ->
            val existingData = discordClient.getMessageById(old.channelId, old.messageId)
                .data.onErrorResume(ClientException.isStatusCode(403, 404)) { Mono.empty() }
                .awaitSingleOrNull()

            if (existingData == null) {
                // Message or channel was deleted OR access was revoked, treat this as deleted
                deleteStaticMessage(guildId, old.messageId)
                return@forEach
            }

            // Check if the message is in a thread that is archived
            val channelData = discordClient.getChannelById(old.channelId)
                .data.onErrorResume(ClientException.isStatusCode(403, 404)) { Mono.empty() }
                .awaitSingleOrNull()

            if (channelData == null) {
                // Somehow the message exists but the channel doesn't? this code should never be called, but just in case lol
                deleteStaticMessage(guildId, old.messageId)
                return@forEach
            }

            // Check if channel is archived - set as disabled
            if (channelData.threadMetadata().isPresent && channelData.threadMetadata().get().archived()) {
                val updated = old.copy(enabled = false)

                staticMessageRepository.updateByGuildIdAndMessageId(
                    guildId = updated.guildId.asLong(),
                    messageId = updated.messageId.asLong(),
                    channelId = updated.channelId.asLong(),
                    type = updated.type.value,
                    lastUpdate = updated.lastUpdate,
                    scheduledUpdate = updated.scheduledUpdate,
                    forcedUpdate = updated.forcedUpdate,
                    enabled = updated.enabled,
                    calendarNumber = updated.calendarNumber,
                ).awaitSingleOrNull()

                staticMessageCache.put(guildId, key = updated.messageId, updated)
                return@forEach
            }

            var forcedUpdate: Instant? = null
            val additionalComponents = mutableListOf<LayoutComponent>()
            val embed: EmbedCreateSpec

            // Handle type specific behavior and rendering
            when (old.type) {
                StaticMessage.Type.CALENDAR_OVERVIEW -> {
                    val events = calendarService.getUpcomingEvents(guildId, calendarNumber, OVERVIEW_EVENT_COUNT)
                    embed = embedService.calendarOverviewEmbed(calendar, events, showUpdate = true)
                }
                StaticMessage.Type.CALENDAR_WEEKLY -> {
                    val events = calendarService.getEventsInNextNDays(guildId, calendarNumber, 7)
                    embed = embedService.calendarWeekOverviewEmbed(calendar, events, showUpdate = true)
                }
                StaticMessage.Type.NEXT_EVENT -> {
                    val event = calendarService.getUpcomingEvents(guildId, calendarNumber, 1).firstOrNull()
                    if (event != null) {
                        additionalComponents.addAll(componentService.getEventRsvpComponents(event, settings))
                        forcedUpdate = event.end
                    }

                    embed = embedService.nextUpcomingEventEmbed(event, null, settings, includeRsvp = false, showUpdate = true)
                }
                StaticMessage.Type.NEXT_EVENT_WITH_RSVP -> {
                    val event = calendarService.getUpcomingEvents(guildId, calendarNumber, 1).firstOrNull()
                    val rsvp = if (event == null) null else rsvpService.getRsvp(guildId, event.id)
                    if (event != null) {
                        additionalComponents.addAll(componentService.getEventRsvpComponents(event, settings, true))
                        forcedUpdate = event.end
                    }

                    embed = embedService.nextUpcomingEventEmbed(event, rsvp, settings, includeRsvp = true, showUpdate = true)
                }
            }

            discordClient.getMessageById(old.channelId, old.messageId).edit(
                MessageEditRequest.builder()
                    .addEmbed(embed.asRequest())
                    .componentsOrNull((additionalComponents + componentService.getStaticMessageComponents()).map { it.data })
                    .build()
            ).awaitSingleOrNull()

            val updated = old.copy(
                lastUpdate = Instant.now(),
                scheduledUpdate = if (old.scheduledUpdate.isBefore(Instant.now())) old.scheduledUpdate.plus(1, ChronoUnit.DAYS) else old.scheduledUpdate,
                forcedUpdate = forcedUpdate,
                enabled = true,
            )
            staticMessageRepository.updateByGuildIdAndMessageId(
                guildId = updated.guildId.asLong(),
                messageId = updated.messageId.asLong(),
                channelId = updated.channelId.asLong(),
                type = updated.type.value,
                lastUpdate = updated.lastUpdate,
                scheduledUpdate = updated.scheduledUpdate,
                forcedUpdate = updated.forcedUpdate,
                enabled = updated.enabled,
                calendarNumber = updated.calendarNumber,
            ).awaitSingleOrNull()

            staticMessageCache.put(guildId, key = updated.messageId, updated)
            metricService.incrementStaticMessagesUpdated(updated.type)
        }

        taskTimer.stop()
        metricService.recordStaticMessageTaskDuration("guild_calendar", taskTimer.totalTimeMillis)
    }

    suspend fun deleteStaticMessage(guildId: Snowflake, messageId: Snowflake) {
        staticMessageRepository.deleteAllByGuildIdAndMessageId(guildId.asLong(), messageId.asLong()).awaitSingleOrNull()
        staticMessageCache.evict(guildId, key = messageId)
    }

    suspend fun deleteStaticMessagesForCalendarDeletion(guildId: Snowflake, calendarNumber: Int) {
        staticMessageRepository.deleteByGuildIdAndCalendarNumber(guildId.asLong(), calendarNumber).awaitSingleOrNull()
        staticMessageRepository.decrementCalendarsByGuildIdAndCalendarNumber(guildId.asLong(), calendarNumber).awaitSingleOrNull()
        staticMessageCache.evictAll(guildId)
    }
}
