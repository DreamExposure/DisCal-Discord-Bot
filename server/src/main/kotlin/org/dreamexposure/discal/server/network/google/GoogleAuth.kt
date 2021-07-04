package org.dreamexposure.discal.server.network.google

import com.google.api.services.calendar.CalendarScopes
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.RequestBody
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.google.GoogleCredentialData
import org.dreamexposure.discal.core.`object`.network.google.DisCalPoll
import org.dreamexposure.discal.core.crypto.AESEncryption
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.exceptions.GoogleAuthCancelException
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.network.google.Authorization
import org.dreamexposure.discal.core.utils.GlobalVal
import org.json.JSONObject
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import kotlin.system.exitProcess

object GoogleAuth {
    fun requestCode(credNumber: Int): Mono<Void> {
        return Mono.defer {
            val body: RequestBody = FormBody.Builder()
                    .addEncoded("client_id", Authorization.getAuth().clientData.clientId)
                    .addEncoded("scope", CalendarScopes.CALENDAR)
                    .build()

            val httpRequest = Request.Builder()
                    .url("https://accounts.google.com/o/oauth2/device/code")
                    .post(body)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build()

            Mono.fromCallable {
                Authorization.getAuth().client.newCall(httpRequest).execute()
            }.subscribeOn(Schedulers.boundedElastic()).map { response ->
                val responseBody = response.body()!!.string()

                if (response.code() == GlobalVal.STATUS_SUCCESS) {
                    val codeResponse = JSONObject(responseBody)

                    val url = codeResponse.getString("verification_url")
                    val code = codeResponse.getString("user_code")
                    LogFeed.log(LogObject.forDebug("[!GDC!] DisCal Google Cred Auth $credNumber", "$url | $code"))

                    val pol = DisCalPoll(credNumber,
                            codeResponse.getInt("interval"),
                            codeResponse.getInt("expires_in"),
                            codeResponse.getInt("expires_in"),
                            codeResponse.getString("device_code")
                    )

                    scheduleNextPoll(pol)
                } else {
                    LogFeed.log(LogObject.forDebug("Error request access token",
                            "Status code: ${response.code()} | ${response.message()} | $responseBody"))
                }
            }
        }.then()
    }

    private fun pollForAuth(poll: DisCalPoll): Mono<Void> {
        return Mono.defer {
            val body: RequestBody = FormBody.Builder()
                    .addEncoded("client_id", Authorization.getAuth().clientData.clientId)
                    .addEncoded("client_secret", Authorization.getAuth().clientData.clientSecret)
                    .addEncoded("code", poll.deviceCode)
                    .addEncoded("grant_type", "http://oauth.net/grant_type/device/1.0")
                    .build()

            val httpRequest = Request.Builder()
                    .url("https://www.googleapis.com/oauth2/v4/token")
                    .post(body)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build()

            Mono.fromCallable {
                Authorization.getAuth().client.newCall(httpRequest).execute()
            }.subscribeOn(Schedulers.boundedElastic()).flatMap { response ->
                val responseBody = response.body()!!.string()

                if (response.code() == GlobalVal.STATUS_FORBIDDEN) {
                    //Handle access denied
                    LogFeed.log(LogObject.forDebug("[!GDC!] Access denied for credential: ${poll.credNumber}"))

                    Mono.error<GoogleAuthCancelException>(GoogleAuthCancelException())
                } else if (response.code() == GlobalVal.STATUS_BAD_REQUEST
                        || response.code() == GlobalVal.STATUS_PRECONDITION_REQUIRED) {
                    //See if auth is pending, if so, just reschedule.

                    val aprError = JSONObject(responseBody)
                    when {
                        aprError.optString("error").equals("authorization_pending", true) -> {
                            //Response pending
                            Mono.empty()
                        }
                        aprError.optString("error").equals("expired_token", true) -> {
                            //Token expired, auth is cancelled
                            LogFeed.log(LogObject.forDebug("[!GDC!] token expired."))

                            Mono.error(GoogleAuthCancelException())
                        }
                        else -> {
                            LogFeed.log(LogObject.forDebug("[!GDC!] Poll Failure!",
                                    "Status code: ${response.code()} | ${response.message()} | $responseBody"))

                            Mono.error(GoogleAuthCancelException())
                        }
                    }
                } else if (response.code() == GlobalVal.STATUS_RATE_LIMITED) {
                    //We got rate limited... oops. Lets just poll half as often...
                    poll.interval = poll.interval * 2

                    Mono.empty()
                } else if (response.code() == GlobalVal.STATUS_SUCCESS) {
                    //Access granted, save credentials...
                    val aprGrant = JSONObject(responseBody)
                    val aes = AESEncryption(BotSettings.CREDENTIALS_KEY.get())

                    val encryptedRefresh = aes.encrypt(aprGrant.getString("refresh_token"))
                    val encryptedAccess = aes.encrypt(aprGrant.getString("access_token"))

                    val creds = GoogleCredentialData(poll.credNumber, encryptedRefresh, encryptedAccess)

                    DatabaseManager.updateCredentialData(creds)
                            .then(Mono.error(GoogleAuthCancelException()))
                } else {
                    //Unknown network error...
                    LogFeed.log(LogObject.forDebug("[!GDC!] Network error; poll failure",
                            "Status code: ${response.code()} | ${response.message()} | $responseBody"))

                    Mono.error(GoogleAuthCancelException())
                }
            }
        }.then()
    }

    private fun scheduleNextPoll(poll: DisCalPoll) {
        Mono.defer {
            poll.remainingSeconds = poll.remainingSeconds - poll.interval

            pollForAuth(poll)
        }.then(Mono.delay(Duration.ofSeconds(poll.interval.toLong())))
                .repeat()
                .then()
                .doOnError(GoogleAuthCancelException::class.java) {
                    //Exit, this is an illegal state as this is only supposed to get run when generating new credentials
                    exitProcess(420)
                }
                .subscribe()
    }
}
