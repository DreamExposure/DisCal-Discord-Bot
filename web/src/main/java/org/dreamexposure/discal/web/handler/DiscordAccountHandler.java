package org.dreamexposure.discal.web.handler;

import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@SuppressWarnings({"RedundantCast", "Duplicates", "ConstantConditions"})
public class DiscordAccountHandler {
    private static DiscordAccountHandler instance;
    private static Timer timer;

    private final HashMap<String, Map<String, Object>> discordAccounts = new HashMap<>();

    //Instance handling
    private DiscordAccountHandler() {
    } //Prevent initialization

    public static DiscordAccountHandler getHandler() {
        if (instance == null)
            instance = new DiscordAccountHandler();

        return instance;
    }

    public void init() {
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                DiscordAccountHandler.this.removeTimedOutAccounts();
            }
        }, 60 * 30 * 1000);
    }

    public void shutdown() {
        if (timer != null)
            timer.cancel();
    }

    //Boolean/checkers
    public boolean hasAccount(final HttpServletRequest request) {
        try {
            return this.discordAccounts.containsKey((String) request.getSession(true).getAttribute("account"));
        } catch (final Exception e) {
            return false;
        }
    }

    //Getters
    public Map<String, Object> getAccount(final HttpServletRequest request) {
        if ((String) request.getSession(true).getAttribute("account") != null && this.discordAccounts.containsKey((String) request.getSession(true).getAttribute("account"))) {
            final Map<String, Object> m = this.discordAccounts.get((String) request.getSession(true).getAttribute("account"));
            m.remove("last_use");
            m.put("last_use", System.currentTimeMillis());

            //Remove this in case it exists. A new one is generated when using the embed page anyway.
            m.remove("embed_key");

            return m;

        } else {
            //Not logged in...
            final Map<String, Object> m = new HashMap<>();
            m.put("logged_in", false);
            m.put("bot_id", BotSettings.ID.get());
            m.put("year", LocalDate.now().getYear());
            m.put("redirect_uri", BotSettings.REDIR_URI.get());
            m.put("bot_invite", BotSettings.INVITE_URL.get());
            m.put("support_invite", BotSettings.SUPPORT_INVITE.get());
            m.put("api_url", BotSettings.API_URL.get());

            return m;
        }
    }

    public Map<String, Object> getEmbedAccount(final HttpServletRequest request) {
        if ((String) request.getSession(true).getAttribute("account") != null && this.discordAccounts.containsKey((String) request.getSession(true).getAttribute("account"))) {
            final Map<String, Object> m = this.discordAccounts.get((String) request.getSession(true).getAttribute("account"));
            m.remove("last_use");
            m.put("last_use", System.currentTimeMillis());

            if (!m.containsKey("embed_key")) {
                //Get and add read-only API key for embed page. Only good for one hour.
                try {
                    final OkHttpClient client = new OkHttpClient();
                    final RequestBody keyGrantRequestBody = RequestBody.create(GlobalConst.JSON, "");
                    final Request keyGrantRequest = new Request.Builder()
                        .url(BotSettings.API_URL_INTERNAL.get() + "/v2/account/key/readonly/get")
                        .header("Authorization", BotSettings.BOT_API_TOKEN.get())
                        .post(keyGrantRequestBody)
                        .build();
                    final Response keyGrantResponse = client.newCall(keyGrantRequest).execute();

                    //Handle response...
                    if (keyGrantResponse.isSuccessful()) {
                        final JSONObject keyGrantResponseBody = new JSONObject(keyGrantResponse.body().string());
                        //API key received, map....
                        m.put("embed_key", keyGrantResponseBody.getString("key"));
                    } else {
                        //Something didn't work... add invalid key that embed page is programmed to respond to.
                        LogFeed.log(LogObject
                            .forDebug("Embed Key Fail: ", keyGrantResponse.body().string()));
                        m.put("embed_key", "internal_error");
                    }
                } catch (final Exception e) {
                    //Something didn't work... add invalid key that embed page is programmed to respond to.
                    LogFeed.log(LogObject.forException("Embed key get failure", e, this.getClass()));
                    m.put("embed_key", "internal_error");
                }
            }

            return m;

        } else {
            //Not logged in...
            final Map<String, Object> m = new HashMap<>();
            m.put("logged_in", false);
            m.put("bot_id", BotSettings.ID.get());
            m.put("year", LocalDate.now().getYear());
            m.put("redirect_uri", BotSettings.REDIR_URI.get());
            m.put("bot_invite", BotSettings.INVITE_URL.get());
            m.put("support_invite", BotSettings.SUPPORT_INVITE.get());
            m.put("api_url", BotSettings.API_URL.get());

            //Get and add read-only API key for embed page. Only good for one hour.
            try {
                final OkHttpClient client = new OkHttpClient();
                final RequestBody keyGrantRequestBody = RequestBody.create(GlobalConst.JSON, "");
                final Request keyGrantRequest = new Request.Builder()
                    .url(BotSettings.API_URL_INTERNAL.get() + "/v2/account/key/readonly/get")
                    .header("Authorization", BotSettings.BOT_API_TOKEN.get())
                    .post(keyGrantRequestBody)
                    .build();
                final Response keyGrantResponse = client.newCall(keyGrantRequest).execute();

                //Handle response...
                if (keyGrantResponse.isSuccessful()) {
                    final JSONObject keyGrantResponseBody = new JSONObject(keyGrantResponse.body().string());
                    //API key received, map....
                    m.put("embed_key", keyGrantResponseBody.getString("key"));
                } else {
                    //Something didn't work... add invalid key that embed page is programmed to respond to.
                    LogFeed.log(LogObject.forDebug("Embed key fail",
                        keyGrantResponse.body().string()));
                    m.put("embed_key", "internal_error");
                }
            } catch (final Exception e) {
                //Something didn't work... add invalid key that embed page is programmed to respond to.
                LogFeed.log(LogObject.forException("Embed key get failure", e, this.getClass()));
                m.put("embed_key", "internal_error");
            }

            return m;
        }
    }


    //Functions
    public void addAccount(final Map<String, Object> m, final HttpServletRequest request) {
        this.discordAccounts.remove((String) request.getSession(true).getAttribute("account"));
        m.remove("last_use");
        m.put("last_use", System.currentTimeMillis());
        this.discordAccounts.put((String) request.getSession(true).getAttribute("account"), m);
    }

    public void removeAccount(final HttpServletRequest request) {
        if ((String) request.getSession(true).getAttribute("account") != null && this.hasAccount(request))
            this.discordAccounts.remove((String) request.getSession(true).getAttribute("account"));
    }

    private void removeTimedOutAccounts() {
        final long limit = Long.parseLong(BotSettings.TIME_OUT.get());
        final List<String> toRemove = new ArrayList<>();
        for (final String id : this.discordAccounts.keySet()) {
            final Map<String, Object> m = this.discordAccounts.get(id);
            final long lastUse = (long) m.get("last_use");
            if (System.currentTimeMillis() - lastUse > limit)
                toRemove.remove(id); //Timed out, remove account info and require sign in.
        }

        for (final String id : toRemove) {
            this.discordAccounts.remove(id);
        }
    }
}
