package org.dreamexposure.discal.client.module.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Image;
import org.dreamexposure.discal.client.message.CalendarMessageFormatter;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Created by Nova Fox on 6/16/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class TimeCommand implements Command {

    /**
     * Gets the command this Object is responsible for.
     *
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "time";
    }

    /**
     * Gets the short aliases of the command this object is responsible for.
     * <br>
     * This will return an empty ArrayList if none are present
     *
     * @return The aliases of the command.
     */
    @Override
    public ArrayList<String> getAliases() {
        return new ArrayList<>();
    }

    /**
     * Gets the info on the command (not sub command) to be used in help menus.
     *
     * @return The command info.
     */
    @Override
    public CommandInfo getCommandInfo() {
        return new CommandInfo(
            "time",
            "Displays the current time for the calendar in its respective TimeZone.",
            "!time"
        );
    }

    /**
     * Issues the command this Object is responsible for.
     *
     * @param args     The command arguments.
     * @param event    The event received.
     * @param settings The guild settings.
     * @return {@code true} if successful, else {@code false}.
     */
    @Override
    public Mono<Void> issueCommand(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return this.calendarTime(event, settings);
    }

    //TODO: Support multiple calendars
    private Mono<Void> calendarTime(final MessageCreateEvent event, final GuildSettings settings) {
        return DatabaseManager.INSTANCE.getMainCalendar(settings.getGuildID())
            .flatMap(calData -> CalendarWrapper.getCalendar(calData).flatMap(cal -> {
                final LocalDateTime ldt = LocalDateTime.now(ZoneId.of(cal.getTimeZone()));

                DateTimeFormatter fmt;
                if (settings.getTwelveHour()) fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss a");
                else fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");


                final String correctTime = fmt.format(ldt);

                return event.getGuild().flatMap(guild ->
                    Messages.sendMessage(embed -> {
                        if (settings.getBranded()) {
                            embed.setAuthor(guild.getName(), GlobalConst.discalSite,
                                guild.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                        } else {
                            embed.setAuthor("DisCal", GlobalConst.discalSite,
                                GlobalConst.iconUrl);
                        }

                        embed.setTitle(Messages.getMessage("Embed.Time.Title", settings));

                        embed.addField(Messages.getMessage("Embed.Time.Time", settings), correctTime, false);

                        embed.addField(Messages.getMessage("Embed.Time.TimeZone", settings), cal.getTimeZone(), false);

                        embed.setFooter(Messages.getMessage("Embed.Time.Footer", settings), null);
                        embed.setUrl(CalendarMessageFormatter.getCalendarLink(settings.getGuildID(), calData.getCalendarNumber()));

                        embed.setColor(GlobalConst.discalColor);
                    }, event));
            }))
            .switchIfEmpty(Messages.sendMessage(
                Messages.getMessage("Creator.Calendar.NoCalendar", settings), event))
            .then();
    }
}
