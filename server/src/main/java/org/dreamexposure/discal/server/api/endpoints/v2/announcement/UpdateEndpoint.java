package org.dreamexposure.discal.server.api.endpoints.v2.announcement;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.server.utils.Authentication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.core.object.util.Snowflake;

@RestController
@RequestMapping("/v2/announcement")
public class UpdateEndpoint {
	@PostMapping(value = "/update", produces = "application/json")
	public String updateAnnouncement(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
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
			JSONObject body = new JSONObject(requestBody);
			Snowflake guildId = Snowflake.of(body.getLong("guild_id"));
			UUID announcementId = UUID.fromString(body.getString("announcement_id"));

			Announcement a = DatabaseManager.getManager().getAnnouncement(announcementId, guildId);

			if (a != null) {
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
				if (body.has("info_only"))
					a.setInfoOnly(body.getBoolean("info_only"));
				if (body.has("enabled"))
					a.setEnabled(body.getBoolean("enabled"));

				//Handle subscribers....
				if (body.has("remove_subscriber_roles")) {
					JSONArray jRemoveRoles = body.getJSONArray("remove_subscriber_roles");
					for (int i = 0; i < jRemoveRoles.length(); i++)
						a.getSubscriberRoleIds().remove(jRemoveRoles.getString(i));
				}
				if (body.has("remove_subscriber_users")) {
					JSONArray jRemoveUsers = body.getJSONArray("remove_subscriber_users");
					for (int i = 0; i < jRemoveUsers.length(); i++)
						a.getSubscriberUserIds().remove(jRemoveUsers.getString(i));
				}

				if (body.has("add_subscriber_roles")) {
					JSONArray rAddRoles = body.getJSONArray("add_subscriber_roles");
					for (int i = 0; i < rAddRoles.length(); i++)
						a.getSubscriberRoleIds().add(rAddRoles.getString(i));
				}
				if (body.has("add_subscriber_users")) {
					JSONArray rAddUsers = body.getJSONArray("add_subscriber_users");
					for (int i = 0; i < rAddUsers.length(); i++)
						a.getSubscriberUserIds().add(rAddUsers.getString(i));
				}

				//Update in database now.
				if (DatabaseManager.getManager().updateAnnouncement(a)) {
					response.setContentType("application/json");
					response.setStatus(200);
					return JsonUtils.getJsonResponseMessage("Announcement successfully updated");
				}
			} else {
				response.setContentType("application/json");
				response.setStatus(404);
				return JsonUtils.getJsonResponseMessage("Announcement not Found");
			}

			response.setContentType("application/json");
			response.setStatus(500);
			return JsonUtils.getJsonResponseMessage("Internal Server Error");
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return JsonUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[API-v2] Internal update announcement error", e, true, this.getClass());

			response.setContentType("application/json");
			response.setStatus(500);
			return JsonUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}