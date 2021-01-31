package org.dreamexposure.discal.server.api.endpoints.v2.event.list;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.JsonUtil;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.core.wrapper.google.EventWrapper;
import org.dreamexposure.discal.server.utils.Authentication;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.common.util.Snowflake;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v2/events/list")
public class ListEventRangeEndpoint {
    @PostMapping(value = "/range", produces = "application/json")
    public String getEventsForRange(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String rBody) {
        //Authenticate...
        final AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.getSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return JsonUtil.INSTANCE.encodeToString(AuthenticationState.class, authState);
        }

        //Okay, now handle actual request.
        try {
            final JSONObject requestBody = new JSONObject(rBody);

            final Snowflake guildId = Snowflake.of(requestBody.getString("guild_id"));
            final int calNumber = requestBody.getInt("calendar_number");
            final long startEpoch = requestBody.getLong("epoch_start");
            final long endEpoch = requestBody.getLong("epoch_end");
            final GuildSettings settings = DatabaseManager.getSettings(guildId).block();

            //okay, lets actually get the range's events.
            final List<JSONObject> events = DatabaseManager.getCalendar(settings.getGuildID(), calNumber)
                .flatMap(calData -> EventWrapper.getEvents(calData, startEpoch, endEpoch))
                .flatMapMany(Flux::fromIterable)
                .map(e -> JsonUtils.convertEventToJson(e, settings))
                .collectList()
                .block();

            final JSONObject body = new JSONObject();
            body.put("events", events);

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
                .forException("[API-v2]", "get events for range err", e, this.getClass()));

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}
