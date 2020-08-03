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
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.object.event.Recurrence;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.EventUtils;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.ImageUtils;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.server.utils.Authentication;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.common.util.Snowflake;

@SuppressWarnings("Duplicates")
@RestController
@RequestMapping("/api/v1/events")
public class EventEndpoint {

    @PostMapping(value = "/list/month", produces = "application/json")
    public static String getEventsForMonth(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String rBody) {
        //Authenticate...
        final AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.isSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return authState.toJson();
        }

        //Okay, now handle actual request.
        final JSONObject requestBody = new JSONObject(rBody);

        final int daysInMonth = Integer.parseInt(requestBody.getString("DaysInMonth"));
        final long startEpoch = Long.parseLong(requestBody.getString("StartEpoch"));
        final long endEpoch = startEpoch + (86400000L * daysInMonth);
        final long guildId = requestBody.getLong("guild_id");
        final GuildSettings settings = DatabaseManager.getSettings(Snowflake.of(guildId)).block();

        //okay, lets actually get the month's events.
        try {
            final Calendar service = CalendarAuth.getCalendarService(settings).block();

            final CalendarData calendarData = DatabaseManager.getMainCalendar(settings.getGuildID()).block();
            final Events events = service.events().list(calendarData.getCalendarAddress())
                .setTimeMin(new DateTime(startEpoch))
                .setTimeMax(new DateTime(endEpoch))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setShowDeleted(false)
                .execute();
            final List<Event> items = events.getItems();

            final List<JSONObject> eventsJson = new ArrayList<>();
            for (final Event e : items) {
                final JSONObject jo = new JSONObject();
                jo.put("id", e.getId());
                jo.put("epochStart", e.getStart().getDateTime().getValue());
                jo.put("epochEnd", e.getEnd().getDateTime().getValue());

                eventsJson.add(jo);
            }

            final JSONObject body = new JSONObject();
            body.put("events", eventsJson);
            body.put("count", eventsJson.size() + "");

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_SUCCESS);
            return body.toString();
        } catch (final Exception e) {
            LogFeed.log(LogObject
                .forException("[WEB-API-v1]", "get events for month err", e, EventEndpoint.class));

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }

    @PostMapping(value = "/list/date", produces = "application/json")
    public static String getEventsForSelectedDate(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String rBody) {
        //Authenticate...
        final AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.isSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return authState.toJson();
        }

        //Okay, now handle actual request.
        final JSONObject requestBody = new JSONObject(rBody);
        final long startEpoch = Long.parseLong(requestBody.getString("StartEpoch"));
        final long endEpoch = startEpoch + 86400000L;
        final long guildId = requestBody.getLong("guild_id");
        final GuildSettings settings = DatabaseManager.getSettings(Snowflake.of(guildId)).block();

        //okay, lets actually get the month's events.
        try {
            final Calendar service = CalendarAuth.getCalendarService(settings).block();

            final CalendarData calendarData = DatabaseManager.getMainCalendar(settings.getGuildID()).block();
            final Events events = service.events().list(calendarData.getCalendarAddress())
                .setTimeMin(new DateTime(startEpoch))
                .setTimeMax(new DateTime(endEpoch))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setShowDeleted(false)
                .execute();
            final List<Event> items = events.getItems();

            String tz = "Error/Unknown";
            try {
                tz = service.calendars().get(calendarData.getCalendarAddress()).execute().getTimeZone();
            } catch (final Exception ignore) {
            }

            final List<JSONObject> eventsJson = new ArrayList<>();
            for (final Event e : items) {
                final JSONObject jo = new JSONObject();
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

                if (e.getRecurrence() != null && !e.getRecurrence().isEmpty()) {
                    jo.put("recur", true);
                    final Recurrence r = new Recurrence().fromRRule(e.getRecurrence().get(0));

                    final JSONObject rjo = new JSONObject();
                    rjo.put("frequency", r.getFrequency().name());
                    rjo.put("count", r.getCount());
                    rjo.put("interval", r.getInterval());

                    jo.put("recurrence", rjo);
                } else {
                    jo.put("recur", false);

                    final JSONObject rjo = new JSONObject();
                    rjo.put("frequency", EventFrequency.DAILY.name());
                    rjo.put("count", -1);
                    rjo.put("interval", 1);

                    jo.put("recurrence", rjo);
                }

                final EventData ed = DatabaseManager.getEventData(settings.getGuildID(), e.getId()).block();

                jo.put("image", ed.getImageLink());

                eventsJson.add(jo);
            }

            final JSONObject body = new JSONObject();
            body.put("events", eventsJson);
            body.put("count", eventsJson.size());

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_SUCCESS);
            return body.toString();
        } catch (final Exception e) {
            LogFeed.log(LogObject
                .forException("[WEB-API-v1]", "get events for date err", e, EventEndpoint.class));

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }

    @PostMapping(value = "/update", produces = "application/json")
    public static String updateEvent(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String rBody) {
        //Authenticate...
        final AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.isSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return authState.toJson();
        }

        //Okay, now handle actual request.
        final JSONObject body = new JSONObject(rBody);
        final String eventId = body.getString("id");
        final long guildId = body.getLong("guild_id");
        final GuildSettings settings = DatabaseManager.getSettings(Snowflake.of(guildId)).block();

        //Okay, time to update the event
        try {
            final Calendar service = CalendarAuth.getCalendarService(settings).block();

            final CalendarData calendarData = DatabaseManager.getMainCalendar(settings.getGuildID()).block();
            final com.google.api.services.calendar.model.Calendar cal = service.calendars().get(calendarData.getCalendarId()).execute();

            final Event event = new Event();
            event.setId(eventId);
            event.setVisibility("public");
            event.setSummary(body.getString("summary"));
            event.setDescription(body.getString("description"));

            final EventDateTime start = new EventDateTime();
            start.setDateTime(new DateTime(body.getLong("epochStart")));
            event.setStart(start.setTimeZone(cal.getTimeZone()));

            final EventDateTime end = new EventDateTime();
            end.setDateTime(new DateTime(body.getLong("epochEnd")));
            event.setEnd(end.setTimeZone(cal.getTimeZone()));

            if (!"NONE".equalsIgnoreCase(body.getString("color")))
                event.setColorId(EventColor.fromNameOrHexOrID(body.getString("color")).getId() + "");

            if (!"".equalsIgnoreCase(body.getString("location")) || !"N/a".equalsIgnoreCase(body.getString("location")))
                event.setLocation(body.getString("location"));

            final JSONObject recur = body.getJSONObject("recurrence");
            if (recur.getBoolean("recur")) {
                //Handle recur
                final Recurrence recurrence = new Recurrence();
                recurrence.setFrequency(EventFrequency.fromValue(recur.getString("frequency")));
                recurrence.setCount(recur.getInt("count"));
                recurrence.setInterval(recur.getInt("interval"));

                final String[] rr = {recurrence.toRRule()};
                event.setRecurrence(Arrays.asList(rr));
            }

            EventData ed = EventData.empty();
            if (!"".equalsIgnoreCase(body.getString("image"))) {
                ed = EventData.fromImage(
                    Snowflake.of(guildId),
                    eventId,
                    end.getDateTime().getValue(),
                    body.getString("image")
                );

                if (!ImageUtils.validate(ed.getImageLink(), settings.isPatronGuild()).block()) {
                    final JSONObject respondBody = new JSONObject();
                    respondBody.put("Message", "Failed to create event!");
                    respondBody.put("reason", "Invalid image link and/or GIF image not supported.");


                    response.setContentType("application/json");
                    response.setStatus(GlobalConst.STATUS_BAD_REQUEST);
                    return respondBody.toString();
                }
            }

            if (ed.shouldBeSaved())
                DatabaseManager.updateEventData(ed).subscribe();

            service.events().update(calendarData.getCalendarId(), eventId, event).execute();

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_SUCCESS);
            return JsonUtils.getJsonResponseMessage("Successfully updated event!");

        } catch (final Exception e) {
            LogFeed.log(LogObject
                .forException("[WEB-API-v1]", "update event err", e, EventEndpoint.class));
            e.printStackTrace();

            final JSONObject respondBody = new JSONObject();
            respondBody.put("Message", "Failed to create event!");
            respondBody.put("reason", "Google API may be at fault. Please try again.");

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return respondBody.toString();
        }
    }

    @PostMapping(value = "/create", produces = "application/json")
    public static String createEvent(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String rBody) {
        //Authenticate...
        final AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.isSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return authState.toJson();
        }

        //Okay, now handle actual request.
        final JSONObject body = new JSONObject(rBody);
        final long guildId = body.getLong("guild_id");
        final GuildSettings settings = DatabaseManager.getSettings(Snowflake.of(guildId)).block();

        //Okay, time to create the event
        try {
            final Calendar service = CalendarAuth.getCalendarService(settings).block();

            final CalendarData calendarData = DatabaseManager.getMainCalendar(settings.getGuildID()).block();
            final com.google.api.services.calendar.model.Calendar cal = service.calendars().get(calendarData.getCalendarId()).execute();

            final Event event = new Event();
            event.setId(KeyGenerator.generateEventId());
            event.setVisibility("public");
            event.setSummary(body.getString("summary"));
            event.setDescription(body.getString("description"));

            final EventDateTime start = new EventDateTime();
            start.setDateTime(new DateTime(body.getLong("epochStart")));
            event.setStart(start.setTimeZone(cal.getTimeZone()));

            final EventDateTime end = new EventDateTime();
            end.setDateTime(new DateTime(body.getLong("epochEnd")));
            event.setEnd(end.setTimeZone(cal.getTimeZone()));

            if (!"NONE".equalsIgnoreCase(body.getString("color")))
                event.setColorId(EventColor.fromNameOrHexOrID(body.getString("color")).getId() + "");

            if (!"".equalsIgnoreCase(body.getString("location")) || !"N/a".equalsIgnoreCase(body.getString("location")))
                event.setLocation(body.getString("location"));

            final JSONObject recur = body.getJSONObject("recurrence");
            if (recur.getBoolean("recur")) {
                //Handle recur
                final Recurrence recurrence = new Recurrence();
                recurrence.setFrequency(EventFrequency.fromValue(recur.getString("frequency")));
                recurrence.setCount(recur.getInt("count"));
                recurrence.setInterval(recur.getInt("interval"));

                final String[] rr = {recurrence.toRRule()};
                event.setRecurrence(Arrays.asList(rr));
            }

            EventData ed = EventData.empty();
            if (!"".equalsIgnoreCase(body.getString("image"))) {
                ed = EventData.fromImage(
                    Snowflake.of(guildId),
                    event.getId(),
                    end.getDateTime().getValue(),
                    body.getString("image")
                );

                if (!ImageUtils.validate(ed.getImageLink(), settings.isPatronGuild()).block()) {
                    final JSONObject respondBody = new JSONObject();
                    respondBody.put("Message", "Failed to update event!");
                    respondBody.put("reason", "Invalid image link and/or GIF image not supported.");


                    response.setContentType("application/json");
                    response.setStatus(GlobalConst.STATUS_BAD_REQUEST);
                    return respondBody.toString();
                }
            }

            if (ed.shouldBeSaved())
                DatabaseManager.updateEventData(ed).subscribe();

            final Event confirmed = service.events().insert(calendarData.getCalendarId(), event).execute();

            final JSONObject respondBody = new JSONObject();
            respondBody.put("Message", "Successfully create event!");
            respondBody.put("id", confirmed.getId());

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_SUCCESS);
            return respondBody.toString();

        } catch (final Exception e) {
            LogFeed.log(LogObject
                .forException("[WEB-API-v1]", "Event create err", e, EventEndpoint.class));
            e.printStackTrace();

            final JSONObject respondBody = new JSONObject();
            respondBody.put("Message", "Failed to create event!");
            respondBody.put("reason", "Google API may be at fault. Please try again.");

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);

            return respondBody.toString();
        }
    }

    @PostMapping(value = "/delete", produces = "application/json")
    public static String deleteEvent(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String rBody) {
        //Authenticate...
        final AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.isSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return authState.toJson();
        }

        //Okay, now handle actual request.
        final JSONObject requestBody = new JSONObject(rBody);
        final String eventId = requestBody.getString("id");
        final long guildId = requestBody.getLong("guild_id");
        final GuildSettings settings = DatabaseManager.getSettings(Snowflake.of(guildId)).block();

        //okay, time to properly delete the event
        if (EventUtils.deleteEvent(settings, 1, eventId).block()) {
            //Deleted!
            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_SUCCESS);
            return JsonUtils.getJsonResponseMessage("Successfully deleted event!");
        } else {
            //Oh nos! we failed >.<
            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);

            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}