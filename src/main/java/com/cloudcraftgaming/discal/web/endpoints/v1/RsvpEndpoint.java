package com.cloudcraftgaming.discal.web.endpoints.v1;

import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.object.event.RsvpData;
import com.cloudcraftgaming.discal.api.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.web.utils.ResponseUtils;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import static spark.Spark.halt;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("ThrowableNotThrown")
public class RsvpEndpoint {
	public static String getRsvp(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			String guildId = jsonMain.getString("GUILD_ID");
			String eventId = jsonMain.getString("EVENT_ID");

			RsvpData rsvp = DatabaseManager.getManager().getRsvpData(Long.valueOf(guildId), eventId);

			JSONObject body = new JSONObject();
			body.put("GUILD_ID", guildId);
			body.put("EVENT_ID", eventId);
			body.put("EVENT_END", rsvp.getEventEnd());
			body.put("ON_TIME", rsvp.getGoingOnTime());
			body.put("LATE", rsvp.getGoingLate());
			body.put("UNDECIDED", rsvp.getUndecided());
			body.put("NOT_GOING", rsvp.getNotGoing());

			response.type("application/json");
			response.status(200);
			response.body(body.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			ExceptionHandler.sendException(null, "[WEB-API] Internal get RSVP data error", e, RsvpEndpoint.class);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String updateRsvp(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			String guildId = jsonMain.getString("GUILD_ID");
			String eventId = jsonMain.getString("EVENT_ID");

			RsvpData rsvp = DatabaseManager.getManager().getRsvpData(Long.valueOf(guildId), eventId);

			if (jsonMain.has("ON_TIME")) {
				rsvp.getGoingOnTime().clear();
				for (int i = 0; i < jsonMain.getJSONArray("ON_TIME").length(); i++) {
					String s = jsonMain.getJSONArray("ON_TIME").getString(i);
					rsvp.getGoingOnTime().add(s);
				}
			}

			if (jsonMain.has("LATE")) {
				rsvp.getGoingOnTime().clear();
				for (int i = 0; i < jsonMain.getJSONArray("LATE").length(); i++) {
					String s = jsonMain.getJSONArray("LATE").getString(i);
					rsvp.getGoingLate().add(s);
				}
			}

			if (jsonMain.has("UNDECIDED")) {
				rsvp.getGoingOnTime().clear();
				for (int i = 0; i < jsonMain.getJSONArray("UNDECIDED").length(); i++) {
					String s = jsonMain.getJSONArray("UNDECIDED").getString(i);
					rsvp.getUndecided().add(s);
				}
			}

			if (jsonMain.has("NOT_GOING")) {
				rsvp.getGoingOnTime().clear();
				for (int i = 0; i < jsonMain.getJSONArray("NOT_GOING").length(); i++) {
					String s = jsonMain.getJSONArray("NOT_GOING").getString(i);
					rsvp.getNotGoing().add(s);
				}
			}

			if (DatabaseManager.getManager().updateRsvpData(rsvp)) {
				response.type("application/json");
				response.status(200);
				JSONObject body = new JSONObject();
				body.put("GUILD_ID", guildId);
				body.put("EVENT_ID", eventId);
				body.put("MESSAGE", "Successfully updated RSVP Data");
				response.body(body.toString());
			} else {
				response.type("application/json");
				response.status(500);
				response.body(ResponseUtils.getJsonResponseMessage("Failed to update RSVP data"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			ExceptionHandler.sendException(null, "[WEB-API] Internal update RSVP data error", e, RsvpEndpoint.class);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}
}