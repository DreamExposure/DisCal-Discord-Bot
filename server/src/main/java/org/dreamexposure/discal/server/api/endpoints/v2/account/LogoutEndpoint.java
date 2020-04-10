package org.dreamexposure.discal.server.api.endpoints.v2.account;

import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.server.utils.Authentication;
import org.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/v2/account")
public class LogoutEndpoint {
	@GetMapping(value = "/logout")
	public String logoutOfAccount(HttpServletRequest request, HttpServletResponse response) {
		//Check auth
		AuthenticationState authState = Authentication.authenticate(request);
		if (!authState.isSuccess()) {
			response.setStatus(authState.getStatus());
			response.setContentType("application/json");
			return authState.toJson();
		} else if (authState.isReadOnly()) {
			response.setStatus(401);
			response.setContentType("application/json");
			return JsonUtils.getJsonResponseMessage("Read-Only key not Allowed");
		}

		try {
			//If this is a temp key, it will be removed...
			Authentication.removeTempKey(authState.getKeyUsed());

			response.setContentType("application/json");
			response.setStatus(200);
			return JsonUtils.getJsonResponseMessage("Logged Out");
		} catch (JSONException e) {
			e.printStackTrace();

			response.setContentType("application/json");
			response.setStatus(400);
			return JsonUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception("[API-v2] Internal logout of account exception", e, true, this.getClass());

			response.setContentType("application/json");
			response.setStatus(500);
			return JsonUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}
