package org.dreamexposure.discal.server.api.endpoints.v2.rsvp;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.event.RsvpData;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.common.util.Snowflake;

@RestController
@RequestMapping("/v2/rsvp")
public class UpdateRsvpEndpoint {
    @SuppressWarnings("ConstantConditions")
    @PostMapping(value = "/update", produces = "application/json")
    public String updateRsvp(final HttpServletRequest request, final HttpServletResponse response,
                             @RequestBody final String rBody) {
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
            final JSONObject requestBody = new JSONObject(rBody);

            final String guildId = requestBody.getString("guild_id");
            final String eventId = requestBody.getString("event_id");

            final RsvpData rsvp = DatabaseManager.getRsvpData(Snowflake.of(guildId), eventId).block();

            //Handle limit change
            rsvp.setLimit(requestBody.optInt("limit", rsvp.getLimit()));

            //handle removals... (We do this first just in case they are using the limit)
            if (requestBody.has("to_remove")) {
                final JSONObject jToRemove = requestBody.getJSONObject("to_remove");
                if (jToRemove.has("on_time")) {
                    final JSONArray ar = jToRemove.getJSONArray("on_time");
                    for (int i = 0; i < jToRemove.length(); i++)
                        rsvp.getGoingOnTime().remove(ar.getString(i));
                }
                if (jToRemove.has("late")) {
                    final JSONArray ar = jToRemove.getJSONArray("late");
                    for (int i = 0; i < jToRemove.length(); i++)
                        rsvp.getGoingLate().remove(ar.getString(i));
                }
                if (jToRemove.has("not_going")) {
                    final JSONArray ar = jToRemove.getJSONArray("not_going");
                    for (int i = 0; i < jToRemove.length(); i++)
                        rsvp.getNotGoing().remove(ar.getString(i));
                }
                if (jToRemove.has("undecided")) {
                    final JSONArray ar = jToRemove.getJSONArray("undecided");
                    for (int i = 0; i < jToRemove.length(); i++)
                        rsvp.getUndecided().remove(ar.getString(i));
                }
            }

            //Handle additions...
            if (requestBody.has("to_add")) {
                final JSONObject jToAdd = requestBody.getJSONObject("to_add");
                if (jToAdd.has("on_time")) {
                    final JSONArray ar = jToAdd.getJSONArray("on_time");
                    for (int i = 0; i < jToAdd.length(); i++) {
                        if (rsvp.hasRoom(ar.getString(i))) {
                            rsvp.removeCompletely(ar.getString(i));
                            rsvp.getGoingOnTime().add(ar.getString(i));
                        }
                    }
                }
                if (jToAdd.has("late")) {
                    final JSONArray ar = jToAdd.getJSONArray("late");
                    for (int i = 0; i < jToAdd.length(); i++) {
                        if (rsvp.hasRoom(ar.getString(i))) {
                            rsvp.removeCompletely(ar.getString(i));
                            rsvp.getGoingLate().add(ar.getString(i));
                        }
                    }
                }
                if (jToAdd.has("not_going")) {
                    final JSONArray ar = jToAdd.getJSONArray("not_going");
                    for (int i = 0; i < jToAdd.length(); i++) {
                        rsvp.removeCompletely(ar.getString(i));
                        rsvp.getNotGoing().add(ar.getString(i)); //Limit not needed here
                    }
                }
                if (jToAdd.has("undecided")) {
                    final JSONArray ar = jToAdd.getJSONArray("undecided");
                    for (int i = 0; i < jToAdd.length(); i++) {
                        rsvp.removeCompletely(ar.getString(i));
                        rsvp.getUndecided().add(ar.getString(i)); //Limit also not needed here
                    }
                }
            }

            if (DatabaseManager.updateRsvpData(rsvp).block()) {
                response.setContentType("application/json");
                response.setStatus(GlobalConst.STATUS_SUCCESS);

                return JsonUtils.getJsonResponseMessage("RSVP successfully updated");
            }

            //Shouldn't get here, but if we did, the update probably failed...
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
                .forException("[API-v2]", "Failed to get RSVP", e, this.getClass()));

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);

            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}
