package org.dreamexposure.discal.server.api.endpoints.v1;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.event.RsvpData;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.server.utils.Authentication;
import org.dreamexposure.discal.server.utils.ResponseUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"ThrowableNotThrown", "Duplicates"})
@RestController
@RequestMapping("/api/v1/rsvp")
public class RsvpEndpoint {

	@PostMapping(value = "/get", produces = "application/json")
	public static String getRsvp(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
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
			long guildId = jsonMain.getLong("guild_id");
			String eventId = jsonMain.getString("id");

			RsvpData rsvp = DatabaseManager.getManager().getRsvpData(guildId, eventId);

			JSONObject body = new JSONObject();
			body.put("on_time", rsvp.getGoingOnTime());
			body.put("late", rsvp.getGoingLate());
			body.put("undecided", rsvp.getUndecided());
			body.put("not_going", rsvp.getNotGoing());

			response.setContentType("application/json");
			response.setStatus(200);
			return body.toString();
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return ResponseUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal get RSVP data error", e, RsvpEndpoint.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/update", produces = "application/json")
	public static String updateRsvp(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
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
			long guildId = jsonMain.getLong("guild_id");
			String eventId = jsonMain.getString("id");

			RsvpData rsvp = DatabaseManager.getManager().getRsvpData(guildId, eventId);

			if (jsonMain.has("on_time")) {
				rsvp.getGoingOnTime().clear();
				for (int i = 0; i < jsonMain.getJSONArray("on_time").length(); i++) {
					String s = jsonMain.getJSONArray("on_time").getString(i);
					rsvp.getGoingOnTime().add(s);
				}
			}

			if (jsonMain.has("late")) {
				rsvp.getGoingOnTime().clear();
				for (int i = 0; i < jsonMain.getJSONArray("late").length(); i++) {
					String s = jsonMain.getJSONArray("late").getString(i);
					rsvp.getGoingLate().add(s);
				}
			}

			if (jsonMain.has("undecided")) {
				rsvp.getGoingOnTime().clear();
				for (int i = 0; i < jsonMain.getJSONArray("undecided").length(); i++) {
					String s = jsonMain.getJSONArray("undecided").getString(i);
					rsvp.getUndecided().add(s);
				}
			}

			if (jsonMain.has("not_going")) {
				rsvp.getGoingOnTime().clear();
				for (int i = 0; i < jsonMain.getJSONArray("not_going").length(); i++) {
					String s = jsonMain.getJSONArray("not_going").getString(i);
					rsvp.getNotGoing().add(s);
				}
			}

			if (DatabaseManager.getManager().updateRsvpData(rsvp)) {
				response.setContentType("application/json");
				response.setStatus(200);
				return ResponseUtils.getJsonResponseMessage("Successfully updated RSVP data");
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
			Logger.getLogger().exception(null, "[WEB-API] Internal update RSVP data error", e, RsvpEndpoint.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}