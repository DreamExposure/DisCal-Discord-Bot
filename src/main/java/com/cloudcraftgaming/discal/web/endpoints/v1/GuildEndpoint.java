package com.cloudcraftgaming.discal.web.endpoints.v1;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.bot.utils.PermissionChecker;
import com.cloudcraftgaming.discal.web.utils.ResponseUtils;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;

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
			Long guildId = jsonMain.getLong("GUILD_ID");

			GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);

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
			ExceptionHandler.sendException(null, "[WEB-API] Internal get guild settings error", e, GuildEndpoint.class);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String updateSettings(Request request, Response response) {
		try {
			JSONObject body = new JSONObject((request.body()));

			Long guildId = body.getLong("GUILD_ID");

			GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);

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
			ExceptionHandler.sendException(null, "[WEB-API] Internal update guild settings error", e, GuildEndpoint.class);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String getUserGuilds(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());

			long userId = jsonMain.getLong("USER_ID");
			IUser user = Main.client.getUserByID(userId);

			//Find all guilds user is in...
			ArrayList<IGuild> guilds = new ArrayList<>();
			for (IGuild g : Main.client.getGuilds()) {
				if (g.getUserByID(userId) != null) {
					guilds.add(g);
				}
			}

			//Get needed data
			ArrayList<JSONObject> guildData = new ArrayList<>();
			for (IGuild g : guilds) {
				JSONObject d = new JSONObject();
				d.put("GUILD_ID", g.getLongID());
				d.put("IS_OWNER", g.getOwnerLongID() == userId);
				d.put("MANAGER_SERVER", PermissionChecker.hasManageServerRole(g, user));
				d.put("DISCAL_CONTROL", PermissionChecker.hasSufficientRole(g, user));

				guildData.add(d);
			}

			JSONObject body = new JSONObject();
			body.put("USER_ID", userId);
			body.put("GUILD_COUNT", guildData.size());
			body.put("GUILDS", guildData);

			response.type("application/json");
			response.status(200);
			response.body(body.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			ExceptionHandler.sendException(null, "[WEB-API] Internal get guilds from users error", e, GuildEndpoint.class);
		}
		return response.body();
	}
}