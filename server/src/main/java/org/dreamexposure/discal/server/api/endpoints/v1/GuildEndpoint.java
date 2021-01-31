package org.dreamexposure.discal.server.api.endpoints.v1;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.common.util.Snowflake;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("Duplicates")
@RestController
@RequestMapping("/api/v1/guild")
public class GuildEndpoint {

    @PostMapping(value = "/settings/get", produces = "application/json")
    public static String getSettings(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String requestBody) {
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
            final long guildId = jsonMain.getLong("guild_id");

            final GuildSettings settings = DatabaseManager.getSettings(Snowflake.of(guildId)).block();

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_SUCCESS);

            final JSONObject body = new JSONObject();
            body.put("control_role", settings.getControlRole());
            body.put("discal_channel", settings.getDiscalChannel());
            body.put("simple_announcement", settings.getSimpleAnnouncements());
            body.put("lang", settings.getLang());
            body.put("prefix", settings.getPrefix());
            body.put("patron_guild", settings.getPatronGuild());
            body.put("dev_guild", settings.getDevGuild());
            body.put("max_calendars", settings.getMaxCalendars());

            return body.toString();
        } catch (final JSONException e) {
            e.printStackTrace();

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_BAD_REQUEST);
            return JsonUtils.getJsonResponseMessage("Bad Request");
        } catch (final Exception e) {
            LogFeed.log(LogObject
                .forException("[WEB-API-v1]", "get guild settings err", e, GuildEndpoint.class));

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }

    @PostMapping(value = "/settings/update", produces = "application/json")
    public static String updateSettings(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String requestBody) {
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

            final long guildId = body.getLong("guild_id");

            GuildSettings settings = DatabaseManager.getSettings(Snowflake.of(guildId)).block();

            settings = settings.copy(settings.getGuildID(),
                body.optString("control_role", settings.getControlRole()),
                body.optString("discal_channel", settings.getDiscalChannel()),
                body.optBoolean("simple_announcement", settings.getSimpleAnnouncements()),
                body.optString("lang", settings.getLang()),
                body.optString("prefix", settings.getPrefix()),
                settings.getPatronGuild(),
                settings.getDevGuild(),
                settings.getMaxCalendars(),
                settings.getTwelveHour(),
                settings.getBranded());

            if (DatabaseManager.updateSettings(settings).block()) {
                response.setContentType("application/json");
                response.setStatus(GlobalConst.STATUS_SUCCESS);
                return JsonUtils.getJsonResponseMessage("Successfully updated guild settings!");
            } else {
                response.setContentType("application/json");
                response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
                return JsonUtils.getJsonResponseMessage("Internal Server Error");
            }
        } catch (final JSONException e) {
            e.printStackTrace();
            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_BAD_REQUEST);
            return JsonUtils.getJsonResponseMessage("Bad Request");
        } catch (final Exception e) {
            LogFeed.log(LogObject
                .forException("[WEB-API-v1]", "update guild settings err", e, GuildEndpoint.class));
            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }

    @PostMapping(value = "/info/from-user/list", produces = "application/json")
    public static String getUserGuilds(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String requestBody) {
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

            final long userId = jsonMain.getLong("USER_ID");
            final JSONObject body = new JSONObject();

            body.put("Message", "This endpoint is being redone. Sorry!!!!");

            //TODO: Properly handle this as its old and won't work on the SERVER!!!
            /*
            IUser user = DisCalAPI.getAPI().getClient().getUserByID(userId);

            //Find all guilds user is in...
            ArrayList<IGuild> guilds = new ArrayList<>();
            for (IGuild g : DisCalAPI.getAPI().getClient().getGuilds()) {
                if (g.getUserByID(userId) != null)
                    guilds.add(g);
            }

            //Get needed data
            ArrayList<JSONObject> guildData = new ArrayList<>();
            for (IGuild g : guilds) {
                JSONObject d = new JSONObject();
                d.put("GUILD_ID", g.getLongID());
                d.put("IS_OWNER", g.getOwnerLongID() == userId);
                d.put("MANAGE_SERVER", PermissionChecker.hasManageServerRole(g, user));
                d.put("DISCAL_CONTROL", PermissionChecker.hasSufficientRole(g, user));

                guildData.add(d);
            }

            body.put("USER_ID", userId);
            body.put("GUILD_COUNT", guildData.size());
            body.put("GUILDS", guildData);
            */

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_SUCCESS);
            return body.toString();
        } catch (final JSONException e) {
            e.printStackTrace();
            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_BAD_REQUEST);
            return JsonUtils.getJsonResponseMessage("Bad Request");
        } catch (final Exception e) {
            LogFeed.log(LogObject
                .forException("[WEB-API-v1]", "get guilds for user err", e, GuildEndpoint.class));
            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}
