package org.dreamexposure.discal.server.api.endpoints.v2.rsvp;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.event.RsvpData;
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
@RequestMapping("/v2/rsvp")
public class UpdateRsvpEndpoint {
	@PostMapping(value = "/update", produces = "application/json")
	public String updateRsvp(HttpServletRequest request, HttpServletResponse response, @RequestBody String rBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		} else if (authState.isReadOnly()) {
			response.setStatus(401);
			response.setContentType("application/json");
			return JsonUtils.getJsonResponseMessage("Read-Only key not Allowed");
		}

		//Okay, now handle actual request.
		try {
			JSONObject requestBody = new JSONObject(rBody);

			String guildId = requestBody.getString("guild_id");
			String eventId = requestBody.getString("event_id");

			RsvpData rsvp = DatabaseManager.getRsvpData(Snowflake.of(guildId), eventId).block();

			//Handle additions...
			if (requestBody.has("to_add")) {
				JSONObject jToAdd = requestBody.getJSONObject("to_add");
				if (jToAdd.has("on_time")) {
					JSONArray ar = jToAdd.getJSONArray("on_time");
					for (int i = 0; i < jToAdd.length(); i++)
						rsvp.getGoingOnTime().add(ar.getString(i));
				}
				if (jToAdd.has("late")) {
					JSONArray ar = jToAdd.getJSONArray("late");
					for (int i = 0; i < jToAdd.length(); i++)
						rsvp.getGoingLate().add(ar.getString(i));
				}
				if (jToAdd.has("not_going")) {
					JSONArray ar = jToAdd.getJSONArray("not_going");
					for (int i = 0; i < jToAdd.length(); i++)
						rsvp.getNotGoing().add(ar.getString(i));
				}
				if (jToAdd.has("undecided")) {
					JSONArray ar = jToAdd.getJSONArray("undecided");
					for (int i = 0; i < jToAdd.length(); i++)
						rsvp.getUndecided().add(ar.getString(i));
				}
			}

			//handle removals...
			if (requestBody.has("to_remove")) {
				JSONObject jToRemove = requestBody.getJSONObject("to_remove");
				if (jToRemove.has("on_time")) {
					JSONArray ar = jToRemove.getJSONArray("on_time");
					for (int i = 0; i < jToRemove.length(); i++)
						rsvp.getGoingOnTime().remove(ar.getString(i));
				}
				if (jToRemove.has("late")) {
					JSONArray ar = jToRemove.getJSONArray("late");
					for (int i = 0; i < jToRemove.length(); i++)
						rsvp.getGoingLate().remove(ar.getString(i));
				}
				if (jToRemove.has("not_going")) {
					JSONArray ar = jToRemove.getJSONArray("not_going");
					for (int i = 0; i < jToRemove.length(); i++)
						rsvp.getNotGoing().remove(ar.getString(i));
				}
				if (jToRemove.has("undecided")) {
					JSONArray ar = jToRemove.getJSONArray("undecided");
					for (int i = 0; i < jToRemove.length(); i++)
						rsvp.getUndecided().remove(ar.getString(i));
				}
			}

			if (DatabaseManager.updateRsvpData(rsvp).block()) {
				response.setContentType("application/json");
				response.setStatus(200);

				return JsonUtils.getJsonResponseMessage("RSVP successfully updated");
			}

			//Shouldn't get here, but if we did, the update probably failed...
			response.setContentType("application/json");
			response.setStatus(500);

			return JsonUtils.getJsonResponseMessage("Internal Server Error");
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