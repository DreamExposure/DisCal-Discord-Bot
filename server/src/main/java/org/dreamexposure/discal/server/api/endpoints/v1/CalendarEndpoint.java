package org.dreamexposure.discal.server.api.endpoints.v1;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
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
import java.util.ArrayList;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"ThrowableNotThrown", "Duplicates"})
@RestController
@RequestMapping("/api/v1/calendar")
public class CalendarEndpoint {

	@PostMapping(value = "/get", produces = "application/json")
	public static String getCalendar(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
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
			int calNumber = jsonMain.getInt("number");

			CalendarData calendar = DatabaseManager.getManager().getCalendar(guildId, calNumber);

			if (!calendar.getCalendarAddress().equalsIgnoreCase("primary")) {

				JSONObject body = new JSONObject();
				body.put("external", calendar.isExternal());
				body.put("id", calendar.getCalendarId());
				body.put("address", calendar.getCalendarAddress());

				response.setContentType("application/json");
				response.setStatus(200);
				return body.toString();
			} else {
				response.setContentType("application/json");
				response.setStatus(404);
				return ResponseUtils.getJsonResponseMessage("Calendar not found");
			}
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return ResponseUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal get calendar error", e, CalendarEndpoint.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/list", produces = "application/json")
	public static String listCalendars(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
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

			ArrayList<JSONObject> cals = new ArrayList<>();
			for (CalendarData cal : DatabaseManager.getManager().getAllCalendars(guildId)) {
				if (!cal.getCalendarAddress().equalsIgnoreCase("primary")) {
					JSONObject body = new JSONObject();
					body.put("number", cal.getCalendarNumber());
					body.put("external", cal.isExternal());
					body.put("id", cal.getCalendarId());
					body.put("address", cal.getCalendarAddress());

					cals.add(body);
				}
			}

			JSONObject body = new JSONObject();
			body.put("count", cals.size());
			body.put("calendars", cals);

			response.setContentType("application/json");
			response.setStatus(200);
			return body.toString();
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return ResponseUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal list calendars error", e, CalendarEndpoint.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}