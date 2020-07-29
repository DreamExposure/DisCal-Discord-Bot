package org.dreamexposure.discal.client.module.command;

import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.utils.GeneralUtils;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CommandExecutor {
    private static final List<Command> commands = new ArrayList<>();

    private CommandExecutor() {
    }

    //Functional

    /**
     * Registers a command that can be executed.
     *
     * @param _command The command to register.
     */
    public static void registerCommand(Command _command) {
        commands.add(_command);
    }

    public static Mono<Void> issueCommand(String cmd, List<String> argsOr, MessageCreateEvent event, GuildSettings settings) {
        String[] args;
        if (argsOr.size() > 0) {
            String toParse = GeneralUtils.getContent(argsOr, 0);
            args = GeneralUtils.overkillParser(toParse).split(" ");
        } else {
            args = new String[0];
        }

        return Mono.from(getCommand(cmd)
            .flatMap(c -> c.issueCommand(args, event, settings)));
    }

    /**
     * Gets an ArrayList of all valid commands.
     *
     * @return An ArrayList of all valid commands.
     */
    public static ArrayList<String> getAllCommands() {
        ArrayList<String> cmds = new ArrayList<>();
        for (Command c : commands) {
            if (!cmds.contains(c.getCommand()))
                cmds.add(c.getCommand());
        }
        return cmds;
    }

    public static List<Command> getCommands() {
        return commands;
    }

    public static Mono<Command> getCommand(String cmdNameOrAlias) {
        return Flux.fromIterable(commands)
            .filter(c ->
                c.getCommand().equalsIgnoreCase(cmdNameOrAlias)
                    || c.getAliases().contains(cmdNameOrAlias.toLowerCase()))
            .next();
    }
}