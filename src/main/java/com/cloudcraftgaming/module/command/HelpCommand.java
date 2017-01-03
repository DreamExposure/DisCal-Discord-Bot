package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.utils.Message;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class HelpCommand implements ICommand {
    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        Message.sendMessage("All commands: addCalendar, linkCalendar, help", event, client);
        return true;
    }
}
