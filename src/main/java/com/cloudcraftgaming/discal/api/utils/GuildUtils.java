package com.cloudcraftgaming.discal.api.utils;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.api.object.web.WebGuild;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nova Fox on 12/17/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class GuildUtils {
	public static List<WebGuild> getGuilds(String userId) {
		List<WebGuild> guilds = new ArrayList<>();
		IUser user = Main.client.getUserByID(Long.valueOf(userId));
		for (IGuild g : Main.client.getGuilds()) {
			if (g.getUserByID(Long.valueOf(userId)) != null) {
				WebGuild wg = new WebGuild().fromGuild(g);
				wg.setManageServer(PermissionChecker.hasManageServerRole(g, user));
				wg.setDiscalRole(PermissionChecker.hasSufficientRole(g, user));
				guilds.add(wg);
			}
		}
		return guilds;
	}
}