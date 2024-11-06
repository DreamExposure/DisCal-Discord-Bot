package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.CalendarCache
import org.dreamexposure.discal.CalendarMetadataCache
import org.dreamexposure.discal.core.crypto.AESEncryption
import org.dreamexposure.discal.core.database.CalendarMetadataData
import org.dreamexposure.discal.core.database.CalendarMetadataRepository
import org.dreamexposure.discal.core.exceptions.NotFoundException
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import org.springframework.stereotype.Component

@Component
class CalendarService(
    private val calendarMetadataRepository: CalendarMetadataRepository,
    private val calendarMetadataCache: CalendarMetadataCache,
    private val calendarProviders: List<CalendarProvider>,
    private val calendarCache: CalendarCache,
    private val settingsService: GuildSettingsService,
) {
    /////////
    /// Calendar count
    /////////
    suspend fun getCalendarCount(): Long = calendarMetadataRepository.countAll().awaitSingle()

    suspend fun getCalendarCount(guildId: Snowflake) = calendarMetadataRepository.countAllByGuildId(guildId.asLong()).awaitSingle()

    /////////
    /// Calendar metadata - Prefer using full Calendar implementation
    /////////
    suspend fun getAllCalendarMetadata(guildId: Snowflake): List<CalendarMetadata> {
        var calendars = calendarMetadataCache.get(key = guildId)?.toList()
        if (calendars != null) return calendars

        calendars = calendarMetadataRepository.findAllByGuildId(guildId.asLong())
            .flatMap { mono { CalendarMetadata(it) } }
            .collectList()
            .awaitSingle()

        calendarMetadataCache.put(key = guildId, value = calendars.toTypedArray())
        return calendars
    }

    suspend fun getCalendarMetadata(guildId: Snowflake, number: Int): CalendarMetadata? {
        return getAllCalendarMetadata(guildId).firstOrNull { it.number == number }
    }

    suspend fun createCalendarMetadata(calendar: CalendarMetadata): CalendarMetadata {
        val aes = AESEncryption(calendar.secrets.privateKey)
        val encryptedRefreshToken = aes.encrypt(calendar.secrets.refreshToken).awaitSingle()
        val encryptedAccessToken = aes.encrypt(calendar.secrets.accessToken).awaitSingle()

        calendarMetadataRepository.save(CalendarMetadataData(
            guildId = calendar.guildId.asLong(),
            calendarNumber = calendar.number,
            host = calendar.host.name,
            calendarId = calendar.id,
            calendarAddress = calendar.address,
            external = calendar.external,
            credentialId = calendar.secrets.credentialId,
            privateKey = calendar.secrets.privateKey,
            accessToken = encryptedAccessToken,
            refreshToken = encryptedRefreshToken,
            expiresAt = calendar.secrets.expiresAt.toEpochMilli(),
        )).flatMap { mono { CalendarMetadata(it) } }.awaitSingle()

        val cached = calendarMetadataCache.get(key = calendar.guildId)
        if (cached != null) calendarMetadataCache.put(key = calendar.guildId, value = cached + calendar)

        return calendar
    }

    suspend fun updateCalendarMetadata(calendar: CalendarMetadata) {
        val aes = AESEncryption(calendar.secrets.privateKey)
        val encryptedRefreshToken = aes.encrypt(calendar.secrets.refreshToken).awaitSingle()
        val encryptedAccessToken = aes.encrypt(calendar.secrets.accessToken).awaitSingle()

        calendarMetadataRepository.updateCalendarByGuildIdAndCalendarNumber(
            guildId = calendar.guildId.asLong(),
            calendarNumber = calendar.number,
            host = calendar.host.name,
            calendarId = calendar.id,
            calendarAddress = calendar.address,
            external = calendar.external,
            credentialId = calendar.secrets.credentialId,
            privateKey = calendar.secrets.privateKey,
            accessToken = encryptedAccessToken,
            refreshToken = encryptedRefreshToken,
            expiresAt = calendar.secrets.expiresAt.toEpochMilli(),
        ).awaitSingleOrNull()

        val cached = calendarMetadataCache.get(key = calendar.guildId)
        if (cached != null) {
            val newList = cached.toMutableList()
            newList.removeIf { it.number == calendar.number }
            calendarMetadataCache.put(key = calendar.guildId,value = (newList + calendar).toTypedArray())
        }

        val cachedFullCalendar = calendarCache.get(calendar.guildId, calendar.number)
        if (cachedFullCalendar != null) calendarCache.put(calendar.guildId, calendar.number, cachedFullCalendar.copy(metadata = calendar))
    }

    /////////
    /// Calendar
    /////////
    suspend fun getCalendar(guildId: Snowflake, number: Int): Calendar? {
        var calendar = calendarCache.get(guildId, number)
        if (calendar != null) return calendar

        // TODO: Is this how I want to handle that actually???
        val metadata = getCalendarMetadata(guildId, number) ?: return null

        calendar = calendarProviders
            .first { it.host == metadata.host }
            .getCalendar(metadata)
        if (calendar != null) calendarCache.put(guildId, number, calendar)

        return calendar
    }

    suspend fun createCalendar(guildId: Snowflake, spec: Calendar.CreateSpec): Calendar {
        val calendar = calendarProviders
            .first { it.host == spec.host }
            .createCalendar(guildId, spec)

        createCalendarMetadata(calendar.metadata)

        calendarCache.put(guildId, calendar.metadata.number, calendar)
        return calendar
    }

    suspend fun updateCalendar(guildId: Snowflake, number: Int, spec: Calendar.UpdateSpec): Calendar {
        val metadata = getCalendarMetadata(guildId, number) ?: throw NotFoundException("Cannot update a calendar that does not exist")

        val calendar = calendarProviders
            .first { it.host == metadata.host }
            .updateCalendar(guildId, metadata, spec)

        calendarCache.put(guildId, calendar.metadata.number, calendar)
        return calendar
    }

    suspend fun deleteCalendar(guildId: Snowflake, number: Int) {
        val metadata = getCalendarMetadata(guildId, number) ?: return

        calendarProviders.first { it.host == metadata.host }.deleteCalendar(guildId, metadata)
        calendarCache.evict(guildId, metadata.number)
        val cached = calendarMetadataCache.get(key = guildId)
        if (cached != null) {
            val newList = cached.toMutableList()
            newList.removeIf { it.number == number }
            calendarMetadataCache.put(key = guildId, value = newList.toTypedArray())
        }

        // TODO: Need to call a modern version of DatabaseManager.deleteCalendarAndRelatedData method

    }


    /////////
    /// Extra functions
    /////////
    suspend fun canAddNewCalendar(guildId: Snowflake): Boolean {
        val calCount = getCalendarCount(guildId)
        if (calCount == 0L) return true

        val settings = settingsService.getSettings(guildId)
        return calCount < settings.maxCalendars
    }
}
