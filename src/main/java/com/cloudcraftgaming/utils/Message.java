package com.cloudcraftgaming.utils;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.MessageBuilder;


/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class Message {
    public static String lineBreak = System.getProperty("line.separator");

    public static void sendMessage(String message, MessageReceivedEvent event, IDiscordClient client) {
        try {
            new MessageBuilder(client).appendContent(message).withChannel(event.getMessage().getChannel()).build();
        } catch (Exception e) {
            //Failed to send message.
        }
    }

    public static void sendMessage(String message, IChannel channel, IDiscordClient client) {
        try {
            new MessageBuilder(client).appendContent(message).withChannel(channel).build();
        } catch (Exception e) {
            //Failed to send message.
        }
    }
    
    public static void sendMessage(EmbedObject embed, MessageReceivedEvent event, IDiscordClient client) {
        try {
            new MessageBuilder(client).withEmbed(embed).withChannel(event.getMessage().getChannel()).build();
        } catch (Exception e) {
            //Failed to send message.
        }
    }

    public static void sendMessage(EmbedObject embed, IChannel channel, IDiscordClient client) {
        try {
            new MessageBuilder(client).withEmbed(embed).withChannel(channel).build();
        } catch (Exception e) {
            //Failed to send message.
        }
    }

    public static void sendMessage(EmbedObject embed, String message, MessageReceivedEvent event, IDiscordClient client) {
        try {
            new MessageBuilder(client).appendContent(message).withEmbed(embed).withChannel(event.getMessage().getChannel()).build();
        } catch (Exception e) {
            //Failed to send message.
        }
    }

    public static void sendMessage(EmbedObject embed, String message, IChannel channel, IDiscordClient client) {
        try {
            new MessageBuilder(client).appendContent(message).withEmbed(embed).withChannel(channel).build();
        } catch (Exception e) {
            //Failed to send message.
        }
    }
}