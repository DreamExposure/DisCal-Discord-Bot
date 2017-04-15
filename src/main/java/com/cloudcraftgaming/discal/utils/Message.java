package com.cloudcraftgaming.discal.utils;

import com.cloudcraftgaming.discal.Main;
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
    public static IMessage sendMessage(String message, MessageReceivedEvent event, IDiscordClient client) {
        return RequestBuffer.request(() -> {
            try {
                return new MessageBuilder(client).appendContent(message).withChannel(event.getMessage().getChannel()).build();
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
                return null;
            }
        }).get();
    }

    /**
     * Sends a message via Discord as DisCal.
     * @param message The message to send, with formatting.
     * @param channel The channel to send the message to.
     * @param client The Client associated with the Bot.
     */
    public static IMessage sendMessage(String message, IChannel channel, IDiscordClient client) {
        return RequestBuffer.request(() -> {
            try {
                return new MessageBuilder(client).appendContent(message).withChannel(channel).build();
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
                return null;
            }
        }).get();
    }

    /**
     * Sends a message via Discord as DisCal.
     * @param embed The EmbedObject to append to the message.
     * @param event The event received (to send to the same channel and guild).
     * @param client The Client associated with the Bot.
     */
    public static IMessage sendMessage(EmbedObject embed, MessageReceivedEvent event, IDiscordClient client) {
        return RequestBuffer.request(() -> {
            try {
                return new MessageBuilder(client).withEmbed(embed).withChannel(event.getMessage().getChannel()).build();

            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
                return null;
            }
        }).get();
    }

    /**
     * Sends a message via Discord as DisCal.
     * @param embed The EmbedObject to append to the message.
     * @param channel The channel to send the message to.
     * @param client The Client associated with the Bot.
     */
    public static IMessage sendMessage(EmbedObject embed, IChannel channel, IDiscordClient client) {
        return RequestBuffer.request(() -> {
            try {
                return new MessageBuilder(client).withEmbed(embed).withChannel(channel).build();
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
                return null;
            }
        }).get();
    }

    /**
     * Sends a message via Discord as DisCal.
     * @param embed The EmbedObject to append to the message.
     * @param message The message to send, with formatting.
     * @param event The event received (to send to the same channel and guild).
     * @param client The Client associated with the Bot.
     */
    public static IMessage sendMessage(EmbedObject embed, String message, MessageReceivedEvent event, IDiscordClient client) {
        return RequestBuffer.request(() -> {
            try {
                return new MessageBuilder(client).appendContent(message).withEmbed(embed).withChannel(event.getMessage().getChannel()).build();
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
                return null;
            }
        }).get();
    }

    /**
     * Sends a message via Discord as DisCal.
     * @param embed The EmbedObject to append to the message.
     * @param message The message to send, with formatting.
     * @param channel The channel to send the message to.
     * @param client The Client associated with the Bot.
     */
    public static IMessage sendMessage(EmbedObject embed, String message, IChannel channel, IDiscordClient client) {
        return RequestBuffer.request(() -> {
            try {
                return new MessageBuilder(client).appendContent(message).withEmbed(embed).withChannel(channel).build();
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
                return null;
            }
        }).get();
    }

    public static IMessage sendDirectMessage(String message, IUser user) {
        return RequestBuffer.request(() -> {
           try {
               IPrivateChannel pc = user.getOrCreatePMChannel();
               pc.sendMessage(message);
               return new MessageBuilder(Main.client).withChannel(pc).appendContent(message).build();
           } catch (DiscordException | MissingPermissionsException e) {
               //Failed to send message.
			   return null;
           }
        }).get();
    }

    public static IMessage sendDirectMessage(EmbedObject embed, IUser user) {
        return RequestBuffer.request(() -> {
            try {
                IPrivateChannel pc = user.getOrCreatePMChannel();
                return new MessageBuilder(Main.client).withChannel(pc).withEmbed(embed).build();
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
				return null;
            }
        }).get();
    }

    public static IMessage sendDirectMessage(String message, EmbedObject embed, IUser user) {
        return RequestBuffer.request(() -> {
            try {
                IPrivateChannel pc = user.getOrCreatePMChannel();
                return new MessageBuilder(Main.client).withChannel(pc).appendContent(message).withEmbed(embed).build();
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to send message.
				return null;
            }
        }).get();
    }

    public static boolean deleteMessage(MessageReceivedEvent event) {
        return RequestBuffer.request(() -> {
            try {
                if (!event.getMessage().isDeleted()) {
                    event.getMessage().delete();
                }
                return true;
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to delete
                return false;
            }
        }).get();
    }

    public static boolean deleteMessage(IMessage message) {
        return RequestBuffer.request(() -> {
           try {
               if (!message.isDeleted()) {
                   message.delete();
               }
               return true;
           } catch (DiscordException | MissingPermissionsException e) {
               //Failed to delete.
               return false;
           }
        }).get();
    }

    public static boolean editMessage(IMessage message, String content) {
        return RequestBuffer.request(() -> {
            try {
                if (!message.isDeleted()) {
                    message.edit(content);
                }
                return true;
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to edit.
                return false;
            }
        }).get();
    }

    public static boolean editMessage(IMessage message, String content, EmbedObject embed) {
        return RequestBuffer.request(() -> {
            try {
                if (!message.isDeleted()) {
                    message.edit(content, embed);
                }
                return true;
            } catch (DiscordException | MissingPermissionsException e) {
                //Failed to edit.
                return false;
            }
        }).get();
    }
}