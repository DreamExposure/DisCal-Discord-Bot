package com.cloudcraftgaming.discal.web.endpoints.v1;

import com.cloudcraftgaming.discal.api.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.calendar.CalendarData;
import com.cloudcraftgaming.discal.api.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.web.utils.ResponseUtils;
import com.google.api.services.calendar.model.Calendar;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static spark.Spark.halt;

/**
 * Created by Nova Fox on 11/11/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("ThrowableNotThrown")
public class TimeEndpoint {
	public static String getTime(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			Long guildId = jsonMain.getLong("GUILD_ID");
			GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);

			CalendarData data;
			if (jsonMain.has("CALENDAR_NUMBER")) {
				//Get time of specific calendar...
				data = DatabaseManager.getManager().getCalendar(guildId, jsonMain.getInt("CALENDAR_NUMBER"));

			} else {
				//Default to main calendar
				data = DatabaseManager.getManager().getMainCalendar(guildId);
			}

			if (data.getCalendarAddress().equalsIgnoreCase("primary")) {
				//Does not have a calendar.
				response.status(404);
				response.type("application/json");
				response.body(ResponseUtils.getJsonResponseMessage("Calendar not Found"));
			} else {
				Calendar cal;
				if (settings.useExternalCalendar()) {
					cal = CalendarAuth.getCalendarService(settings).calendars().get(data.getCalendarAddress()).execute();
				} else {
					cal = CalendarAuth.getCalendarService().calendars().get(data.getCalendarAddress()).execute();
				}
				LocalDateTime ldt = LocalDateTime.now(ZoneId.of(cal.getTimeZone()));

				//Okay... format and then we can go from there...
				DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss a");
				String thisIsTheCorrectTime = format.format(ldt);

				response.type("application/json");
				response.status(200);

				JSONObject body = new JSONObject();
				body.put("GUILD_ID", guildId);
				body.put("CALENDAR_NUMBER", data.getCalendarNumber());
				body.put("TIME", new JSONObject().put("EPOCH", System.currentTimeMillis()).put("HUMAN_READABLE", thisIsTheCorrectTime));

				response.body(body.toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			ExceptionHandler.sendException(null, "[WEB-API] Failed to get time!", e, TimeEndpoint.class);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}
}