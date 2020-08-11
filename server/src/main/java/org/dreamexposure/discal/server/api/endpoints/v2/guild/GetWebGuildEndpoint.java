package org.dreamexposure.discal.server.api.endpoints.v2.guild;

import org.dreamexposure.discal.core.exceptions.BotNotInGuildException;
import org.dreamexposure.discal.core.file.ReadFile;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.object.web.WebGuild;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.core.utils.PermissionChecker;
import org.dreamexposure.discal.server.DisCalServer;
import org.dreamexposure.discal.server.utils.Authentication;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.common.util.Snowflake;
import discord4j.rest.entity.RestGuild;
import discord4j.rest.entity.RestMember;

@RestController
@RequestMapping("/v2/guild/")
public class GetWebGuildEndpoint {
    @PostMapping(value = "/get", produces = "application/json")
    public String getSettings(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String requestBody) {
        //Authenticate...
        final AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.isSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return authState.toJson();
        }
        //Okay, now handle actual request.
        try {
            final JSONObject jsonMain = new JSONObject(requestBody);
            final Snowflake guildId = Snowflake.of(jsonMain.getString("guild_id"));
            final Snowflake userId = Snowflake.of(jsonMain.getString("user_id"));

            final RestGuild g = DisCalServer.getClient().getGuildById(guildId);

            final WebGuild wg = WebGuild.fromGuild(g);

            final RestMember m = g.member(userId);

            if (m != null) { //Assume false if we can't get the user...
                //TODO: Check logs for full stack trace here, something weird is up with it
                wg.setManageServer(PermissionChecker.hasManageServerRole(m, g).blockOptional().orElse(false));
                wg.setDiscalRole(PermissionChecker.hasSufficientRole(m, wg.getSettings()).blockOptional().orElse(false));
            }

            //Add available langs so that editing of langs can be done on the website
            //noinspection unchecked
            for (final String l : new ArrayList<String>(ReadFile.readAllLangFiles().block().keySet())) {
                wg.getAvailableLangs().add(l);
            }

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_SUCCESS);
            return wg.toJson(!authState.isFromDiscalNetwork()).toString();
        } catch (final BotNotInGuildException e) {
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
