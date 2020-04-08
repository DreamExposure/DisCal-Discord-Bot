package org.dreamexposure.discal.server.api.endpoints.v2.calendar;

import com.google.api.services.calendar.Calendar;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.CalendarUtils;
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
@RequestMapping("/v2/calendar")
public class ListCalendarEndpoint {
	@PostMapping(value = "/list", produces = "application/json")
	public String listCalendars(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
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
			Snowflake guildId = Snowflake.of(jsonMain.getString("guild_id"));

			GuildSettings settings = DatabaseManager.getSettings(guildId).block();
			Calendar service = CalendarAuth.getCalendarService(settings);

			JSONArray jCals = new JSONArray();
			for (CalendarData calData : DatabaseManager.getAllCalendars(guildId).block()) {
				if (!calData.getCalendarAddress().equalsIgnoreCase("primary")
						&& CalendarUtils.calendarExists(calData, settings)) {
					com.google.api.services.calendar.model.Calendar cal = service.calendars()
							.get(calData.getCalendarAddress())
							.execute();

					JSONObject jCal = new JSONObject();

					jCal.put("calendar_address", calData.getCalendarAddress());
					jCal.put("calendar_id", calData.getCalendarId());
					jCal.put("calendar_number", calData.getCalendarNumber());
					jCal.put("external", calData.isExternal());
					jCal.put("summary", cal.getSummary());
					jCal.put("description", cal.getDescription());
					jCal.put("timezone", cal.getTimeZone());

					jCals.put(jCal);
				}
			}

			JSONObject body = new JSONObject();
			body.put("calendars", jCals);

			response.setContentType("application/json");
			response.setStatus(200);
			return body.toString();
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return JsonUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[API-v2] Internal list calendars error", e, true, this.getClass());

			response.setContentType("application/json");
			response.setStatus(500);
			return JsonUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}
