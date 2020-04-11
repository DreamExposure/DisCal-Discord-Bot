package org.dreamexposure.discal.server.api.endpoints.v2.rsvp;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.event.RsvpData;
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
@RequestMapping("/v2/rsvp")
public class GetRsvpEndpoint {
	@PostMapping(value = "/get", produces = "application/json")
	public String getRsvp(HttpServletRequest request, HttpServletResponse response, @RequestBody String rBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		}

		//Okay, now handle actual request.
		try {
			JSONObject requestBody = new JSONObject(rBody);

			String guildId = requestBody.getString("guild_id");
			String eventId = requestBody.getString("event_id");

			RsvpData rsvp = DatabaseManager.getRsvpData(Snowflake.of(guildId), eventId).block();

			response.setContentType("application/json");
			response.setStatus(200);

			return rsvp.toJson().toString();
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);

			return JsonUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			LogFeed.log(LogObject
					.forException("[API-v2]", "Failed to get RSVP", e, this.getClass()));

			response.setContentType("application/json");
			response.setStatus(500);

			return JsonUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}