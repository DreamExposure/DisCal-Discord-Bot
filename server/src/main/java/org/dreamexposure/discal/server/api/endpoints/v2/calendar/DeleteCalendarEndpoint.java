package org.dreamexposure.discal.server.api.endpoints.v2.calendar;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.CalendarUtils;
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

import discord4j.rest.util.Snowflake;

@RestController
@RequestMapping("/v2/calendar")
public class DeleteCalendarEndpoint {
    @PostMapping(value = "/delete", produces = "application/json")
    public String deleteCalendar(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
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
            JSONObject jsonMain = new JSONObject(requestBody);
            Snowflake guildId = Snowflake.of(jsonMain.getString("guild_id"));
            int calNumber = jsonMain.getInt("calendar_number");

            GuildSettings settings = DatabaseManager.getSettings(guildId).block();
            CalendarData calendar = DatabaseManager.getCalendar(guildId, calNumber).block();

            if (!calendar.getCalendarAddress().equalsIgnoreCase("primary")) {
                if (CalendarUtils.calendarExists(calendar, settings)) {
                    if (CalendarUtils.deleteCalendar(calendar, settings)) {
                        response.setContentType("application/json");
                        response.setStatus(200);
                        return JsonUtils.getJsonResponseMessage("Calendar successfully deleted");
                    }
                    response.setContentType("application/json");
                    response.setStatus(500);
                    return JsonUtils.getJsonResponseMessage("Internal Server Error");
                }
            }
            response.setContentType("application/json");
            response.setStatus(404);
            return JsonUtils.getJsonResponseMessage("Calendar not found");
        } catch (JSONException e) {
            e.printStackTrace();

            response.setContentType("application/json");
            response.setStatus(400);
            return JsonUtils.getJsonResponseMessage("Bad Request");
        } catch (Exception e) {
            LogFeed.log(LogObject.forException("[API-v2]", "Delete cal err", e, this.getClass()));

            response.setContentType("application/json");
            response.setStatus(500);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}

