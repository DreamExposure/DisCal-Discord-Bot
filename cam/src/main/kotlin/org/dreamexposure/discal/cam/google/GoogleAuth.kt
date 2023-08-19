package org.dreamexposure.discal.cam.google

import com.google.api.client.http.HttpStatusCodes.STATUS_CODE_BAD_REQUEST
import com.google.api.client.http.HttpStatusCodes.STATUS_CODE_OK
import okhttp3.FormBody
import okhttp3.Request
import org.dreamexposure.discal.cam.json.google.ErrorData
import org.dreamexposure.discal.cam.json.google.RefreshData
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.crypto.AESEncryption
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.google.DisCalGoogleCredential
import org.dreamexposure.discal.core.exceptions.AccessRevokedException
import org.dreamexposure.discal.core.exceptions.EmptyNotAllowedException
import org.dreamexposure.discal.core.exceptions.NotFoundException
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.`object`.network.discal.CredentialData
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.GlobalVal.HTTP_CLIENT
import org.dreamexposure.discal.core.utils.GlobalVal.JSON_FORMAT
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Instant
import kotlin.system.exitProcess

@Suppress("BlockingMethodInNonBlockingContext")
object GoogleAuth {
    private val CREDENTIALS: Flux<DisCalGoogleCredential>

    init {
        val credCount = Config.SECRET_GOOGLE_CREDENTIAL_COUNT.getInt()
        CREDENTIALS = Flux.range(0, credCount)
                .flatMap(DatabaseManager::getCredentialData)
                .map(::DisCalGoogleCredential)
                .doOnError { exitProcess(1) }
                .cache()
    }

    fun requestNewAccessToken(calendarData: CalendarData): Mono<CredentialData> {
        return Mono.just(AESEncryption(calendarData.privateKey)).flatMap { aes ->
            if (!calendarData.expired()) {
                return@flatMap aes.decrypt(calendarData.encryptedAccessToken)
                        .map { CredentialData(it, calendarData.expiresAt) }
            }

            aes.decrypt(calendarData.encryptedRefreshToken)
                    .flatMap(this::doAccessTokenRequest)
                    .flatMap { data ->
                        //calendarData.encryptedAccessToken = aes.encrypt(data.accessToken)
                        calendarData.expiresAt = data.validUntil.minusSeconds(60) // Add a minute of wiggle room

                        aes.encrypt(data.accessToken)
                                .doOnNext { calendarData.encryptedAccessToken = it }
                                .then(DatabaseManager.updateCalendar(calendarData).thenReturn(data))
                    }
        }
    }

    fun requestNewAccessToken(credentialId: Int): Mono<CredentialData> {
        return CREDENTIALS
                .filter { it.credentialData.credentialNumber == credentialId }
                .next()
                .switchIfEmpty(Mono.error(NotFoundException()))
                .flatMap { credential ->
                    if (!credential.expired()) {
                        return@flatMap credential.getAccessToken()
                                .map { CredentialData(it, credential.credentialData.expiresAt) }
                    }

                    credential.getRefreshToken()
                            .flatMap(this::doAccessTokenRequest)
                            .flatMap { credential.setAccessToken(it.accessToken).thenReturn(it) }
                            .doOnNext { credential.credentialData.expiresAt = it.validUntil }
                            .flatMap { DatabaseManager.updateCredentialData(credential.credentialData).thenReturn(it) }
                }.switchIfEmpty(Mono.error(EmptyNotAllowedException()))

    }

    private fun doAccessTokenRequest(refreshToken: String): Mono<CredentialData> {
        return Mono.fromCallable {
            val body = FormBody.Builder()
                    .addEncoded("client_id", Config.SECRET_GOOGLE_CLIENT_ID.getString())
                    .addEncoded("client_secret", Config.SECRET_GOOGLE_CLIENT_SECRET.getString())
                    .addEncoded("refresh_token", refreshToken)
                    .addEncoded("grant_type", "refresh_token")
                    .build()

            val request = Request.Builder()
                    .url("https://www.googleapis.com/oauth2/v4/token")
                    .post(body)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build()

            HTTP_CLIENT.newCall(request).execute()
        }.subscribeOn(Schedulers.boundedElastic()).flatMap { response ->
            when (response.code) {
                STATUS_CODE_OK -> {
                    val body = JSON_FORMAT.decodeFromString(RefreshData.serializer(), response.body!!.string())
                    response.body?.close()
                    response.close()

                    Mono.just(CredentialData(body.accessToken, Instant.now().plusSeconds(body.expiresIn.toLong())))
                }
                STATUS_CODE_BAD_REQUEST -> {
                    val body = JSON_FORMAT.decodeFromString(ErrorData.serializer(), response.body!!.string())
                    response.body?.close()
                    response.close()

                    LOGGER.error("[Google] Access Token Request: $body")

                    if (body.error == "invalid_grant") {
                        LOGGER.debug(DEFAULT, "[Google] Access to resource has been revoked")
                        Mono.error(AccessRevokedException())
                    } else {
                        LOGGER.debug(DEFAULT, "[Google] Error requesting new access token | ${response.code} | ${response.message} | $body")
                        Mono.empty()
                    }
                }
                else -> {
                    // Failed to get OK. Send debug info
                    LOGGER.debug(DEFAULT, "[Google] Error requesting new access token | ${response.code} " +
                            "| ${response.message} | ${response.body?.string()}")
                    response.body?.close()
                    response.close()
                    Mono.empty()
                }
            }
        }
    }
}
