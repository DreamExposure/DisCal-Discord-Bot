package com.cloudcraftgaming.discal.web.handler;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.web.WebGuild;
import org.json.JSONException;
import spark.Request;
import spark.Response;
import sx.blah.discord.handle.obj.IGuild;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.halt;

/**
 * Created by Nova Fox on 12/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"unchecked", "ThrowableNotThrown"})
public class DashboardHandler {
	public static String handleGuildSelect(Request request, Response response) {
		try {
			String guildId = request.queryParams("guild");

			WebGuild wg = new WebGuild().fromGuild(Main.client.getGuildByID(Long.valueOf(guildId)));

			Map m = DiscordAccountHandler.getHandler().getAccount(request.session().id());

			if (m.containsKey("selected")) {
				m.remove("selected");
			}

			m.put("selected", wg);

			if (m.containsKey("settings")) {
				m.remove("settings");
			}

			if (m.containsKey("ext")) {
				m.remove("ext");
			}

			DiscordAccountHandler.getHandler().appendAccount(m, request.session().id());

			response.redirect("/dashboard/guild", 301);
		} catch (JSONException e) {
			e.printStackTrace();
			response.redirect("/dashboard", 301);
		} catch (Exception e) {
			e.printStackTrace();
			halt(500, "Internal Server Exception");
		}
		return response.body();
	}

	public static String handleSettingsSelect(Request request, Response response) {
		try {
			String settings = request.queryParams("settings");

			Map m = new HashMap();
			m.put("settings", settings);

			if (settings.equalsIgnoreCase("ext")) {
				//Extension handling
				String ext = request.queryParams("ext");
				m.put("ext", ext);
			}

			DiscordAccountHandler.getHandler().appendAccount(m, request.session().id());

			response.redirect("/dashboard/guild", 301);
		} catch (JSONException e) {
			e.printStackTrace();
			response.redirect("/dashboard", 301);
		} catch (Exception e) {
			e.printStackTrace();
			halt(500, "Internal Server Exception");
		}
		return response.body();
	}

	public static String handleSettingsUpdate(Request request, Response response) {
		try {
			if (request.queryParams().contains("bot-nick")) {
				//Update bot nickname...
				Map m = DiscordAccountHandler.getHandler().getAccount(request.session().id());
				WebGuild g = (WebGuild) m.get("selected");

				g.setBotNick(request.queryParams("bot-nick"));

				IGuild guild = Main.client.getGuildByID(Long.valueOf(g.getId()));

				guild.setUserNickname(Main.client.getOurUser(), g.getBotNick());
			} else if (request.queryParams().contains("prefix")) {
				//Update prefix...
				Map m = DiscordAccountHandler.getHandler().getAccount(request.session().id());
				WebGuild g = (WebGuild) m.get("selected");

				g.setPrefix(request.queryParams("prefix"));

				GuildSettings settings = DatabaseManager.getManager().getSettings(Long.valueOf(g.getId()));
				settings.setPrefix(g.getPrefix());

				DatabaseManager.getManager().updateSettings(settings);
			}

			//Finally redirect back to the dashboard
			response.redirect("/dashboard/guild", 301);
		} catch (Exception e) {
			e.printStackTrace();
			halt(500, "Internal Server Exception");
		}
		return response.body();
	}
}