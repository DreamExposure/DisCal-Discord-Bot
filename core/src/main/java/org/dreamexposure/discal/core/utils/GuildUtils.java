package org.dreamexposure.discal.core.utils;

import org.dreamexposure.discal.core.object.web.WebGuild;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nova Fox on 11/6/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class GuildUtils {
	public static boolean active(long id) {
		//TODO: Determine an accurate way to detect if a guild is still connected to DisCal
		return true;
	}

	public static List<WebGuild> getGuilds(String userId, IDiscordClient client) {
		List<WebGuild> guilds = new ArrayList<>();

		for (IGuild g : client.getGuilds()) {
			for (IUser m : g.getUsers()) {
				if (m.getStringID().equals(userId)) {
					WebGuild wg = new WebGuild().fromGuild(g);
					wg.setManageServer(PermissionChecker.hasManageServerRole(g, m));
					wg.setDiscalRole(PermissionChecker.hasSufficientRole(g, m));
					guilds.add(wg);
				}
			}
		}

		return guilds;
	}
}