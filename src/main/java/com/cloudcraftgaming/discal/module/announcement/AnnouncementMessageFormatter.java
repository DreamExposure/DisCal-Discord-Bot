package com.cloudcraftgaming.discal.module.announcement;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.data.BotData;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.EmbedBuilder;

import java.io.IOException;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementMessageFormatter {

    /**
     * Gets the EmbedObject for an Announcement.
     * @param a The Announcement to embed.
     * @return The EmbedObject for the Announcement.
     */
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
        em.appendField("Additional Info", a.getInfo(), false);
        em.withColor(36, 153, 153);

        return em.build();
    }

    /**
     * Gets the EmbedObject for a Condensed Announcement.
     * @param a The Announcement to embed.
     * @return The EmbedObject for a Condensed Announcement.
     */
    public static EmbedObject getCondensedAnnouncementEmbed(Announcement a) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("Condensed Announcement Info");
        em.appendField("Announcement ID", a.getAnnouncementId().toString(), false);
        em.appendField("Time Before", condensedTime(a), false);

        if (a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
            em.appendField("Event ID", a.getEventId(), false);
            try {
                Calendar service = CalendarAuth.getCalendarService();
                BotData data = DatabaseManager.getManager().getData(a.getGuildId());
                Event event = service.events().get(data.getCalendarAddress(), a.getEventId()).execute();
                em.appendField("Event Summary", event.getSummary(), true);
            } catch (IOException e) {
                em.appendField("Event Summary", "Unknown (Error)", true);
            }
        }
        em.withFooterText("Type: " + a.getAnnouncementType().name());
        em.withColor(36, 153, 153);

        return em.build();
    }

    /**
     * Gets the specified channel via its ID.
     * @param a The Announcement involved.
     * @return The Name of the channel from its ID.
     */
    private static String channelFromId(Announcement a) {
        IGuild g = Main.client.getGuildByID(a.getGuildId());
        if (!a.getAnnouncementChannelId().equalsIgnoreCase("N/a")) {
            return g.getChannelByID(a.getAnnouncementChannelId()).getName();
        }
        return "Unset or Invalid";
    }

    /**
     * Gets the formatted time from an Announcement.
     * @param a The Announcement.
     * @return The formatted time from an Announcement.
     */
    private static String condensedTime(Announcement a) {
        return a.getHoursBefore() + "H" + a.getMinutesBefore() + "m";
    }
}