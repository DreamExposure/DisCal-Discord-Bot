package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.AnnouncementCache
import org.dreamexposure.discal.core.database.AnnouncementRepository
import org.dreamexposure.discal.core.`object`.new.Announcement
import org.springframework.stereotype.Component

@Component
class AnnouncementService(
    private val announcementRepository: AnnouncementRepository,
    private val announcementCache: AnnouncementCache,
) {
    suspend fun getAnnouncementCount(): Long = announcementRepository.count().awaitSingle()

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

    suspend fun getAllAnnouncements(guildId: Snowflake, type: Announcement.Type): List<Announcement> {
        return getAllAnnouncements(guildId).filter { it.type == type }
    }

    suspend fun getEnabledAnnouncements(guildId: Snowflake): List<Announcement> {
        return getAllAnnouncements(guildId).filter(Announcement::enabled)
    }

    suspend fun getEnabledAnnouncements(guildId: Snowflake, type: Announcement.Type): List<Announcement> {
        return getEnabledAnnouncements(guildId).filter { it.type == type }
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
            eventId = announcement.eventId,
            eventColor = announcement.eventColor.name,
            hoursBefore = announcement.hoursBefore,
            minutesBefore = announcement.minutesBefore,
            info = announcement.info,
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
}
