package org.dreamexposure.discal.client.network.google

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import org.dreamexposure.discal.client.message.Messages
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.`object`.google.ExternalGoogleAuthPoll
import org.dreamexposure.discal.core.crypto.AESEncryption
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.exceptions.google.GoogleAuthCancelException
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.GlobalVal.discalColor
import org.dreamexposure.discal.core.utils.GlobalVal.iconUrl
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper
import org.dreamexposure.discal.core.wrapper.google.GoogleAuthWrapper
import org.json.JSONObject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.function.TupleUtils
import java.time.Instant
import java.util.function.Predicate

@Suppress("BlockingMethodInNonBlockingContext")
object GoogleExternalAuthHandler {
    fun requestCode(event: MessageCreateEvent, settings: GuildSettings): Mono<Void> {
        return GoogleAuthWrapper.requestDeviceCode().flatMap { response ->
            if (response.code == GlobalVal.STATUS_SUCCESS) {
                //Got code -- Send message with code, start auth poll
                val successJson = JSONObject(response.body!!.string())
                response.body?.close()
                response.close()

                val embed = EmbedCreateSpec.builder()
                        .author("DisCal", BotSettings.BASE_URL.get(), iconUrl)
                        .title(Messages.getMessage("Embed.AddCalendar.Code.Title", settings))
                        .addField(
                                Messages.getMessage("Embed.AddCalendar.Code.Code", settings),
                                successJson.getString("user_code"),
                                true)
                        .footer(Messages.getMessage("Embed.AddCalendar.Code.Footer", settings), null)
                        .url(successJson.getString("verification_url"))
                        .color(discalColor)
                        .build()

                event.message.authorAsMember.flatMap { user ->
                    val poll = ExternalGoogleAuthPoll(
                            user,
                            settings,
                            interval = successJson.getInt("interval"),
                            expiresIn = successJson.getInt("expires_in"),
                            remainingSeconds = successJson.getInt("expires_in"),
                            deviceCode = successJson.getString("device_code")
                    ) { this.pollForAuth(it as ExternalGoogleAuthPoll) }

                    Messages.sendDirectMessage(
                            Messages.getMessage("AddCalendar.Auth.Code.Request.Success", settings),
                            embed,
                            user
                    ).then(GoogleAuthWrapper.scheduleOAuthPoll(poll))
                }
            } else {
                //Bad response -- Log, send message
                val body = response.body?.string()
                response.body?.close()
                response.close()
                LOGGER.debug(DEFAULT, "Error request access token | Status code: ${response.code} | ${
                    response
                            .message
                } | $body")

                event.message.authorAsMember.flatMap {
                    Messages.sendDirectMessage(
                            Messages.getMessage("AddCalendar.Auth.Code.Request.Failure.NotOkay", settings), it)
                }
            }.then()
        }
    }

    private fun pollForAuth(poll: ExternalGoogleAuthPoll): Mono<Void> {
        return GoogleAuthWrapper.requestPollResponse(poll).flatMap { response ->
            when (response.code) {
                GlobalVal.STATUS_FORBIDDEN -> {
                    //Handle access denied -- Send message, cancel poll
                    Messages.sendDirectMessage(Messages.getMessage("AddCalendar.Auth.Poll.Failure.Deny", poll
                            .settings), poll.user)
                            .then(Mono.error(GoogleAuthCancelException()))
                }
                GlobalVal.STATUS_BAD_REQUEST, GlobalVal.STATUS_PRECONDITION_REQUIRED -> {
                    //See if auth is pending, if so, just reschedule...
                    val errorJson = JSONObject(response.body!!.string())
                    response.body?.close()
                    response.close()
                    when {
                        "authorization_pending".equals(errorJson.getString("error"), true) -> {
                            //Response pending
                            Mono.empty()
                        }
                        "expired_token".equals(errorJson.getString("error"), true) -> {
                            //Token is expired -- Send message, cancel poll
                            Messages.sendDirectMessage(Messages.getMessage("AddCal.Auth.Poll.Failure.Expired", poll
                                    .settings), poll.user)
                                    .then(Mono.error(GoogleAuthCancelException()))
                        }
                        else -> {
                            //Unknown error -- Log, send message, cancel poll
                            LOGGER.debug(DEFAULT, "[E.GCA] Poll failure", "Status: ${response.code} | ${response.message} | $errorJson")

                            Messages.sendDirectMessage(Messages.getMessage("Notification.Error.Network", poll.settings),
                                    poll.user)
                                    .then(Mono.error(GoogleAuthCancelException()))
                        }
                    }
                }
                GlobalVal.STATUS_RATE_LIMITED -> {
                    //We got rate limited. Oops. Let's just poll half as often.
                    poll.interval = poll.interval * 2

                    //Nothing else needs to be done for this to be handled
                    Mono.empty<Void>()
                }
                GlobalVal.STATUS_SUCCESS -> {
                    //Access granted -- Save creds, get calendars, list for user, cancel auth
                    val successJson = JSONObject(response.body!!.string())
                    response.body?.close()
                    response.close()

                    //Save creds
                    val calData = CalendarData.emptyExternal(poll.settings.guildID, CalendarHost.GOOGLE)
                    val encryption = AESEncryption(calData.privateKey)


                    val accessMono = encryption.encrypt(successJson.getString("access_token"))
                    val refreshMono = encryption.encrypt(successJson.getString("refresh_token"))

                    Mono.zip(accessMono, refreshMono).flatMap(TupleUtils.function { access, refresh ->
                        calData.encryptedAccessToken = access
                        calData.encryptedRefreshToken = refresh
                        calData.expiresAt = Instant.now().plusSeconds(successJson.getLong("expires_in"))


                        DatabaseManager.updateCalendar(calData)
                                .then(CalendarWrapper.getUsersExternalCalendars(calData))
                                .flatMapMany { Flux.fromIterable(it) }
                                .map { cal ->
                                    EmbedCreateSpec.builder()
                                            .author("DisCal", BotSettings.BASE_URL.get(), iconUrl)
                                            .title(Messages.getMessage("Embed.AddCalendar.List.Title", poll.settings))
                                            .addField(
                                                    Messages.getMessage("Embed.AddCalendar.List.Name", poll.settings),
                                                    cal.summary,
                                                    false)
                                            .addField(
                                                    Messages.getMessage("Embed.AddCalendar.List.TimeZone", poll.settings),
                                                    cal.timeZone,
                                                    false)
                                            .addField(
                                                    Messages.getMessage("Embed.AddCalendar.List.ID", poll.settings),
                                                    cal.id,
                                                    false)
                                            .color(discalColor)
                                            .build()
                                }.flatMap { Messages.sendDirectMessage(it, poll.user) }
                                .switchIfEmpty {
                                    Messages.sendDirectMessage(
                                            Messages.getMessage("AddCalendar.Auth.Poll.Failure.ListCalendars", poll.settings),
                                            poll.user
                                    )
                                }.then(Mono.error(GoogleAuthCancelException()))
                    })
                }
                else -> {
                    //Unknown error -- Log, send message, cancel poll
                    LOGGER.debug(DEFAULT, "Network error | poll failure" +
                            " | Status code: ${response.code} | ${response.message} | ${response.body?.string()}")
                    response.body?.close()
                    response.close()

                    Messages.sendDirectMessage(
                            Messages.getMessage("Notification.Error.Network", poll.settings), poll.user)
                            .then(Mono.error(GoogleAuthCancelException()))
                }
            }
        }.onErrorResume(Predicate.not(GoogleAuthCancelException::class::isInstance)) {
            //Other error -- Log, send message, cancel poll
            LOGGER.error(DEFAULT, "Failed to poll for authorization to google account", it)

            Messages.sendDirectMessage(Messages.getMessage("Notification.Error.Unknown", poll.settings), poll.user)
                    .then(Mono.error(GoogleAuthCancelException()))
        }
    }
}
