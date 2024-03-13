package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.discordjson.json.MessageCreateRequest
import discord4j.discordjson.json.MessageEditRequest
import discord4j.rest.http.client.ClientException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.StaticMessageCache
import org.dreamexposure.discal.core.database.StaticMessageData
import org.dreamexposure.discal.core.database.StaticMessageRepository
import org.dreamexposure.discal.core.exceptions.NotFoundException
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.discord4j.getSettings
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
    private val staticMessageRepository: StaticMessageRepository,
    private val staticMessageCache: StaticMessageCache,
    private val embedService: EmbedService,
    private val componentService: ComponentService,
    private val metricService: MetricService,
    private val beanFactory: BeanFactory,
) {
    private val discordClient: DiscordClient
        get() = beanFactory.getBean()

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

    ///////////////////////////////////////////////////////////////////////////////////////////
    ////// TODO: Need to be able to break some of this out for when I support more types //////
    ///////////////////////////////////////////////////////////////////////////////////////////
    suspend fun createStaticMessage(
        guildId: Snowflake,
        channelId: Snowflake,
        calendarNumber: Int,
        updateHour: Long
    ): StaticMessage {

        // Gather everything we need
        val settings = discordClient.getGuildById(guildId).getSettings().awaitSingle()
        val calendar = discordClient.getGuildById(guildId)
            .getCalendar(calendarNumber)
            .awaitSingleOrNull() ?: throw NotFoundException("Calendar not found")
        val channel = discordClient.getChannelById(channelId)
        val embed = embedService.calendarOverviewEmbed(calendar, settings, showUpdate = true)
        val nextUpdate = ZonedDateTime.now(calendar.timezone)
            .truncatedTo(ChronoUnit.DAYS)
            .plusHours(updateHour + 24)
            .toInstant()


        // Finally create the message
        val message = channel.createMessage(
            MessageCreateRequest.builder()
                .addEmbed(embed.asRequest())
                .components(componentService.getStaticMessageComponents().map { it.data })
                .build()
        ).awaitSingle()
        val saved = staticMessageRepository.save(
            StaticMessageData(
                guildId = guildId.asLong(),
                messageId = message.id().asLong(),
                channelId = channelId.asLong(),
                type = StaticMessage.Type.CALENDAR_OVERVIEW.value,
                lastUpdate = Instant.now(),
                scheduledUpdate = nextUpdate,
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

        val settings = discordClient.getGuildById(guildId).getSettings().awaitSingle()
        val calendar = discordClient.getGuildById(guildId)
            .getCalendar(old.calendarNumber)
            .awaitSingleOrNull() ?: throw NotFoundException("Calendar not found")

        // Finally update the message
        val embed = embedService.calendarOverviewEmbed(calendar, settings, showUpdate = true)

        discordClient.getMessageById(old.channelId, old.messageId).edit(
            MessageEditRequest.builder()
                .addEmbed(embed.asRequest())
                .components(componentService.getStaticMessageComponents().map { it.data })
                .build()
        ).awaitSingleOrNull()

        val updated = old.copy(
            lastUpdate = Instant.now(),
            scheduledUpdate = if (old.scheduledUpdate.isBefore(Instant.now())) old.scheduledUpdate.plus(1, ChronoUnit.DAYS) else old.scheduledUpdate
        )
        staticMessageRepository.updateByGuildIdAndMessageId(
            guildId = updated.guildId.asLong(),
            messageId = updated.messageId.asLong(),
            channelId = updated.channelId.asLong(),
            type = updated.type.value,
            lastUpdate = updated.lastUpdate,
            scheduledUpdate = updated.scheduledUpdate,
            calendarNumber = updated.calendarNumber,
        ).awaitSingleOrNull()

        staticMessageCache.put(guildId, key = updated.messageId, updated)

        taskTimer.stop()
        metricService.recordStaticMessageTaskDuration("single", taskTimer.totalTimeMillis)
        metricService.incrementStaticMessagesUpdated(updated.type)
    }

    suspend fun updateStaticMessages(guildId: Snowflake, calendarNumber: Int) {
        val taskTimer = StopWatch()
        taskTimer.start()

        val oldVersions = getStaticMessagesForCalendar(guildId, calendarNumber)
        val settings = discordClient.getGuildById(guildId).getSettings().awaitSingle()
        val calendar = discordClient.getGuildById(guildId)
            .getCalendar(calendarNumber)
            .awaitSingleOrNull() ?: throw NotFoundException("Calendar not found")
        val embed = embedService.calendarOverviewEmbed(calendar, settings, showUpdate = true)

        oldVersions.forEach { old ->
            val existingData = discordClient.getMessageById(old.channelId, old.messageId)
                .data.onErrorResume(ClientException.isStatusCode(403, 404)) { Mono.empty() }
                .awaitSingleOrNull()

            if (existingData == null) {
                // Message or channel was deleted OR access was revoked, treat this as deleted
                deleteStaticMessage(guildId, old.messageId)
                return@forEach
            }

            discordClient.getMessageById(old.channelId, old.messageId).edit(
                MessageEditRequest.builder()
                    .addEmbed(embed.asRequest())
                    .components(componentService.getStaticMessageComponents().map { it.data })
                    .build()
            ).awaitSingleOrNull()

            val updated = old.copy(
                lastUpdate = Instant.now(),
                scheduledUpdate = if (old.scheduledUpdate.isBefore(Instant.now())) old.scheduledUpdate.plus(1, ChronoUnit.DAYS) else old.scheduledUpdate
            )
            staticMessageRepository.updateByGuildIdAndMessageId(
                guildId = updated.guildId.asLong(),
                messageId = updated.messageId.asLong(),
                channelId = updated.channelId.asLong(),
                type = updated.type.value,
                lastUpdate = updated.lastUpdate,
                scheduledUpdate = updated.scheduledUpdate,
                calendarNumber = updated.calendarNumber,
            ).awaitSingleOrNull()

            staticMessageCache.put(guildId, key = updated.messageId, updated)
            metricService.incrementStaticMessagesUpdated(updated.type)
        }

        taskTimer.stop()
        metricService.recordStaticMessageTaskDuration("guild_calendar", taskTimer.totalTimeMillis)
    }

    suspend fun deleteStaticMessage(guildId: Snowflake, messageId: Snowflake) {
        staticMessageRepository.deleteByGuildIdAndMessageId(guildId.asLong(), messageId.asLong()).awaitSingleOrNull()
        staticMessageCache.evict(guildId, key = messageId)
    }
}
