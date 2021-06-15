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
        this.statuses.add("Discord Calendar");
        this.statuses.add("!help for help");
        this.statuses.add("!DisCal for info");
        this.statuses.add("Powered by DreamExposure");
        this.statuses.add("Ultimate calendar bot!");
        this.statuses.add("%guCount% guilds on shard!");
        this.statuses.add("%calCount% calendars!");
        this.statuses.add("%annCount% announcements!");
        this.statuses.add("%shards% shards!");
        this.statuses.add("Version " + GlobalConst.version);
        this.statuses.add("DisCal is on Patreon!");
        this.index = 0;
    }

    @Override
    public void run() {
        if (DisCalClient.getClient() != null) {

            String status = this.statuses.get(this.index);
            status = status.replace("%guCount%", DisCalClient.getClient().getGuilds().count().block() + "");
            status = status.replace("%calCount%", DatabaseManager.INSTANCE.getCalendarCount().block() + "");
            status = status.replace("%annCount%", DatabaseManager.INSTANCE.getAnnouncementCount().block() + "");
            status = status.replace("%shards%", BotSettings.SHARD_COUNT.get());

            DisCalClient.getClient().updatePresence(Presence.online(Activity.playing(status))).subscribe();
            //Set new index.
            if (this.index + 1 >= this.statuses.size())
                this.index = 0;
            else
                this.index++;
        }
    }
}
