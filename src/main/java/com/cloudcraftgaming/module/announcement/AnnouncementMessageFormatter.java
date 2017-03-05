package com.cloudcraftgaming.module.announcement;

import com.cloudcraftgaming.Main;
import sx.blah.discord.handle.obj.IGuild;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementMessageFormatter {
    private static String lineBreak = System.getProperty("line.separator");

    public static String getFormatEventMessage(Announcement a) {
        return "~-~-~- Announcement Info ~-~-~-" + lineBreak
                + "Announcement ID: " + a.getAnnouncementType() + lineBreak + lineBreak
                + "Type: " + a.getAnnouncementType().name() + lineBreak + lineBreak
                + "Event ID: " + a.getEventId() + lineBreak + lineBreak
                + "Hours Before: "  + a.getHoursBefore() + lineBreak
                + "Minutes Before: " + a.getMinutesBefore() + lineBreak
                + "In channel (name): " + channelFromId(a);
                //TODO: Add subscribers list here...
    }

    private static String channelFromId(Announcement a) {
        IGuild g = Main.client.getGuildByID(a.getGuildId());
        if (!a.getAnnouncementChannelId().equalsIgnoreCase("N/a")) {
            return g.getChannelByID(a.getAnnouncementChannelId()).getName();
        }
        return "Unset or Invalid";
    }
}