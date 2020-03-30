package org.dreamexposure.discal.server.api.endpoints.v2.event;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
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
@RequestMapping("/v2/events")
public class GetEventEndpoint {
	@PostMapping(value = "/get", produces = "application/json")
	public String getEventsForMonth(HttpServletRequest request, HttpServletResponse response, @RequestBody String rBody) {
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
			int calNumber = requestBody.getInt("calendar_number");
			String eventId = requestBody.getString("event_id");
			GuildSettings settings = DatabaseManager.getManager().getSettings(Snowflake.of(guildId));

			//okay, get the calendar service and then the event
			Calendar service = CalendarAuth.getCalendarService(settings);

			CalendarData calendarData = DatabaseManager.getManager().getCalendar(settings.getGuildID(), calNumber);
			Event event = service.events().get(calendarData.getCalendarAddress(), eventId).execute();

			response.setContentType("application/json");
			if (event != null) {
				response.setStatus(200);
				return JsonUtils.convertEventToJson(event, settings).toString();
			} else {
				response.setStatus(404);
				return JsonUtils.getJsonResponseMessage("Event not Found");
			}
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return JsonUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[API-v2] Failed to retrieve event by ID.", e, true, this.getClass());

			response.setContentType("application/json");
			response.setStatus(500);
			return JsonUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}
