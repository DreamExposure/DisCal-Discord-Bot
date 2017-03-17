package com.cloudcraftgaming.discal.module.command;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.util.ArrayList;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CommandExecutor {
    private static CommandExecutor instance;
    private IDiscordClient client;
    private final ArrayList<ICommand> commands = new ArrayList<>();

    private CommandExecutor() {}

    public static CommandExecutor getExecutor() {
        if (instance == null) {
            instance = new CommandExecutor();
        }
        return instance;
    }

    public CommandExecutor enable(IDiscordClient _client) {
        client = _client;
        EventDispatcher dispatcher = client.getDispatcher();
        dispatcher.registerListener(new CommandListener(this));
        return instance;
    }


    //Functionals
    public Boolean registerCommand(ICommand _command) {
        commands.add(_command);
        return true;
    }

    void issueCommand(String cmd, String[] args, MessageReceivedEvent event) {
        for (ICommand c : commands) {
            if (c.getCommand().equalsIgnoreCase(cmd)) {
                c.issueCommand(args, event, client);
            }
        }

    }

    ArrayList<String> getAllCommands() {
        ArrayList<String> cmds = new ArrayList<>();
        for (ICommand c : commands) {
            if (!cmds.contains(c.getCommand())) {
                cmds.add(c.getCommand());
            }
        }
        return cmds;
    }
}