package org.dreamexposure.discal.server.api.endpoints.v1;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.crypto.KeyGenerator;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.enums.event.EventFrequency;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.object.event.Recurrence;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.object.web.WebGuild;
import org.dreamexposure.discal.core.utils.EventUtils;
import org.dreamexposure.discal.core.utils.ImageUtils;
import org.dreamexposure.discal.server.handler.DiscordAccountHandler;
import org.dreamexposure.discal.server.utils.Authentication;
import org.dreamexposure.discal.server.utils.ResponseUtils;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
@RestController
@RequestMapping("/api/v1/events")
public class EventEndpoint {

	@PostMapping(value = "/list/month", produces = "application/json")
	public static String getEventsForMonth(HttpServletRequest request, HttpServletResponse response, @RequestBody String rBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		}

		//Okay, now handle actual request.
		JSONObject requestBody = new JSONObject(rBody);

		Integer daysInMonth = Integer.valueOf(requestBody.getString("DaysInMonth"));
		Long startEpoch = Long.valueOf(requestBody.getString("StartEpoch"));
		Long endEpoch = startEpoch + (86400000L * daysInMonth);
		GuildSettings settings;

		if (DiscordAccountHandler.getHandler().hasAccount(request)) {
			if (requestBody.has("guild_id")) {
				long guildId = Long.valueOf(requestBody.getString("guild_id"));
				settings = DatabaseManager.getManager().getSettings(guildId);
			} else {
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");
				g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));
				settings = g.getSettings();
			}
		} else {
			long guildId = requestBody.getLong("guild_id");
			settings = DatabaseManager.getManager().getSettings(guildId);
		}

		//okay, lets actually get the month's events.
		try {
			Calendar service = CalendarAuth.getCalendarService(settings);

			CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(settings.getGuildID());
			Events events = service.events().list(calendarData.getCalendarAddress())
					.setTimeMin(new DateTime(startEpoch))
					.setTimeMax(new DateTime(endEpoch))
					.setOrderBy("startTime")
					.setSingleEvents(true)
					.setShowDeleted(false)
					.execute();
			List<Event> items = events.getItems();

			List<JSONObject> eventsJson = new ArrayList<>();
			for (Event e: items) {
				JSONObject jo = new JSONObject();
				jo.put("id", e.getId());
				jo.put("epochStart", e.getStart().getDateTime().getValue());
				jo.put("epochEnd", e.getEnd().getDateTime().getValue());

				eventsJson.add(jo);
			}

			JSONObject body = new JSONObject();
			body.put("events", eventsJson);
			body.put("count", eventsJson.size() + "");

			response.setContentType("application/json");
			response.setStatus(200);
			return body.toString();
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Failed to retrieve events for a month.", e, EventEndpoint.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/list/date", produces = "application/json")
	public static String getEventsForSelectedDate(HttpServletRequest request, HttpServletResponse response, @RequestBody String rBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		}

		//Okay, now handle actual request.
		JSONObject requestBody = new JSONObject(rBody);
		Long startEpoch = Long.valueOf(requestBody.getString("StartEpoch"));
		Long endEpoch = startEpoch + 86400000L;
		GuildSettings settings;

		if (DiscordAccountHandler.getHandler().hasAccount(request)) {
			if (requestBody.has("guild_id")) {
				long guildId = requestBody.getLong("guild_id");
				settings = DatabaseManager.getManager().getSettings(guildId);
			} else {
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");
				g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));
				settings = g.getSettings();
			}
		} else {
			long guildId = requestBody.getLong("guild_id");
			settings = DatabaseManager.getManager().getSettings(guildId);
		}

		//okay, lets actually get the month's events.
		try {
			Calendar service = CalendarAuth.getCalendarService(settings);

			CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(settings.getGuildID());
			Events events = service.events().list(calendarData.getCalendarAddress())
					.setTimeMin(new DateTime(startEpoch))
					.setTimeMax(new DateTime(endEpoch))
					.setOrderBy("startTime")
					.setSingleEvents(true)
					.setShowDeleted(false)
					.execute();
			List<Event> items = events.getItems();

			String tz = "Error/Unknown";
			try {
				tz = service.calendars().get(calendarData.getCalendarAddress()).execute().getTimeZone();
			} catch (Exception ignore) {
			}

			List<JSONObject> eventsJson = new ArrayList<>();
			for (Event e : items) {
				JSONObject jo = new JSONObject();
				jo.put("id", e.getId());
				jo.put("epochStart", e.getStart().getDateTime().getValue());
				jo.put("epochEnd", e.getEnd().getDateTime().getValue());
				jo.put("timezone", tz);
				jo.put("summary", e.getSummary());
				jo.put("description", e.getDescription());
				if (e.getLocked() != null)
					jo.put("location", e.getLocation());
				else
					jo.put("location", "N/a");

				jo.put("color", EventColor.fromNameOrHexOrID(e.getColorId()).name());
				jo.put("isParent", !(e.getId().contains("_")));

				if (e.getRecurrence() != null && e.getRecurrence().size() > 0) {
					jo.put("recur", true);
					Recurrence r = new Recurrence().fromRRule(e.getRecurrence().get(0));

					JSONObject rjo = new JSONObject();
					rjo.put("frequency", r.getFrequency().name());
					rjo.put("count", r.getCount());
					rjo.put("interval", r.getInterval());

					jo.put("recurrence", rjo);
				} else {
					jo.put("recur", false);

					JSONObject rjo = new JSONObject();
					rjo.put("frequency", EventFrequency.DAILY.name());
					rjo.put("count", -1);
					rjo.put("interval", 1);

					jo.put("recurrence", rjo);
				}

				EventData ed = DatabaseManager.getManager().getEventData(settings.getGuildID(), e.getId());

				jo.put("image", ed.getImageLink());

				eventsJson.add(jo);
			}

			JSONObject body = new JSONObject();
			body.put("events", eventsJson);
			body.put("count", eventsJson.size());

			response.setContentType("application/json");
			response.setStatus(200);
			return body.toString();
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Failed to retrieve events for specific date!", e, EventEndpoint.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/update", produces = "application/json")
	public static String updateEvent(HttpServletRequest request, HttpServletResponse response, @RequestBody String rBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		}

		//Okay, now handle actual request.
		JSONObject body = new JSONObject(rBody);
		String eventId = body.getString("id");
		GuildSettings settings;

		if (DiscordAccountHandler.getHandler().hasAccount(request)) {
			Map m = DiscordAccountHandler.getHandler().getAccount(request);
			WebGuild g = (WebGuild) m.get("selected");
			g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));
			settings = g.getSettings();
		} else {
			long guildId = body.getLong("guild_id");
			settings = DatabaseManager.getManager().getSettings(guildId);
		}

		//Okay, time to update the event
		try {
			Calendar service = CalendarAuth.getCalendarService(settings);

			CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(settings.getGuildID());
			com.google.api.services.calendar.model.Calendar cal = service.calendars().get(calendarData.getCalendarId()).execute();

			Event event = new Event();
			event.setId(eventId);
			event.setVisibility("public");
			event.setSummary(body.getString("summary"));
			event.setDescription(body.getString("description"));

			EventDateTime start = new EventDateTime();
			start.setDateTime(new DateTime(body.getLong("epochStart")));
			event.setStart(start.setTimeZone(cal.getTimeZone()));

			EventDateTime end = new EventDateTime();
			end.setDateTime(new DateTime(body.getLong("epochEnd")));
			event.setEnd(end.setTimeZone(cal.getTimeZone()));

			if (!body.getString("color").equalsIgnoreCase("NONE"))
				event.setColorId(EventColor.fromNameOrHexOrID(body.getString("color")).getId() + "");

			if (!body.getString("location").equalsIgnoreCase("") || !body.getString("location").equalsIgnoreCase("N/a"))
				event.setLocation(body.getString("location"));

			JSONObject recur = body.getJSONObject("recurrence");
			if (recur.getBoolean("recur")) {
				//Handle recur
				Recurrence recurrence = new Recurrence();
				recurrence.setFrequency(EventFrequency.fromValue(recur.getString("frequency")));
				recurrence.setCount(recur.getInt("count"));
				recurrence.setInterval(recur.getInt("interval"));

				String[] rr = new String[]{recurrence.toRRule()};
				event.setRecurrence(Arrays.asList(rr));
			}

			EventData ed = new EventData(settings.getGuildID());
			if (!body.getString("image").equalsIgnoreCase("")) {
				ed.setImageLink(body.getString("image"));
				ed.setEventId(eventId);
				ed.setEventEnd(event.getEnd().getDateTime().getValue());

				if (!ImageUtils.validate(ed.getImageLink())) {
					JSONObject respondBody = new JSONObject();
					respondBody.put("Message", "Failed to create event!");
					respondBody.put("reason", "Invalid image link and/or GIF image not supported.");


					response.setContentType("application/json");
					response.setStatus(400);
					return respondBody.toString();
				}
			}

			if (ed.shouldBeSaved())
				DatabaseManager.getManager().updateEventData(ed);

			service.events().update(calendarData.getCalendarId(), eventId, event).execute();

			response.setContentType("application/json");
			response.setStatus(200);
			return ResponseUtils.getJsonResponseMessage("Successfully updated event!");

		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Failed to update event!", e, EventEndpoint.class);
			e.printStackTrace();

			JSONObject respondBody = new JSONObject();
			respondBody.put("Message", "Failed to create event!");
			respondBody.put("reason", "Google API may be at fault. Please try again.");

			response.setContentType("application/json");
			response.setStatus(500);
			return respondBody.toString();
		}
	}

	@PostMapping(value = "/create", produces = "application/json")
	public static String createEvent(HttpServletRequest request, HttpServletResponse response, @RequestBody String rBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		}

		//Okay, now handle actual request.
		JSONObject body = new JSONObject(rBody);
		GuildSettings settings;

		if (DiscordAccountHandler.getHandler().hasAccount(request)) {
			Map m = DiscordAccountHandler.getHandler().getAccount(request);
			WebGuild g = (WebGuild) m.get("selected");
			g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));
			settings = g.getSettings();
		} else {
			long guildId = body.getLong("guild_id");
			settings = DatabaseManager.getManager().getSettings(guildId);
		}

		//Okay, time to create the event
		try {
			Calendar service = CalendarAuth.getCalendarService(settings);

			CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(settings.getGuildID());
			com.google.api.services.calendar.model.Calendar cal = service.calendars().get(calendarData.getCalendarId()).execute();

			Event event = new Event();
			event.setId(KeyGenerator.generateEventId());
			event.setVisibility("public");
			event.setSummary(body.getString("summary"));
			event.setDescription(body.getString("description"));

			EventDateTime start = new EventDateTime();
			start.setDateTime(new DateTime(body.getLong("epochStart")));
			event.setStart(start.setTimeZone(cal.getTimeZone()));

			EventDateTime end = new EventDateTime();
			end.setDateTime(new DateTime(body.getLong("epochEnd")));
			event.setEnd(end.setTimeZone(cal.getTimeZone()));

			if (!body.getString("color").equalsIgnoreCase("NONE"))
				event.setColorId(EventColor.fromNameOrHexOrID(body.getString("color")).getId() + "");

			if (!body.getString("location").equalsIgnoreCase("") || !body.getString("location").equalsIgnoreCase("N/a"))
				event.setLocation(body.getString("location"));

			JSONObject recur = body.getJSONObject("recurrence");
			if (recur.getBoolean("recur")) {
				//Handle recur
				Recurrence recurrence = new Recurrence();
				recurrence.setFrequency(EventFrequency.fromValue(recur.getString("frequency")));
				recurrence.setCount(recur.getInt("count"));
				recurrence.setInterval(recur.getInt("interval"));

				String[] rr = new String[]{recurrence.toRRule()};
				event.setRecurrence(Arrays.asList(rr));
			}

			EventData ed = new EventData(settings.getGuildID());
			ed.setEventId(event.getId());

			if (!body.getString("image").equalsIgnoreCase("")) {
				ed.setImageLink(body.getString("image"));
				ed.setEventEnd(event.getEnd().getDateTime().getValue());

				if (!ImageUtils.validate(ed.getImageLink())) {
					response.setContentType("application/json");
					response.setStatus(400);

					JSONObject respondBody = new JSONObject();
					respondBody.put("Message", "Failed to create event!");
					respondBody.put("reason", "Invalid image link and/or GIF image not supported.");

					return respondBody.toString();
				}
			}


			if (ed.shouldBeSaved())
				DatabaseManager.getManager().updateEventData(ed);

			Event confirmed = service.events().insert(calendarData.getCalendarId(), event).execute();

			JSONObject respondBody = new JSONObject();
			respondBody.put("Message", "Successfully create event!");
			respondBody.put("id", confirmed.getId());

			response.setContentType("application/json");
			response.setStatus(200);
			return respondBody.toString();

		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Failed to create event!", e, EventEndpoint.class);
			e.printStackTrace();

			JSONObject respondBody = new JSONObject();
			respondBody.put("Message", "Failed to create event!");
			respondBody.put("reason", "Google API may be at fault. Please try again.");

			response.setContentType("application/json");
			response.setStatus(500);

			return respondBody.toString();
		}
	}

	@PostMapping(value = "/delete", produces = "application/json")
	public static String deleteEvent(HttpServletRequest request, HttpServletResponse response, @RequestBody String rBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		}

		//Okay, now handle actual request.
		JSONObject requestBody = new JSONObject(rBody);
		String eventId = requestBody.getString("id");
		GuildSettings settings;

		//Check if logged in, else get guild ID from body.
		if (DiscordAccountHandler.getHandler().hasAccount(request)) {
			Map m = DiscordAccountHandler.getHandler().getAccount(request);
			WebGuild g = (WebGuild) m.get("selected");
			g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));
			settings = g.getSettings();
		} else {
			long guildId = requestBody.getLong("guild_id");
			settings = DatabaseManager.getManager().getSettings(guildId);
		}

		//okay, time to properly delete the event
		if (EventUtils.deleteEvent(settings, eventId)) {
			//Deleted!
			response.setContentType("application/json");
			response.setStatus(200);
			return ResponseUtils.getJsonResponseMessage("Successfully deleted event!");
		} else {
			//Oh nos! we failed >.<
			response.setContentType("application/json");
			response.setStatus(500);

			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}