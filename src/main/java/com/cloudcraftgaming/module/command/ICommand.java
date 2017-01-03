package com.cloudcraftgaming.module.command;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public interface ICommand {
    public String getCommand();

    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client);
}