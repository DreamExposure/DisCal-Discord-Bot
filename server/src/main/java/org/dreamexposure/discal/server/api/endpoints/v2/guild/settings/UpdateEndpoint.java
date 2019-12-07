package org.dreamexposure.discal.server.api.endpoints.v2.guild.settings;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.server.utils.Authentication;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.core.object.util.Snowflake;

@RestController
@RequestMapping("/v2/guild/settings")
public class UpdateEndpoint {
	@PostMapping(value = "/update", produces = "application/json")
	public String updateSettings(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
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
			long guildId = body.getLong("guild_id");

			GuildSettings settings = DatabaseManager.getManager().getSettings(Snowflake.of(guildId));

			//Handle various things that are allowed to change.
			if (body.has("control_role"))
				settings.setControlRole(body.getString("control_role"));
			if (body.has("discal_channel"))
				settings.setDiscalChannel(body.getString("discal_channel"));
			if (body.has("simple_announcements"))
				settings.setSimpleAnnouncements(body.getBoolean("simple_announcements"));
			if (body.has("lang"))
				settings.setLang(body.getString("lang"));
			if (body.has("prefix"))
				settings.setPrefix(body.getString("prefix"));

			//Allow Official DisCal Shards to change some other things...
			if (authState.isFromDiscalNetwork()) {
				if (body.has("external_calendar"))
					settings.setUseExternalCalendar(body.getBoolean("external_calendar"));
				if (body.has("patron_guild"))
					settings.setPatronGuild(body.getBoolean("patron_guild"));
				if (body.has("dev_guild"))
					settings.setDevGuild(body.getBoolean("dev_guild"));
				if (body.has("branded"))
					settings.setBranded(body.getBoolean("branded"));
			}

			if (DatabaseManager.getManager().updateSettings(settings)) {
				response.setContentType("application/json");
				response.setStatus(200);
				return JsonUtils.getJsonResponseMessage("Successfully updated guild settings!");
			} else {
				response.setContentType("application/json");
				response.setStatus(500);
				return JsonUtils.getJsonResponseMessage("Internal Server Error");
			}
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return JsonUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[API-v2] Internal update guild settings error", e, true, this.getClass());

			response.setContentType("application/json");
			response.setStatus(500);
			return JsonUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}
