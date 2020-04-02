package org.dreamexposure.discal.core.utils;

import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.web.WebGuild;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 11/6/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class GuildUtils {
	public static int findShard(Snowflake id) {
		return ((int) id.asLong() >> 22) % Integer.parseInt(BotSettings.SHARD_COUNT.get());
	}

	public static boolean active(Snowflake id) {
		//TODO: Determine an accurate way to detect if a guild is still connected to DisCal
		return true;
	}

	public static List<WebGuild> getGuilds(JSONArray ids, String userId, DiscordClient client) {
		List<WebGuild> guilds = new ArrayList<>();

		for (int i = 0; i < ids.length(); i++) {
			Guild g = client.getGuildById(Snowflake.of(ids.getLong(i))).onErrorResume(e -> Mono.empty()).block();
			if (g != null) {
				Member m = g.getMemberById(Snowflake.of(userId)).block();

				WebGuild wg = new WebGuild().fromGuild(g);
				wg.setManageServer(PermissionChecker.hasManageServerRole(m).blockOptional().orElse(false));
				wg.setDiscalRole(PermissionChecker.hasSufficientRole(m, wg.getSettings()).blockOptional().orElse(false));
				guilds.add(wg);
			}
		}
		return guilds;
	}
}