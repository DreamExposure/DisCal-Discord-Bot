package org.dreamexposure.discal.server.api.endpoints.v2.guild;

import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.JsonUtil;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.server.DisCalServer;
import org.dreamexposure.discal.server.utils.Authentication;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ImmutableNicknameModifyData;
import discord4j.rest.entity.RestGuild;

@RestController
@RequestMapping("/v2/guild/")
public class UpdateWebGuildEndpoint {
    @PostMapping(value = "/update", produces = "application/json")
    public String getSettings(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String requestBody) {
        //Authenticate...
        final AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.getSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return JsonUtil.INSTANCE.encodeToString(AuthenticationState.class, authState);
        }

        //Okay, now handle actual request.
        try {
            final JSONObject jsonMain = new JSONObject(requestBody);
            final Snowflake guildId = Snowflake.of(jsonMain.getString("guild_id"));

            final RestGuild g = DisCalServer.getClient().getGuildById(guildId);

            if (g != null) {
                //Handle the changes now that we have confirmed the guild exists..

                //Right now its just the nickname, but more may be added eventually
                if (jsonMain.has("bot_nick")) {
                    g.modifyOwnNickname(ImmutableNicknameModifyData
                        .of(Optional.ofNullable(jsonMain.getString("bot_nick")))
                    ).subscribe();
                }

                response.setContentType("application/json");
                response.setStatus(GlobalConst.STATUS_SUCCESS);
                return JsonUtils.getJsonResponseMessage("Successfully updated guild!");
            }
            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_NOT_FOUND);
            return JsonUtils.getJsonResponseMessage("Guild not connected to DisCal");
        } catch (final JSONException e) {
            e.printStackTrace();

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_BAD_REQUEST);
            return JsonUtils.getJsonResponseMessage("Bad Request");
        } catch (final Exception e) {
            LogFeed.log(LogObject
                .forException("[API-v2]", "get guild settings err", e, this.getClass()));

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}
