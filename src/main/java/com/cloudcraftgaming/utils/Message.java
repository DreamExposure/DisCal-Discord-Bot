package com.cloudcraftgaming.utils;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.MessageBuilder;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class Message {
    public static void sendMessage(String message, MessageReceivedEvent event, IDiscordClient client) {
        try {
            new MessageBuilder(client).appendContent(message).withChannel(event.getMessage().getChannel()).build();
        } catch (Exception e) {
            //Failed to send message.
        }
    }
}