package org.dreamexposure.discal.core.utils;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import org.dreamexposure.discal.core.object.web.WebGuild;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nova Fox on 11/6/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class GuildUtils {
	public static boolean active(Snowflake id) {
		//TODO: Determine an accurate way to detect if a guild is still connected to DisCal
		return true;
	}

	public static List<WebGuild> getGuilds(String userId, DiscordClient client) {
		List<WebGuild> guilds = new ArrayList<>();

		for (Guild g : client.getGuilds().toIterable()) {
			for (Member m : g.getMembers().toIterable()) {
				if (m.getId().asString().equals(userId)) {
					WebGuild wg = new WebGuild().fromGuild(g);
					wg.setManageServer(PermissionChecker.hasManageServerRole(m));
					wg.setDiscalRole(PermissionChecker.hasSufficientRole(g, m));
					guilds.add(wg);
				}
			}
		}

		return guilds;
	}
}