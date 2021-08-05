package org.dreamexposure.discal.client.message;

import com.google.api.services.calendar.model.Calendar;
import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Image;
import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.PreCalendar;
import org.dreamexposure.discal.core.utils.GlobalVal;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class CalendarMessageFormatter {
    @Deprecated
    public static String getCalendarLink(final Snowflake guildId) {
        return BotSettings.BASE_URL.get() + "/embed/" + guildId.asString() + "/calendar/1";
    }

    @Deprecated
    public static Mono<EmbedCreateSpec> getCalendarLinkEmbed(final Calendar cal, final GuildSettings settings) {
        return DisCalClient.getClient().getGuildById(settings.getGuildID()).map(g -> {
            var builder = EmbedCreateSpec.builder();

            if (settings.getBranded())
                builder.author(g.getName(), BotSettings.BASE_URL.get(),
                    g.getIconUrl(Image.Format.PNG).orElse(GlobalVal.getIconUrl()));
            else
                builder.author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl());

            builder.title(Messages.getMessage("Embed.Calendar.Link.Title", settings));

            if (cal.getSummary() != null)
                builder.addField(Messages.getMessage("Embed.Calendar.Link.Summary", settings), cal.getSummary(), true);

            if (cal.getDescription() != null)
                builder.addField(Messages.getMessage("Embed.Calendar.Link.Description", settings), cal.getDescription(), true);

            builder.addField(Messages.getMessage("Embed.Calendar.Link.TimeZone", settings), cal.getTimeZone(), false);
            builder.url(CalendarMessageFormatter.getCalendarLink(settings.getGuildID()));
            builder.footer(Messages.getMessage("Embed.Calendar.Link.CalendarId", "%id%", cal.getId(), settings), null);
            builder.color(GlobalVal.getDiscalColor());

            return builder.build();
        });
    }


    public static Mono<EmbedCreateSpec> getPreCalendarEmbed(final PreCalendar calendar, final GuildSettings settings) {
        return DisCalClient.getClient().getGuildById(settings.getGuildID()).map(g -> {
            var builder = EmbedCreateSpec.builder();

            if (settings.getBranded())
                builder.author(g.getName(), BotSettings.BASE_URL.get(),
                    g.getIconUrl(Image.Format.PNG).orElse(GlobalVal.getIconUrl()));
            else
                builder.author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl());

            builder.title(Messages.getMessage("Embed.Calendar.Pre.Title", settings));
            if (!calendar.getSummary().isEmpty())
                builder.addField(Messages.getMessage("Embed.Calendar.Pre.Summary", settings), calendar.getSummary(), true);
            else
                builder.addField(Messages.getMessage("Embed.Calendar.Pre.Summary", settings), "***UNSET***", true);

            if (!calendar.getDescription().isEmpty())
                builder.addField(Messages.getMessage("Embed.Calendar.Pre.Description", settings), calendar.getDescription(), false);
            else
                builder.addField(Messages.getMessage("Embed.Calendar.Pre.Description", settings), "***UNSET***", false);

            if (calendar.getTimezone() != null)
                builder.addField(Messages.getMessage("Embed.Calendar.Pre.TimeZone", settings), calendar.getTimezone(), true);
            else
                builder.addField(Messages.getMessage("Embed.Calendar.Pre.TimeZone", settings), "***UNSET***", true);

            if (calendar.getEditing())
                builder.addField(Messages.getMessage("Embed.Calendar.Pre.CalendarId", settings), calendar.getCalendarId(), false);


            builder.footer(Messages.getMessage("Embed.Calendar.Pre.Key", settings), null);
            builder.color(GlobalVal.getDiscalColor());

            return builder.build();
        });
    }
}
