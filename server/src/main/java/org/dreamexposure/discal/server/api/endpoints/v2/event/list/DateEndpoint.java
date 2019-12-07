package org.dreamexposure.discal.server.api.endpoints.v2.event.list;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.server.utils.Authentication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.core.object.util.Snowflake;

@RestController
@RequestMapping("/v2/events/list")
public class DateEndpoint {
	@PostMapping(value = "/list/date", produces = "application/json")
	public String getEventsForDate(HttpServletRequest request, HttpServletResponse response, @RequestBody String rBody) {
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

			long guildId = requestBody.getLong("guild_id");
			int calNumber = requestBody.getInt("calendar_number");
			long startEpoch = requestBody.getLong("start_epoch");
			long endEpoch = startEpoch + GlobalConst.oneDayMs;
			GuildSettings settings = DatabaseManager.getManager().getSettings(Snowflake.of(guildId));

			//okay, lets actually get the date's events.
			Calendar service = CalendarAuth.getCalendarService(settings);

			CalendarData calendarData = DatabaseManager.getManager().getCalendar(settings.getGuildID(), calNumber);
			Events events = service.events().list(calendarData.getCalendarAddress())
					.setTimeMin(new DateTime(startEpoch))
					.setTimeMax(new DateTime(endEpoch))
					.setOrderBy("startTime")
					.setSingleEvents(true)
					.setShowDeleted(false)
					.execute();
			List<Event> items = events.getItems();

			JSONArray jEvents = new JSONArray();
			for (Event e : items)
				jEvents.put(JsonUtils.convertEventToJson(e, settings));

			JSONObject body = new JSONObject();
			body.put("events", jEvents);

			response.setContentType("application/json");
			response.setStatus(200);
			return body.toString();
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return JsonUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[API-v2] Failed to retrieve events for a date.", e, true, this.getClass());

			response.setContentType("application/json");
			response.setStatus(500);
			return JsonUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}
