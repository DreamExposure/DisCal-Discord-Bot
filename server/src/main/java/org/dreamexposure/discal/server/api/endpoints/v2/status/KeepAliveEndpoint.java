package org.dreamexposure.discal.server.api.endpoints.v2.status;

import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.network.discal.ConnectedClient;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
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
	public String keepAlive(HttpServletRequest request, HttpServletResponse response, @RequestBody String rBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		} else if (!authState.isFromDiscalNetwork()) {
			response.setStatus(401);
			response.setContentType("application/json");
			return JsonUtils.getJsonResponseMessage("Only official DisCal clients can use this Endpoint");
		}

		//Okay, now handle actual request.
		try {
			JSONObject body = new JSONObject(rBody);
			int index = body.getInt("index");
			if (DisCalServer.getNetworkInfo().clientExists(index)) {
				//In network, update info...
				ConnectedClient cc = DisCalServer.getNetworkInfo().getClient(index);

				cc.setLastKeepAlive(System.currentTimeMillis());
				cc.setConnectedServers(body.getInt("guilds"));
				cc.setMemUsed(body.getDouble("memory"));
				cc.setUptime(body.getString("uptime"));
				cc.setIpForRestart(body.getString("ip"));
				cc.setPortForRestart(body.getInt("port"));

				if (!cc.getPid().equals(body.getString("pid"))) {
					//Was restarted at some point, so we are "re-adding" to network
					cc.setPid(body.getString("pid"));

					Logger.getLogger().status("Client pid changed", "Shard Index of changed Client: " + cc.getClientIndex());
				}
			} else {
				//Not in network, add info...
				ConnectedClient cc = new ConnectedClient(index);

				cc.setLastKeepAlive(System.currentTimeMillis());
				cc.setConnectedServers(body.getInt("guilds"));
				cc.setMemUsed(body.getDouble("memory"));
				cc.setUptime(body.getString("uptime"));

				//Network handling stuffs
				cc.setIpForRestart(body.getString("ip"));
				cc.setPortForRestart(body.getInt("port"));
				cc.setPid(body.getString("pid"));

				DisCalServer.getNetworkInfo().addClient(cc);
			}

			response.setContentType("application/json");
			response.setStatus(200);
			return JsonUtils.getJsonResponseMessage("Success!");
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return JsonUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[API-v2] Internal keep alive error", e, true, this.getClass());

			response.setContentType("application/json");
			response.setStatus(500);
			return JsonUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}
