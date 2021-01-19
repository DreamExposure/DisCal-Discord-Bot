package org.dreamexposure.discal.server.api.endpoints.v1;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.common.util.Snowflake;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("Duplicates")
@RestController
@RequestMapping("/api/v1/calendar")
public class CalendarEndpoint {

    @PostMapping(value = "/get", produces = "application/json")
    public static String getCalendar(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String requestBody) {
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
            final long guildId = jsonMain.getLong("guild_id");
            final int calNumber = jsonMain.getInt("number");

            final CalendarData calendar = DatabaseManager.getCalendar(Snowflake.of(guildId), calNumber).block();

            if (!"primary".equalsIgnoreCase(calendar.getCalendarAddress())) {

                final JSONObject body = new JSONObject();
                body.put("external", calendar.getExternal());
                body.put("id", calendar.getCalendarId());
                body.put("address", calendar.getCalendarAddress());

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
            LogFeed.log(LogObject
                .forException("[WEB-API-v1]", "get calendar err", e, CalendarEndpoint.class));

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }

    @PostMapping(value = "/list", produces = "application/json")
    public static String listCalendars(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String requestBody) {
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
            final long guildId = jsonMain.getLong("guild_id");

            final List<JSONObject> cals = new ArrayList<>();
            for (final CalendarData cal : DatabaseManager.getAllCalendars(Snowflake.of(guildId)).block()) {
                if (!"primary".equalsIgnoreCase(cal.getCalendarAddress())) {
                    final JSONObject body = new JSONObject();
                    body.put("number", cal.getCalendarNumber());
                    body.put("external", cal.getExternal());
                    body.put("id", cal.getCalendarId());
                    body.put("address", cal.getCalendarAddress());

                    cals.add(body);
                }
            }

            final JSONObject body = new JSONObject();
            body.put("count", cals.size());
            body.put("calendars", cals);

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_SUCCESS);
            return body.toString();
        } catch (final JSONException e) {
            e.printStackTrace();

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_BAD_REQUEST);
            return JsonUtils.getJsonResponseMessage("Bad Request");
        } catch (final Exception e) {
            LogFeed.log(LogObject
                .forException("[WEB-API-v1]", "List calendars err", e, CalendarEndpoint.class));

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}
