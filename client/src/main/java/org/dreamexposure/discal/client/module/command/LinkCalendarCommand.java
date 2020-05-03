package org.dreamexposure.discal.client.module.command;

import org.dreamexposure.discal.client.message.CalendarMessageFormatter;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;

import java.util.ArrayList;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class LinkCalendarCommand implements Command {
    /**
     * Gets the command this Object is responsible for.
     *
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "LinkCalendar";
    }

    /**
     * Gets the short aliases of the command this object is responsible for.
     * </br>
     * This will return an empty ArrayList if none are present
     *
     * @return The aliases of the command.
     */
    @Override
    public ArrayList<String> getAliases() {
        ArrayList<String> aliases = new ArrayList<>();
        aliases.add("linkcal");
        aliases.add("calendarlink");
        aliases.add("callink");
        aliases.add("linkcallador");
        return aliases;
    }

    /**
     * Gets the info on the command (not sub command) to be used in help menus.
     *
     * @return The command info.
     */
    @Override
    public CommandInfo getCommandInfo() {
        return new CommandInfo(
            "linkCalendar",
            "Links the guild's calendar in a pretty embed!",
            "!linkCalendar"
        );
    }

    /**
     * Issues the command this Object is responsible for.
     *
     * @param args  The command arguments.
     * @param event The event received.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public Mono<Void> issueCommand(String[] args, MessageCreateEvent event, GuildSettings settings) {
        //TODO: Support multiple calendars...
        return DatabaseManager.getMainCalendar(settings.getGuildID())
            .flatMap(calData -> CalendarWrapper.getCalendar(calData, settings)
                .flatMap(cal -> CalendarMessageFormatter.getCalendarLinkEmbed(cal, calData.getCalendarNumber(), settings)
                    .flatMap(embed -> Messages.sendMessage(embed, event))
                ).switchIfEmpty(Messages.sendMessage(Messages.getMessage("Notification.Error.Unknown", settings), event))
            ).switchIfEmpty(Messages.sendMessage(Messages.getMessage("Creator.Calendar.NoCalendar", settings), event))
            .then();
    }
}