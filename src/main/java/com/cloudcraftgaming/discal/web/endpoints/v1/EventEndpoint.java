package com.cloudcraftgaming.discal.web.endpoints.v1;

import com.cloudcraftgaming.discal.api.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.enums.event.EventColor;
import com.cloudcraftgaming.discal.api.enums.event.EventFrequency;
import com.cloudcraftgaming.discal.api.object.calendar.CalendarData;
import com.cloudcraftgaming.discal.api.object.event.EventData;
import com.cloudcraftgaming.discal.api.object.event.Recurrence;
import com.cloudcraftgaming.discal.api.object.web.WebGuild;
import com.cloudcraftgaming.discal.api.utils.EventUtils;
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
					rjo.put("recurCount", -1);
					rjo.put("interval", 1);

					jo.put("recurrence", rjo);
				}

				EventData ed = DatabaseManager.getManager().getEventData(Long.valueOf(g.getId()), e.getId());

				jo.put("image", ed.getImageLink());

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

	public static String updateEvent(Request request, Response response) {


		return response.body();
	}

	public static String createEvent(Request request, Response response) {


		return response.body();
	}

	public static String deleteEvent(Request request, Response response) {
		JSONObject requestBody = new JSONObject(request.body());
		String eventId = requestBody.getString("id");

		Map m = DiscordAccountHandler.getHandler().getAccount(request.session().id());
		WebGuild g = (WebGuild) m.get("selected");
		g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));

		//okay, time to properly delete the event
		if (EventUtils.deleteEvent(g.getSettings(), eventId)) {
			//Deleted!
			JSONObject r = new JSONObject();
			r.put("message", "Successfully deleted event!");
			response.body(r.toString());
		} else {
			//Oh nos! we failed >.<
			JSONObject r = new JSONObject();
			r.put("message", "Failed to delete event!");
			response.status(500);
			response.body(r.toString());
		}

		return response.body();
	}
}