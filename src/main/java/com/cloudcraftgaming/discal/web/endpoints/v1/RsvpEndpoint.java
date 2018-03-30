package com.cloudcraftgaming.discal.web.endpoints.v1;

import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.object.event.RsvpData;
import com.cloudcraftgaming.discal.logger.Logger;
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
			long guildId = jsonMain.getLong("guild_id");
			String eventId = jsonMain.getString("id");

			RsvpData rsvp = DatabaseManager.getManager().getRsvpData(guildId, eventId);

			JSONObject body = new JSONObject();
			body.put("on_time", rsvp.getGoingOnTime());
			body.put("late", rsvp.getGoingLate());
			body.put("undecided", rsvp.getUndecided());
			body.put("not_going", rsvp.getNotGoing());

			response.type("application/json");
			response.status(200);
			response.body(body.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal get RSVP data error", e, RsvpEndpoint.class, true);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String updateRsvp(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			long guildId = jsonMain.getLong("guild_id");
			String eventId = jsonMain.getString("id");

			RsvpData rsvp = DatabaseManager.getManager().getRsvpData(guildId, eventId);

			if (jsonMain.has("on_time")) {
				rsvp.getGoingOnTime().clear();
				for (int i = 0; i < jsonMain.getJSONArray("on_time").length(); i++) {
					String s = jsonMain.getJSONArray("on_time").getString(i);
					rsvp.getGoingOnTime().add(s);
				}
			}

			if (jsonMain.has("late")) {
				rsvp.getGoingOnTime().clear();
				for (int i = 0; i < jsonMain.getJSONArray("late").length(); i++) {
					String s = jsonMain.getJSONArray("late").getString(i);
					rsvp.getGoingLate().add(s);
				}
			}

			if (jsonMain.has("undecided")) {
				rsvp.getGoingOnTime().clear();
				for (int i = 0; i < jsonMain.getJSONArray("undecided").length(); i++) {
					String s = jsonMain.getJSONArray("undecided").getString(i);
					rsvp.getUndecided().add(s);
				}
			}

			if (jsonMain.has("not_going")) {
				rsvp.getGoingOnTime().clear();
				for (int i = 0; i < jsonMain.getJSONArray("not_going").length(); i++) {
					String s = jsonMain.getJSONArray("not_going").getString(i);
					rsvp.getNotGoing().add(s);
				}
			}

			if (DatabaseManager.getManager().updateRsvpData(rsvp)) {
				response.type("application/json");
				response.status(200);
				response.body(ResponseUtils.getJsonResponseMessage("Successfully updated RSVP data"));
			} else {
				response.type("application/json");
				response.status(500);
				response.body(ResponseUtils.getJsonResponseMessage("Failed to update RSVP data"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal update RSVP data error", e, RsvpEndpoint.class, true);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}
}