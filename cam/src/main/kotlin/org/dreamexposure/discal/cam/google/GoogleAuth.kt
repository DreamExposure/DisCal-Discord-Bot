package org.dreamexposure.discal.cam.google

import com.google.api.client.http.HttpStatusCodes.STATUS_CODE_BAD_REQUEST
import com.google.api.client.http.HttpStatusCodes.STATUS_CODE_OK
import okhttp3.FormBody
import okhttp3.Request
import org.dreamexposure.discal.cam.json.google.ErrorData
import org.dreamexposure.discal.cam.json.google.RefreshData
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.`object`.google.ClientData
import org.dreamexposure.discal.core.`object`.network.discal.CredentialData
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.google.DisCalGoogleCredential
import org.dreamexposure.discal.core.exceptions.AccessRevokedException
import org.dreamexposure.discal.core.logger.LOGGER
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
    private val clientData = ClientData(BotSettings.GOOGLE_CLIENT_ID.get(), BotSettings.GOOGLE_CLIENT_SECRET.get())
    private val CREDENTIALS: Flux<DisCalGoogleCredential>

    init {
        val credCount = BotSettings.CREDENTIALS_COUNT.get().toInt()
        CREDENTIALS = Flux.range(0, credCount)
                .flatMap(DatabaseManager::getCredentialData)
                .map(::DisCalGoogleCredential)
                .doOnError { exitProcess(1) }
                .cache()
    }

    fun requestNewAccessToken(calendarData: CalendarData): Mono<CredentialData> {
        TODO()
    }

    fun requestNewAccessToken(credential: DisCalGoogleCredential): Mono<CredentialData> {
        if (!credential.expired()) {
            return Mono.just(CredentialData(credential.getAccessToken(), credential.credentialData.expiresAt))
        }

        return doAccessTokenRequest(credential.getRefreshToken()).flatMap { data ->
            credential.setAccessToken(data.accessToken)
            credential.credentialData.expiresAt = data.validUntil

            DatabaseManager.updateCredentialData(credential.credentialData).thenReturn(data)
        }
    }

    private fun doAccessTokenRequest(refreshToken: String): Mono<CredentialData> {
        return Mono.fromCallable {
            val body = FormBody.Builder()
                    .addEncoded("client_id", clientData.clientId)
                    .addEncoded("client_secret", clientData.clientSecret)
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

                    if (body.error == "invalid_grant") {
                        LOGGER.debug(DEFAULT, "[Google] Access to resource has been revoked")
                        Mono.error<CredentialData>(AccessRevokedException())
                    } else {
                        LOGGER.debug(DEFAULT, "[Google] Error requesting new access token | ${response.code} | ${response.message} | $body")
                    }
                    Mono.empty()
                }
                else -> {
                    // Failed to get OK. Send debug info
                    LOGGER.debug(DEFAULT,"[Google] Error requesting new access token | ${response.code} " +
                            "| ${response.message} | ${response.body?.string()}")
                    response.body?.close()
                    response.close()
                    Mono.empty()
                }
            }
        }.doOnError {
            LOGGER.error("[Google] Failed to request new access token", it)
        }.onErrorResume { Mono.empty() }
    }
}
