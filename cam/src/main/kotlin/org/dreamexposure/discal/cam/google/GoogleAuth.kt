package org.dreamexposure.discal.cam.google

import com.google.api.client.http.HttpStatusCodes.STATUS_CODE_BAD_REQUEST
import com.google.api.client.http.HttpStatusCodes.STATUS_CODE_OK
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import okhttp3.FormBody
import okhttp3.Request
import org.dreamexposure.discal.cam.json.google.ErrorData
import org.dreamexposure.discal.cam.json.google.RefreshData
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.CredentialService
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.crypto.AESEncryption
import org.dreamexposure.discal.core.entities.google.DisCalGoogleCredential
import org.dreamexposure.discal.core.exceptions.AccessRevokedException
import org.dreamexposure.discal.core.exceptions.EmptyNotAllowedException
import org.dreamexposure.discal.core.exceptions.NotFoundException
import org.dreamexposure.discal.core.extensions.isExpiredTtl
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.network.discal.CredentialData
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.GlobalVal.HTTP_CLIENT
import org.dreamexposure.discal.core.utils.GlobalVal.JSON_FORMAT
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Instant
import kotlin.system.exitProcess

@Component
class GoogleAuth(
    private val credentialService: CredentialService,
    private val calendarService: CalendarService,
) {
    private final val CREDENTIALS: Flux<DisCalGoogleCredential> // TODO: Refactor as kc

    init {
        val credCount = Config.SECRET_GOOGLE_CREDENTIAL_COUNT.getInt()

        CREDENTIALS = Flux.range(0, credCount)
            .flatMap { mono { credentialService.getCredential(it) } }
                .map(::DisCalGoogleCredential)
                .doOnError { exitProcess(1) }
                .cache()
    }

    suspend fun requestNewAccessToken(calendar: Calendar): CredentialData? {
        val aes = AESEncryption(calendar.secrets.privateKey)
        if (!calendar.secrets.expiresAt.isExpiredTtl()) {
            return aes.decrypt(calendar.secrets.encryptedAccessToken)
                .map { CredentialData(it, calendar.secrets.expiresAt) }
                .awaitSingle()
        }

        val refreshToken = aes.decrypt(calendar.secrets.encryptedRefreshToken).awaitSingle()
        val refreshedCredential = doAccessTokenRequest(refreshToken) ?: return null

        calendar.secrets.expiresAt = refreshedCredential.validUntil.minusSeconds(60) // Add a minute of wiggle room
        calendar.secrets.encryptedAccessToken = aes.encrypt(refreshedCredential.accessToken).awaitSingle()

        calendarService.updateCalendar(calendar)

        return refreshedCredential
    }

    fun requestNewAccessToken(credentialId: Int): Mono<CredentialData> { // TODO: Refactor as kc
        return CREDENTIALS
            .filter { it.credential.credentialNumber == credentialId }
                .next()
                .switchIfEmpty(Mono.error(NotFoundException()))
                .flatMap { credential ->
                    if (!credential.expired()) {
                        return@flatMap credential.getAccessToken()
                            .map { CredentialData(it, credential.credential.expiresAt) }
                    }

                    credential.getRefreshToken()
                        .flatMap { mono { doAccessTokenRequest(it) } }
                        .flatMap { credential.setAccessToken(it.accessToken).thenReturn(it) }
                        .doOnNext { credential.credential.expiresAt = it.validUntil }
                        .flatMap(mono { credentialService.updateCredential(credential.credential) }::thenReturn)
                }.switchIfEmpty(Mono.error(EmptyNotAllowedException()))

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
                val body = JSON_FORMAT.decodeFromString(RefreshData.serializer(), response.body!!.string())
                response.body?.close()
                response.close()

                CredentialData(body.accessToken, Instant.now().plusSeconds(body.expiresIn.toLong()))
            }
            STATUS_CODE_BAD_REQUEST -> {
                val body = JSON_FORMAT.decodeFromString(ErrorData.serializer(), response.body!!.string())
                response.body?.close()
                response.close()

                LOGGER.error("[Google] Access Token Request: $body")

                if (body.error == "invalid_grant") {
                    LOGGER.debug(DEFAULT, "[Google] Access to resource has been revoked")
                    throw AccessRevokedException()
                } else {
                    LOGGER.error(DEFAULT, "[Google] Error requesting new access token | ${response.code} | ${response.message} | $body")
                    return null
                }
            }
            else -> {
                // Failed to get OK. Send error info
                LOGGER.error(DEFAULT, "[Google] Error requesting new access token | ${response.code} ${response.message} | ${response.body?.string()}")
                response.body?.close()
                response.close()

                null
            }
        }
    }
}
