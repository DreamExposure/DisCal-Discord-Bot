package org.dreamexposure.discal.client.network.google;

import com.google.api.client.http.HttpStatusCodes;
import com.google.api.services.calendar.CalendarScopes;

import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.crypto.AESEncryption;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.exceptions.GoogleAuthCancelException;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.network.google.Authorization;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.network.google.Poll;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;
import org.json.JSONObject;

import java.util.function.Consumer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
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
@SuppressWarnings({"ConstantConditions", "OptionalGetWithoutIsPresent"})
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

    public Mono<Void> requestCode(MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            RequestBody body = new FormBody.Builder()
                .addEncoded("client_id", Authorization.getAuth().getClientData().getClientId())
                .addEncoded("scope", CalendarScopes.CALENDAR)
                .build();

            Request httpRequest = new okhttp3.Request.Builder()
                .url("https://accounts.google.com/o/oauth2/device/code")
                .post(body)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();


            return Mono.fromCallable(() -> Authorization.getAuth().getClient().newCall(httpRequest).execute())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(response -> Mono.fromCallable(() -> response.body().string()).flatMap(responseBody -> {
                    if (response.code() == HttpStatusCodes.STATUS_CODE_OK) {
                        JSONObject codeResponse = new JSONObject(responseBody);

                        //Send DM to user with code.
                        Consumer<EmbedCreateSpec> embed = spec -> {
                            spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);
                            spec.setTitle(Messages.getMessage("Embed.AddCalendar.Code.Title", settings));

                            spec.addField(
                                Messages.getMessage("Embed.AddCalendar.Code.Code", settings),
                                codeResponse.getString("user_code"),
                                true);
                            spec.setFooter(Messages.getMessage("Embed.AddCalendar.Code.Footer", settings), null);

                            spec.setUrl(codeResponse.getString("verification_url"));
                            spec.setColor(GlobalConst.discalColor);
                        };

                        return event.getMessage().getAuthorAsMember().flatMap(user -> {
                            //Start timer to poll Google Cal for auth
                            Poll poll = new Poll(user, settings);

                            poll.setDevice_code(codeResponse.getString("device_code"));
                            poll.setRemainingSeconds(codeResponse.getInt("expires_in"));
                            poll.setExpires_in(codeResponse.getInt("expires_in"));
                            poll.setInterval(codeResponse.getInt("interval"));

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

    Mono<Void> pollForAuth(Poll poll) {
        return Mono.defer(() -> {
            RequestBody body = new FormBody.Builder()
                .addEncoded("client_id", Authorization.getAuth().getClientData().getClientId())
                .addEncoded("client_secret", Authorization.getAuth().getClientData().getClientSecret())
                .addEncoded("code", poll.getDevice_code())
                .addEncoded("grant_type", "http://oauth.net/grant_type/device/1.0")
                .build();

            Request httpRequest = new okhttp3.Request.Builder()
                .url("https://www.googleapis.com/oauth2/v4/token")
                .post(body)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

            return Mono.fromCallable(() -> Authorization.getAuth().getClient().newCall(httpRequest).execute())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(response -> Mono.fromCallable(() -> response.body().string()).flatMap(responseBody -> {
                    if (response.code() == 403) {
                        //Handle access denied
                        return Messages.sendDirectMessage(Messages.getMessage("AddCalendar.Auth.Poll.Failure.Deny",
                            poll.getSettings()), poll.getUser())
                            .then(Mono.error(new GoogleAuthCancelException()));
                    } else if (response.code() == 400 || response.code() == 428) {
                        //See if auth is pending, if so, just reschedule.
                        JSONObject aprError = new JSONObject(responseBody);

                        if (aprError.getString("error").equalsIgnoreCase("authorization_pending")) {
                            //Response pending
                            return Mono.empty();
                        } else if (aprError.getString("error").equalsIgnoreCase("expired_token")) {
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
                    } else if (response.code() == 429) {
                        //We got rate limited... oops. Let's just poll half as often.
                        poll.setInterval(poll.getInterval() * 2);
                        //PollManager.getManager().scheduleNextPoll(poll);

                        return Mono.empty();
                    } else if (response.code() == HttpStatusCodes.STATUS_CODE_OK) {
                        LogFeed.log(LogObject.forDebug("Auth 200: 1"));
                        //Access granted
                        JSONObject aprGrant = new JSONObject(responseBody);

                        //Save credentials securely.
                        return DatabaseManager.getSettings(poll.getSettings().getGuildID()).map(gs -> {
                            LogFeed.log(LogObject.forDebug("Auth 200: 2"));
                            AESEncryption encryption = new AESEncryption(gs);

                            gs.setEncryptedAccessToken(encryption.encrypt(aprGrant.getString("access_token")));
                            gs.setEncryptedRefreshToken(encryption.encrypt(aprGrant.getString("refresh_token")));
                            gs.setUseExternalCalendar(true);

                            return DatabaseManager.updateSettings(gs)
                                .doOnNext(s -> LogFeed.log(LogObject.forDebug("Auth 200: 2.1")))
                                .then(CalendarWrapper.getUsersExternalCalendars(gs)
                                    .doOnNext(s -> LogFeed.log(LogObject.forDebug("Auth 200: 2.2")))
                                    .flatMap(cals -> Flux.fromIterable(cals)
                                        .doOnNext(s -> LogFeed.log(LogObject.forDebug("Auth 200: 2.3")))
                                        .map(i -> (Consumer<EmbedCreateSpec>) spec -> {
                                            spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                                            spec.setTitle(
                                                Messages.getMessage("Embed.AddCalendar.List.Title", gs));

                                            spec.addField(
                                                Messages.getMessage("Embed.AddCalendar.List.Name", gs),
                                                i.getSummary(),
                                                false);

                                            spec.addField(
                                                Messages.getMessage("Embed.AddCalendar.List.TimeZone", gs),
                                                i.getTimeZone(),
                                                false);

                                            spec.addField(
                                                Messages.getMessage("Embed.AddCalendar.List.ID", gs),
                                                i.getId(),
                                                false);

                                            spec.setColor(GlobalConst.discalColor);
                                        })
                                        .doOnNext(s -> LogFeed.log(LogObject.forDebug("Auth 200: 3")))
                                        .flatMap(em -> Messages.sendDirectMessage(em, poll.getUser()))
                                        .doOnNext(s -> LogFeed.log(LogObject.forDebug("Auth 200: 3.1")))
                                        .then(Mono.just(GlobalConst.NOT_EMPTY))
                                    ))
                                .switchIfEmpty(Messages.sendDirectMessage(
                                    Messages.getMessage("AddCalendar.Auth.Poll.Failure.ListCalendars", gs)
                                    , poll.getUser()).then(Mono.empty()))
                                .then(Mono.error(new GoogleAuthCancelException()));
                        });
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