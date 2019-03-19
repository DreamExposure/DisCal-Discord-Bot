package org.dreamexposure.discal.client.module.misc;

import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.utils.GlobalConst;

import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Created by Nova Fox on 3/5/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class StatusChanger extends TimerTask {
    private final ArrayList<String> statuses = new ArrayList<>();
    private Integer index;

    /**
     * Creates the StatusChanger and its Statuses list.
     */
	public StatusChanger() {
		statuses.add("Discord Calendar");
		statuses.add("!help for help");
		statuses.add("!DisCal for info");
		statuses.add("Powered by DreamExposure");
		statuses.add("Ultimate calendar bot!");
		statuses.add("%guCount% guilds on shard!");
		statuses.add("%calCount% calendars!");
		statuses.add("%annCount% announcements!");
		statuses.add("%shards% shards!");
		statuses.add("Version " + GlobalConst.version);
		statuses.add("DisCal is on Patreon!");
		index = 0;
	}

	@Override
	public void run() {
		String status = statuses.get(index);
		status = status.replace("%guCount%", DisCalClient.getClient().getGuilds().count().block() + "");
		status = status.replace("%calCount%", DatabaseManager.getManager().getCalendarCount() + "");
		status = status.replace("%annCount%", DatabaseManager.getManager().getAnnouncementCount() + "");
		status = status.replace("%shards%", BotSettings.SHARD_COUNT.get());

		DisCalClient.getClient().updatePresence(Presence.online(Activity.playing(status))).subscribe();
		//Set new index.
		if (index + 1 >= statuses.size())
			index = 0;
		else
			index++;
	}
}