package org.dreamexposure.discal.cam.google

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.api.client.http.HttpStatusCodes.STATUS_CODE_BAD_REQUEST
import com.google.api.client.http.HttpStatusCodes.STATUS_CODE_OK
import kotlinx.coroutines.reactor.awaitSingle
import okhttp3.FormBody
import okhttp3.Request
import org.dreamexposure.discal.cam.json.google.ErrorData
import org.dreamexposure.discal.cam.json.google.RefreshData
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.CredentialService
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.exceptions.AccessRevokedException
import org.dreamexposure.discal.core.exceptions.EmptyNotAllowedException
import org.dreamexposure.discal.core.exceptions.NotFoundException
import org.dreamexposure.discal.core.extensions.isExpiredTtl
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.network.discal.CredentialData
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.GlobalVal.HTTP_CLIENT
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.time.Instant

@Component
class GoogleAuth(
    private val credentialService: CredentialService,
    private val calendarService: CalendarService,
    private val objectMapper: ObjectMapper,
) {

    suspend fun requestNewAccessToken(calendar: Calendar): CredentialData? {
        if (!calendar.secrets.expiresAt.isExpiredTtl()) return CredentialData(calendar.secrets.accessToken, calendar.secrets.expiresAt)

        LOGGER.debug("Refreshing access token | guildId:{} | calendar:{}", calendar.guildId, calendar.number)

        val refreshedCredential = doAccessTokenRequest(calendar.secrets.refreshToken) ?: return null
        calendar.secrets.accessToken = refreshedCredential.accessToken
        calendar.secrets.expiresAt = refreshedCredential.validUntil.minus(Duration.ofMinutes(5)) // Add some wiggle room
        calendarService.updateCalendar(calendar)

        LOGGER.debug("Refreshing access token | guildId:{} | calendar:{}", calendar.guildId, calendar.number)

        return refreshedCredential
    }

    suspend fun requestNewAccessToken(credentialId: Int): CredentialData {
        val credential = credentialService.getCredential(credentialId) ?: throw NotFoundException()
        if (!credential.expiresAt.isExpiredTtl()) return CredentialData(credential.accessToken, credential.expiresAt)

        LOGGER.debug("Refreshing access token | credentialId:$credentialId")

        val refreshedCredentialData = doAccessTokenRequest(credential.refreshToken) ?: throw EmptyNotAllowedException()
        credential.accessToken = refreshedCredentialData.accessToken
        credential.expiresAt = refreshedCredentialData.validUntil.minus(Duration.ofMinutes(5)) // Add some wiggle room
        credentialService.updateCredential(credential)

        LOGGER.debug("Refreshed access token | credentialId:{} | validUntil{}", credentialId, credential.expiresAt)

        return refreshedCredentialData
    }

    private suspend fun doAccessTokenRequest(refreshToken: String): CredentialData? {
        val requestFormBody = FormBody.Builder()
            .addEncoded("client_id", Config.SECRET_GOOGLE_CLIENT_ID.getString())
            .addEncoded("client_secret", Config.SECRET_GOOGLE_CLIENT_SECRET.getString())
            .addEncoded("refresh_token", refreshToken)
            .addEncoded("grant_type", "refresh_token")
            .build()
        val request = Request.Builder()
            .url("https://www.googleapis.com/oauth2/v4/token")
            .post(requestFormBody)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build()


        val response = Mono.fromCallable(HTTP_CLIENT.newCall(request)::execute)
            .subscribeOn(Schedulers.boundedElastic())
            .awaitSingle()

        return when (response.code) {
            STATUS_CODE_OK -> {
                val body = objectMapper.readValue<RefreshData>(response.body!!.string())
                response.close()

                CredentialData(body.accessToken, Instant.now().plusSeconds(body.expiresIn.toLong()))
            }
            STATUS_CODE_BAD_REQUEST -> {
                val bodyRaw = response.body!!.string()
                LOGGER.error("[Google] Access Token Request: $bodyRaw")
                val body = objectMapper.readValue<ErrorData>(bodyRaw)
                response.close()


                if (body.error == "invalid_grant") {
                    LOGGER.debug(DEFAULT, "[Google] Access to resource has been revoked")
                    throw AccessRevokedException()
                } else {
                    LOGGER.error(DEFAULT, "[Google] Error requesting new access token | ${response.code} | ${response.message} | $body")
                    null
                }
            }
            else -> {
                // Failed to get OK. Send error info
                LOGGER.error(DEFAULT, "[Google] Error requesting new access token | ${response.code} ${response.message} | ${response.body?.string()}")
                response.close()

                null
            }
        }
    }
}
