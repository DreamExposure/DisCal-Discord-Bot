package org.dreamexposure.discal.server.api.endpoints.v2.calendar;

import com.google.api.services.calendar.Calendar;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.CalendarUtils;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.common.util.Snowflake;

@RestController
@RequestMapping("/v2/calendar")
public class ListCalendarEndpoint {
    @PostMapping(value = "/list", produces = "application/json")
    public String listCalendars(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String requestBody) {
        //Authenticate...
        final AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.isSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return authState.toJson();
        }

        //Okay, now handle actual request.
        try {
            final JSONObject jsonMain = new JSONObject(requestBody);
            final Snowflake guildId = Snowflake.of(jsonMain.getString("guild_id"));

            final GuildSettings settings = DatabaseManager.getSettings(guildId).block();
            final Calendar service = CalendarAuth.getCalendarService(settings).block();

            final JSONArray jCals = new JSONArray();
            for (final CalendarData calData : DatabaseManager.getAllCalendars(guildId).block()) {
                if (!"primary".equalsIgnoreCase(calData.getCalendarAddress())
                    && CalendarUtils.calendarExists(calData, settings).block()) {
                    final com.google.api.services.calendar.model.Calendar cal = service.calendars()
                        .get(calData.getCalendarAddress())
                        .execute();

                    final JSONObject jCal = new JSONObject();

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

            final JSONObject body = new JSONObject();
            body.put("calendars", jCals);

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_SUCCESS);
            return body.toString();
        } catch (final JSONException e) {
            e.printStackTrace();

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_BAD_REQUEST);
            return JsonUtils.getJsonResponseMessage("Bad Request");
        } catch (final Exception e) {
            LogFeed.log(LogObject.forException("[API-v2]", "list calendars err", e, this.getClass()));

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}
