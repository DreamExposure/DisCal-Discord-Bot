package org.dreamexposure.discal.server.api.endpoints.v2.announcement;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementModifier;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.JsonUtil;
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

import discord4j.common.util.Snowflake;

@RestController
@RequestMapping("/v2/announcement")
public class UpdateAnnouncementEndpoint {
    @PostMapping(value = "/update", produces = "application/json")
    public String updateAnnouncement(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
        //Authenticate...
        final AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.getSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return JsonUtil.INSTANCE.encodeToString(AuthenticationState.class, authState);
        } else if (authState.getReadOnly()) {
            response.setStatus(GlobalConst.STATUS_AUTHORIZATION_DENIED);
            response.setContentType("application/json");
            return JsonUtils.getJsonResponseMessage("Read-Only key not Allowed");
        }

        //Okay, now handle actual request.
        try {
            final JSONObject body = new JSONObject(requestBody);
            final Snowflake guildId = Snowflake.of(body.getString("guild_id"));
            final UUID announcementId = UUID.fromString(body.getString("announcement_id"));

            final Announcement a = DatabaseManager.getAnnouncement(announcementId, guildId).block();

            if (a != null) {
                if (body.has("channel"))
                    a.setAnnouncementChannelId(body.getString("channel"));
                if (body.has("event_id"))
                    a.setEventId(body.getString("event_id"));
                if (body.has("event_color"))
                    a.setEventColor(EventColor.Companion.fromNameOrHexOrId(body.getString("event_color")));
                if (body.has("type"))
                    a.setType(AnnouncementType.Companion.fromValue(body.getString("type")));
                if (body.has("modifier"))
                    a.setModifier(AnnouncementModifier.Companion.fromValue(body.getString("modifier")));
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
                if (body.has("publish"))
                    a.setPublish(body.getBoolean("publish"));

                //Handle subscribers....
                if (body.has("remove_subscriber_roles")) {
                    final JSONArray jRemoveRoles = body.getJSONArray("remove_subscriber_roles");
                    for (int i = 0; i < jRemoveRoles.length(); i++)
                        a.getSubscriberRoleIds().remove(jRemoveRoles.getString(i));
                }
                if (body.has("remove_subscriber_users")) {
                    final JSONArray jRemoveUsers = body.getJSONArray("remove_subscriber_users");
                    for (int i = 0; i < jRemoveUsers.length(); i++)
                        a.getSubscriberUserIds().remove(jRemoveUsers.getString(i));
                }

                if (body.has("add_subscriber_roles")) {
                    final JSONArray rAddRoles = body.getJSONArray("add_subscriber_roles");
                    for (int i = 0; i < rAddRoles.length(); i++)
                        a.getSubscriberRoleIds().add(rAddRoles.getString(i));
                }
                if (body.has("add_subscriber_users")) {
                    final JSONArray rAddUsers = body.getJSONArray("add_subscriber_users");
                    for (int i = 0; i < rAddUsers.length(); i++)
                        a.getSubscriberUserIds().add(rAddUsers.getString(i));
                }

                //Update in database now.
                if (DatabaseManager.updateAnnouncement(a).block()) {
                    response.setContentType("application/json");
                    response.setStatus(GlobalConst.STATUS_SUCCESS);
                    return JsonUtils.getJsonResponseMessage("Announcement successfully updated");
                }
            } else {
                response.setContentType("application/json");
                response.setStatus(GlobalConst.STATUS_NOT_FOUND);
                return JsonUtils.getJsonResponseMessage("Announcement not Found");
            }

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        } catch (final JSONException e) {
            e.printStackTrace();

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_BAD_REQUEST);
            return JsonUtils.getJsonResponseMessage("Bad Request");
        } catch (final Exception e) {
            LogFeed.log(LogObject
                .forException("[API-v2]", "update announcement err", e, this.getClass()));

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}
