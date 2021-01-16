package org.dreamexposure.discal.client.module.command;

import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.client.network.google.GoogleExternalAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.PermissionChecker;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;

import java.util.ArrayList;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 6/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AddCalendarCommand implements Command {
    /**
     * Gets the command this Object is responsible for.
     *
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "addCalendar";
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
        final ArrayList<String> aliases = new ArrayList<>();
        aliases.add("addcal");

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
            "addCalendar",
            "Starts the process of adding an external calendar",
            "!addCalendar (calendar ID)"
        );
    }

    /**
     * Issues the command this Object is responsible for.
     *
     * @param args  The command arguments.
     * @param event The event received.
     * @return {@code true} if successful, else {@code false}.
     */
    @Override
    public Mono<Void> issueCommand(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return Mono.just(settings)
            .filter(s -> s.isDevGuild() || s.isPatronGuild())
            .flatMap(s -> PermissionChecker.hasManageServerRole(event)
                .filter(identity -> identity)
                .flatMap(ignore -> {
                    if (args.length == 0) {
                        return DatabaseManager.getMainCalendar(settings.getGuildID())
                            .hasElement()
                            .flatMap(hasCal -> {
                                if (hasCal) {
                                    return Messages.sendMessage(
                                        Messages.getMessage("Creator.Calendar.HasCalendar", settings), event);
                                } else {
                                    return GoogleExternalAuth.getAuth().requestCode(event, settings)
                                        .then(Messages.sendMessage(
                                            Messages.getMessage("AddCalendar.Start", settings), event));
                                }
                            });
                    } else if (args.length == 1) {
                        return DatabaseManager.getMainCalendar(settings.getGuildID()).hasElement().flatMap(hasCal -> {
                            if (hasCal) {
                                return Messages.sendMessage(Messages.getMessage("Creator.Calendar.HasCalendar", settings), event);
                            } else if ("N/a".equalsIgnoreCase(settings.getEncryptedAccessToken())
                                && "N/a".equalsIgnoreCase(settings.getEncryptedRefreshToken())) {
                                return Messages.sendMessage(Messages.getMessage("AddCalendar.Select.NotAuth", settings), event);
                            } else {
                                return CalendarWrapper.getUsersExternalCalendars(settings)
                                    .flatMapMany(Flux::fromIterable)
                                    .any(c -> !c.isDeleted() && c.getId().equals(args[0]))
                                    .flatMap(valid -> {
                                        if (valid) {
                                            final CalendarData data = new CalendarData(settings.getGuildID(), 1,
                                                args[0], args[0], true, 0);

                                            //update guild settings to reflect changes...
                                            settings.setUseExternalCalendar(true);

                                            //combine db calls and message send to be executed together async
                                            final Mono<Boolean> calInsert = DatabaseManager.updateCalendar(data);
                                            final Mono<Boolean> settingsUpdate = DatabaseManager.updateSettings(settings);
                                            final Mono<Message> sendMsg = Messages.sendMessage(
                                                Messages.getMessage("AddCalendar.Select.Success", settings), event);

                                            return Mono.when(calInsert, settingsUpdate, sendMsg)
                                                .thenReturn(GlobalConst.NOT_EMPTY);
                                        } else {
                                            return Messages.sendMessage(Messages
                                                .getMessage("AddCalendar.Select.Failure.Invalid", settings), event);
                                        }
                                    });
                            }
                        });
                    } else {
                        //Invalid argument count...
                        return Messages.sendMessage(Messages.getMessage("AddCalendar.Specify", settings), event);
                    }
                })
                .switchIfEmpty(Messages.sendMessage(Messages.getMessage("Notification.Perm.MANAGE_SERVER", settings), event))
            )
            .switchIfEmpty(Messages.sendMessage(Messages.getMessage("Notification.Patron", settings), event))
            .then();
    }
}
