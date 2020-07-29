package org.dreamexposure.discal.server.api.endpoints.v2.account;

import org.dreamexposure.discal.core.crypto.KeyGenerator;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.server.utils.Authentication;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/v2/account")
public class LoginEndpoint {
    @PostMapping(value = "/login", produces = "application/json")
    public String loginForKey(HttpServletRequest request, HttpServletResponse response) {
        //Check auth, must be from within DisCal network, as this is generating an API key...
        AuthenticationState authState = Authentication.authenticate(request);
        if (!authState.isSuccess()) {
            response.setStatus(authState.getStatus());
            response.setContentType("application/json");
            return authState.toJson();
        } else if (!authState.isFromDiscalNetwork()) {
            response.setStatus(401);
            response.setContentType("application/json");
            return JsonUtils.getJsonResponseMessage("Unauthorized to use this Endpoint.");
        }

        try {
            //Generate temporary API key. This key should only be valid for 48 hours unless refreshed..
            String key = KeyGenerator.csRandomAlphaNumericString(64);

            //Save key to memory... so it can be used for authentication just like the others
            Authentication.saveTempKey(key);

            //Return key to web....
            JSONObject responseBody = new JSONObject();
            responseBody.put("key", key);

            response.setContentType("application/json");
            response.setStatus(200);
            return responseBody.toString();
        } catch (JSONException e) {
            e.printStackTrace();

            response.setContentType("application/json");
            response.setStatus(400);
            return JsonUtils.getJsonResponseMessage("Bad Request");
        } catch (Exception e) {
            LogFeed.log(LogObject
                    .forException("[API-v2]", "login for key err", e, this.getClass()));

            response.setContentType("application/json");
            response.setStatus(500);
            return JsonUtils.getJsonResponseMessage("Internal Server Error");
        }
    }
}
