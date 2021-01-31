package org.dreamexposure.discal.client.message;

import com.google.api.services.calendar.model.Calendar;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.PreCalendar;
import org.dreamexposure.discal.core.utils.GlobalConst;

import java.util.function.Consumer;

import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class CalendarMessageFormatter {
    @Deprecated
    public static String getCalendarLink(final Snowflake guildId) {
        return "https://www.discalbot.com/embed/" + guildId.asString() + "/calendar/1";
    }

    public static String getCalendarLink(final Snowflake guildId, final int calNumber) {
        return "https://www.discalbot.com/embed/" + guildId.asString() + "/calendar/" + calNumber;
    }

    @Deprecated
    public static Mono<Consumer<EmbedCreateSpec>> getCalendarLinkEmbed(final Calendar cal, final GuildSettings settings) {
        return DisCalClient.getClient().getGuildById(settings.getGuildID()).map(g -> spec -> {
            if (settings.getBranded())
                spec.setAuthor(g.getName(), GlobalConst.discalSite, g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
            else
                spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

            spec.setTitle(Messages.getMessage("Embed.Calendar.Link.Title", settings));

            if (cal.getSummary() != null)
                spec.addField(Messages.getMessage("Embed.Calendar.Link.Summary", settings), cal.getSummary(), true);

            if (cal.getDescription() != null)
                spec.addField(Messages.getMessage("Embed.Calendar.Link.Description", settings), cal.getDescription(), true);

            spec.addField(Messages.getMessage("Embed.Calendar.Link.TimeZone", settings), cal.getTimeZone(), false);
            spec.setUrl(CalendarMessageFormatter.getCalendarLink(settings.getGuildID()));
            spec.setFooter(Messages.getMessage("Embed.Calendar.Link.CalendarId", "%id%", cal.getId(), settings), null);
            spec.setColor(GlobalConst.discalColor);
        });
    }

    public static Mono<Consumer<EmbedCreateSpec>> getCalendarLinkEmbed(final Calendar cal, final int calNum, final GuildSettings settings) {
        return DisCalClient.getClient().getGuildById(settings.getGuildID()).map(g -> spec -> {
            if (settings.getBranded())
                spec.setAuthor(g.getName(), GlobalConst.discalSite, g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
            else
                spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

            spec.setTitle(Messages.getMessage("Embed.Calendar.Link.Title", settings));

            if (cal.getSummary() != null)
                spec.addField(Messages.getMessage("Embed.Calendar.Link.Summary", settings), cal.getSummary(), true);

            if (cal.getDescription() != null)
                spec.addField(Messages.getMessage("Embed.Calendar.Link.Description", settings), cal.getDescription(), true);

            spec.addField(Messages.getMessage("Embed.Calendar.Link.TimeZone", settings), cal.getTimeZone(), false);
            spec.setUrl(CalendarMessageFormatter.getCalendarLink(settings.getGuildID(), calNum));
            spec.setFooter(Messages.getMessage("Embed.Calendar.Link.CalendarId", "%id%", cal.getId(), settings), null);
            spec.setColor(GlobalConst.discalColor);
        });
    }

    public static Mono<Consumer<EmbedCreateSpec>> getPreCalendarEmbed(final PreCalendar calendar, final GuildSettings settings) {
        return DisCalClient.getClient().getGuildById(settings.getGuildID()).map(g -> spec -> {
            if (settings.getBranded())
                spec.setAuthor(g.getName(), GlobalConst.discalSite, g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
            else
                spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

            spec.setTitle(Messages.getMessage("Embed.Calendar.Pre.Title", settings));
            if (calendar.getSummary() != null)
                spec.addField(Messages.getMessage("Embed.Calendar.Pre.Summary", settings), calendar.getSummary(), true);
            else
                spec.addField(Messages.getMessage("Embed.Calendar.Pre.Summary", settings), "***UNSET***", true);

            if (calendar.getDescription() != null)
                spec.addField(Messages.getMessage("Embed.Calendar.Pre.Description", settings), calendar.getDescription(), false);
            else
                spec.addField(Messages.getMessage("Embed.Calendar.Pre.Description", settings), "***UNSET***", false);

            if (calendar.getTimezone() != null)
                spec.addField(Messages.getMessage("Embed.Calendar.Pre.TimeZone", settings), calendar.getTimezone(), true);
            else
                spec.addField(Messages.getMessage("Embed.Calendar.Pre.TimeZone", settings), "***UNSET***", true);

            if (calendar.getEditing())
                spec.addField(Messages.getMessage("Embed.Calendar.Pre.CalendarId", settings), calendar.getCalendarId(), false);


            spec.setFooter(Messages.getMessage("Embed.Calendar.Pre.Key", settings), null);
            spec.setColor(GlobalConst.discalColor);
        });
    }
}
