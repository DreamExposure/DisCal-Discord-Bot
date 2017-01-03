package com.cloudcraftgaming.module.command;

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
    private IDiscordClient client;
    private final ArrayList<ICommand> commands = new ArrayList<>();

    public CommandExecutor enable(IDiscordClient _client) {
        client = _client;
        EventDispatcher dispatcher = client.getDispatcher();
        dispatcher.registerListener(new MessageListener(this));
        return this;
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
}