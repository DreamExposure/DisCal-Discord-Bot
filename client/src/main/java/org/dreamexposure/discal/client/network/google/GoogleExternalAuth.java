package org.dreamexposure.discal.client.network.google;

import com.google.api.client.http.HttpStatusCodes;
import com.google.api.services.calendar.CalendarScopes;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.crypto.AESEncryption;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.calendar.CalendarHost;
import org.dreamexposure.discal.core.exceptions.GoogleAuthCancelException;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.network.google.Authorization;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.network.google.Poll;
import org.dreamexposure.discal.core.utils.GlobalVal;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;
import org.json.JSONObject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author NovaFox161
 * Date Created: 9/9/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
@SuppressWarnings("ConstantConditions")
public class GoogleExternalAuth {
    static {
        auth = new GoogleExternalAuth();
    }

    private final static GoogleExternalAuth auth;

    private GoogleExternalAuth() {
    }

    public static GoogleExternalAuth getAuth() {
        return auth;
    }

    public Mono<Void> requestCode(final MessageCreateEvent event, final GuildSettings settings) {
        return Mono.defer(() -> {
            final RequestBody body = new FormBody.Builder()
                .addEncoded("client_id", Authorization.getAuth().getClientData().getClientId())
                .addEncoded("scope", CalendarScopes.CALENDAR)
                .build();

            final Request httpRequest = new okhttp3.Request.Builder()
                .url("https://accounts.google.com/o/oauth2/device/code")
                .post(body)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();


            return Mono.fromCallable(() -> Authorization.getAuth().getClient().newCall(httpRequest).execute())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(response -> Mono.fromCallable(() -> response.body().string()).flatMap(responseBody -> {
                    if (response.code() == HttpStatusCodes.STATUS_CODE_OK) {
                        final JSONObject codeResponse = new JSONObject(responseBody);

                        //Send DM to user with code.
                        var embed = EmbedCreateSpec.builder()
                            .author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl())
                            .title(Messages.getMessage("Embed.AddCalendar.Code.Title", settings))
                            .addField(
                                Messages.getMessage("Embed.AddCalendar.Code.Code", settings),
                                codeResponse.getString("user_code"),
                                true)
                            .footer(Messages.getMessage("Embed.AddCalendar.Code.Footer", settings), null)
                            .url(codeResponse.getString("verification_url"))
                            .color(GlobalVal.getDiscalColor())
                            .build();

                        return event.getMessage().getAuthorAsMember().flatMap(user -> {
                            //Start timer to poll Google Cal for auth
                            final Poll poll = new Poll(
                                user,
                                settings,
                                codeResponse.getInt("interval"),
                                codeResponse.getInt("expires_in"),
                                codeResponse.getInt("expires_in"),
                                codeResponse.getString("device_code")
                            );

                            PollManager.getManager().scheduleNextPoll(poll);

                            return Messages.sendDirectMessage(
                                Messages.getMessage("AddCalendar.Auth.Code.Request.Success", settings), embed, user);
                        });
                    } else {
                        LogFeed.log(LogObject
                            .forDebug("Error request access token", "Status code: " + response.code() +
                                " | " + response.message() +
                                " | " + responseBody));

                        return event.getMessage().getAuthorAsMember()
                            .flatMap(m -> Messages.sendDirectMessage(
                                Messages.getMessage("AddCalendar.Auth.Code.Request.Failure.NotOkay", settings), m));
                    }
                }));
        })
            .onErrorResume(e -> {
                //Failed, report issue to dev.
                LogFeed.log(LogObject.forException("Failed to request google access code", e, this.getClass()));

                return event.getMessage().getAuthorAsMember()
                    .flatMap(m -> Messages.sendDirectMessage(
                        Messages.getMessage("AddCalendar.Auth.Code.Request.Failure.Unknown", settings), m));
            })
            .then();
    }

    Mono<Void> pollForAuth(final Poll poll) {
        return Mono.defer(() -> {
            final RequestBody body = new FormBody.Builder()
                .addEncoded("client_id", Authorization.getAuth().getClientData().getClientId())
                .addEncoded("client_secret", Authorization.getAuth().getClientData().getClientSecret())
                .addEncoded("code", poll.getDeviceCode())
                .addEncoded("grant_type", "http://oauth.net/grant_type/device/1.0")
                .build();

            final Request httpRequest = new okhttp3.Request.Builder()
                .url("https://www.googleapis.com/oauth2/v4/token")
                .post(body)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

            return Mono.fromCallable(() -> Authorization.getAuth().getClient().newCall(httpRequest).execute())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(response -> Mono.fromCallable(() -> response.body().string()).flatMap(responseBody -> {
                    if (response.code() == GlobalVal.STATUS_FORBIDDEN) {
                        //Handle access denied
                        return Messages.sendDirectMessage(Messages.getMessage("AddCalendar.Auth.Poll.Failure.Deny",
                            poll.getSettings()), poll.getUser())
                            .then(Mono.error(new GoogleAuthCancelException()));
                    } else if (response.code() == GlobalVal.STATUS_BAD_REQUEST
                        || response.code() == GlobalVal.STATUS_PRECONDITION_REQUIRED) {
                        //See if auth is pending, if so, just reschedule.
                        final JSONObject aprError = new JSONObject(responseBody);

                        if ("authorization_pending".equalsIgnoreCase(aprError.getString("error"))) {
                            //Response pending
                            return Mono.empty();
                        } else if ("expired_token".equalsIgnoreCase(aprError.getString("error"))) {
                            //Token expired, auth is cancelled
                            return Messages.sendDirectMessage(
                                Messages.getMessage("AddCalendar.Auth.Poll.Failure.Expired", poll.getSettings()),
                                poll.getUser())
                                .then(Mono.error(new GoogleAuthCancelException()));
                        } else {
                            LogFeed.log(LogObject.forDebug("Poll Failure!", "Status code: " + response.code() +
                                " | " + response.message() +
                                " | " + responseBody));

                            return Messages.sendDirectMessage(
                                Messages.getMessage("Notification.Error.Network", poll.getSettings()), poll.getUser())
                                .then(Mono.error(new GoogleAuthCancelException()));
                        }
                    } else if (response.code() == GlobalVal.STATUS_RATE_LIMITED) {
                        //We got rate limited... oops. Let's just poll half as often.
                        poll.setInterval(poll.getInterval() * 2);
                        //PollManager.getManager().scheduleNextPoll(poll);

                        return Mono.empty();
                    } else if (response.code() == HttpStatusCodes.STATUS_CODE_OK) {
                        //Access granted
                        final JSONObject aprGrant = new JSONObject(responseBody);

                        //Save credentials securely.
                        CalendarData calData = CalendarData.emptyExternal(poll.getSettings().getGuildID(), CalendarHost.GOOGLE);

                        final AESEncryption encryption = new AESEncryption(calData.getPrivateKey());


                        calData.setEncryptedAccessToken(encryption.encrypt(aprGrant.getString("access_token")));
                        calData.setEncryptedRefreshToken(encryption.encrypt(aprGrant.getString("refresh_token")));

                        //Update settings and then we will list the calendars for the user
                        return DatabaseManager.INSTANCE.updateCalendar(calData)
                            .then(CalendarWrapper.getUsersExternalCalendars(calData))
                            .flatMapMany(Flux::fromIterable)
                            .map(i -> EmbedCreateSpec.builder()
                                .author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl())
                                .title(Messages.getMessage("Embed.AddCalendar.List.Title", poll.getSettings()))
                                .addField(
                                    Messages.getMessage("Embed.AddCalendar.List.Name", poll.getSettings()),
                                    i.getSummary(),
                                    false)
                                .addField(
                                    Messages.getMessage("Embed.AddCalendar.List.TimeZone", poll.getSettings()),
                                    i.getTimeZone(),
                                    false)
                                .addField(
                                    Messages.getMessage("Embed.AddCalendar.List.ID", poll.getSettings()),
                                    i.getId(),
                                    false)
                                .color(GlobalVal.getDiscalColor())
                                .build())
                            .flatMap(em -> Messages.sendDirectMessage(em, poll.getUser()))
                            .switchIfEmpty(Messages.sendDirectMessage(
                                Messages.getMessage("AddCalendar.Auth.Poll.Failure.ListCalendars", poll.getSettings()),
                                poll.getUser()))
                            .then(Mono.error(new GoogleAuthCancelException()));
                    } else {
                        //Unknown network error...
                        LogFeed.log(LogObject.forDebug("Network error; poll failure", "Status code: " + response.code()
                            + " | " + response.message() + " | " + responseBody));

                        //Unknown network error...
                        return Messages.sendDirectMessage(
                            Messages.getMessage("Notification.Error.Network", poll.getSettings()), poll.getUser())
                            .then(Mono.error(new GoogleAuthCancelException()));
                    }
                }));
        })
            .onErrorResume(e -> !(e instanceof GoogleAuthCancelException), e -> {
                LogFeed.log(LogObject.forException("Failed to poll for authorization to google account", e,
                    this.getClass()));

                return Messages.sendDirectMessage(
                    Messages.getMessage("Notification.Error.Unknown", poll.getSettings()), poll.getUser())
                    .then(Mono.error(new GoogleAuthCancelException()));
            })
            .then();
    }
}
