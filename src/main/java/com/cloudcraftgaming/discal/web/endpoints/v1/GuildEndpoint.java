package com.cloudcraftgaming.discal.web.endpoints.v1;

import com.cloudcraftgaming.discal.api.DisCalAPI;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.web.AuthenticationState;
import com.cloudcraftgaming.discal.api.utils.PermissionChecker;
import com.cloudcraftgaming.discal.logger.Logger;
import com.cloudcraftgaming.discal.web.utils.Authentication;
import com.cloudcraftgaming.discal.web.utils.ResponseUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("ThrowableNotThrown")
@RestController
@RequestMapping("/api/v1/guild")
public class GuildEndpoint {

	@PostMapping(value = "/settings/get", produces = "application/json")
	public static String getSettings(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		}

		//Okay, now handle actual request.
		try {
			JSONObject jsonMain = new JSONObject(requestBody);
			Long guildId = jsonMain.getLong("guild_id");

			GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);

			response.setContentType("application/json");
			response.setStatus(200);

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

			return body.toString();
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return ResponseUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal get guild settings error", e, GuildEndpoint.class, true);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/settings/update", produces = "application/json")
	public static String updateSettings(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		}

		//Okay, now handle actual request.
		try {
			JSONObject body = new JSONObject(requestBody);

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
				response.setContentType("application/json");
				response.setStatus(200);
				return ResponseUtils.getJsonResponseMessage("Successfully updated guild settings!");
			} else {
				response.setContentType("application/json");
				response.setStatus(500);
				return ResponseUtils.getJsonResponseMessage("Internal Server Error");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			response.setContentType("application/json");
			response.setStatus(400);
			return ResponseUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal update guild settings error", e, GuildEndpoint.class, true);
			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/info/from-user/list", produces = "application/json")
	public static String getUserGuilds(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		}

		//Okay, now handle actual request.
		try {
			JSONObject jsonMain = new JSONObject(requestBody);

			long userId = jsonMain.getLong("USER_ID");
			IUser user = DisCalAPI.getAPI().getClient().getUserByID(userId);

			//Find all guilds user is in...
			ArrayList<IGuild> guilds = new ArrayList<>();
			for (IGuild g : DisCalAPI.getAPI().getClient().getGuilds()) {
				if (g.getUserByID(userId) != null)
					guilds.add(g);
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

			response.setContentType("application/json");
			response.setStatus(200);
			return body.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			response.setContentType("application/json");
			response.setStatus(400);
			return ResponseUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal get guilds from users error", e, GuildEndpoint.class, true);
			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}