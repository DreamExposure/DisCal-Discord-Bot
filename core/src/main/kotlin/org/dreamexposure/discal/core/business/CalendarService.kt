package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.CalendarCache
import org.dreamexposure.discal.core.database.CalendarRepository
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.springframework.stereotype.Component

@Component
class DefaultCalendarService(
    private val calendarRepository: CalendarRepository,
    private val calendarCache: CalendarCache,
) : CalendarService {
    override suspend fun getAllCalendars(guildId: Snowflake): List<Calendar> {
        var calendars = calendarCache.get(guildId.asLong())?.toList()
        if (calendars != null) return calendars

        calendars = calendarRepository.findAllByGuildId(guildId.asLong())
            .map(::Calendar)
            .collectList()
            .awaitSingle()

        calendarCache.put(guildId.asLong(), calendars.toTypedArray())
        return calendars
    }

    override suspend fun getCalendar(guildId: Snowflake, number: Int): Calendar? {
        return getAllCalendars(guildId).first { it.number == number }
    }

    override suspend fun updateCalendar(calendar: Calendar) {
        calendarRepository.updateCalendarByGuildIdAndCalendarNumber(
            guildId = calendar.guildId.asLong(),
            calendarNumber = calendar.number,
            host = calendar.host.name,
            calendarId = calendar.id,
            calendarAddress = calendar.address,
            external = calendar.external,
            credentialId = calendar.secrets.credentialId,
            privateKey = calendar.secrets.privateKey,
            accessToken = calendar.secrets.encryptedAccessToken,
            refreshToken = calendar.secrets.encryptedRefreshToken,
            expiresAt = calendar.secrets.expiresAt.toEpochMilli(),
        ).awaitSingleOrNull()

        val cached = calendarCache.get(calendar.guildId.asLong())
        if (cached != null) {
            val newList = cached.toMutableList()
            newList.removeIf { it.number == calendar.number }
            calendarCache.put(calendar.guildId.asLong(), (newList + calendar).toTypedArray())
        }
    }

}

interface CalendarService {
    // TODO: Need a function to invalidate cache because bot and API are using Db Manager

    suspend fun getAllCalendars(guildId: Snowflake): List<Calendar>

    suspend fun getCalendar(guildId: Snowflake, number: Int): Calendar?

    suspend fun updateCalendar(calendar: Calendar)
}
