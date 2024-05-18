package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.CalendarMetadataCache
import org.dreamexposure.discal.core.crypto.AESEncryption
import org.dreamexposure.discal.core.database.CalendarMetadataRepository
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import org.springframework.stereotype.Component

@Component
class CalendarService(
    private val calendarMetadataRepository: CalendarMetadataRepository,
    private val calendarMetadataCache: CalendarMetadataCache,
    private val settingsService: GuildSettingsService,
) {
    suspend fun getCalendarCount(): Long = calendarMetadataRepository.countAll().awaitSingle()

    suspend fun getCalendarCount(guildId: Snowflake) = calendarMetadataRepository.countAllByGuildId(guildId.asLong()).awaitSingle()

    // TODO: Exposing CalendarMetadata directly should not be done once a higher abstraction has been implemented
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

    // TODO: This should be privated once a higher abstraction has been implemented
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
    }

    suspend fun canAddNewCalendar(guildId: Snowflake): Boolean {
        val calCount = getCalendarCount(guildId)
        if (calCount == 0L) return true

        val settings = settingsService.getSettings(guildId)
        return calCount < settings.maxCalendars
    }
}
