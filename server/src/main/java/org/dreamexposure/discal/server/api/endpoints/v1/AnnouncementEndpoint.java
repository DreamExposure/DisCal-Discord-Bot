package org.dreamexposure.discal.server.api.endpoints.v1;

import discord4j.core.object.util.Snowflake;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.server.utils.Authentication;
import org.dreamexposure.discal.server.utils.ResponseUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"ThrowableNotThrown", "Duplicates"})
@RestController
@RequestMapping("/api/v1/announcement")
public class AnnouncementEndpoint {

	@PostMapping(value = "/get", produces = "application/json")
	public static String getAnnouncement(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		}

		//Okay, now handle actual request.
		try {
			JSONObject jsonMain = new JSONObject(requestBody);
			Long guildId = jsonMain.getLong("guild_id");
			String announcementId = jsonMain.getString("id");

			Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(announcementId), Snowflake.of(guildId));

			if (a != null) {

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

				response.setContentType("application/json");
				response.setStatus(200);

				return body.toString();
			} else {
				response.setContentType("application/json");
				response.setStatus(200);
				return ResponseUtils.getJsonResponseMessage("Announcement not found.");
			}
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return ResponseUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal get announcement error", e, AnnouncementEndpoint.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/create", produces = "application/json")
	public static String createAnnouncement(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		}

		//Okay, now handle actual request.
		try {
			JSONObject jsonMain = new JSONObject(requestBody);
			Long guildId = jsonMain.getLong("guild_id");

			Announcement a = new Announcement(Snowflake.of(guildId));

			JSONObject body = new JSONObject(requestBody);
			a.setAnnouncementChannelId(body.getString("channel"));
			a.setAnnouncementType(AnnouncementType.fromValue(body.getString("type")));

			if (a.getAnnouncementType().equals(AnnouncementType.COLOR))
				a.setEventColor(EventColor.fromNameOrHexOrID(body.getString("event_color")));

			if (a.getAnnouncementType().equals(AnnouncementType.RECUR) || a.getAnnouncementType().equals(AnnouncementType.SPECIFIC))
				a.setEventId(body.getString("event_id"));


			a.setHoursBefore(body.getInt("hours"));
			a.setMinutesBefore(body.getInt("minutes"));
			a.setInfo(body.getString("info"));

			if (DatabaseManager.getManager().updateAnnouncement(a)) {
				response.setContentType("application/json");
				response.setStatus(200);

				JSONObject responseBody = new JSONObject();
				responseBody.put("Message", "Successfully created announcement");
				responseBody.put("id", a.getAnnouncementId().toString());

				return responseBody.toString();
			} else {
				response.setContentType("application/json");
				response.setStatus(500);
				return ResponseUtils.getJsonResponseMessage("Internal Server Error");
			}
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return ResponseUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal create announcement error", e, AnnouncementEndpoint.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/update", produces = "application/json")
	public static String updateAnnouncement(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		}

		//Okay, now handle actual request.
		try {
			JSONObject jsonMain = new JSONObject(requestBody);
			Long guildId = jsonMain.getLong("guild_id");
			String announcementId = jsonMain.getString("id");

			Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(announcementId), Snowflake.of(guildId));

			if (a != null) {

				JSONObject body = new JSONObject(requestBody);

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
				if (body.has("info_only"))
					a.setInfoOnly(body.getBoolean("info_only"));

				if (DatabaseManager.getManager().updateAnnouncement(a)) {
					response.setContentType("application/json");
					response.setStatus(200);
					return ResponseUtils.getJsonResponseMessage("Successfully updated announcement");
				} else {
					response.setContentType("application/json");
					response.setStatus(500);
					return ResponseUtils.getJsonResponseMessage("Internal Server Error");
				}
			} else {
				response.setContentType("application/json");
				response.setStatus(404);
				return ResponseUtils.getJsonResponseMessage("Announcement not found");
			}
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return ResponseUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal update announcement error", e, AnnouncementEndpoint.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/delete", produces = "application/json")
	public static String deleteAnnouncement(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		}

		//Okay, now handle actual request.
		try {
			JSONObject jsonMain = new JSONObject(requestBody);
			long guildId = jsonMain.getLong("guild_id");
			String announcementId = jsonMain.getString("id");

			if (DatabaseManager.getManager().getAnnouncement(UUID.fromString(announcementId), Snowflake.of(guildId)) != null) {
				if (DatabaseManager.getManager().deleteAnnouncement(announcementId)) {
					response.setContentType("application/json");
					response.setStatus(200);
					return ResponseUtils.getJsonResponseMessage("Successfully deleted announcement");
				} else {
					response.setContentType("application/json");
					response.setStatus(500);
					return ResponseUtils.getJsonResponseMessage("Internal Server Error");
				}
			} else {
				response.setContentType("application/json");
				response.setStatus(404);
				return ResponseUtils.getJsonResponseMessage("Announcement not found");
			}
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return ResponseUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal delete announcement error", e, AnnouncementEndpoint.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/list", produces = "application/json")
	public static String listAnnouncements(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		}

		//Okay, now handle actual request.
		try {
			JSONObject jsonMain = new JSONObject(requestBody);
			long guildId = jsonMain.getLong("guild_id");

			int amount = jsonMain.getInt("amount");

			ArrayList<JSONObject> announcements = new ArrayList<>();
			if (amount == -1) {
				for (Announcement a : DatabaseManager.getManager().getAnnouncements(Snowflake.of(guildId))) {
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
				for (Announcement a : DatabaseManager.getManager().getAnnouncements(Snowflake.of(guildId))) {
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

			response.setContentType("application/json");
			response.setStatus(200);
			return body.toString();
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return ResponseUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB-API] Internal list announcements error", e, AnnouncementEndpoint.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}