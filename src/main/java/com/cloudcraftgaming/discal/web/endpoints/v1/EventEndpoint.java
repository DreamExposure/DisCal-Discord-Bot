package com.cloudcraftgaming.discal.web.endpoints.v1;

import com.cloudcraftgaming.discal.api.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.enums.event.EventColor;
import com.cloudcraftgaming.discal.api.object.calendar.CalendarData;
import com.cloudcraftgaming.discal.api.object.web.WebGuild;
import com.cloudcraftgaming.discal.api.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.web.handler.DiscordAccountHandler;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventEndpoint {
	public static String getEventsForMonth(Request request, Response response) {
		JSONObject requestBody = new JSONObject(request.body());

		Integer daysInMonth = Integer.valueOf(requestBody.getString("DaysInMonth"));
		Long startEpoch = Long.valueOf(requestBody.getString("StartEpoch"));
		Long endEpoch = startEpoch + (86400000L * daysInMonth);

		Map m = DiscordAccountHandler.getHandler().getAccount(request.session().id());
		WebGuild g = (WebGuild) m.get("selected");
		g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));

		//okay, lets actually get the month's events.
		try {
			Calendar service;
			if (g.getSettings().useExternalCalendar()) {
				service = CalendarAuth.getCalendarService(g.getSettings());
			} else {
				service = CalendarAuth.getCalendarService();
			}
			CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(Long.valueOf(g.getId()));
			Events events = service.events().list(calendarData.getCalendarAddress())
					.setTimeMin(new DateTime(startEpoch))
					.setTimeMax(new DateTime(endEpoch))
					.setOrderBy("startTime")
					.setSingleEvents(true)
					.setShowDeleted(false)
					.execute();
			List<Event> items = events.getItems();

			List<JSONObject> eventsJson = new ArrayList<>();
			for (Event e : items) {
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
			ExceptionHandler.sendException(null, "[WEB] Failed to retrieve events for a month.", e, EventEndpoint.class);
			return response.body();
		}
		return response.body();
	}

	public static String getEventsForSelectedDate(Request request, Response response) {
		JSONObject requestBody = new JSONObject(request.body());
		Long startEpoch = Long.valueOf(requestBody.getString("StartEpoch"));
		Long endEpoch = startEpoch + 86400000L;

		Map m = DiscordAccountHandler.getHandler().getAccount(request.session().id());
		WebGuild g = (WebGuild) m.get("selected");
		g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));

		//okay, lets actually get the month's events.
		try {
			Calendar service;
			if (g.getSettings().useExternalCalendar()) {
				service = CalendarAuth.getCalendarService(g.getSettings());
			} else {
				service = CalendarAuth.getCalendarService();
			}
			CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(Long.valueOf(g.getId()));
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
				jo.put("location", e.getLocation());
				jo.put("color", EventColor.fromNameOrHexOrID(e.getColorId()).name());

				eventsJson.add(jo);
			}

			JSONObject body = new JSONObject();
			body.put("events", eventsJson);
			body.put("count", eventsJson.size() + "");

			response.body(body.toString());
		} catch (Exception e) {
			response.body("Internal server error!");
			ExceptionHandler.sendException(null, "[WEB] Failed to retrieve events for specific date!", e, EventEndpoint.class);
			return response.body();
		}
		return response.body();
	}
}