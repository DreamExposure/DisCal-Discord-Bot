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
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarListEntry;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.io.IOException;
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
        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            HttpPost request = new HttpPost("https://accounts.google.com/o/oauth2/device/code");
            CodeRequest cr = new CodeRequest();
            cr.client_id = clientData.getClientId();
            cr.scope = CalendarScopes.CALENDAR;
            String json = Main.gson.toJson(cr);
            request.setEntity(new StringEntity(json, ContentType.create("application/x-www-form-urlencoded")));

            HttpResponse httpResponse = httpClient.execute(request);

            CodeResponse response = Main.gson.fromJson(httpResponse.getEntity().toString(), CodeResponse.class);


            //Send DM to user with code.
            EmbedBuilder em = new EmbedBuilder();
            em.withAuthorIcon(Main.client.getGuildByID(266063520112574464L).getIconURL());
            em.withAuthorName("DisCal");
            em.withTitle("User Auth");
            em.appendField("Code", response.user_code, true);
            em.withFooterText("Please visit the URL and enter the code!");

            em.withUrl(response.verification_url);
            em.withColor(36, 153, 153);

            IUser user = event.getAuthor();
            Message.sendDirectMessage("Please authorize DisCal access to your Google Calendar so that it can use your external calendar!", em.build(), user);

            //Start timer to poll Google Cal for auth
            Poll poll = new Poll(user, event.getGuild());

            poll.setDevice_code(response.device_code);
            poll.setRemainingSeconds(response.expires_in);
            poll.setExpires_in(response.expires_in);
            poll.setInterval(response.interval);
            pollForAuth(poll);

        } catch (Exception e) {
            //Failed, report issue to dev.
            ExceptionHandler.sendException(event.getAuthor(), "Failed to request Google Access Code", e, this.getClass());
            IUser u = event.getAuthor();
            Message.sendDirectMessage("Uh oh... something failed. I have emailed the developer! Please try again!", u);
        }
    }

    public String requestNewAccessToken(GuildSettings settings, AESEncryption encryption) {
        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            HttpPost request = new HttpPost("https://www.googleapis.com/oauth2/v4/token");

            AuthRefreshRequest arr = new AuthRefreshRequest();
            arr.client_id = clientData.getClientId();
            arr.client_secret = clientData.getClientSecret();
            arr.refresh_token = encryption.decrypt(settings.getEncryptedRefreshToken());

            String json = Main.gson.toJson(arr);
            request.setEntity(new StringEntity(json, ContentType.create("application/x-www-form-urlencoded")));

            HttpResponse httpResponse = httpClient.execute(request);

            AuthRefreshResponse response = Main.gson.fromJson(httpResponse.getEntity().toString(), AuthRefreshResponse.class);

            //Update Db data.
            settings.setEncryptedAccessToken(encryption.encrypt(response.access_token));
            DatabaseManager.getManager().updateSettings(settings);

            //Okay, we can return the access token to be used when this method is called.
            return response.access_token;

        } catch (Exception e) {
            //Error occurred, lets just log it and return null.
            ExceptionHandler.sendException(null, "Failed to request new access token.", e, this.getClass());
            return null;
        }
    }

    public void pollForAuth(Poll poll) {
        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            AuthPollRequest apr = new AuthPollRequest();
            apr.client_id = clientData.getClientId();
            apr.client_secret = clientData.getClientSecret();
            apr.code = poll.getDevice_code();
            apr.grant_type = "http://oauth.net/grant_type/device/1.0";

            HttpPost request = new HttpPost("https://www.googleapis.com/oauth2/v4/token");

            String json = Main.gson.toJson(apr);
            request.setEntity(new StringEntity(json, ContentType.create("application/x-www-form-urlencoded")));

            //Execute
            HttpResponse httpResponse = httpClient.execute(request);

            //Handle response.
            if (httpResponse.getStatusLine().getStatusCode() == 403) {
                //Handle access denied
                Message.sendDirectMessage("You have denied DisCal use of your calendars! If this was a mistake just restart the process!", poll.getUser());
            } else if (httpResponse.getStatusLine().getStatusCode() == 400) {
                try {
                    //See if auth is pending, if so, just reschedule.
                    AuthPollResponseError apre = Main.gson.fromJson(httpResponse.getEntity().toString(), AuthPollResponseError.class);
                    if (apre.error.equalsIgnoreCase("authorization_pending")) {
                        //Response pending
                        PollManager.getManager().scheduleNextPoll(poll);
                    } else {
                        Message.sendDirectMessage("There was a network error.. Please try again!", poll.getUser());
                    }
                } catch (Exception e) {
                    //Auth is not pending, error occurred.
                    ExceptionHandler.sendException(poll.getUser(), "Failed to poll for authorization to google account.", e, this.getClass());
                    Message.sendDirectMessage("Uh oh... something failed. I have emailed the developer! Please try again!", poll.getUser());
                }
            } else if (httpResponse.getStatusLine().getStatusCode() == 429) {
                //We got rate limited... oops. Let's just poll half as often.
                poll.setInterval(poll.getInterval() * 2);
            } else {
                //Access granted
                AuthPollResponseGrant aprg = Main.gson.fromJson(httpResponse.getEntity().toString(), AuthPollResponseGrant.class);

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