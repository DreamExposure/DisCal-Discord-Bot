package org.dreamexposure.discal.server.api.endpoints.v2.calendar;

import com.google.api.services.calendar.Calendar;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.CalendarUtils;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.JsonUtil;
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

import discord4j.common.util.Snowflake;

@RestController
@RequestMapping("/v2/calendar")
public class GetCalendarEndpoint {
    @PostMapping(value = "/get", produces = "application/json")
    public String getCalendar(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String requestBody) {
        //Authenticate...
        final AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.getSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return JsonUtil.INSTANCE.encodeToString(AuthenticationState.class, authState);
        }

        //Okay, now handle actual request.
        try {
            final JSONObject jsonMain = new JSONObject(requestBody);
            final Snowflake guildId = Snowflake.of(jsonMain.getString("guild_id"));
            final int calNumber = jsonMain.getInt("calendar_number");

            final CalendarData calData = DatabaseManager.getCalendar(guildId, calNumber).block();

            if (!"primary".equalsIgnoreCase(calData.getCalendarAddress())
                && CalendarUtils.calendarExists(calData).block()) {
                final Calendar service = CalendarAuth.getCalendarService(calData).block();
                final com.google.api.services.calendar.model.Calendar cal = service.calendars()
                    .get(calData.getCalendarAddress())
                    .execute();

                final JSONObject body = new JSONObject();
                body.put("calendar_address", calData.getCalendarAddress());
                body.put("calendar_id", calData.getCalendarId());
                body.put("calendar_number", calData.getCalendarNumber());
                body.put("external", calData.getExternal());
                body.put("summary", cal.getSummary());
                body.put("description", cal.getDescription());
                body.put("timezone", cal.getTimeZone());

                response.setContentType("application/json");
                response.setStatus(GlobalConst.STATUS_SUCCESS);
                return body.toString();
            } else {
                response.setContentType("application/json");
                response.setStatus(GlobalConst.STATUS_NOT_FOUND);
                return JsonUtils.getJsonResponseMessage("Calendar not found");
            }
        } catch (final JSONException e) {
            e.printStackTrace();

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_BAD_REQUEST);
            return JsonUtils.getJsonResponseMessage("Bad Request");
        } catch (final Exception e) {
            LogFeed.log(LogObject.forException("[API-v2]", "get calendar err", e, this.getClass()));

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}
