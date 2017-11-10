package com.cloudcraftgaming.discal.web.endpoints.v1;

import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.web.utils.ResponseUtils;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import static spark.Spark.halt;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("ThrowableNotThrown")
public class GuildEndpoint {
	public static String getSettings(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			String guildId = jsonMain.getString("GUILD_ID");

			GuildSettings settings = DatabaseManager.getManager().getSettings(Long.valueOf(guildId));

			response.type("application/json");
			response.status(200);

			JSONObject body = new JSONObject();
			body.put("GUILD_ID", settings.getGuildID());
			body.put("EXTERNAL_CALENDAR", settings.useExternalCalendar());
			body.put("CONTROL_ROLE", settings.getControlRole());
			body.put("DISCAL_CHANNEL", settings.getDiscalChannel());
			body.put("SIMPLE_ANNOUNCEMENT", settings.usingSimpleAnnouncements());
			body.put("LANG", settings.getLang());
			body.put("PREFIX", settings.getPrefix());
			body.put("PATRON_GUILD", settings.isPatronGuild());
			body.put("DEV_GUILD", settings.isDevGuild());
			body.put("MAX_CALENDARS", settings.getMaxCalendars());

			response.body(body.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			e.printStackTrace();
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String updateSettings(Request request, Response response) {
		try {
			JSONObject body = new JSONObject((request.body()));

			String guildId = body.getString("GUILD_ID");

			GuildSettings settings = DatabaseManager.getManager().getSettings(Long.valueOf(guildId));

			if (body.has("EXTERNAL_CALENDAR"))
				settings.setUseExternalCalendar(body.getBoolean("EXTERNAL_CALENDAR"));
			if (body.has("CONTROL_ROLE"))
				settings.setControlRole(body.getString("CONTROL_ROLE"));
			if (body.has("DISCAL_CHANNEL"))
				settings.setDiscalChannel(body.getString("DISCAL_CHANNEL"));
			if (body.has("SIMPLE_ANNOUNCEMENT"))
				settings.setSimpleAnnouncements(body.getBoolean("SIMPLE_ANNOUNCEMENT"));
			if (body.has("LANG"))
				settings.setLang(body.getString("LANG"));
			if (body.has("PREFIX"))
				settings.setPrefix(body.getString("PREFIX"));
			if (body.has("PATRON_GUILD"))
				settings.setPatronGuild(body.getBoolean("PATRON_GUILD"));
			if (body.has("DEV_GUILD"))
				settings.setDevGuild(body.getBoolean("DEV_GUILD"));
			if (body.has("MAX_CALENDARS"))
				settings.setMaxCalendars(body.getInt("MAX_CALENDARS"));

			if (DatabaseManager.getManager().updateSettings(settings)) {
				response.type("application/json");
				response.status(200);
				response.body(ResponseUtils.getJsonResponseMessage("Successfully updated settings!"));
			} else {
				response.type("application/json");
				response.status(500);
				response.body(ResponseUtils.getJsonResponseMessage("Failed to update settings!"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			e.printStackTrace();
			halt(500, "Internal Server Error");
		}
		return response.body();
	}
}