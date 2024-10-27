package org.dreamexposure.discal.core.wrapper.google

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpStatusCodes
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import discord4j.common.util.Snowflake
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.exceptions.EmptyNotAllowedException
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.`object`.network.discal.CredentialData
import org.dreamexposure.discal.core.`object`.rest.RestError
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.GlobalVal.HTTP_CLIENT
import org.dreamexposure.discal.core.utils.GlobalVal.JSON_FORMAT
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import com.google.api.services.calendar.Calendar as GoogleCalendarService

@Deprecated("Use GoogleCalendarApiWrapper")
object GoogleAuthWrapper {
    private val discalTokens: MutableMap<Int, CredentialData> = ConcurrentHashMap()
    private val externalTokens: MutableMap<Snowflake, CredentialData> = ConcurrentHashMap()

    private fun authorize(credentialId: Int): Mono<Credential> {
        return getAccessToken(credentialId)
                .map(GoogleCredential()::setAccessToken)
                .ofType(Credential::class.java) //Cast down to the class it extends
                .switchIfEmpty(Mono.error(EmptyNotAllowedException()))
    }

    private fun authorize(calData: CalendarData): Mono<Credential> {
        return Mono.just(calData).filter { !"N/a".equals(calData.encryptedAccessToken, true) }
                .flatMap(this::getAccessToken)
                .map(GoogleCredential()::setAccessToken)
                .ofType(Credential::class.java) //Cast down to the class it extends
                .switchIfEmpty(Mono.error(EmptyNotAllowedException()))
    }

    private fun buildService(credential: Credential): GoogleCalendarService {

        return GoogleCalendarService.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("DisCal")
                .build()
    }

    private fun getAccessToken(credentialId: Int): Mono<String> {
        val token = discalTokens[credentialId]
        if (token != null && !token.isExpired()) {
            return Mono.just(token.accessToken)
        }

        LOGGER.debug("Refreshing local token via CAM | credentialId:$credentialId")

        return Mono.fromCallable {
            val url = "${Config.URL_CAM.getString()}/v1/token".toHttpUrlOrNull()!!.newBuilder()
                .addQueryParameter("host", CalendarHost.GOOGLE.name)
                .addQueryParameter("id", credentialId.toString())
                .build()

            val request = Request.Builder().get()
                .header("Authorization",  "Int ${Config.SECRET_DISCAL_API_KEY.getString()}")
                .url(url)
                .build()

            HTTP_CLIENT.newCall(request).execute()
        }.subscribeOn(Schedulers.boundedElastic()).flatMap { response ->
            when (response.code) {
                HttpStatusCodes.STATUS_CODE_OK -> {
                    val data = JSON_FORMAT.decodeFromString(CredentialData.serializer(), response.body!!.string())
                    response.body?.close()
                    response.close()

                    discalTokens[credentialId] = data
                    Mono.just(data.accessToken)
                }
                else -> {
                    val error = JSON_FORMAT.decodeFromString(RestError.serializer(), response.body!!.string())
                    response.body?.close()
                    response.close()

                    // Log because this really shouldn't be happening
                    LOGGER.error(DEFAULT, "[Google] Error requesting access token from CAM for Int. | credentialId:$credentialId | error:$error")
                    Mono.empty()
                }
            }
        }.doOnError {
            LOGGER.error("GoogleAuth | Failed to get access token from CAM | credentialId:$credentialId", it)
        }.onErrorResume { Mono.empty() }
    }

    private fun getAccessToken(calData: CalendarData): Mono<String> {
        val token = externalTokens[calData.guildId]
        if (token != null && !token.isExpired()) {
            return Mono.just(token.accessToken)
        }

        LOGGER.debug("Refreshing local token via CAM | guild:{} | host:{} | calendarId:{} ", calData.guildId, calData.host, calData.calendarNumber)

        return Mono.fromCallable {
            val url = "${Config.URL_CAM.getString()}/v1/token".toHttpUrlOrNull()!!.newBuilder()
                .addQueryParameter("host", calData.host.name)
                .addQueryParameter("guild", calData.guildId.asString())
                .addQueryParameter("id", calData.calendarNumber.toString())
                .build()

            val request = Request.Builder().get()
                .header("Authorization", "Int ${Config.SECRET_DISCAL_API_KEY.getString()}")
                .url(url)
                .build()

            HTTP_CLIENT.newCall(request).execute()
        }.subscribeOn(Schedulers.boundedElastic()).flatMap { response ->
            when (response.code) {
                HttpStatusCodes.STATUS_CODE_OK -> {
                    val data = JSON_FORMAT.decodeFromString(CredentialData.serializer(), response.body!!.string())
                    response.body?.close()
                    response.close()

                    externalTokens[calData.guildId] = data
                    Mono.just(data.accessToken)
                }
                else -> {
                    val error = JSON_FORMAT.decodeFromString(RestError.serializer(), response.body!!.string())
                    response.body?.close()
                    response.close()

                    when (error) {
                        RestError.ACCESS_REVOKED -> {
                            // Delete calendar, user MUST reauthorize discal as the refresh token isn't valid.
                            DatabaseManager.deleteCalendarAndRelatedData(calData).then(Mono.empty())
                        }
                        else -> {
                            //An unknown/unsupported error has occurred, log and return empty, upstream can handle this
                            LOGGER.debug(DEFAULT, "[Google] Error requesting access token from CAM for Ext. | {}", error)
                            Mono.empty()
                        }
                    }
                }
            }
        }
    }

    fun getCalendarService(calData: CalendarData): Mono<GoogleCalendarService> {
        return Mono.defer {
            if (calData.external) {
                authorize(calData).map(this::buildService)
            } else {
                getCalendarService(calData.credentialId)
            }
        }.switchIfEmpty(Mono.error(EmptyNotAllowedException()))
    }

    fun getCalendarService(credentialId: Int): Mono<GoogleCalendarService> {
        return authorize(credentialId)
                .map(this::buildService)
                .switchIfEmpty(Mono.error(EmptyNotAllowedException()))
    }

    fun randomCredentialId() = Random.nextInt(Config.SECRET_GOOGLE_CREDENTIAL_COUNT.getInt())
}
