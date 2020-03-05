package org.dreamexposure.discal.server.api.endpoints.v2.announcement;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.server.utils.Authentication;
import org.json.JSONArray;
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
@RequestMapping("/v2/announcement")
public class ListAnnouncementEndpoint {
	@PostMapping(value = "/list", produces = "application/json")
	public String listAnnouncements(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
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
			Snowflake guildId = Snowflake.of(body.getLong("guild_id"));
			int amount = body.getInt("amount");

			JSONArray jAnnouncements = new JSONArray();
			if (amount < 1) {
				for (Announcement a : DatabaseManager.getManager().getAnnouncements(guildId))
					jAnnouncements.put(a.toJson());
			} else {
				int i = 0;
				for (Announcement a : DatabaseManager.getManager().getAnnouncements(guildId)) {
					if (i < amount) {
						jAnnouncements.put(a.toJson());
						i++;
					} else
						break;
				}
			}

			JSONObject responseBody = new JSONObject();
			responseBody.put("message", "Listed announcements successfully");
			responseBody.put("announcements", jAnnouncements);

			response.setContentType("application/json");
			response.setStatus(200);
			return responseBody.toString();
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return JsonUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[API-v2] Internal list announcements error", e, true, this.getClass());

			response.setContentType("application/json");
			response.setStatus(500);
			return JsonUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}
