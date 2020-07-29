package org.dreamexposure.discal.server.api.endpoints.v2.event;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.crypto.KeyGenerator;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.object.event.Recurrence;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.ImageUtils;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.server.utils.Authentication;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.common.util.Snowflake;

@RestController
@RequestMapping("/v2/events")
public class CreateEventEndpoint {
    @PostMapping(value = "/create", produces = "application/json")
    public String createEvent(HttpServletRequest request, HttpServletResponse response, @RequestBody String rBody) {
        //Authenticate...
        AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.isSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return authState.toJson();
        } else if (authState.isReadOnly()) {
            response.setStatus(401);
            response.setContentType("application/json");
            return JsonUtils.getJsonResponseMessage("Read-Only key not Allowed");
        }

        //Okay, now handle actual request.
        try {
            JSONObject requestBody = new JSONObject(rBody);

            String guildId = requestBody.getString("guild_id");
            int calNumber = requestBody.getInt("calendar_number");


            //okay, lets actually create the event
            GuildSettings settings = DatabaseManager.getSettings(Snowflake.of(guildId)).block();
            CalendarData calData = DatabaseManager.getCalendar(settings.getGuildID(), calNumber).block();

            com.google.api.services.calendar.Calendar service = CalendarAuth.getCalendarService(settings).block();
            Calendar cal = service.calendars().get(calData.getCalendarId()).execute();

            Event event = new Event();
            event.setId(KeyGenerator.generateEventId());
            event.setVisibility("public");

            EventDateTime start = new EventDateTime();
            start.setDateTime(new DateTime(requestBody.getLong("epoch_start")));
            event.setStart(start.setTimeZone(cal.getTimeZone()));
            EventDateTime end = new EventDateTime();
            end.setDateTime(new DateTime(requestBody.getLong("epoch_end")));
            event.setEnd(end.setTimeZone(cal.getTimeZone()));

            if (requestBody.has("summary"))
                event.setSummary(requestBody.getString("summary"));
            if (requestBody.has("description"))
                event.setDescription(requestBody.getString("description"));
            if (requestBody.has("color"))
                event.setColorId(EventColor.fromNameOrHexOrID(requestBody.getString("color")).getId() + "");
            if (requestBody.has("location"))
                event.setLocation(requestBody.getString("location"));
            if (requestBody.has("recur") && requestBody.getBoolean("recur")) {
                JSONObject recur = requestBody.getJSONObject("recurrence");

                Recurrence recurrence = new Recurrence().fromJson(recur);
                String[] rr = new String[]{recurrence.toRRule()};

                event.setRecurrence(Arrays.asList(rr));
            }

            EventData eventData = EventData.empty();
            if (requestBody.has("image")) {
                if (ImageUtils.validate(requestBody.getString("image"), settings.isPatronGuild()).block()) {
                    //Link is good...
                    eventData = EventData.fromImage(
                        Snowflake.of(guildId),
                        event.getId(),
                        event.getEnd().getDateTime().getValue(),
                        requestBody.getString("image")
                    );

                }
            }
            //Everything supported is now checked for, lets create this on google's end now.
            Event confirmed = service.events().insert(cal.getId(), event).execute();
            if (eventData.shouldBeSaved())
                DatabaseManager.updateEventData(eventData).subscribe();

            //If we get here, nothing errored, and everything should be created correctly...
            JSONObject responseBody = new JSONObject();
            responseBody.put("message", "Event Successfully Created");
            responseBody.put("event_id", confirmed.getId());

            response.setContentType("application/json");
            response.setStatus(200);
            return responseBody.toString();
        } catch (JSONException e) {
            e.printStackTrace();

            response.setContentType("application/json");
            response.setStatus(400);
            return JsonUtils.getJsonResponseMessage("Bad Request");
        } catch (Exception e) {
            LogFeed.log(LogObject.forException("[API-v2]", "update event err", e, this.getClass()));
            response.setContentType("application/json");
            response.setStatus(500);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}
