package org.dreamexposure.discal.core.network.google;

import org.dreamexposure.discal.core.crypto.AESEncryption;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.network.google.ClientData;
import org.dreamexposure.discal.core.utils.CalendarUtils;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
        this.clientData = new ClientData(BotSettings.GOOGLE_CLIENT_ID.get(), BotSettings.GOOGLE_CLIENT_SECRET.get());
        this.client = new OkHttpClient();
    }

    //Getters
    public ClientData getClientData() {
        return this.clientData;
    }

    public OkHttpClient getClient() {
        return this.client;
    }


    //TODO: Rewrite this to be reactive
    public String requestNewAccessToken(GuildSettings settings, AESEncryption encryption) {
        try {
            RequestBody body = new FormBody.Builder()
                .addEncoded("client_id", this.clientData.getClientId())
                .addEncoded("client_secret", this.clientData.getClientSecret())
                .addEncoded("refresh_token", encryption.decrypt(settings.getEncryptedRefreshToken()))
                .addEncoded("grant_type", "refresh_token")
                .build();

            Request httpRequest = new okhttp3.Request.Builder()
                .url("https://www.googleapis.com/oauth2/v4/token")
                .post(body)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

            Response httpResponse = this.client.newCall(httpRequest).execute();

            if (httpResponse.code() == GlobalConst.STATUS_SUCCESS) {
                JSONObject autoRefreshResponse = new JSONObject(httpResponse.body().string());

                //Update Db data.
                settings.setEncryptedAccessToken(encryption.encrypt(autoRefreshResponse.getString("access_token")));
                DatabaseManager.updateSettings(settings).subscribe();

                //Okay, we can return the access token to be used when this method is called.
                return autoRefreshResponse.getString("access_token");
            } else if (httpResponse.code() == GlobalConst.STATUS_BAD_REQUEST) {
                JSONObject errorBody = new JSONObject(httpResponse.body().string());

                if ("invalid_grant".equalsIgnoreCase(errorBody.getString("error"))) {
                    // User revoked access to the calendar, delete our reference to it since they need to re-auth anyway

                    DatabaseManager.getCalendar(settings.getGuildID(), 1)
                        .flatMap(cd -> CalendarUtils.deleteCalendar(cd, settings))
                        .subscribe();
                } else {
                    LogFeed.log(LogObject.forDebug("Error requesting new access token.",
                        "Status code: " + httpResponse.code() + " | " + httpResponse.message() +
                            " | " + errorBody));
                }

                return null;
            } else {
                //Failed to get OK. Send debug info.
                LogFeed.log(LogObject.forDebug("Error requesting new access token.",
                    "Status code: " + httpResponse.code() + " | " + httpResponse.message() +
                        " | " + httpResponse.body().string()));
                return null;
            }

        } catch (Exception e) {
            //Error occurred, lets just log it and return null.
            LogFeed.log(LogObject
                .forException("Failed to request new access token.", e, this.getClass()));
            return null;
        }
    }
}