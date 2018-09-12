package org.dreamexposure.discal.client.listeners.discal;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.network.CrossTalkReason;
import org.dreamexposure.discal.core.enums.network.DisCalRealm;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.web.WebGuild;
import org.dreamexposure.discal.core.utils.GuildUtils;
import org.dreamexposure.discal.core.utils.PermissionChecker;
import org.dreamexposure.novautils.event.EventListener;
import org.dreamexposure.novautils.events.network.crosstalk.CrossTalkReceiveEvent;
import org.dreamexposure.novautils.network.crosstalk.ClientSocketHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
@SuppressWarnings({"ConstantConditions", "IfCanBeSwitch"})
public class CrossTalkEventListener implements EventListener {
	@SuppressWarnings("unused")
	public void handle(CrossTalkReceiveEvent event) {
		IGuild g = null;
		//Check if this even applies to us!
		if (event.getData().has("Guild-Id")) {
			g = DisCalClient.getClient().getGuildByID(Long.valueOf(event.getData().getString("Guild-Id")));
			if (g == null) return; //Guild not connected to this client, correct client will handle this.
		}
		if (event.getData().getString("Reason").equals(CrossTalkReason.UPDATE.name())) {
			//Update data of some form...
			if (event.getData().getString("Realm").equals(DisCalRealm.BOT_SETTINGS.name())) {
				//Handle bot settings updates...
				if (event.getData().has("Bot-Nick")) {
					g.setUserNickname(g.getClient().getOurUser(), event.getData().getString("Bot-Nick"));
				}
			}
		} else if (event.getData().getString("Reason").equals(CrossTalkReason.HANDLE.name())) {
			if (event.getData().getString("Realm").equals(DisCalRealm.BOT_LANGS.name())) {
				//Reload lang files...
				MessageManager.reloadLangs();
			} else if (!event.getData().getString("Realm").equals(DisCalRealm.GUILD_LEAVE.name())) {
				//Leave guild...
				g.leave();
			} else if (!event.getData().getString("Realm").equals(DisCalRealm.GUILD_MAX_CALENDARS.name())) {
				//Change max calendar limit..
				GuildSettings settings = DatabaseManager.getManager().getSettings(g.getLongID());

				settings.setMaxCalendars(event.getData().getInt("Max-Calendars"));
				DatabaseManager.getManager().updateSettings(settings);
			} else if (!event.getData().getString("Realm").equals(DisCalRealm.GUILD_IS_DEV.name())) {
				//Change if the guild is a dev guild or not
				GuildSettings settings = DatabaseManager.getManager().getSettings(g.getLongID());

				settings.setDevGuild(!settings.isDevGuild());
				DatabaseManager.getManager().updateSettings(settings);
			} else if (!event.getData().getString("Realm").equals(DisCalRealm.GUILD_IS_PATRON.name())) {
				//Change if the guild is a patron guild or not
				GuildSettings settings = DatabaseManager.getManager().getSettings(g.getLongID());

				settings.setPatronGuild(!settings.isPatronGuild());
				DatabaseManager.getManager().updateSettings(settings);
			}
		} else if (event.getData().getString("Reason").equals(CrossTalkReason.GET.name())) {
			//Probably requires a response...
			if (event.isRequireResponse()) {
				if (event.getData().getString("Realm").equals(DisCalRealm.WEBSITE_DASHBOARD_GUILD.name())) {
					//Requires us to grab data for guild and return a response containing the WebGuild with needed info...
					String memId = event.getData().getString("Member-Id");
					IUser member = g.getUserByID(Long.valueOf(memId));

					JSONObject newData = new JSONObject();
					newData.put("Guild", new WebGuild().fromGuild(g));
					newData.put("Sufficient-Role", PermissionChecker.hasSufficientRole(g, member));
					newData.put("Manager-Server", PermissionChecker.hasManageServerRole(g, member));

					ClientSocketHandler.sendToServer(Integer.valueOf(BotSettings.SHARD_INDEX.get()), newData, BotSettings.CROSSTALK_SERVER_HOST.get(), event.getOneTimeResponsePort());
				} else if (event.getData().getString("Realm").equals(DisCalRealm.WEBSITE_DASHBOARD_DEFAULTS.name())) {
					//Get guilds the user is in...
					String memId = event.getData().getString("Member-Id");

					JSONObject newData = new JSONObject();

					JSONArray guildsArray = new JSONArray();
					List<WebGuild> guilds = GuildUtils.getGuilds(memId, DisCalClient.getClient());

					for (WebGuild wg : guilds) {
						guildsArray.put(wg);
					}

					newData.put("Guilds", guildsArray);
					newData.put("Guild-Count", guilds.size());

					ClientSocketHandler.sendToServer(Integer.valueOf(BotSettings.SHARD_INDEX.get()), newData, BotSettings.CROSSTALK_SERVER_HOST.get(), event.getOneTimeResponsePort());
				} else if (event.getData().getString("Realm").equals(DisCalRealm.WEBSITE_EMBED_CALENDAR.name())) {
					//Get guild for calendar embed on site....
					JSONObject newData = new JSONObject();

					newData.put("Guild", new WebGuild().fromGuild(g));

					ClientSocketHandler.sendToServer(Integer.valueOf(BotSettings.SHARD_INDEX.get()), newData, BotSettings.CROSSTALK_SERVER_HOST.get(), event.getOneTimeResponsePort());
				}
			}
		}
	}
}