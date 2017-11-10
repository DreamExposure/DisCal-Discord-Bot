package com.cloudcraftgaming.discal.web.endpoints.v1;

import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.enums.announcement.AnnouncementType;
import com.cloudcraftgaming.discal.api.enums.event.EventColor;
import com.cloudcraftgaming.discal.api.object.announcement.Announcement;
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
			String guildId = jsonMain.getString("GUILD_ID");
			String announcementId = jsonMain.getString("ANNOUNCEMENT_ID");

			Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(announcementId), Long.valueOf(guildId));

			if (a != null) {

				response.type("application/json");
				response.status(200);

				JSONObject body = new JSONObject();
				body.put("GUILD_ID", a.getGuildId());
				body.put("ANNOUNCEMENT_ID", a.getAnnouncementId().toString());
				body.put("ANNOUNCEMENT_CHANNEL", a.getAnnouncementChannelId());
				body.put("EVENT_ID", a.getEventId());
				body.put("EVENT_COLOR", a.getEventColor().name());
				body.put("TYPE", a.getAnnouncementType().name());
				body.put("HOURS", a.getHoursBefore());
				body.put("MINUTES", a.getMinutesBefore());
				body.put("INFO", a.getInfo());
				body.put("SUBSCRIBERS_ROLE", a.getSubscriberRoleIds());
				body.put("SUBSCRIBERS_USER", a.getSubscriberUserIds());

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
			e.printStackTrace();
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String createAnnouncement(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			String guildId = jsonMain.getString("GUILD_ID");

			Announcement a = new Announcement(Long.valueOf(guildId));

			JSONObject body = new JSONObject(request.body());
			a.setAnnouncementChannelId(body.getString("ANNOUNCEMENT_CHANNEL"));
			a.setEventId(body.getString("EVENT_ID"));
			a.setEventColor(EventColor.fromNameOrHexOrID(body.getString("EVENT_COLOR")));
			a.setAnnouncementType(AnnouncementType.fromValue(body.getString("TYPE")));
			a.setHoursBefore(body.getInt("HOURS"));
			a.setMinutesBefore(body.getInt("MINUTES"));
			a.setInfo(body.getString("INFO"));

			if (DatabaseManager.getManager().updateAnnouncement(a)) {
				response.type("application/json");
				response.status(200);

				JSONObject responseBody = new JSONObject();
				responseBody.put("Message", "Successfully updated announcement!");
				responseBody.put("ANNOUNCEMENT_ID", a.getAnnouncementId().toString());
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
			e.printStackTrace();
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String updateAnnouncement(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			String guildId = jsonMain.getString("GUILD_ID");
			String announcementId = jsonMain.getString("ANNOUNCEMENT_ID");

			Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(announcementId), Long.valueOf(guildId));

			if (a != null) {

				JSONObject body = new JSONObject(request.body());

				if (body.has("ANNOUNCEMENT_CHANNEL"))
					a.setAnnouncementChannelId(body.getString("ANNOUNCEMENT_CHANNEL"));
				if (body.has("EVENT_ID"))
					a.setEventId(body.getString("EVENT_ID"));
				if (body.has("EVENT_COLOR"))
					a.setEventColor(EventColor.fromNameOrHexOrID(body.getString("EVENT_COLOR")));
				if (body.has("TYPE"))
					a.setAnnouncementType(AnnouncementType.fromValue(body.getString("TYPE")));
				if (body.has("HOURS"))
					a.setHoursBefore(body.getInt("HOURS"));
				if (body.has("MINUTES"))
					a.setMinutesBefore(body.getInt("MINUTES"));
				if (body.has("INFO"))
					a.setInfo(body.getString("INFO"));

				if (DatabaseManager.getManager().updateAnnouncement(a)) {
					response.type("application/json");
					response.status(200);
					response.body(ResponseUtils.getJsonResponseMessage("Announcement successfully updated!"));
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
			e.printStackTrace();
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String deleteAnnouncement(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			String guildId = jsonMain.getString("GUILD_ID");
			String announcementId = jsonMain.getString("ANNOUNCEMENT_ID");

			if (DatabaseManager.getManager().getAnnouncement(UUID.fromString(announcementId), Long.valueOf(guildId)) != null) {
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
			e.printStackTrace();
			halt(500, "Internal Server Error");
		}
		return response.body();
	}

	public static String listAnnouncements(Request request, Response response) {
		try {
			JSONObject jsonMain = new JSONObject(request.body());
			String guildId = jsonMain.getString("GUILD_ID");

			Integer amount = jsonMain.getInt("AMOUNT");

			ArrayList<JSONObject> announcements = new ArrayList<>();
			if (amount == -1) {
				for (Announcement a : DatabaseManager.getManager().getAnnouncements(Long.valueOf(guildId))) {
					JSONObject obj = new JSONObject();
					obj.put("GUILD_ID", a.getGuildId());
					obj.put("ANNOUNCEMENT_ID", a.getAnnouncementId().toString());
					obj.put("ANNOUNCEMENT_CHANNEL", a.getAnnouncementChannelId());
					obj.put("EVENT_ID", a.getEventId());
					obj.put("EVENT_COLOR", a.getEventColor().name());
					obj.put("TYPE", a.getAnnouncementType().name());
					obj.put("HOURS", a.getHoursBefore());
					obj.put("MINUTES", a.getMinutesBefore());
					obj.put("INFO", a.getInfo());
					obj.put("SUBSCRIBERS_ROLE", a.getSubscriberRoleIds());
					obj.put("SUBSCRIBERS_USER", a.getSubscriberUserIds());

					announcements.add(obj);
				}
			} else {
				int i = 0;
				for (Announcement a : DatabaseManager.getManager().getAnnouncements(Long.valueOf(guildId))) {
					if (i < amount) {
						JSONObject obj = new JSONObject();
						obj.put("GUILD_ID", a.getGuildId());
						obj.put("ANNOUNCEMENT_ID", a.getAnnouncementId().toString());
						obj.put("ANNOUNCEMENT_CHANNEL", a.getAnnouncementChannelId());
						obj.put("EVENT_ID", a.getEventId());
						obj.put("EVENT_COLOR", a.getEventColor().name());
						obj.put("TYPE", a.getAnnouncementType().name());
						obj.put("HOURS", a.getHoursBefore());
						obj.put("MINUTES", a.getMinutesBefore());
						obj.put("INFO", a.getInfo());
						obj.put("SUBSCRIBERS_ROLE", a.getSubscriberRoleIds());
						obj.put("SUBSCRIBERS_USER", a.getSubscriberUserIds());

						announcements.add(obj);
						i++;
					} else {
						break;
					}
				}
			}

			JSONObject body = new JSONObject();
			body.put("AMOUNT", announcements.size());
			body.put("ANNOUNCEMENTS", announcements);

			response.type("application/json");
			response.status(200);
			response.body(body.toString());

		} catch (JSONException e) {
			e.printStackTrace();
			halt(400, "Bad Request");
		} catch (Exception e) {
			e.printStackTrace();
			halt(500, "Internal Server Error");
		}
		return response.body();
	}
}