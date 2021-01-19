package org.dreamexposure.discal.server.api.endpoints.v2.guild.settings;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.network.DisCalRealm;
import org.dreamexposure.discal.core.file.ReadFile;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.GuildUtils;
import org.dreamexposure.discal.core.utils.JsonUtil;
import org.dreamexposure.discal.core.utils.JsonUtils;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;

@RestController
@RequestMapping("/v2/guild/settings")
public class UpdateGuildSettingsEndpoint {
    @PostMapping(value = "/update", produces = "application/json")
    public String updateSettings(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String requestBody) {
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
            final String guildId = body.getString("guild_id");

            final GuildSettings settings = DatabaseManager.getSettings(Snowflake.of(guildId)).block();

            //Handle various things that are allowed to change.
            if (body.has("control_role"))
                settings.setControlRole(body.getString("control_role"));
            if (body.has("discal_channel")) {
                final String id = body.getString("discal_channel");
                if ("0".equalsIgnoreCase(id) || "all".equalsIgnoreCase(id))
                    settings.setDiscalChannel("all");
                else
                    settings.setDiscalChannel(id);
            }
            if (body.has("simple_announcements"))
                settings.setSimpleAnnouncements(body.getBoolean("simple_announcements"));
            if (body.has("lang")) {
                final String lang = body.getString("lang");
                //noinspection unchecked
                if (new ArrayList<String>(ReadFile.readAllLangFiles().block().keySet()).contains(lang.toUpperCase()))
                    settings.setLang(body.getString("lang"));
            }
            if (body.has("prefix"))
                settings.setPrefix(body.getString("prefix"));

            //Allow Official DisCal Shards to change some other things...
            if (authState.getFromDiscalNetwork()) {
                if (body.has("external_calendar"))
                    settings.setUseExternalCalendar(body.getBoolean("external_calendar"));
                if (body.has("patron_guild"))
                    settings.setPatronGuild(body.getBoolean("patron_guild"));
                if (body.has("dev_guild"))
                    settings.setDevGuild(body.getBoolean("dev_guild"));
                if (body.has("branded"))
                    settings.setBranded(body.getBoolean("branded"));
            }

            if (DatabaseManager.updateSettings(settings).block()) {
                response.setContentType("application/json");
                response.setStatus(GlobalConst.STATUS_SUCCESS);

                //Invalidate the cache on the shard this guild is on...
                final Thread thread = new Thread(() -> {
                    try {
                        final JSONObject requestJson = new JSONObject();

                        requestJson.put("realm", DisCalRealm.BOT_INVALIDATE_CACHES.name());

                        final int shardIndex = GuildUtils.findShard(Snowflake.of(guildId));

                        final OkHttpClient client = new OkHttpClient();
                        final okhttp3.RequestBody cacheRequestBody = okhttp3.RequestBody.create(GlobalConst.JSON, requestJson.toString());
                        final Request cacheRequest = new Request.Builder()
                            .url(BotSettings.COM_SUB_DOMAIN.get() + shardIndex + ".discalbot.com/api/v1/com/bot/action/handle")
                            .header("Authorization", BotSettings.BOT_API_TOKEN.get())
                            .post(cacheRequestBody)
                            .build();
                        //If this fails, its not a huge deal, the cache will just be out of date for up to an hour max...
                        client.newCall(cacheRequest).execute();
                    } catch (final Exception e) {
                        LogFeed.log(LogObject
                            .forException("[API-v2]", "cache invalidate fail", e, this.getClass()));
                    }
                });
                thread.setDaemon(true);
                thread.start();

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
                .forException("[API-v2]", "Update guild settings err", e, this.getClass()));

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}
