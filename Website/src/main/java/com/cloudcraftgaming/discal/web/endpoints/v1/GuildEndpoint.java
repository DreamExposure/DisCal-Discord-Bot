package com.cloudcraftgaming.discal.web.endpoints.v1;

import com.cloudcraftgaming.discal.api.DisCalAPI;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.utils.PermissionChecker;
import com.cloudcraftgaming.discal.logger.Logger;
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
			Long guildId = jsonMain.getLong("guild_id");

			GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);

			response.type("application/json");
			response.status(200);

			JSONObject body = new JSONObject();
			body.put("external_calendar", settings.useExternalCalendar());
			body.put("control_role", settings.getControlRole());
			body.put("discal_channel", settings.getDiscalChannel());
			body.put("simple_announcement", settings.usingSimpleAnnouncements());
			body.put("lang", settings.getLang());
			body.put("prefix", settings.getPrefix());
			body.put("patron_guild", settings.isPatronGuild());
			body.put("dev_guild", settings.isDevGuild());
			body.put("max_calendars", settings.getMaxCalendars());

			response.body(body.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal get guild settings error", e, GuildEndpoint.class, true);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String updateSettings(Request request, Response response) {
		try {
			JSONObject body = new JSONObject((request.body()));

			Long guildId = body.getLong("guild_id");

			GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);

			if (body.has("control_role"))
				settings.setControlRole(body.getString("control_role"));
			if (body.has("discal_channel"))
				settings.setDiscalChannel(body.getString("discal_channel"));
			if (body.has("simple_announcement"))
				settings.setSimpleAnnouncements(body.getBoolean("simple_announcement"));
			if (body.has("lang"))
				settings.setLang(body.getString("lang"));
			if (body.has("prefix"))
				settings.setPrefix(body.getString("prefix"));

			if (DatabaseManager.getManager().updateSettings(settings)) {
				response.type("application/json");
				response.status(200);
				response.body(ResponseUtils.getJsonResponseMessage("Successfully updated guild settings!"));
			} else {
				response.type("application/json");
				response.status(500);
				response.body(ResponseUtils.getJsonResponseMessage("Failed to update settings!"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal update guild settings error", e, GuildEndpoint.class, true);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String getUserGuilds(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());

			long userId = jsonMain.getLong("USER_ID");
			IUser user = DisCalAPI.getAPI().getClient().getUserByID(userId);

			//Find all guilds user is in...
			ArrayList<IGuild> guilds = new ArrayList<>();
			for (IGuild g : DisCalAPI.getAPI().getClient().getGuilds()) {
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
				d.put("MANAGE_SERVER", PermissionChecker.hasManageServerRole(g, user));
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
			Logger.getLogger().exception(null, "[WEB-API] Internal get guilds from users error", e, GuildEndpoint.class, true);
		}
		return response.body();
	}
}