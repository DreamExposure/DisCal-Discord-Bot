package org.dreamexposure.discal.core.network.google;

import com.google.api.client.http.HttpStatusCodes;
import okhttp3.*;
import org.dreamexposure.discal.core.crypto.AESEncryption;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.network.google.ClientData;
import org.json.JSONObject;

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

	//Getters
	public ClientData getClientData() {
		return clientData;
	}

	public OkHttpClient getClient() {
		return client;
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
				JSONObject autoRefreshResponse = new JSONObject(httpResponse.body().string());

				//Update Db data.
				settings.setEncryptedAccessToken(encryption.encrypt(autoRefreshResponse.getString("access_token")));
				DatabaseManager.getManager().updateSettings(settings);

				//Okay, we can return the access token to be used when this method is called.
				return autoRefreshResponse.getString("access_token");
			} else {
				//Failed to get OK. Send debug info.
				Logger.getLogger().debug(null, "Error requesting new access token.", "Status code: " + httpResponse.code() + " | " + httpResponse.message() + " | " + httpResponse.body().string(), true, this.getClass());
				return null;
			}

		} catch (Exception e) {
			//Error occurred, lets just log it and return null.
			Logger.getLogger().exception(null, "Failed to request new access token.", e, true, this.getClass());
			return null;
		}
	}
}