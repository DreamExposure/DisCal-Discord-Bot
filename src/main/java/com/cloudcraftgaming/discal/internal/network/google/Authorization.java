package com.cloudcraftgaming.discal.internal.network.google;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.calendar.calendar.CalendarMessageFormatter;
import com.cloudcraftgaming.discal.internal.crypto.AESEncryption;
import com.cloudcraftgaming.discal.internal.data.BotSettings;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.internal.network.google.json.*;
import com.cloudcraftgaming.discal.internal.network.google.utils.Poll;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.utils.Message;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Nova Fox on 3/23/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("WeakerAccess")
public class Authorization {
    private static Authorization instance;
    private ClientData clientData;

    private Authorization() {} //Prevent initialization.

    public static Authorization getAuth() {
        if (instance == null) {
            instance = new Authorization();
        }
        return instance;
    }

    public void init(BotSettings settings) {
        clientData = new ClientData(settings.getGoogleClientId(), settings.getGoogleClientSecret());
    }

    public void requestCode(MessageReceivedEvent event) {
        try {
			String body = "client_id=" + clientData.getClientId() + "&scope=" + CalendarScopes.CALENDAR;

			com.mashape.unirest.http.HttpResponse<JsonNode> response = Unirest.post("https://accounts.google.com/o/oauth2/device/code").header("Content-Type", "application/x-www-form-urlencoded").body(body).asJson();

			if (response.getStatus() == HttpStatusCodes.STATUS_CODE_OK) {
				Type type = new TypeToken<CodeResponse>(){}.getType();
				CodeResponse cr = new Gson().fromJson(response.getBody().toString(), type);

				//Send DM to user with code.
				EmbedBuilder em = new EmbedBuilder();
				em.withAuthorIcon(Main.client.getGuildByID(266063520112574464L).getIconURL());
				em.withAuthorName("DisCal");
				em.withTitle("User Auth");
				em.appendField("Code", cr.user_code, true);
				em.withFooterText("Please visit the URL and enter the code!");

				em.withUrl(cr.verification_url);
				em.withColor(36, 153, 153);

				IUser user = event.getAuthor();
				Message.sendDirectMessage("Please authorize DisCal access to your Google Calendar so that it can use your external calendar!", em.build(), user);

				//Start timer to poll Google Cal for auth
				Poll poll = new Poll(user, event.getGuild());

				poll.setDevice_code(cr.device_code);
				poll.setRemainingSeconds(cr.expires_in);
				poll.setExpires_in(cr.expires_in);
				poll.setInterval(cr.interval);
				pollForAuth(poll);
			} else {
				Message.sendDirectMessage("Error requesting access code! The development team has been alerted to the issue! Try again later!", event.getAuthor());

				ExceptionHandler.sendDebug(event.getAuthor(), "Error requesting access token.", "Status code: " + response.getStatus() + " | " + response.getStatusText() + " | " + response.getBody().toString(), this.getClass());
			}
        } catch (Exception e) {
            //Failed, report issue to dev.
            ExceptionHandler.sendException(event.getAuthor(), "Failed to request Google Access Code", e, this.getClass());
            IUser u = event.getAuthor();
            Message.sendDirectMessage("Uh oh... something failed. I have alerted the development team! Please try again!", u);
        }
    }

    public String requestNewAccessToken(GuildSettings settings, AESEncryption encryption) {
        try {
			String body = "client_id=" + clientData.getClientId() + "&client_secret=" + clientData.getClientSecret() + "&refresh_token=" + encryption.decrypt(settings.getEncryptedRefreshToken()) + "&grant_type=refresh_token";

			com.mashape.unirest.http.HttpResponse<JsonNode> httpResponse = Unirest.post("https://www.googleapis.com/oauth2/v4/token").header("Content-Type", "application/x-www-form-urlencoded").body(body).asJson();

			if (httpResponse.getStatus() == HttpStatusCodes.STATUS_CODE_OK) {

				Type type = new TypeToken<AuthRefreshResponse>(){}.getType();
				AuthRefreshResponse response = new Gson().fromJson(httpResponse.getBody().toString(), type);

				//Update Db data.
				settings.setEncryptedAccessToken(encryption.encrypt(response.access_token));
				DatabaseManager.getManager().updateSettings(settings);

				//Okay, we can return the access token to be used when this method is called.
				return response.access_token;
			} else {
				//Failed to get OK. Send debug info.
				ExceptionHandler.sendDebug(null, "Error requesting new access token.", "Status code: " + httpResponse.getStatus() + " | " + httpResponse.getStatusText() + " | " + httpResponse.getBody().toString(), this.getClass());
				return null;
			}

        } catch (Exception e) {
            //Error occurred, lets just log it and return null.
            ExceptionHandler.sendException(null, "Failed to request new access token.", e, this.getClass());
            return null;
        }
    }

    public void pollForAuth(Poll poll) {
        try {
            String body = "client_id=" + clientData.getClientId() + "&client_secret=" + clientData.getClientSecret() + "&code=" + poll.getDevice_code() + "&grant_type=http://oauth.net/grant_type/device/1.0";

            //Execute
			com.mashape.unirest.http.HttpResponse<JsonNode> response = Unirest.post("https://www.googleapis.com/oauth2/v4/token").header("Content-Type", "application/x-www-form-urlencoded").body(body).asJson();

            //Handle response.
            if (response.getStatus() == 403) {
                //Handle access denied
                Message.sendDirectMessage("You have denied DisCal use of your calendars! If this was a mistake just restart the process!", poll.getUser());
            } else if (response.getStatus() == 400) {
                try {
                    //See if auth is pending, if so, just reschedule.
					Type type = new TypeToken<AuthPollResponseError>(){}.getType();
					AuthPollResponseError apre = new Gson().fromJson(response.getBody().toString(), type);

                    if (apre.error.equalsIgnoreCase("authorization_pending")) {
                        //Response pending
                        PollManager.getManager().scheduleNextPoll(poll);
                    } else {
                        Message.sendDirectMessage("There was a network error.. Please try again!", poll.getUser());
						ExceptionHandler.sendDebug(poll.getUser(), "Poll Failure!", "Status code: " + response.getStatus() + " | " + response.getStatusText() + " | " + response.getBody().toString(), this.getClass());
                    }
                } catch (Exception e) {
                    //Auth is not pending, error occurred.
                    ExceptionHandler.sendException(poll.getUser(), "Failed to poll for authorization to google account.", e, this.getClass());
					ExceptionHandler.sendDebug(poll.getUser(), "More info on failure", "Status code: " + response.getStatus() + " | " + response.getStatusText() + " | " + response.getBody().toString(), this.getClass());
                    Message.sendDirectMessage("Uh oh... something failed. I have emailed the developer! Please try again!", poll.getUser());
                }
            } else if (response.getStatus() == 429) {
                //We got rate limited... oops. Let's just poll half as often.
                poll.setInterval(poll.getInterval() * 2);
            } else if (response.getStatus() == HttpStatusCodes.STATUS_CODE_OK) {
                //Access granted
				Type type = new TypeToken<AuthPollResponseGrant>(){}.getType();
				AuthPollResponseGrant aprg = new Gson().fromJson(response.getBody().toString(), type);

                //Save credentials securely.
                GuildSettings gs = DatabaseManager.getManager().getSettings(poll.getGuild().getLongID());
                AESEncryption encryption = new AESEncryption(gs);
                gs.setEncryptedAccessToken(encryption.encrypt(aprg.access_token));
                gs.setEncryptedRefreshToken(encryption.encrypt(aprg.refresh_token));
                DatabaseManager.getManager().updateSettings(gs);

	            try {
		            Calendar service = CalendarAuth.getCalendarService(gs);
		            List<CalendarListEntry> items = service.calendarList().list().setMinAccessRole("writer").execute().getItems();
		            Message.sendDirectMessage("Calendars found! Please send the message of the ID of the calendar you wish to have DisCal use in your guild with `!addCalendar <calendar ID>`! To make this easier for you, here is a list of the calendars you can select (This may take while to list if you have a lot of calendars):", poll.getUser());
		            for (CalendarListEntry i : items) {
		            	if (!i.isDeleted()) {
							EmbedBuilder em = new EmbedBuilder();
							em.withAuthorIcon(Main.client.getGuildByID(266063520112574464L).getIconURL());
							em.withAuthorName("DisCal");
							em.withTitle("Calendar Selection");
							em.appendField("Calendar Name", i.getSummary(), false);
							em.appendField("TimeZone", i.getTimeZone(), false);
							em.appendField("Calendar ID", i.getId(), false);

							em.withUrl(CalendarMessageFormatter.getCalendarLink(i.getId()));
							em.withColor(56, 138, 237);
							Message.sendDirectMessage(em.build(), poll.getUser());
						}
					}
					//Response will be handled in guild, and will check. We already saved the tokens anyway.
	            } catch (IOException e1) {
	            	//Failed to get calendars list and check for calendars.
		            ExceptionHandler.sendException(poll.getUser(), "Failed to list calendars from external account!", e1, this.getClass());

		            Message.sendDirectMessage("I have failed to list your calendars! Please specify the ID of the calendar that you want DisCal to use!", poll.getUser());
	            }
            }
        } catch (Exception e) {
            //Handle exception.
            ExceptionHandler.sendException(poll.getUser(), "Failed to poll for authorization to google account", e, this.getClass());
            Message.sendDirectMessage("Uh oh... An error has occurred! DisCal is sorry. I has emailed the developer for you! Please try again, I will try I my hardest!", poll.getUser());
        }
    }
}