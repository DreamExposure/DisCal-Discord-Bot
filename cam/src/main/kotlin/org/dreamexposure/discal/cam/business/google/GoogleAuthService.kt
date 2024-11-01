package org.dreamexposure.discal.cam.business.google

import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.CredentialService
import org.dreamexposure.discal.core.business.google.GoogleAuthApiWrapper
import org.dreamexposure.discal.core.exceptions.EmptyNotAllowedException
import org.dreamexposure.discal.core.exceptions.NotFoundException
import org.dreamexposure.discal.core.extensions.isExpiredTtl
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import org.dreamexposure.discal.core.`object`.new.model.discal.cam.TokenV1Model
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class GoogleAuthService(
    private val credentialService: CredentialService,
    private val calendarService: CalendarService,
    private val googleAuthApiWrapper: GoogleAuthApiWrapper,
) {
    suspend fun requestNewAccessToken(calendar: CalendarMetadata): TokenV1Model? {
        if (!calendar.secrets.expiresAt.isExpiredTtl()) return TokenV1Model(calendar.secrets.accessToken, calendar.secrets.expiresAt)

        LOGGER.debug("Refreshing access token | guildId:{} | calendar:{}", calendar.guildId, calendar.number)

        val refreshed = googleAuthApiWrapper.refreshAccessToken(calendar.secrets.refreshToken).entity ?: return null
        calendar.secrets.accessToken = refreshed.accessToken
        calendar.secrets.expiresAt = Instant.now().plusSeconds(refreshed.expiresIn.toLong()).minus(Duration.ofMinutes(5)) // Add some wiggle room
        calendarService.updateCalendarMetadata(calendar)

        LOGGER.debug("Refreshed access token | guildId:{} | calendar:{}, validUntil:{}", calendar.guildId, calendar.number, calendar.external)

        return TokenV1Model(calendar.secrets.accessToken, calendar.secrets.expiresAt)
    }

    suspend fun requestNewAccessToken(credentialId: Int): TokenV1Model {
        val credential = credentialService.getCredential(credentialId) ?: throw NotFoundException()
        if (!credential.expiresAt.isExpiredTtl()) return TokenV1Model(credential.accessToken, credential.expiresAt)

        LOGGER.debug("Refreshing access token | credentialId:$credentialId")

        val refreshed = googleAuthApiWrapper.refreshAccessToken(credential.refreshToken).entity ?: throw EmptyNotAllowedException()
        credential.accessToken = refreshed.accessToken
        credential.expiresAt = Instant.now().plusSeconds(refreshed.expiresIn.toLong()).minus(Duration.ofMinutes(5)) // Add some wiggle room
        credentialService.updateCredential(credential)

        LOGGER.debug("Refreshed access token | credentialId:{} | validUntil:{}", credentialId, credential.expiresAt)

        return TokenV1Model(credential.accessToken, credential.expiresAt)
    }
}
