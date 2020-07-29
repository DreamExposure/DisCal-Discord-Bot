package org.dreamexposure.discal.server.api.endpoints.v2.event.list;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.server.utils.Authentication;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.common.util.Snowflake;

@RestController
@RequestMapping("/v2/events/list")
public class ListEventMonthEndpoint {
    @PostMapping(value = "/month", produces = "application/json")
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

            Snowflake guildId = Snowflake.of(requestBody.getString("guild_id"));
            int calNumber = requestBody.getInt("calendar_number");
            int daysInMonth = requestBody.getInt("days_in_month");
            long startEpoch = requestBody.getLong("epoch_start");
            long endEpoch = startEpoch + (GlobalConst.oneDayMs * daysInMonth);
            GuildSettings settings = DatabaseManager.getSettings(guildId).block();

            //okay, lets actually get the month's events.
            Calendar service = CalendarAuth.getCalendarService(settings).block();

            CalendarData calendarData = DatabaseManager.getCalendar(settings.getGuildID(), calNumber).block();

            Events events = service.events().list(calendarData.getCalendarAddress())
                    .setTimeMin(new DateTime(startEpoch))
                    .setTimeMax(new DateTime(endEpoch))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setShowDeleted(false)
                    .execute();
            List<Event> items = events.getItems();

            List<JSONObject> jEvents = new ArrayList<>();
            for (Event e : items) {
                jEvents.add(JsonUtils.convertEventToJson(e, settings));
            }

            JSONObject body = new JSONObject();
            body.put("events", jEvents);
            body.put("message", "Events successfully listed.");

            response.setContentType("application/json");
            response.setStatus(200);
            return body.toString();
        } catch (JSONException e) {
            response.setContentType("application/json");
            response.setStatus(400);
            return JsonUtils.getJsonResponseMessage("Bad Request");
        } catch (Exception e) {
            LogFeed.log(LogObject
                    .forException("[API-v2]", "get events for month err", e, this.getClass()));

            response.setContentType("application/json");
            response.setStatus(500);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}
