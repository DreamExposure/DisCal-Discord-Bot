package com.cloudcraftgaming.module.announcement;

import com.cloudcraftgaming.Main;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementMessageFormatter {

    public static EmbedObject getFormatAnnouncementEmbed(Announcement a) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("Announcement Info");
        em.appendField("Announcement ID", a.getAnnouncementId().toString(), true);
        em.appendField("Announcement Type", a.getAnnouncementType().name(), true);
        em.appendField("Event ID", a.getEventId(), true);
        em.appendField("Hours Before", String.valueOf(a.getHoursBefore()), true);
        em.appendField("Minutes Before", String.valueOf(a.getMinutesBefore()), true);
        em.appendField("In Channel (Name)", channelFromId(a), true);
        em.withColor(36, 153, 153);

        return em.build();
    }

    public static EmbedObject getCondensedAnnouncementEmbed(Announcement a) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("Condensed Announcement Info");
        em.appendField("Announcement ID", a.getAnnouncementId().toString(), false);
        em.appendField("Time Before", condensedTime(a), false);
        em.withFooterText("Type: " + a.getAnnouncementType().name());
        em.withColor(36, 153, 153);

        return em.build();
    }

    private static String channelFromId(Announcement a) {
        IGuild g = Main.client.getGuildByID(a.getGuildId());
        if (!a.getAnnouncementChannelId().equalsIgnoreCase("N/a")) {
            return g.getChannelByID(a.getAnnouncementChannelId()).getName();
        }
        return "Unset or Invalid";
    }

    private static String condensedTime(Announcement a) {
        return a.getHoursBefore() + "H" + a.getMinutesBefore() + "m";
    }
}