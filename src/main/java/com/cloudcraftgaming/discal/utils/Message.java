package com.cloudcraftgaming.discal.utils;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;


/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class Message {
    public static String lineBreak = System.getProperty("line.separator");

    /**
     * Sends a message via Discord as DisCal.
     * @param message The message to send, with formatting.
     * @param event The Event received (to send to the same channel and guild).
     * @param client The Client associated with the Bot.
     */
    public static void sendMessage(String message, MessageReceivedEvent event, IDiscordClient client) {
        RequestBuffer.request(() -> {
            try {
                new MessageBuilder(client).appendContent(message).withChannel(event.getMessage().getChannel()).build();
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
            }
        });
    }

    /**
     * Sends a message via Discord as DisCal.
     * @param message The message to send, with formatting.
     * @param channel The channel to send the message to.
     * @param client The Client associated with the Bot.
     */
    public static void sendMessage(String message, IChannel channel, IDiscordClient client) {
        RequestBuffer.request(() -> {
            try {
                new MessageBuilder(client).appendContent(message).withChannel(channel).build();
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
            }
        });
    }

    /**
     * Sends a message via Discord as DisCal.
     * @param embed The EmbedObject to append to the message.
     * @param event The event received (to send to the same channel and guild).
     * @param client The Client associated with the Bot.
     */
    public static void sendMessage(EmbedObject embed, MessageReceivedEvent event, IDiscordClient client) {
        RequestBuffer.request(() -> {
            try {
                new MessageBuilder(client).withEmbed(embed).withChannel(event.getMessage().getChannel()).build();
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
            }
        });
    }

    /**
     * Sends a message via Discord as DisCal.
     * @param embed The EmbedObject to append to the message.
     * @param channel The channel to send the message to.
     * @param client The Client associated with the Bot.
     */
    public static void sendMessage(EmbedObject embed, IChannel channel, IDiscordClient client) {
        RequestBuffer.request(() -> {
            try {
                new MessageBuilder(client).withEmbed(embed).withChannel(channel).build();
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
            }
        });
    }

    /**
     * Sends a message via Discord as DisCal.
     * @param embed The EmbedObject to append to the message.
     * @param message The message to send, with formatting.
     * @param event The event received (to send to the same channel and guild).
     * @param client The Client associated with the Bot.
     */
    public static void sendMessage(EmbedObject embed, String message, MessageReceivedEvent event, IDiscordClient client) {
        RequestBuffer.request(() -> {
            try {
                new MessageBuilder(client).appendContent(message).withEmbed(embed).withChannel(event.getMessage().getChannel()).build();
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
            }
        });
    }

    /**
     * Sends a message via Discord as DisCal.
     * @param embed The EmbedObject to append to the message.
     * @param message The message to send, with formatting.
     * @param channel The channel to send the message to.
     * @param client The Client associated with the Bot.
     */
    public static void sendMessage(EmbedObject embed, String message, IChannel channel, IDiscordClient client) {
        RequestBuffer.request(() -> {
            try {
                new MessageBuilder(client).appendContent(message).withEmbed(embed).withChannel(channel).build();
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
            }
        });
    }

    public static void sendDirectMessage(String message, IUser user) {
        RequestBuffer.request(() -> {
           try {
               IPrivateChannel pc = user.getOrCreatePMChannel();
               pc.sendMessage(message);
           } catch (DiscordException | MissingPermissionsException e) {
               //Failed to send message.
           }
        });
    }

    public static void sendDirectMessage(EmbedObject embed, IUser user) {
        RequestBuffer.request(() -> {
            try {
                IPrivateChannel pc = user.getOrCreatePMChannel();
                pc.sendMessage("", embed, false);
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
            }
        });
    }

    public static void sendDirectMessage(String message, EmbedObject embed, IUser user) {
        RequestBuffer.request(() -> {
            try {
                IPrivateChannel pc = user.getOrCreatePMChannel();
                pc.sendMessage(message, embed, false);
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
            }
        });
    }

    public static boolean deleteMessage(MessageReceivedEvent event) {
        RequestBuffer.request(() -> {
            try {
                if (!event.getMessage().isDeleted()) {
                    event.getMessage().delete();
                }
                return true;
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to delete
                return false;
            }
        });
        return false;
    }

    public static boolean deleteMessage(IMessage message) {
        RequestBuffer.request(() -> {
           try {
               if (!message.isDeleted()) {
                   message.delete();
               }
               return true;
           } catch (DiscordException | MissingPermissionsException e) {
               //Failed to delete.
               return false;
           }
        });
        return false;
    }
}