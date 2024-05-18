package org.dreamexposure.discal.cam.managers

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.cam.business.google.GoogleAuthService
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.model.discal.cam.TokenV1Model
import org.springframework.stereotype.Component

@Component
class CalendarAuthManager(
    private val calendarService: CalendarService,
    private val googleAuthService: GoogleAuthService,
) {
    suspend fun getCredentialData(host: CalendarHost, id: Int, guild: Snowflake?): TokenV1Model? {
        return try {
            when (host) {
                CalendarHost.GOOGLE -> {
                    if (guild == null) {
                        // Internal (owned by DisCal, should never go bad)
                        googleAuthService.requestNewAccessToken(id)
                    } else {
                        // External (owned by user)
                        val calendar = calendarService.getCalendarMetadata(guild, id) ?: return null
                        googleAuthService.requestNewAccessToken(calendar)
                    }
                }
            }
        } catch (ex: Exception) {
            LOGGER.error("Get CredentialData Exception | guildId:$guild | credentialId:$id | calendarHost:${host.name}", ex)
            throw ex // rethrow
        }
    }
}
