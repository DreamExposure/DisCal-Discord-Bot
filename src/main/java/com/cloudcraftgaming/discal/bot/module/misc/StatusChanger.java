package com.cloudcraftgaming.discal.bot.module.misc;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.api.DisCalAPI;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.web.handler.DiscordAccountHandler;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

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
        statuses.add("Made by NovaFox161");
        statuses.add("Official calendar bot!");
        statuses.add("Used on %guCount% guilds!");
        statuses.add("%calCount% calendars!");
        statuses.add("%annCount% announcements!");
        statuses.add("%shards% shards!");
		if (BotSettings.RUN_API.get().equalsIgnoreCase("true"))
			statuses.add("%users% using Dashboard!");

        statuses.add("Version " + Main.version);
        statuses.add("DisCal is on Patreon!");
        statuses.add("Share DisCal!!");
        index = 0;
    }

    @Override
    public void run() {
        String status = statuses.get(index);
		status = status.replace("%guCount%", DisCalAPI.getAPI().getClient().getGuilds().size() + "");
        status = status.replace("%calCount%", DatabaseManager.getManager().getCalendarCount() + "");
        status = status.replace("%annCount%", DatabaseManager.getManager().getAnnouncementCount() + "");
		status = status.replace("%shards%", DisCalAPI.getAPI().getClient().getShardCount() + "");
		status = status.replace("%users%", DiscordAccountHandler.getHandler().accountCount() + "");
		DisCalAPI.getAPI().getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, status);

        //Set new index.
		if (index + 1 >= statuses.size())
            index = 0;
		else
            index++;
    }
}