package org.dreamexposure.discal.server.api.endpoints.v2.guild;

import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.object.web.WebGuild;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v2/guild/")
public class GetWebGuildEndpoint {
	@PostMapping(value = "/get", produces = "application/json")
	public String getSettings(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
		//Authenticate...
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		}

		//Okay, now handle actual request.
		try {
			JSONObject jsonMain = new JSONObject(requestBody);
			Snowflake guildId = Snowflake.of(jsonMain.getLong("guild_id"));
			Snowflake userId = Snowflake.of(jsonMain.getLong("user_id"));

			Guild g = DisCalServer.getClient()
					.getGuildById(guildId).onErrorResume(e -> Mono.empty()).block();

			if (g != null) {
				WebGuild wg = new WebGuild().fromGuild(g);

				Member m = g.getMemberById(userId).onErrorResume(e -> Mono.empty()).block();

				if (m != null) { //Assume false if we can't get the user...
					wg.setManageServer(PermissionChecker.hasManageServerRole(m));
					wg.setDiscalRole(PermissionChecker.hasSufficientRole(g, m));
				}

				response.setContentType("application/json");
				response.setStatus(200);
				return wg.toJson().toString();
			}
			response.setContentType("application/json");
			response.setStatus(404);
			return JsonUtils.getJsonResponseMessage("Guild not connected to DisCal");
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return JsonUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[API-v2] Internal get guild settings error", e, true, this.getClass());

			response.setContentType("application/json");
			response.setStatus(500);
			return JsonUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}
