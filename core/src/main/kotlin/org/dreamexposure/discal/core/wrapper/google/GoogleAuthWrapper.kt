@file:Suppress("DEPRECATION")

package org.dreamexposure.discal.core.wrapper.google

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.CalendarScopes
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.`object`.google.ClientData
import org.dreamexposure.discal.core.`object`.google.GoogleAuthPoll
import org.dreamexposure.discal.core.crypto.AESEncryption
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.google.DisCalGoogleCredential
import org.dreamexposure.discal.core.exceptions.EmptyNotAllowedException
import org.dreamexposure.discal.core.exceptions.google.GoogleAuthCancelException
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.json.JSONObject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.time.Instant
import kotlin.random.Random
import kotlin.system.exitProcess
import com.google.api.services.calendar.Calendar as GoogleCalendarService

@Suppress("BlockingMethodInNonBlockingContext")
object GoogleAuthWrapper {
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

    private fun authorize(credentialId: Int): Mono<Credential> {
        return CREDENTIALS
                .filter { it.credentialData.credentialNumber == credentialId }
                .next()
                .flatMap(this::requestNewAccessToken)
                .map(GoogleCredential()::setAccessToken)
                .ofType(Credential::class.java) //Cast down to the class it extends
                .switchIfEmpty(Mono.error(EmptyNotAllowedException()))
    }

    private fun authorize(calData: CalendarData): Mono<Credential> {
        return Mono.just(calData).filter { !"N/a".equals(calData.encryptedAccessToken, true) }
                .flatMap(this::requestNewAccessToken)
                .map(GoogleCredential()::setAccessToken)
                .ofType(Credential::class.java) //Cast down to the class it extends
                .switchIfEmpty(Mono.error(EmptyNotAllowedException()))
    }

    private fun buildService(credential: Credential): GoogleCalendarService {

        return GoogleCalendarService.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("DisCal")
                .build()
    }

    private fun requestNewAccessToken(calData: CalendarData, encryption: AESEncryption): Mono<String> {
        //Check expire time, if fine, we can just pass the access_token right back without any requests.
        if (!calData.expired()) {
            return Mono.just(encryption.decrypt(calData.encryptedAccessToken))
        }

        return Mono.fromCallable {
            val body = FormBody.Builder()
                    .addEncoded("client_id", clientData.clientId)
                    .addEncoded("client_secret", clientData.clientSecret)
                    .addEncoded("refresh_token", encryption.decrypt(calData.encryptedRefreshToken))
                    .addEncoded("grant_type", "refresh_token")
                    .build()

            val request = Request.Builder()
                    .url("https://www.googleapis.com/oauth2/v4/token")
                    .post(body)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build()

            GlobalVal.HTTP_CLIENT.newCall(request).execute()
        }.subscribeOn(Schedulers.boundedElastic()).flatMap { response ->
            when (response.code) {
                GlobalVal.STATUS_SUCCESS -> {
                    val responseJson = JSONObject(response.body!!.string())
                    response.body?.close()
                    response.close()

                    //Update Db data and return
                    calData.encryptedAccessToken = encryption.encrypt(responseJson.getString("access_token"))
                    calData.expiresAt = Instant.now().plusSeconds(responseJson.getLong("expires_in"))

                    DatabaseManager.updateCalendar(calData).thenReturn(responseJson.getString("access_token"))
                }
                GlobalVal.STATUS_BAD_REQUEST -> {
                    val errorBody = JSONObject(response.body!!.string())
                    response.body?.close()
                    response.close()

                    if ("invalid_grant".equals(errorBody.getString("error"), true)) {
                        //User revoked access to calendar, delete our reference as they need to re-auth it.
                        return@flatMap Mono.`when`(
                                DatabaseManager.deleteCalendar(calData),
                                DatabaseManager.deleteAllEventData(calData.guildId, calData.calendarNumber),
                                DatabaseManager.deleteAllRsvpData(calData.guildId, calData.calendarNumber),
                                DatabaseManager.deleteAllAnnouncementData(calData.guildId, calData.calendarNumber)
                        ).then(Mono.empty())
                    } else {
                        LOGGER.debug(DEFAULT, "[!DGC!] err requesting new access token. " +
                                "Code: ${response.code} | ${response.message} | $errorBody")
                        return@flatMap Mono.empty()
                    }
                }
                else -> {
                    //Failed to get OK. Send debug info...
                    LOGGER.debug(DEFAULT, "[!DGC!] Err requesting new access token. Code: ${response.code} | ${response.message} | ${response.body?.string()}")
                    response.body?.close()
                    response.close()

                    return@flatMap Mono.empty()
                }
            }
        }.doOnError {
            LOGGER.error("[!DGC!] Failed to request new access token", it)
        }.onErrorResume { Mono.empty() }
    }

    private fun requestNewAccessToken(calData: CalendarData): Mono<String> {
        return requestNewAccessToken(calData, AESEncryption(calData.privateKey))
    }

    private fun requestNewAccessToken(credential: DisCalGoogleCredential): Mono<String> {
        //Check expire time, if fine, we can just pass the access_token right back without any requests.
        if (!credential.expired()) {
            return Mono.just(credential.getAccessToken())
        }

        return Mono.fromCallable {
            val body = FormBody.Builder()
                    .addEncoded("client_id", this.clientData.clientId)
                    .addEncoded("client_secret", this.clientData.clientSecret)
                    .addEncoded("refresh_token", credential.getRefreshToken())
                    .addEncoded("grant_type", "refresh_token")
                    .build()

            val request = Request.Builder()
                    .url("https://www.googleapis.com/oauth2/v4/token")
                    .post(body)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build()

            GlobalVal.HTTP_CLIENT.newCall(request).execute()
        }.subscribeOn(Schedulers.boundedElastic()).flatMap { response ->
            when (response.code) {
                GlobalVal.STATUS_SUCCESS -> {
                    val responseJson = JSONObject(response.body!!.string())
                    response.body?.close()
                    response.close()

                    //Update DB and return
                    credential.setAccessToken(responseJson.getString("access_token"))
                    credential.credentialData.expiresAt = Instant.now().plusSeconds(responseJson.getLong("expires_in"))
                    DatabaseManager.updateCredentialData(credential.credentialData)
                            .thenReturn(responseJson.getString
                            ("access_token"))
                }
                GlobalVal.STATUS_BAD_REQUEST -> {
                    val errorBody = JSONObject(response.body!!.string())
                    response.body?.close()
                    response.close()

                    if ("invalid_grant".equals(errorBody.getString("error"), true)) {
                        //We revoked access to this account. Is this on purpose??
                        LOGGER.debug(DEFAULT, "[!DGC!] GOOGLE CALENDAR CREDENTIAL REFRESH FAILURE CredId: " +
                                "${credential.credentialData.credentialNumber}")
                    } else {
                        LOGGER.debug(DEFAULT, "[!DGC!] Error requesting new access token. Status code: " +
                                "${response.code} | ${response.message} | $errorBody")
                    }
                    return@flatMap Mono.empty()
                }
                else -> {
                    //Failed to get OK. Send debug info.
                    LOGGER.debug(DEFAULT, "[!DGC!] Error requesting new access token. Status code: ${response.code} |" +
                            " ${response.message} | ${response.body?.string()}")
                    response.body?.close()
                    response.close()
                    return@flatMap Mono.empty()
                }
            }
        }.doOnError {
            LOGGER.error(DEFAULT, "[!DGC!] Failed to request new access token", it)
        }.onErrorResume { Mono.empty() }
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

    fun getAllDisCalServices(): Flux<GoogleCalendarService> {
        return credentialsCount()
                .flatMapMany { Flux.range(0, it) }
                .flatMap(this::getCalendarService)
                .switchIfEmpty(Mono.error(EmptyNotAllowedException()))
    }

    fun credentialsCount(): Mono<Int> = CREDENTIALS.count().map(Long::toInt)

    fun randomCredentialId(): Mono<Int> = credentialsCount().map(Random::nextInt)

    fun requestDeviceCode(): Mono<Response> {
        return Mono.fromCallable {
            val body = FormBody.Builder()
                    .addEncoded("client_id", clientData.clientId)
                    .addEncoded("scope", CalendarScopes.CALENDAR)
                    .build()

            val request = Request.Builder()
                    .url("https://accounts.google.com/o/oauth2/device/code")
                    .post(body)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build()

            GlobalVal.HTTP_CLIENT.newCall(request).execute()
        }.subscribeOn(Schedulers.boundedElastic())
    }

    fun requestPollResponse(poll: GoogleAuthPoll): Mono<Response> {
        return Mono.fromCallable {
            val body = FormBody.Builder()
                    .addEncoded("client_id", clientData.clientId)
                    .addEncoded("client_secret", clientData.clientSecret)
                    .addEncoded("code", poll.deviceCode)
                    .addEncoded("grant_type", "http://oauth.net/grant_type/device/1.0")
                    .build()

            val request = Request.Builder()
                    .url("https://www.googleapis.com/oauth2/v4/token")
                    .post(body)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build()

            GlobalVal.HTTP_CLIENT.newCall(request).execute()
        }.subscribeOn(Schedulers.boundedElastic())
    }

    fun scheduleOAuthPoll(poll: GoogleAuthPoll): Mono<Void> {
        poll.remainingSeconds = poll.remainingSeconds - poll.interval

        return poll.callback(poll)
                .then(Mono.delay(Duration.ofSeconds(poll.interval.toLong())))
                .repeat()
                .then()
                .onErrorResume(GoogleAuthCancelException::class.java) { Mono.empty() }
    }
}
