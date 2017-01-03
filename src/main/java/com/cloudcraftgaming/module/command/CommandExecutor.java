package com.cloudcraftgaming.module.command;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.modules.IModule;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CommandExecutor implements IModule {
    IDiscordClient client;

    @Override
    public boolean enable(IDiscordClient _client) {
        client = _client;
        EventDispatcher dispatcher = client.getDispatcher();
        dispatcher.registerListener(new MessageListener(this));
        return true;
    }

    @Override
    public void disable() {

    }

    @Override
    public String getName() {
        return "CommandExecutor";
    }

    @Override
    public String getAuthor() {
        return "NovaFox161";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getMinimumDiscord4JVersion() {
        return "2.7.0";
    }
}