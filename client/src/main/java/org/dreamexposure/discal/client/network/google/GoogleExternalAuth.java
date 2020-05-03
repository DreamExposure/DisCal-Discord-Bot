package org.dreamexposure.discal.client.network.google;

import com.google.api.client.http.HttpStatusCodes;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarListEntry;

import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.crypto.AESEncryption;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.network.google.Authorization;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.network.google.Poll;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;
import org.json.JSONObject;

import java.util.List;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    private static GoogleExternalAuth auth;

    private GoogleExternalAuth() {
    }

    public static GoogleExternalAuth getAuth() {
        if (auth == null)
            auth = new GoogleExternalAuth();

        return auth;
    }

    public void requestCode(MessageCreateEvent event, GuildSettings settings) {
        try {
            RequestBody body = new FormBody.Builder()
                .addEncoded("client_id", Authorization.getAuth().getClientData().getClientId())
                .addEncoded("scope", CalendarScopes.CALENDAR)
                .build();

            Request httpRequest = new okhttp3.Request.Builder()
                .url("https://accounts.google.com/o/oauth2/device/code")
                .post(body)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

            Response response = Authorization.getAuth().getClient().newCall(httpRequest).execute();

            if (response.code() == HttpStatusCodes.STATUS_CODE_OK) {
                JSONObject codeResponse = new JSONObject(response.body().string());

                //Send DM to user with code.
                Consumer<EmbedCreateSpec> embed = spec -> {
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);
                    spec.setTitle(Messages.getMessage("Embed.AddCalendar.Code.Title", settings));
                    spec.addField(Messages.getMessage("Embed.AddCalendar.Code.Code", settings), codeResponse.getString("user_code"), true);
                    spec.setFooter(Messages.getMessage("Embed.AddCalendar.Code.Footer", settings), null);

                    spec.setUrl(codeResponse.getString("verification_url"));
                    spec.setColor(GlobalConst.discalColor);
                };


                User user = event.getMember().get();
                Messages
                    .sendDirectMessage(Messages
                        .getMessage("AddCalendar.Auth.Code.Request.Success", settings), embed, user)
                    .subscribe();

                //Start timer to poll Google Cal for auth
                Poll poll = new Poll(user, event.getGuild().block());

                poll.setDevice_code(codeResponse.getString("device_code"));
                poll.setRemainingSeconds(codeResponse.getInt("expires_in"));
                poll.setExpires_in(codeResponse.getInt("expires_in"));
                poll.setInterval(codeResponse.getInt("interval"));
                pollForAuth(poll);
            } else {
                Messages
                    .sendDirectMessage(Messages
                            .getMessage("AddCalendar.Auth.Code.Request.Failure.NotOkay", settings),
                        event.getMember().get())
                    .subscribe();

                LogFeed.log(LogObject
                    .forDebug("Error request access token", "Status code: " + response.code() +
                        " | " + response.message() +
                        " | " + response.body().string()));
            }
        } catch (Exception e) {
            //Failed, report issue to dev.
            LogFeed.log(LogObject
                .forException("Failed to request google access code", e, this.getClass()));

            Member u = event.getMember().get();
            Messages
                .sendDirectMessage(Messages
                    .getMessage("AddCalendar.Auth.Code.Request.Failure.Unknown", settings), u)
                .subscribe();
        }
    }

    void pollForAuth(Poll poll) {
        GuildSettings settings = DatabaseManager.getSettings(poll.getGuild().getId()).block();
        try {
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

            //Execute
            Response response = Authorization.getAuth().getClient().newCall(httpRequest).execute();


            //Handle response.
            if (response.code() == 403) {
                //Handle access denied
                Messages
                    .sendDirectMessage(Messages.getMessage("AddCalendar.Auth.Poll.Failure.Deny",
                        settings), poll.getUser())
                    .subscribe();
            } else if (response.code() == 400 || response.code() == 428) {
                try {
                    //See if auth is pending, if so, just reschedule.
                    JSONObject aprError = new JSONObject(response.body().string());

                    if (aprError.getString("error").equalsIgnoreCase("authorization_pending")) {
                        //Response pending
                        PollManager.getManager().scheduleNextPoll(poll);
                    } else if (aprError.getString("error").equalsIgnoreCase("expired_token")) {
                        Messages
                            .sendDirectMessage(Messages
                                    .getMessage("AddCalendar.Auth.Poll.Failure.Expired", settings),
                                poll.getUser())
                            .subscribe();
                    } else {
                        Messages
                            .sendDirectMessage(Messages
                                .getMessage("Notification.Error.Network", settings), poll.getUser())
                            .subscribe();
                        LogFeed.log(LogObject
                            .forDebug("Poll Failure!", "Status code: " + response.code() +
                                " | " + response.message() +
                                " | " + response.body().string()));
                    }
                } catch (Exception e) {
                    //Auth is not pending, error occurred.
                    LogFeed.log(LogObject
                        .forException("Failed to poll for authorization to google account.", e,
                            this.getClass()));
                    LogFeed.log(LogObject
                        .forDebug("More info on failure", "Status code: " + response.code() +
                            " | " + response.message() +
                            " | " + response.body().string()));

                    Messages
                        .sendDirectMessage(Messages.getMessage("Notification.Error.Network",
                            settings), poll.getUser())
                        .subscribe();
                }
            } else if (response.code() == 429) {
                //We got rate limited... oops. Let's just poll half as often.
                poll.setInterval(poll.getInterval() * 2);
                PollManager.getManager().scheduleNextPoll(poll);
            } else if (response.code() == HttpStatusCodes.STATUS_CODE_OK) {
                //Access granted
                JSONObject aprGrant = new JSONObject(response.body().string());

                //Save credentials securely.
                GuildSettings gs = DatabaseManager.getSettings(poll.getGuild().getId()).block();
                AESEncryption encryption = new AESEncryption(gs);
                gs.setEncryptedAccessToken(encryption.encrypt(aprGrant.getString("access_token")));
                gs.setEncryptedRefreshToken(encryption.encrypt(aprGrant.getString("refresh_token")));
                gs.setUseExternalCalendar(true);
                DatabaseManager.updateSettings(gs).subscribe();

                List<CalendarListEntry> items = CalendarWrapper.getUsersExternalCalendars(settings).block();

                if (items == null) {
                    Messages
                        .sendDirectMessage(Messages
                                .getMessage("AddCalendar.Auth.Poll.Failure.ListCalendars", settings),
                            poll.getUser())
                        .subscribe();
                } else {
                    Messages.sendDirectMessage(Messages
                        .getMessage("AddCalendar.Auth.Poll.Success", settings), poll.getUser())
                        .subscribe();
                    for (CalendarListEntry i : items) {
                        if (!i.isDeleted()) {
                            Consumer<EmbedCreateSpec> embed = spec -> {
                                spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);
                                spec.setTitle(Messages.getMessage("Embed.AddCalendar.List.Title", settings));
                                spec.addField(Messages.getMessage("Embed.AddCalendar.List.Name", settings), i.getSummary(), false);
                                spec.addField(Messages.getMessage("Embed.AddCalendar.List.TimeZone", settings), i.getTimeZone(), false);
                                spec.addField(Messages.getMessage("Embed.AddCalendar.List.ID", settings), i.getId(), false);
                                spec.setColor(GlobalConst.discalColor);
                            };

                            Messages.sendDirectMessage(embed, poll.getUser()).subscribe();
                        }
                    }
                    //Response will be handled in guild, and will check. We already saved the tokens anyway.
                }
            } else {
                //Unknown network error...
                Messages
                    .sendDirectMessage(Messages.getMessage("Notification.Error.Network", settings),
                        poll.getUser())
                    .subscribe();

                LogFeed.log(LogObject
                    .forDebug("Network error; poll failure", "Status code: " + response.code()
                        + " | " + response.message() + " | " + response.body().string()));
            }
        } catch (Exception e) {
            //Handle exception.
            LogFeed.log(LogObject
                .forException("Failed to poll for authorization to google account", e,
                    this.getClass()));

            Messages
                .sendDirectMessage(Messages.getMessage("Notification.Error.Unknown", settings),
                    poll.getUser())
                .subscribe();
        }
    }
}