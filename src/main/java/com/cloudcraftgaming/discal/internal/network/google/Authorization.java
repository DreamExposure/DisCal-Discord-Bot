package com.cloudcraftgaming.discal.internal.network.google;

import com.cloudcraftgaming.discal.internal.email.EmailSender;
import com.cloudcraftgaming.discal.internal.network.google.json.CodeRequest;
import com.cloudcraftgaming.discal.internal.network.google.json.CodeResponse;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

/**
 * Created by Nova Fox on 3/23/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class Authorization {
    public static String requestCode(MessageReceivedEvent event) {
        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            HttpPost request = new HttpPost("https://accounts.google.com/o/oauth2/device/code");
            CodeRequest cr = new CodeRequest();
            //TODO: Set client ID here
            cr.scope = "calendar";
            String json = new Gson().toJson(cr);
            request.setEntity(new StringEntity(json, ContentType.create("application/x-www-form-urlencoded")));

            HttpResponse httpResponse = httpClient.execute(request);

            CodeResponse response = new Gson().fromJson(httpResponse.getEntity().toString(), CodeResponse.class);

            //TODO: Send DM to user with code if applicable.

        } catch (Exception e) {
            //Failed, report issue to dev.
            EmailSender.getSender().sendExceptionEmail(e, Authorization.class);
        }
        return "Uh oh... something failed. I have emailed the developer! Please try again!";
    }
}