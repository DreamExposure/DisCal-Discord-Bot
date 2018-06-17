package com.cloudcraftgaming.discal.api.message;

import com.cloudcraftgaming.discal.api.DisCalAPI;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("UnusedReturnValue")
public class Message {
	/**
	 * Sends a message via Discord as DisCal.
	 *
	 * @param message The message to send, with formatting.
	 * @param event   The Event received (to send to the same channel and guild).
	 */
	public static IMessage sendMessage(String message, MessageReceivedEvent event) {
		return RequestBuffer.request(() -> {
			try {
				return new MessageBuilder(DisCalAPI.getAPI().getClient()).appendContent(message).withChannel(event.getMessage().getChannel()).build();
			} catch (DiscordException | MissingPermissionsException e) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	/**
	 * Sends a message via Discord as DisCal.
	 *
	 * @param message The message to send, with formatting.
	 * @param channel The channel to send the message to.
	 */
	public static IMessage sendMessage(String message, IChannel channel) {
		return RequestBuffer.request(() -> {
			try {
				return new MessageBuilder(DisCalAPI.getAPI().getClient()).appendContent(message).withChannel(channel).build();
			} catch (DiscordException | MissingPermissionsException e) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	/**
	 * Sends a message via Discord as DisCal.
	 *
	 * @param embed The EmbedObject to append to the message.
	 * @param event The event received (to send to the same channel and guild).
	 */
	public static IMessage sendMessage(EmbedObject embed, MessageReceivedEvent event) {
		return RequestBuffer.request(() -> {
			try {
				return new MessageBuilder(DisCalAPI.getAPI().getClient()).withEmbed(embed).withChannel(event.getMessage().getChannel()).build();

			} catch (DiscordException | MissingPermissionsException e) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	/**
	 * Sends a message via Discord as DisCal.
	 *
	 * @param embed   The EmbedObject to append to the message.
	 * @param channel The channel to send the message to.
	 */
	public static IMessage sendMessage(EmbedObject embed, IChannel channel) {
		return RequestBuffer.request(() -> {
			try {
				return new MessageBuilder(DisCalAPI.getAPI().getClient()).withEmbed(embed).withChannel(channel).build();
			} catch (DiscordException | MissingPermissionsException e) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	/**
	 * Sends a message via Discord as DisCal.
	 *
	 * @param embed   The EmbedObject to append to the message.
	 * @param message The message to send, with formatting.
	 * @param event   The event received (to send to the same channel and guild).
	 */
	public static IMessage sendMessage(EmbedObject embed, String message, MessageReceivedEvent event) {
		return RequestBuffer.request(() -> {
			try {
				return new MessageBuilder(DisCalAPI.getAPI().getClient()).appendContent(message).withEmbed(embed).withChannel(event.getMessage().getChannel()).build();
			} catch (DiscordException | MissingPermissionsException e) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	/**
	 * Sends a message via Discord as DisCal.
	 *
	 * @param embed   The EmbedObject to append to the message.
	 * @param message The message to send, with formatting.
	 * @param channel The channel to send the message to.
	 */
	public static IMessage sendMessage(EmbedObject embed, String message, IChannel channel) {
		return RequestBuffer.request(() -> {
			try {
				return new MessageBuilder(DisCalAPI.getAPI().getClient()).appendContent(message).withEmbed(embed).withChannel(channel).build();
			} catch (DiscordException | MissingPermissionsException e) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	public static void sendMessageAsync(EmbedObject embedObject, String message, IChannel channel) {
		RequestBuffer.request(() -> {
			try {
				new MessageBuilder(DisCalAPI.getAPI().getClient()).appendContent(message).withEmbed(embedObject).withChannel(channel).build();
			} catch (DiscordException | MissingPermissionsException e) {
				//No reason to print exception.
			}
		});
	}

	public static IMessage sendDirectMessage(String message, IUser user) {
		return RequestBuffer.request(() -> {
			try {
				IPrivateChannel pc = user.getOrCreatePMChannel();
				return new MessageBuilder(DisCalAPI.getAPI().getClient()).withChannel(pc).appendContent(message).build();
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
				return new MessageBuilder(DisCalAPI.getAPI().getClient()).withChannel(pc).withEmbed(embed).build();
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
				return new MessageBuilder(DisCalAPI.getAPI().getClient()).withChannel(pc).appendContent(message).withEmbed(embed).build();
			} catch (DiscordException | MissingPermissionsException e) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	public static boolean deleteMessage(MessageReceivedEvent event) {
		try {
			return RequestBuffer.request(() -> {
				try {
					if (!event.getMessage().isDeleted())
						event.getMessage().delete();

					return true;
				} catch (DiscordException | MissingPermissionsException e) {
					//Failed to delete
					return false;
				}
			}).get();
		} catch (NullPointerException e) {
			return false;
		}
	}

	public static boolean deleteMessage(IMessage message) {
		try {
			return RequestBuffer.request(() -> {
				try {
					if (!message.isDeleted())
						message.delete();

					return true;
				} catch (DiscordException | MissingPermissionsException e) {
					//Failed to delete.
					return false;
				}
			}).get();
		} catch (NullPointerException e) {
			return false;
		}
	}

	public static boolean editMessage(IMessage message, String content) {
		try {
			return RequestBuffer.request(() -> {
				try {
					if (message != null && !message.isDeleted())
						message.edit(content);

					return true;
				} catch (DiscordException | MissingPermissionsException e) {
					//Failed to edit.
					return false;
				}
			}).get();
		} catch (NullPointerException e) {
			return false;
		}
	}

	public static boolean editMessage(IMessage message, String content, EmbedObject embed) {
		try {
			return RequestBuffer.request(() -> {
				try {
					if (!message.isDeleted())
						message.edit(content, embed);

					return true;
				} catch (DiscordException | MissingPermissionsException e) {
					//Failed to edit.
					return false;
				}
			}).get();
		} catch (NullPointerException e) {
			return false;
		}
	}
}