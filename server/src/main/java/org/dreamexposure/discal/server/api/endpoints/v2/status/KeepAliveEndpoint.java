package org.dreamexposure.discal.server.api.endpoints.v2.status;

import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.network.discal.ConnectedClient;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/v2/status")
public class KeepAliveEndpoint {

    @PostMapping(value = "/keep-alive", produces = "application/json")
    public String keepAlive(final HttpServletRequest request, final HttpServletResponse response, @RequestBody final String rBody) {
        //Authenticate...
        final AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.getSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return JsonUtil.INSTANCE.encodeToString(AuthenticationState.class, authState);
        } else if (!authState.getFromDiscalNetwork()) {
            response.setStatus(GlobalConst.STATUS_AUTHORIZATION_DENIED);
            response.setContentType("application/json");
            return JsonUtils.getJsonResponseMessage("Only official DisCal clients can use this Endpoint");
        }

        //Okay, now handle actual request.
        try {
            final JSONObject body = new JSONObject(rBody);
            final int index = body.getInt("index");
            if (DisCalServer.getNetworkInfo().doesClientExist(index)) {
                //In network, update info...
                ConnectedClient cc = DisCalServer.getNetworkInfo().getClient(index);
                String oldPid = cc.getPid();

                cc = cc.copy(
                    cc.getClientIndex(),
                    body.optString("version", "Unknown"),
                    body.optString("d4j_version", "Unknown"),
                    body.getInt("guilds"),
                    System.currentTimeMillis(),
                    body.getString("uptime"),
                    body.getDouble("memory"),
                    body.getString("ip"),
                    body.getInt("port"),
                    body.getString("pid")
                );

                if (!oldPid.equals(body.getString("pid"))) {
                    LogFeed.log(LogObject.forStatus("Client pid changed", "Shard index: " + cc.getClientIndex()));
                }

                DisCalServer.getNetworkInfo().updateClient(cc);
            } else {
                //Not in network, add info...
                final ConnectedClient cc = new ConnectedClient(
                    index,
                    body.optString("version", "Unknown"),
                    body.optString("d4j_version", "Unknown"),
                    body.getInt("guilds"),
                    System.currentTimeMillis(),
                    body.getString("uptime"),
                    body.getDouble("memory"),
                    body.getString("ip"),
                    body.getInt("port"),
                    body.getString("pid")
                );

                DisCalServer.getNetworkInfo().addClient(cc);
            }

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_SUCCESS);
            return JsonUtils.getJsonResponseMessage("Success!");
        } catch (final JSONException e) {
            e.printStackTrace();

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_BAD_REQUEST);
            return JsonUtils.getJsonResponseMessage("Bad Request");
        } catch (final Exception e) {
            LogFeed.log(LogObject.forException("[API-v2]", "keep alive err", e, this.getClass()));

            response.setContentType("application/json");
            response.setStatus(GlobalConst.STATUS_INTERNAL_ERROR);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}
