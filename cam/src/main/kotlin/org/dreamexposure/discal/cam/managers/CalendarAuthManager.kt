package org.dreamexposure.discal.cam.managers

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.cam.google.GoogleAuth
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.network.discal.CredentialData
import org.springframework.stereotype.Component

@Component
class CalendarAuthManager(
    private val calendarService: CalendarService,
    private val googleAuth: GoogleAuth,
) {
    suspend fun getCredentialData(host: CalendarHost, id: Int, guild: Snowflake?): CredentialData? {
        return try {
            when (host) {
                CalendarHost.GOOGLE -> {
                    if (guild == null) {
                        // Internal (owned by DisCal, should never go bad)
                        googleAuth.requestNewAccessToken(id)
                    } else {
                        // External (owned by user)
                        val calendar = calendarService.getCalendar(guild, id) ?: return null
                        googleAuth.requestNewAccessToken(calendar)
                    }
                }
            }
        } catch (ex: Exception) {
            LOGGER.error("Get CredentialData Exception | guildId:$guild | credentialId:$id | calendarHost:${host.name}", ex)
            null
        }
    }
}
