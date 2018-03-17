package com.cloudcraftgaming.discal.web.endpoints.v1;

import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.object.calendar.CalendarData;
import com.cloudcraftgaming.discal.api.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.web.utils.ResponseUtils;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.util.ArrayList;

import static spark.Spark.halt;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("ThrowableNotThrown")
public class CalendarEndpoint {
	public static String getCalendar(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			Long guildId = jsonMain.getLong("guild_id");
			Integer calNumber = jsonMain.getInt("number");

			CalendarData calendar = DatabaseManager.getManager().getCalendar(guildId, calNumber);

			if (!calendar.getCalendarAddress().equalsIgnoreCase("primary")) {

				JSONObject body = new JSONObject();
				body.put("external", calendar.isExternal());
				body.put("id", calendar.getCalendarId());
				body.put("address", calendar.getCalendarAddress());

				response.type("application/json");
				response.status(200);
				response.body(body.toString());
			} else {
				response.type("application/json");
				response.status(404);
				response.body(ResponseUtils.getJsonResponseMessage("Calendar not found"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			ExceptionHandler.sendException(null, "[WEB-API] Internal get calendar error", e, CalendarEndpoint.class);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String listCalendars(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			Long guildId = jsonMain.getLong("guild_id");

			ArrayList<JSONObject> cals = new ArrayList<>();
			for (CalendarData cal : DatabaseManager.getManager().getAllCalendars(guildId)) {
				if (!cal.getCalendarAddress().equalsIgnoreCase("primary")) {
					JSONObject body = new JSONObject();
					body.put("number", cal.getCalendarNumber());
					body.put("external", cal.isExternal());
					body.put("id", cal.getCalendarId());
					body.put("address", cal.getCalendarAddress());

					cals.add(body);
				}
			}

			JSONObject body = new JSONObject();
			body.put("count", cals.size());
			body.put("calendars", cals);

			response.type("application/json");
			response.status(200);
			response.body(body.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			ExceptionHandler.sendException(null, "[WEB-API] Internal list calendars error", e, CalendarEndpoint.class);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}
}