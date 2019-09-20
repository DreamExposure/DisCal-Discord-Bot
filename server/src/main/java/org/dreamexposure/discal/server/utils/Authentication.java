package org.dreamexposure.discal.server.utils;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.object.web.UserAPIAccount;
import org.dreamexposure.discal.web.handler.DiscordAccountHandler;

import javax.servlet.http.HttpServletRequest;

public class Authentication {
	public static AuthenticationState authenticate(HttpServletRequest request) {
		if (!request.getMethod().equalsIgnoreCase("POST")) {
			Logger.getLogger().api("Denied '" + request.getMethod() + "' access", request.getRemoteAddr());
			return new AuthenticationState(false).setStatus(405).setReason("Method not allowed");
		}
		//Check authorization
		if (DiscordAccountHandler.getHandler().hasAccount(request)) {
			//User is logged in from website, no API key needed
			Logger.getLogger().api("API Call from website", request.getRemoteAddr());

			return new AuthenticationState(true).setStatus(200).setReason("Success");
		} else {
			//Requires "Authorization Header
			if (request.getHeader("Authorization") != null) {
				String key = request.getHeader("Authorization");

				//TODO: Handle this shit better but whatever
				if (key.equals("EMBEDDED")) {
					Logger.getLogger().api("User using embed", request.getRemoteAddr(), request.getServerName(), request.getPathInfo());
					//TODO: Verify its using the correct /embed/ path!!!!
					return new AuthenticationState(true).setStatus(200).setReason("Success");
				} else {
					UserAPIAccount acc = DatabaseManager.getManager().getAPIAccount(key);
					if (acc != null) {
						if (acc.isBlocked()) {
							Logger.getLogger().api("Attempted to use blocked API Key: " + acc.getAPIKey(), request.getRemoteAddr());

							return new AuthenticationState(false).setStatus(401).setReason("Unauthorized");
						} else {
							//Everything checks out!
							acc.setUses(acc.getUses() + 1);
							DatabaseManager.getManager().updateAPIAccount(acc);

							return new AuthenticationState(true).setStatus(200).setReason("Success");
						}
					} else {
						Logger.getLogger().api("Attempted to use invalid API Key: " + key, request.getRemoteAddr());
						return new AuthenticationState(false).setStatus(401).setReason("Unauthorized");
					}
				}
			} else {
				Logger.getLogger().api("Attempted to use API without authorization header", request.getRemoteAddr());
				return new AuthenticationState(false).setStatus(400).setReason("Bad Request");
			}
		}
	}
}