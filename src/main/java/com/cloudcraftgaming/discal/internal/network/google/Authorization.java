package com.cloudcraftgaming.discal.internal.network.google;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.internal.email.EmailSender;
import com.cloudcraftgaming.discal.internal.network.google.json.AuthPollRequest;
import com.cloudcraftgaming.discal.internal.network.google.json.CodeRequest;
import com.cloudcraftgaming.discal.internal.network.google.json.CodeResponse;
import com.cloudcraftgaming.discal.utils.Message;
import com.google.api.services.calendar.CalendarScopes;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Created by Nova Fox on 3/23/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class Authorization {
    public static void requestCode(MessageReceivedEvent event) {
        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            HttpPost request = new HttpPost("https://accounts.google.com/o/oauth2/device/code");
            CodeRequest cr = new CodeRequest();
            //TODO: Set client ID here
            cr.scope = CalendarScopes.CALENDAR;
            String json = new Gson().toJson(cr);
            request.setEntity(new StringEntity(json, ContentType.create("application/x-www-form-urlencoded")));

            HttpResponse httpResponse = httpClient.execute(request);

            CodeResponse response = new Gson().fromJson(httpResponse.getEntity().toString(), CodeResponse.class);


            //Send DM to user with code.
            EmbedBuilder em = new EmbedBuilder();
            em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
            em.withAuthorName("DisCal");
            em.withTitle("User Auth");
            em.appendField("Code", response.user_code, true);
            em.withFooterText("Please visit the URL and enter the code!");

            em.withUrl(response.verification_url);
            em.withColor(36, 153, 153);

            IUser user = event.getMessage().getAuthor();
            Message.sendDirectMessage("Please authorize DisCal access to your Google Calendar so that it can use your external calendar!", em.build(), user);

            //Start timer to poll Google Cal for auth

        } catch (Exception e) {
            //Failed, report issue to dev.
            EmailSender.getSender().sendExceptionEmail(e, Authorization.class);
            IUser u = event.getMessage().getAuthor();
            Message.sendDirectMessage("Uh oh... something failed. I have emailed the developer! Please try again!", u);
        }
    }

    public static void pollForAuth(CodeResponse cr) {
        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            AuthPollRequest apr = new AuthPollRequest();
            //TODO: Set client ID and client secret.
            apr.code = cr.device_code;
            apr.grant_type = "http://oauth.net/grant_type/device/1.0";

            HttpPost request = new HttpPost("https://www.googleapis.com/oauth2/v4/token");

            String json = new Gson().toJson(apr);
            request.setEntity(new StringEntity(json, ContentType.create("application/x-www-form-urlencoded")));

            //Execute
            HttpResponse httpResponse = httpClient.execute(request);

            //Handle response.
            CodeResponse response = new Gson().fromJson(httpResponse.getEntity().toString(), CodeResponse.class);



        } catch (Exception e) {
            //TODO: Handle exception.
        }
    }
}