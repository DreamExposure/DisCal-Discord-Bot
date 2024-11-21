package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.discordjson.json.MessageCreateRequest
import discord4j.rest.http.client.ClientException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.AnnouncementCache
import org.dreamexposure.discal.AnnouncementWizardState
import org.dreamexposure.discal.AnnouncementWizardStateCache
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.database.AnnouncementData
import org.dreamexposure.discal.core.database.AnnouncementRepository
import org.dreamexposure.discal.core.extensions.messageContentSafe
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.Announcement
import org.dreamexposure.discal.core.`object`.new.Event
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant

@Component
class AnnouncementService(
    private val announcementRepository: AnnouncementRepository,
    private val announcementCache: AnnouncementCache,
    private val announcementWizardStateCache: AnnouncementWizardStateCache,
    private val embedService: EmbedService,
    private val calendarService: CalendarService,
    private val metricService: MetricService,
    private val beanFactory: BeanFactory,
) {
    private val discordClient: DiscordClient
        get() = beanFactory.getBean()

    private val PROCESS_GUILD_DEFAULT_UPCOMING_EVENTS_COUNT = Config.ANNOUNCEMENT_PROCESS_GUILD_DEFAULT_UPCOMING_EVENTS_COUNT.getInt()

    suspend fun createAnnouncement(announcement: Announcement): Announcement {
        val saved = announcementRepository.save(AnnouncementData(
            announcementId = announcement.id,
            calendarNumber = announcement.calendarNumber,
            guildId = announcement.guildId.asLong(),
            subscribersRole = announcement.subscribers.roles.joinToString(","),
            subscribersUser = announcement.subscribers.users.map(Snowflake::asLong).joinToString(","),
            channelId = announcement.channelId.asString(),
            announcementType = announcement.type.name,
            modifier = announcement.modifier.name,
            eventId = announcement.eventId ?: "N/a",
            eventColor = announcement.eventColor.name,
            hoursBefore = announcement.hoursBefore,
            minutesBefore = announcement.minutesBefore,
            info = announcement.info ?: "None",
            enabled = announcement.enabled,
            publish = announcement.publish,
        )).map(::Announcement).awaitSingle()

        val cached = announcementCache.get(key = announcement.guildId)
        announcementCache.put(key = announcement.guildId, value = cached?.plus(saved) ?: arrayOf(saved))

        return saved
    }

    suspend fun getAnnouncementCount(): Long = announcementRepository.countAll().awaitSingle()

    suspend fun getAllAnnouncements(shardIndex: Int, shardCount: Int): List<Announcement> {
        return announcementRepository.findAllByShardIndexAndEnabledIsTrue(shardCount, shardIndex)
            .map(::Announcement)
            .collectList()
            .awaitSingle()
    }

    suspend fun getAllAnnouncements(guildId: Snowflake): List<Announcement> {
        var announcements = announcementCache.get(key = guildId)?.toList()
        if (announcements != null) return announcements

        announcements = announcementRepository.findAllByGuildId(guildId.asLong())
            .map(::Announcement)
            .collectList()
            .awaitSingle()

        announcementCache.put(key = guildId, value = announcements.toTypedArray())
        return announcements
    }

    suspend fun getAllAnnouncements(guildId: Snowflake, type: Announcement.Type? = null, returnDisabled: Boolean = true): List<Announcement> {
        return getAllAnnouncements(guildId)
            .filter { if (type == null) true else it.type == type }
            .filter { if (returnDisabled) true else it.enabled }
    }

    suspend fun getAnnouncement(guildId: Snowflake, id: String): Announcement? {
        return getAllAnnouncements(guildId).firstOrNull { it.id == id }
    }

    suspend fun updateAnnouncement(announcement: Announcement) {
        announcementRepository.updateByGuildIdAndAnnouncementId(
            guildId = announcement.guildId.asLong(),
            announcementId = announcement.id,
            calendarNumber = announcement.calendarNumber,
            subscribersRole = announcement.subscribers.roles.joinToString(","),
            subscribersUser = announcement.subscribers.users.map(Snowflake::asLong).joinToString(","),
            channelId = announcement.channelId.asString(),
            announcementType = announcement.type.name,
            modifier = announcement.modifier.name,
            eventId = announcement.eventId ?: "N/a",
            eventColor = announcement.eventColor.name,
            hoursBefore = announcement.hoursBefore,
            minutesBefore = announcement.minutesBefore,
            info = announcement.info ?: "None",
            enabled = announcement.enabled,
            publish = announcement.publish,
        ).awaitSingleOrNull()

        val cached = announcementCache.get(key = announcement.guildId)
        if (cached != null) {
            val new = cached
                .filterNot { it.id == announcement.id }
                .plus(announcement)
                .toTypedArray()
            announcementCache.put(key = announcement.guildId, value = new)
        }
    }

    suspend fun deleteAnnouncement(guildId: Snowflake, id: String) {
        announcementRepository.deleteByAnnouncementId(id).awaitSingleOrNull()

        val cached = announcementCache.get(key = guildId)
        if (cached != null) {
            announcementCache.put(key = guildId, value = cached.filterNot { it.id == id }.toTypedArray())
        }
    }

    suspend fun deleteAnnouncements(guildId: Snowflake, eventId: String) {
        announcementRepository.deleteAllByGuildIdAndEventId(guildId.asLong(), eventId).awaitSingleOrNull()

        val cached = announcementCache.get(key = guildId)
        if (cached != null) {
            announcementCache.put(key = guildId, value = cached.filterNot { it.eventId == eventId }.toTypedArray())
        }
    }

    suspend fun deleteAnnouncementsForCalendarDeletion(guildId: Snowflake, calendarNumber: Int) {
        announcementRepository.deleteAllByGuildIdAndCalendarNumber(guildId.asLong(), calendarNumber).awaitSingleOrNull()
        announcementRepository.decrementCalendarsByGuildIdAndCalendarNumber(guildId.asLong(), calendarNumber).awaitSingleOrNull()
        announcementCache.evict(key = guildId)
    }

    suspend fun sendAnnouncement(announcement: Announcement, event: Event) {
        try {
            val channel = discordClient.getChannelById(announcement.channelId)
            // While we don't need the channel data, we do want to make sure it exists
            val existingData = channel
                .data.onErrorResume(ClientException.isStatusCode(404)) { Mono.empty() }
                .awaitSingleOrNull()

            if (existingData == null) {
                // Channel was deleted
                deleteAnnouncement(announcement.guildId, announcement.id)
                return
            }

            val embed = embedService.determineAnnouncementEmbed(announcement, event)

            val message = channel.createMessage(MessageCreateRequest.builder()
                .content(announcement.subscribers.buildMentions().messageContentSafe())
                .addEmbed(embed.asRequest())
                .build()
            ).awaitSingle()

            if (announcement.publish) {
                discordClient.getMessageById(announcement.channelId, Snowflake.of(message.id()))
                    .publish()
                    .awaitSingleOrNull()
            }

            if (announcement.type == Announcement.Type.SPECIFIC) {
                deleteAnnouncement(announcement.guildId, announcement.id)
            }
        } catch (ex: Exception) {
            LOGGER.error("Failed to send announcement | guildId:${announcement.guildId.asLong()} | announcementId:${announcement.id}", ex)
        } finally {
            metricService.incrementAnnouncementPosted()
        }
    }

    suspend fun isInRange(announcement: Announcement, event: Event, maxDifference: Duration): Boolean {
        val timeUntilEvent = Duration.between(Instant.now(), event.start)

        val difference = timeUntilEvent - announcement.getCalculatedTime()

        return if (difference.isNegative) {
            // Event has past, check delete conditions
            if (announcement.type == Announcement.Type.SPECIFIC) deleteAnnouncement(announcement.guildId, announcement.id)

            false
        } else difference <= maxDifference

    }

    suspend fun processAnnouncementsForGuild(guildId: Snowflake, maxDifference: Duration) {
        val taskTimer = StopWatch()
        taskTimer.start()

        // Since we currently can't look up upcoming events from cache cuz I dunno how, we just hold in very temporary and scoped memory at least
        val events: MutableMap<Int, List<Event>> = mutableMapOf()

        // TODO: Need to break this out to add handling for modifiers
        getAllAnnouncements(guildId, returnDisabled = false).forEach { announcement ->
            // Handle specific type first, since we don't need to fetch all events for this
            if (announcement.type == Announcement.Type.SPECIFIC) {
                val event = calendarService.getEvent(guildId, announcement.calendarNumber, announcement.eventId!!) ?: return@forEach
                if (isInRange(announcement, event, maxDifference)) {
                    sendAnnouncement(announcement, event)
                }
            }

            // Get the events to filter through
            var filteredEvents = events[announcement.calendarNumber]
            if (filteredEvents == null) {
                filteredEvents = calendarService.getUpcomingEvents(guildId, announcement.calendarNumber, PROCESS_GUILD_DEFAULT_UPCOMING_EVENTS_COUNT)
                events[announcement.calendarNumber] = filteredEvents
            }

            // Handle filtering out events based on this announcement's types
            if (announcement.type == Announcement.Type.COLOR) {
                filteredEvents = filteredEvents.filter { it.color == announcement.eventColor }
            } else if (announcement.type == Announcement.Type.RECUR) {
                filteredEvents = filteredEvents
                    .filter { it.id.contains("_") }
                    .filter { it.id.split("_")[0] == announcement.eventId }
            }

            // Loop through filtered events and post any announcements in range
            filteredEvents
                .filter { isInRange(announcement, it, maxDifference) }
                .forEach { sendAnnouncement(announcement, it) }

        }

        taskTimer.stop()
        metricService.recordAnnouncementTaskDuration("guild", taskTimer.totalTimeMillis)
    }

    suspend fun getWizard(guildId: Snowflake, userId: Snowflake): AnnouncementWizardState? {
        return announcementWizardStateCache.get(guildId, userId)
    }

    suspend fun putWizard(state: AnnouncementWizardState) {
        announcementWizardStateCache.put(state.guildId, state.userId, state)
    }

    suspend fun cancelWizard(guildId: Snowflake, userId: Snowflake) {
        announcementWizardStateCache.evict(guildId, userId)
    }

    suspend fun cancelWizard(guildId: Snowflake, announcementId: String) {
        announcementWizardStateCache.getAll(guildId)
            .filter { it.entity.id == announcementId }
            .forEach { announcementWizardStateCache.evict(guildId, it.userId) }
    }
}
