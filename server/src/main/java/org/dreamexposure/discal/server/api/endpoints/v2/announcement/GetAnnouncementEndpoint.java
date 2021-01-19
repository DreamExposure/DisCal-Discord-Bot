package org.dreamexposure.discal.server.api.endpoints.v2.announcement;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.JsonUtil;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.server.utils.Authentication;
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
public class GetAnnouncementEndpoint {
    @PostMapping(value = "/get", produces = "application/json")
    public String getAnnouncement(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String requestBody) {
        //Authenticate...
        final AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.getSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return JsonUtil.INSTANCE.encodeToString(AuthenticationState.class, authState);
        }

        //Okay, now handle actual request.
        try {
            final JSONObject body = new JSONObject(requestBody);
            final Snowflake guildId = Snowflake.of(body.getString("guild_id"));
            final UUID announcementId = UUID.fromString(body.getString("announcement_id"));

            final Announcement a = DatabaseManager.getAnnouncement(announcementId, guildId).block();


            response.setContentType("application/json");
            if (a != null) {
                response.setStatus(GlobalConst.STATUS_SUCCESS);
                return JsonUtil.INSTANCE.encodeToString(Announcement.class, a);
            } else {
                response.setStatus(GlobalConst.STATUS_NOT_FOUND);
                return JsonUtils.getJsonResponseMessage("Announcement not found");
            }
        } catch (final JSONException e) {
            e.printStackTrace();

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_BAD_REQUEST);
            return JsonUtils.getJsonResponseMessage("Bad Request");
        } catch (final Exception e) {
            LogFeed.log(LogObject
                .forException("[API-v2]", "get announcement err", e, this.getClass()));

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}
