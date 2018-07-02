package com.cloudcraftgaming.discal.web.endpoints.v1;

import com.cloudcraftgaming.discal.api.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.api.crypto.KeyGenerator;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.enums.event.EventColor;
import com.cloudcraftgaming.discal.api.enums.event.EventFrequency;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.calendar.CalendarData;
import com.cloudcraftgaming.discal.api.object.event.EventData;
import com.cloudcraftgaming.discal.api.object.event.Recurrence;
import com.cloudcraftgaming.discal.api.object.web.WebGuild;
import com.cloudcraftgaming.discal.api.utils.EventUtils;
import com.cloudcraftgaming.discal.api.utils.ImageUtils;
import com.cloudcraftgaming.discal.logger.Logger;
import com.cloudcraftgaming.discal.web.handler.DiscordAccountHandler;
import com.cloudcraftgaming.discal.web.utils.ResponseUtils;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class EventEndpoint {
	public static String getEventsForMonth(Request request, Response response) {
		JSONObject requestBody = new JSONObject(request.body());

		Integer daysInMonth = Integer.valueOf(requestBody.getString("DaysInMonth"));
		Long startEpoch = Long.valueOf(requestBody.getString("StartEpoch"));
		Long endEpoch = startEpoch + (86400000L * daysInMonth);
		GuildSettings settings;

		if (DiscordAccountHandler.getHandler().hasAccount(request.session().id())) {
			Map m = DiscordAccountHandler.getHandler().getAccount(request.session().id());
			WebGuild g = (WebGuild) m.get("selected");
			g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));
			settings = g.getSettings();
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

			response.body(body.toString());
		} catch (Exception e) {
			response.body("Internal server error!");
			Logger.getLogger().exception(null, "[WEB] Failed to retrieve events for a month.", e, EventEndpoint.class, true);
			return response.body();
		}
		return response.body();
	}

	public static String getEventsForSelectedDate(Request request, Response response) {
		JSONObject requestBody = new JSONObject(request.body());
		Long startEpoch = Long.valueOf(requestBody.getString("StartEpoch"));
		Long endEpoch = startEpoch + 86400000L;
		GuildSettings settings;

		if (DiscordAccountHandler.getHandler().hasAccount(request.session().id())) {
			Map m = DiscordAccountHandler.getHandler().getAccount(request.session().id());
			WebGuild g = (WebGuild) m.get("selected");
			g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));
			settings = g.getSettings();
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
			for (Event e: items) {
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

			response.body(body.toString());
		} catch (Exception e) {
			response.body("Internal server error!");
			Logger.getLogger().exception(null, "[WEB] Failed to retrieve events for specific date!", e, EventEndpoint.class, true);
			return response.body();
		}
		return response.body();
	}

	public static String updateEvent(Request request, Response response) {
		JSONObject body = new JSONObject(request.body());
		String eventId = body.getString("id");
		GuildSettings settings;

		if (DiscordAccountHandler.getHandler().hasAccount(request.session().id())) {
			Map m = DiscordAccountHandler.getHandler().getAccount(request.session().id());
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
			event.setVisibility("web/public");
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
					response.status(400);
					JSONObject respondBody = new JSONObject();
					respondBody.put("Message", "Failed to create event!");
					respondBody.put("reason", "Invalid image link and/or GIF image not supported.");

					response.body(respondBody.toString());

					return response.body();
				}
			}

			if (ed.shouldBeSaved())
				DatabaseManager.getManager().updateEventData(ed);

			service.events().update(calendarData.getCalendarId(), eventId, event).execute();

			response.status(200);
			response.body(ResponseUtils.getJsonResponseMessage("Successfully updated event!"));

		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Failed to update event!", e, EventEndpoint.class, true);
			e.printStackTrace();

			JSONObject respondBody = new JSONObject();
			respondBody.put("Message", "Failed to create event!");
			respondBody.put("reason", "Google API may be at fault. Please try again.");

			response.body(respondBody.toString());
		}

		return response.body();
	}

	public static String createEvent(Request request, Response response) {
		JSONObject body = new JSONObject(request.body());
		GuildSettings settings;

		if (DiscordAccountHandler.getHandler().hasAccount(request.session().id())) {
			Map m = DiscordAccountHandler.getHandler().getAccount(request.session().id());
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
			event.setVisibility("web/public");
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
					response.status(400);
					JSONObject respondBody = new JSONObject();
					respondBody.put("Message", "Failed to create event!");
					respondBody.put("reason", "Invalid image link and/or GIF image not supported.");

					response.body(respondBody.toString());

					return response.body();
				}
			}


			if (ed.shouldBeSaved())
				DatabaseManager.getManager().updateEventData(ed);

			Event confirmed = service.events().insert(calendarData.getCalendarId(), event).execute();

			response.status(200);
			JSONObject respondBody = new JSONObject();
			respondBody.put("Message", "Successfully create event!");
			respondBody.put("id", confirmed.getId());

			response.body(respondBody.toString());

		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Failed to create event!", e, EventEndpoint.class, true);
			e.printStackTrace();

			JSONObject respondBody = new JSONObject();
			respondBody.put("Message", "Failed to create event!");
			respondBody.put("reason", "Google API may be at fault. Please try again.");

			response.body(respondBody.toString());
		}

		return response.body();
	}

	public static String deleteEvent(Request request, Response response) {
		JSONObject requestBody = new JSONObject(request.body());
		String eventId = requestBody.getString("id");
		GuildSettings settings;

		//Check if logged in, else get guild ID from body.
		if (DiscordAccountHandler.getHandler().hasAccount(request.session().id())) {
			Map m = DiscordAccountHandler.getHandler().getAccount(request.session().id());
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
			response.body(ResponseUtils.getJsonResponseMessage("Successfully deleted event!"));
		} else {
			//Oh nos! we failed >.<
			response.status(500);
			response.body(ResponseUtils.getJsonResponseMessage("Failed to delete event!"));
		}

		return response.body();
	}
}