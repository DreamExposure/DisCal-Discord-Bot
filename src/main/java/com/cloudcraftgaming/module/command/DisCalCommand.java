package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.utils.Message;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

/**
 * Created by Nova Fox on 1/5/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class DisCalCommand implements ICommand {
    @Override
    public String getCommand() {
        return "Discal";
    }

    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        Message.sendMessage("DisCal is the official Discord Calendar Bot", event, client);
        Message.sendMessage("For more information about DisCal, please visit: https://www.cloudcraftgaming.com/discal/", event, client);
        return false;
    }
}
