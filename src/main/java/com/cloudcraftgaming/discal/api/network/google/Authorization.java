package com.cloudcraftgaming.discal.api.network.google;

import com.cloudcraftgaming.discal.api.DisCalAPI;
import com.cloudcraftgaming.discal.api.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.api.crypto.AESEncryption;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.message.MessageManager;
import com.cloudcraftgaming.discal.api.message.calendar.CalendarMessageFormatter;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.json.google.AuthPollResponseError;
import com.cloudcraftgaming.discal.api.object.json.google.AuthPollResponseGrant;
import com.cloudcraftgaming.discal.api.object.json.google.AuthRefreshResponse;
import com.cloudcraftgaming.discal.api.object.json.google.CodeResponse;
import com.cloudcraftgaming.discal.api.object.network.google.ClientData;
import com.cloudcraftgaming.discal.api.object.network.google.Poll;
import com.cloudcraftgaming.discal.logger.Logger;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("ConstantConditions")
public class Authorization {
	private static Authorization instance;
	private ClientData clientData;
	private OkHttpClient client;

	private Authorization() {
	} //Prevent initialization.

	public static Authorization getAuth() {
		if (instance == null)
			instance = new Authorization();

		return instance;
	}

	public void init() {
		clientData = new ClientData(BotSettings.GOOGLE_CLIENT_ID.get(), BotSettings.GOOGLE_CLIENT_SECRET.get());
		client = new OkHttpClient();
	}

	public void requestCode(MessageReceivedEvent event, GuildSettings settings) {
		try {
			RequestBody body = new FormBody.Builder()
					.addEncoded("client_id", clientData.getClientId())
					.addEncoded("scope", CalendarScopes.CALENDAR)
					.build();

			Request httpRequest = new okhttp3.Request.Builder()
					.url("https://accounts.google.com/o/oauth2/device/code")
					.post(body)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.build();

			Response response = client.newCall(httpRequest).execute();

			if (response.code() == HttpStatusCodes.STATUS_CODE_OK) {
				Type type = new TypeToken<CodeResponse>() {
				}.getType();
				CodeResponse cr = new Gson().fromJson(response.body().string(), type);

				//Send DM to user with code.
				EmbedBuilder em = new EmbedBuilder();
				em.withAuthorIcon(DisCalAPI.getAPI().iconUrl);
				em.withAuthorName("DisCal");
				em.withTitle(MessageManager.getMessage("Embed.AddCalendar.Code.Title", settings));
				em.appendField(MessageManager.getMessage("Embed.AddCalendar.Code.Code", settings), cr.user_code, true);
				em.withFooterText(MessageManager.getMessage("Embed.AddCalendar.Code.Footer", settings));

				em.withUrl(cr.verification_url);
				em.withColor(36, 153, 153);

				IUser user = event.getAuthor();
				MessageManager.sendDirectMessage(MessageManager.getMessage("AddCalendar.Auth.Code.Request.Success", settings), em.build(), user);

				//Start timer to poll Google Cal for auth
				Poll poll = new Poll(user, event.getGuild());

				poll.setDevice_code(cr.device_code);
				poll.setRemainingSeconds(cr.expires_in);
				poll.setExpires_in(cr.expires_in);
				poll.setInterval(cr.interval);
				pollForAuth(poll);
			} else {
				MessageManager.sendDirectMessage(MessageManager.getMessage("AddCalendar.Auth.Code.Request.Failure.NotOkay", settings), event.getAuthor());

				Logger.getLogger().debug(event.getAuthor(), "Error requesting access token.", "Status code: " + response.code() + " | " + response.message() + " | " + response.body().string(), this.getClass(), true);
			}
		} catch (Exception e) {
			//Failed, report issue to dev.
			Logger.getLogger().exception(event.getAuthor(), "Failed to request Google Access Code", e, this.getClass(), true);
			IUser u = event.getAuthor();
			MessageManager.sendDirectMessage(MessageManager.getMessage("AddCalendar.Auth.Code.Request.Failure.Unknown", settings), u);
		}
	}

	public String requestNewAccessToken(GuildSettings settings, AESEncryption encryption) {
		try {
			RequestBody body = new FormBody.Builder()
					.addEncoded("client_id", clientData.getClientId())
					.addEncoded("client_secret", clientData.getClientSecret())
					.addEncoded("refresh_token", encryption.decrypt(settings.getEncryptedRefreshToken()))
					.addEncoded("grant_type", "refresh_token")
					.build();

			Request httpRequest = new okhttp3.Request.Builder()
					.url("https://www.googleapis.com/oauth2/v4/token")
					.post(body)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.build();

			Response httpResponse = client.newCall(httpRequest).execute();

			if (httpResponse.code() == HttpStatusCodes.STATUS_CODE_OK) {

				Type type = new TypeToken<AuthRefreshResponse>() {
				}.getType();
				AuthRefreshResponse response = new Gson().fromJson(httpResponse.body().string(), type);

				//Update Db data.
				settings.setEncryptedAccessToken(encryption.encrypt(response.access_token));
				DatabaseManager.getManager().updateSettings(settings);

				//Okay, we can return the access token to be used when this method is called.
				return response.access_token;
			} else {
				//Failed to get OK. Send debug info.
				Logger.getLogger().debug(null, "Error requesting new access token.", "Status code: " + httpResponse.code() + " | " + httpResponse.message() + " | " + httpResponse.body().string(), this.getClass(), true);
				return null;
			}

		} catch (Exception e) {
			//Error occurred, lets just log it and return null.
			Logger.getLogger().exception(null, "Failed to request new access token.", e, this.getClass(), true);
			return null;
		}
	}

	void pollForAuth(Poll poll) {
		GuildSettings settings = DatabaseManager.getManager().getSettings(poll.getGuild().getLongID());
		try {
			RequestBody body = new FormBody.Builder()
					.addEncoded("client_id", clientData.getClientId())
					.addEncoded("client_secret", clientData.getClientSecret())
					.addEncoded("code", poll.getDevice_code())
					.addEncoded("grant_type", "http://oauth.net/grant_type/device/1.0")
					.build();

			Request httpRequest = new okhttp3.Request.Builder()
					.url("https://www.googleapis.com/oauth2/v4/token")
					.post(body)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.build();

			//Execute
			Response response = client.newCall(httpRequest).execute();


			//Handle response.
			if (response.code() == 403) {
				//Handle access denied
				MessageManager.sendDirectMessage(MessageManager.getMessage("AddCalendar.Auth.Poll.Failure.Deny", settings), poll.getUser());
			} else if (response.code() == 400) {
				try {
					//See if auth is pending, if so, just reschedule.
					Type type = new TypeToken<AuthPollResponseError>() {
					}.getType();
					AuthPollResponseError apre = new Gson().fromJson(response.body().string(), type);

					if (apre.error.equalsIgnoreCase("authorization_pending")) {
						//Response pending
						PollManager.getManager().scheduleNextPoll(poll);
					} else if (apre.error.equalsIgnoreCase("expired_token")) {
						MessageManager.sendDirectMessage(MessageManager.getMessage("AddCalendar.Auth.Poll.Failure.Expired", settings), poll.getUser());
					} else {
						MessageManager.sendDirectMessage(MessageManager.getMessage("Notification.Error.Network", settings), poll.getUser());
						Logger.getLogger().debug(poll.getUser(), "Poll Failure!", "Status code: " + response.code() + " | " + response.message() + " | " + response.body().string(), this.getClass(), true);
					}
				} catch (Exception e) {
					//Auth is not pending, error occurred.
					Logger.getLogger().exception(poll.getUser(), "Failed to poll for authorization to google account.", e, this.getClass(), true);
					Logger.getLogger().debug(poll.getUser(), "More info on failure", "Status code: " + response.code() + " | " + response.message() + " | " + response.body().string(), this.getClass(), true);
					MessageManager.sendDirectMessage(MessageManager.getMessage("Notification.Error.Network", settings), poll.getUser());
				}
			} else if (response.code() == 429) {
				//We got rate limited... oops. Let's just poll half as often.
				poll.setInterval(poll.getInterval() * 2);
				PollManager.getManager().scheduleNextPoll(poll);
			} else if (response.code() == HttpStatusCodes.STATUS_CODE_OK) {
				//Access granted
				Type type = new TypeToken<AuthPollResponseGrant>() {
				}.getType();
				AuthPollResponseGrant aprg = new Gson().fromJson(response.body().string(), type);

				//Save credentials securely.
				GuildSettings gs = DatabaseManager.getManager().getSettings(poll.getGuild().getLongID());
				AESEncryption encryption = new AESEncryption(gs);
				gs.setEncryptedAccessToken(encryption.encrypt(aprg.access_token));
				gs.setEncryptedRefreshToken(encryption.encrypt(aprg.refresh_token));
				gs.setUseExternalCalendar(true);
				DatabaseManager.getManager().updateSettings(gs);

				try {
					Calendar service = CalendarAuth.getCalendarService(gs);
					List<CalendarListEntry> items = service.calendarList().list().setMinAccessRole("writer").execute().getItems();
					MessageManager.sendDirectMessage(MessageManager.getMessage("AddCalendar.Auth.Poll.Success", settings), poll.getUser());
					for (CalendarListEntry i: items) {
						if (!i.isDeleted()) {
							EmbedBuilder em = new EmbedBuilder();
							em.withAuthorIcon(DisCalAPI.getAPI().iconUrl);
							em.withAuthorName("DisCal");
							em.withTitle(MessageManager.getMessage("Embed.AddCalendar.List.Title", settings));
							em.appendField(MessageManager.getMessage("Embed.AddCalendar.List.Name", settings), i.getSummary(), false);
							em.appendField(MessageManager.getMessage("Embed.AddCalendar.List.TimeZone", settings), i.getTimeZone(), false);
							em.appendField(MessageManager.getMessage("Embed.AddCalendar.List.ID", settings), i.getId(), false);

							em.withUrl(CalendarMessageFormatter.getCalendarLink(settings.getGuildID()));
							em.withColor(56, 138, 237);
							MessageManager.sendDirectMessage(em.build(), poll.getUser());
						}
					}
					//Response will be handled in guild, and will check. We already saved the tokens anyway.
				} catch (IOException e1) {
					//Failed to get calendars list and check for calendars.
					Logger.getLogger().exception(poll.getUser(), "Failed to list calendars from external account!", e1, this.getClass(), true);

					MessageManager.sendDirectMessage(MessageManager.getMessage("AddCalendar.Auth.Poll.Failure.ListCalendars", settings), poll.getUser());
				}
			} else {
				//Unknown network error...
				MessageManager.sendDirectMessage(MessageManager.getMessage("Notification.Error.Network", settings), poll.getUser());
				Logger.getLogger().debug(poll.getUser(), "Network error; poll failure", "Status code: " + response.code() + " | " + response.message() + " | " + response.body().string(), this.getClass(), true);
			}
		} catch (Exception e) {
			//Handle exception.
			Logger.getLogger().exception(poll.getUser(), "Failed to poll for authorization to google account", e, this.getClass(), true);
			MessageManager.sendDirectMessage(MessageManager.getMessage("Notification.Error.Unknown", settings), poll.getUser());
		}
	}
}