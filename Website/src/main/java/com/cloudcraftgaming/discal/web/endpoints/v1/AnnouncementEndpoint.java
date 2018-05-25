package com.cloudcraftgaming.discal.web.endpoints.v1;

import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.enums.announcement.AnnouncementType;
import com.cloudcraftgaming.discal.api.enums.event.EventColor;
import com.cloudcraftgaming.discal.api.object.announcement.Announcement;
import com.cloudcraftgaming.discal.logger.Logger;
import com.cloudcraftgaming.discal.web.utils.ResponseUtils;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.UUID;

import static spark.Spark.halt;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("ThrowableNotThrown")
public class AnnouncementEndpoint {
	public static String getAnnouncement(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			Long guildId = jsonMain.getLong("guild_id");
			String announcementId = jsonMain.getString("id");

			Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(announcementId), guildId);

			if (a != null) {

				response.type("application/json");
				response.status(200);

				JSONObject body = new JSONObject();
				body.put("channel", a.getAnnouncementChannelId());
				body.put("event_id", a.getEventId());
				body.put("event_color", a.getEventColor().name());
				body.put("type", a.getAnnouncementType().name());
				body.put("hours", a.getHoursBefore());
				body.put("minutes", a.getMinutesBefore());
				body.put("info", a.getInfo());
				body.put("enabled", a.isEnabled());
				body.put("info_only", a.isInfoOnly());
				body.put("subscribers_role", a.getSubscriberRoleIds());
				body.put("subscribers_user", a.getSubscriberUserIds());

				response.body(body.toString());
			} else {
				response.type("application/json");
				response.status(404);
				response.body(ResponseUtils.getJsonResponseMessage("Announcement not found."));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal get announcement error", e, AnnouncementEndpoint.class, true);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String createAnnouncement(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			Long guildId = jsonMain.getLong("guild_id");

			Announcement a = new Announcement(guildId);

			JSONObject body = new JSONObject(request.body());
			a.setAnnouncementChannelId(body.getString("channel"));
			a.setAnnouncementType(AnnouncementType.fromValue(body.getString("type")));

			if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
				a.setEventColor(EventColor.fromNameOrHexOrID(body.getString("event_color")));
			}
			if (a.getAnnouncementType().equals(AnnouncementType.RECUR) || a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
				a.setEventId(body.getString("event_id"));
			}

			a.setHoursBefore(body.getInt("hours"));
			a.setMinutesBefore(body.getInt("minutes"));
			a.setInfo(body.getString("info"));

			if (DatabaseManager.getManager().updateAnnouncement(a)) {
				response.type("application/json");
				response.status(200);

				JSONObject responseBody = new JSONObject();
				responseBody.put("Message", "Successfully created announcement");
				responseBody.put("id", a.getAnnouncementId().toString());
				response.body(responseBody.toString());
			} else {
				response.type("application/json");
				response.status(500);
				response.body(ResponseUtils.getJsonResponseMessage("Internal Server Error"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal create announcement error", e, AnnouncementEndpoint.class, true);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String updateAnnouncement(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			Long guildId = jsonMain.getLong("guild_id");
			String announcementId = jsonMain.getString("id");

			Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(announcementId), guildId);

			if (a != null) {

				JSONObject body = new JSONObject(request.body());

				if (body.has("channel"))
					a.setAnnouncementChannelId(body.getString("channel"));
				if (body.has("event_id"))
					a.setEventId(body.getString("event_id"));
				if (body.has("event_color"))
					a.setEventColor(EventColor.fromNameOrHexOrID(body.getString("event_color")));
				if (body.has("type"))
					a.setAnnouncementType(AnnouncementType.fromValue(body.getString("type")));
				if (body.has("hours"))
					a.setHoursBefore(body.getInt("hours"));
				if (body.has("minutes"))
					a.setMinutesBefore(body.getInt("minutes"));
				if (body.has("info"))
					a.setInfo(body.getString("info"));
				if (body.has("enabled"))
					a.setEnabled(body.getBoolean("enabled"));
				if (body.has("info_only")) {
					a.setInfoOnly(body.getBoolean("info_only"));
				}

				if (DatabaseManager.getManager().updateAnnouncement(a)) {
					response.type("application/json");
					response.status(200);
					response.body(ResponseUtils.getJsonResponseMessage("Successfully updated announcement"));
				} else {
					response.type("application/json");
					response.status(500);
					response.body(ResponseUtils.getJsonResponseMessage("Internal Server Error"));
				}
			} else {
				response.type("application/json");
				response.status(404);
				response.body(ResponseUtils.getJsonResponseMessage("Announcement not found"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal update announcement error", e, AnnouncementEndpoint.class, true);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String deleteAnnouncement(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			long guildId = jsonMain.getLong("guild_id");
			String announcementId = jsonMain.getString("id");

			if (DatabaseManager.getManager().getAnnouncement(UUID.fromString(announcementId), guildId) != null) {
				if (DatabaseManager.getManager().deleteAnnouncement(announcementId)) {
					response.type("application/json");
					response.status(200);
					response.body(ResponseUtils.getJsonResponseMessage("Successfully deleted announcement"));
				} else {
					response.type("application/json");
					response.status(500);
					response.body(ResponseUtils.getJsonResponseMessage("Internal Server Error"));
				}
			} else {
				response.type("application/json");
				response.status(404);
				response.body(ResponseUtils.getJsonResponseMessage("Announcement not found"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal delete announcement error", e, AnnouncementEndpoint.class, true);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String listAnnouncements(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			long guildId = jsonMain.getLong("guild_id");

			int amount = jsonMain.getInt("amount");

			ArrayList<JSONObject> announcements = new ArrayList<>();
			if (amount == -1) {
				for (Announcement a : DatabaseManager.getManager().getAnnouncements(guildId)) {
					JSONObject obj = new JSONObject();
					obj.put("id", a.getAnnouncementId().toString());
					obj.put("channel", a.getAnnouncementChannelId());
					obj.put("event_id", a.getEventId());
					obj.put("event_color", a.getEventColor().name());
					obj.put("type", a.getAnnouncementType().name());
					obj.put("hours", a.getHoursBefore());
					obj.put("minutes", a.getMinutesBefore());
					obj.put("info", a.getInfo());
					obj.put("enabled", a.isEnabled());
					obj.put("info_only", a.isInfoOnly());
					obj.put("subscribers_role", a.getSubscriberRoleIds());
					obj.put("subscribers_user", a.getSubscriberUserIds());

					announcements.add(obj);
				}
			} else {
				int i = 0;
				for (Announcement a : DatabaseManager.getManager().getAnnouncements(guildId)) {
					if (i < amount) {
						JSONObject obj = new JSONObject();
						obj.put("id", a.getAnnouncementId().toString());
						obj.put("channel", a.getAnnouncementChannelId());
						obj.put("event_id", a.getEventId());
						obj.put("event_color", a.getEventColor().name());
						obj.put("type", a.getAnnouncementType().name());
						obj.put("hours", a.getHoursBefore());
						obj.put("minutes", a.getMinutesBefore());
						obj.put("info", a.getInfo());
						obj.put("enabled", a.isEnabled());
						obj.put("info_only", a.isInfoOnly());
						obj.put("subscribers_role", a.getSubscriberRoleIds());
						obj.put("subscribers_user", a.getSubscriberUserIds());

						announcements.add(obj);
						i++;
					} else {
						break;
					}
				}
			}

			JSONObject body = new JSONObject();
			body.put("amount", announcements.size());
			body.put("announcements", announcements);

			response.type("application/json");
			response.status(200);
			response.body(body.toString());

		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal list announcements error", e, AnnouncementEndpoint.class, true);
			halt(500, "Internal Server Error");
		}
		return response.body();
	}
}