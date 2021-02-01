package org.dreamexposure.discal.server.utils;

import com.google.api.client.http.HttpStatusCodes;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.web.AuthenticationState;
import org.dreamexposure.discal.core.object.web.UserAPIAccount;
import org.dreamexposure.discal.core.utils.GlobalConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;

public class Authentication {
    private static Timer timer;

    private static final Map<String, Long> tempKeys = new HashMap<>();
    private static final Map<String, Long> readOnlyKeys = new HashMap<>();

    public static AuthenticationState authenticate(final HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod()) || "GET".equalsIgnoreCase(request.getMethod())) {
            LogFeed.log(LogObject.forDebug("Denied Access", "Method: " + request.getMethod()));
            return new AuthenticationState(false)
                .status(GlobalConst.STATUS_NOT_ALLOWED)
                .reason("Method not allowed");
        }
        //Requires "Authorization Header
        if (request.getHeader("Authorization") != null) {
            final String key = request.getHeader("Authorization");

            //Check if this is from within the DisCal network...
            if (key.equals(BotSettings.BOT_API_TOKEN.get())) {
                return new AuthenticationState(true)
                    .status(GlobalConst.STATUS_SUCCESS)
                    .reason("Success")
                    .keyUsed(key)
                    .fromDisCalNetwork(true);
                //Check if this is a temp key, granted for a logged in user...
            } else if (tempKeys.containsKey(key)) {
                return new AuthenticationState(true)
                    .status(GlobalConst.STATUS_SUCCESS)
                    .reason("Success")
                    .keyUsed(key)
                    .fromDisCalNetwork(false)
                    .readOnly(false);
            } else if (readOnlyKeys.containsKey(key)) {
                return new AuthenticationState(true)
                    .status(GlobalConst.STATUS_SUCCESS)
                    .reason("Success")
                    .keyUsed(key)
                    .fromDisCalNetwork(false)
                    .readOnly(true);
            } else if ("teapot".equals(key)) {
                return new AuthenticationState(false)
                    .status(GlobalConst.STATUS_TEAPOT)
                    .reason("I'm a teapot")
                    .keyUsed(key);
            }

            //Check if this key is in the database...
            final UserAPIAccount acc = DatabaseManager.getAPIAccount(key).block();
            if (acc != null && !acc.getBlocked()) {
                return new AuthenticationState(true)
                    .status(HttpStatusCodes.STATUS_CODE_OK)
                    .reason("Success")
                    .keyUsed(key)
                    .fromDisCalNetwork(false);
            }

            //If we reach here, the API key does not exist or is blocked...
            return new AuthenticationState(false)
                .status(GlobalConst.STATUS_AUTHORIZATION_DENIED)
                .reason("Authorization Denied");
        } else {
            LogFeed.log(LogObject
                .forDebug("Attempted API use without Authorization Header",
                    "IP: " + request.getRemoteAddr()));
            return new AuthenticationState(false)
                .status(GlobalConst.STATUS_BAD_REQUEST)
                .reason("Bad Request");
        }
    }

    public static void saveTempKey(final String key) {
        if (!tempKeys.containsKey(key))
            tempKeys.put(key, System.currentTimeMillis() + GlobalConst.oneDayMs);
    }

    public static void removeTempKey(final String key) {
        tempKeys.remove(key);
    }

    public static void saveReadOnlyKey(final String key) {
        if (!readOnlyKeys.containsKey(key)) {
            readOnlyKeys.put(key, System.currentTimeMillis() + GlobalConst.oneHourMs);
        }
    }

    //Timer handling
    public static void init() {
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handleExpiredKeys();
            }
        }, GlobalConst.oneHourMs);
    }

    public static void shutdown() {
        if (timer != null)
            timer.cancel();
    }

    private static void handleExpiredKeys() {
        final List<String> allToRemove = new ArrayList<>();

        for (final String key : tempKeys.keySet()) {
            final long expireTime = tempKeys.get(key);
            if (expireTime >= System.currentTimeMillis())
                allToRemove.add(key);
        }

        for (final String key : readOnlyKeys.keySet()) {
            final long expireTime = readOnlyKeys.get(key);
            if (expireTime >= System.currentTimeMillis())
                allToRemove.add(key);
        }

        for (final String key : allToRemove) {
            tempKeys.remove(key);
            readOnlyKeys.remove(key);
        }
    }
}
