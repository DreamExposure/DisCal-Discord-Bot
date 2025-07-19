package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.`object`.component.LayoutComponent
import discord4j.discordjson.json.MessageCreateRequest
import discord4j.rest.http.client.ClientException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.AnnouncementCache
import org.dreamexposure.discal.AnnouncementWizardStateCache
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.database.AnnouncementData
import org.dreamexposure.discal.core.database.AnnouncementRepository
import org.dreamexposure.discal.core.extensions.isExpiredTtl
import org.dreamexposure.discal.core.extensions.messageContentSafe
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.Announcement
import org.dreamexposure.discal.core.`object`.new.AnnouncementWizardState
import org.dreamexposure.discal.core.`object`.new.Event
import org.dreamexposure.discal.core.`object`.new.GuildSettings
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
    private val componentService: ComponentService,
    private val calendarService: CalendarService,
    private val settingsService: GuildSettingsService,
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

    suspend fun getAllAnnouncements(guildId: Snowflake, type: Announcement.Type? = null, modifier: Announcement.Modifier? = null, returnDisabled: Boolean = true): List<Announcement> {
        return getAllAnnouncements(guildId)
            .filter { if (type == null) true else it.type == type }
            .filter { if (modifier == null) true else it.modifier == modifier }
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

        // Cancel any existing wizards
        cancelWizard(announcement.guildId, announcement.id)
    }

    suspend fun deleteAnnouncement(guildId: Snowflake, id: String) {
        cancelWizard(guildId, id)
        announcementRepository.deleteByAnnouncementId(id).awaitSingleOrNull()

        val cached = announcementCache.get(key = guildId)
        if (cached != null) {
            announcementCache.put(key = guildId, value = cached.filterNot { it.id == id }.toTypedArray())
        }
    }

    suspend fun deleteAnnouncements(guildId: Snowflake, eventId: String) {
        cancelWizardByEvent(guildId, eventId)
        announcementRepository.deleteAllByGuildIdAndEventId(guildId.asLong(), eventId).awaitSingleOrNull()

        val cached = announcementCache.get(key = guildId)
        if (cached != null) {
            announcementCache.put(key = guildId, value = cached.filterNot { it.eventId == eventId }.toTypedArray())
        }
    }

    suspend fun deleteAnnouncementsForCalendarDeletion(guildId: Snowflake, calendarNumber: Int) {
        cancelWizard(guildId, calendarNumber)
        announcementRepository.deleteAllByGuildIdAndCalendarNumber(guildId.asLong(), calendarNumber).awaitSingleOrNull()
        announcementRepository.decrementCalendarsByGuildIdAndCalendarNumber(guildId.asLong(), calendarNumber).awaitSingleOrNull()
        announcementCache.evict(key = guildId)
    }

    suspend fun sendAnnouncement(announcement: Announcement, event: Event, settings: GuildSettings) {
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

            val embed = embedService.determineAnnouncementEmbed(announcement, event, settings)

            val message = channel.createMessage(MessageCreateRequest.builder()
                .content(announcement.subscribers.buildMentions().messageContentSafe())
                .addEmbed(embed.asRequest())
                .addAllComponents(componentService.getEventRsvpComponents(event, settings).map(LayoutComponent::getData))
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
        return when (announcement.modifier) {
            Announcement.Modifier.BEFORE -> {
                val timeUntilEvent = Duration.between(Instant.now(), event.start)
                val difference = timeUntilEvent - announcement.getCalculatedTime()

                if (difference.isNegative) {
                    // Event has past, check delete conditions
                    if (announcement.type == Announcement.Type.SPECIFIC) deleteAnnouncement(announcement.guildId, announcement.id)

                    false
                } else difference <= maxDifference
            }
            Announcement.Modifier.DURING -> {
                val timeSinceStart = Duration.between(event.start, Instant.now())
                val difference = timeSinceStart - announcement.getCalculatedTime()

                if (difference.isNegative && !event.isOngoing()) {
                    // Event has past, check delete conditions
                    if (announcement.type == Announcement.Type.SPECIFIC) deleteAnnouncement(announcement.guildId, announcement.id)

                    false
                } else difference <= maxDifference
            }
            Announcement.Modifier.END -> {
                TODO("Gotta figure out how I want to do this one")
            }
        }
    }

    suspend fun processAnnouncementsForGuild(guildId: Snowflake, maxDifference: Duration) {
        val taskTimer = StopWatch()
        taskTimer.start()

        // Get settings and check if announcements are paused
        val settings = settingsService.getSettings(guildId)
        if (settings.pauseAnnouncementsUntil != null && !settings.pauseAnnouncementsUntil.isExpiredTtl()) return

        // Since we currently can't look up upcoming events from cache cuz I dunno how, we just hold in very temporary and scoped memory at least
        val upcomingEvents: MutableMap<Int, List<Event>> = mutableMapOf()
        val ongoingEvents: MutableMap<Int, List<Event>> = mutableMapOf()

        getAllAnnouncements(guildId, returnDisabled = false).forEach { announcement ->
            // Handle specific type first, since we don't need to fetch all events for this
            if (announcement.type == Announcement.Type.SPECIFIC) {
                val event = calendarService.getEvent(guildId, announcement.calendarNumber, announcement.eventId!!) ?: return@forEach
                if (isInRange(announcement, event, maxDifference)) {
                    sendAnnouncement(announcement, event, settings)
                }
            }

            // Get the events to filter through
            var filteredEvents = when (announcement.modifier) {
                Announcement.Modifier.BEFORE -> {
                    var events = upcomingEvents[announcement.calendarNumber]
                    if (events == null) {
                        events = calendarService.getUpcomingEvents(guildId, announcement.calendarNumber, PROCESS_GUILD_DEFAULT_UPCOMING_EVENTS_COUNT)
                        upcomingEvents[announcement.calendarNumber] = events
                    }

                    events
                }
                Announcement.Modifier.DURING -> {
                    var events = ongoingEvents[announcement.calendarNumber]
                    if (events == null) {
                        events = calendarService.getOngoingEvents(guildId, announcement.calendarNumber)
                        ongoingEvents[announcement.calendarNumber] = events
                    }

                    events
                }
                Announcement.Modifier.END -> {
                    TODO("Need to figure out how to implement this still")
                }
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
                .forEach { sendAnnouncement(announcement, it, settings) }

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

    suspend fun cancelWizard(guildId: Snowflake, calendarNumber: Int) {
        announcementWizardStateCache.getAll(guildId)
            .filter { it.entity.calendarNumber == calendarNumber }
            .forEach { announcementWizardStateCache.evict(guildId, it.userId) }
    }

    suspend fun cancelWizardByEvent(guildId: Snowflake, eventId: String) {
        announcementWizardStateCache.getAll(guildId)
            .filter { it.entity.eventId == eventId }
            .forEach { announcementWizardStateCache.evict(guildId, it.userId) }
    }
}
