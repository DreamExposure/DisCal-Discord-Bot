package org.dreamexposure.discal.client.message;

import org.dreamexposure.discal.core.file.ReadFile;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.json.JSONArray;
import org.json.JSONObject;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
public class MessageManager {
	private static JSONObject langs;

	//Lang handling
	public static boolean reloadLangs() {
		try {
			langs = ReadFile.readAllLangFiles();
			return true;
		} catch (Exception e) {
			Logger.getLogger().exception(null, "Failed to reload lang files!", e, MessageManager.class);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String> getLangs() {

		return new ArrayList<String>(langs.keySet());
	}

	public static boolean isSupported(String _value) {
		JSONArray names = langs.names();
		for (int i = 0; i < names.length(); i++) {
			if (_value.equalsIgnoreCase(names.getString(i)))
				return true;
		}
		return false;
	}

	public static String getValidLang(String _value) {
		JSONArray names = langs.names();
		for (int i = 0; i < names.length(); i++) {
			if (_value.equalsIgnoreCase(names.getString(i)))
				return names.getString(i);
		}
		return "ENGLISH";
	}

	public static String getMessage(String key, GuildSettings settings) {
		JSONObject messages;

		if (settings.getLang() != null && langs.has(settings.getLang()))
			messages = langs.getJSONObject(settings.getLang());
		else
			messages = langs.getJSONObject("ENGLISH");

		if (messages.has(key))
			return messages.getString(key).replace("%lb%", GlobalConst.lineBreak);
		else
			return "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!! Message requested: " + key;
	}

	public static String getMessage(String key, String var, String replace, GuildSettings settings) {
		JSONObject messages;

		if (settings.getLang() != null && langs.has(settings.getLang()))
			messages = langs.getJSONObject(settings.getLang());
		else
			messages = langs.getJSONObject("ENGLISH");

		if (messages.has(key))
			return messages.getString(key).replace(var, replace).replace("%lb%", GlobalConst.lineBreak);
		else
			return "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!! Message requested: " + key;
	}

	//Message sending
	public static void sendMessageAsync(String message, IChannel channel) {
		RequestBuffer.request(() -> {
			try {
				new MessageBuilder(channel.getClient()).appendContent(message).withChannel(channel).build();
			} catch (DiscordException | MissingPermissionsException ignore) {
				//No reason to print exception.
			}
		});
	}

	public static void sendMessageAsync(EmbedObject embed, IChannel channel) {
		RequestBuffer.request(() -> {
			try {
				new MessageBuilder(channel.getClient()).withEmbed(embed).withChannel(channel).build();
			} catch (DiscordException | MissingPermissionsException ignore) {
				//No reason to print exception.
			}
		});
	}

	public static void sendMessageAsync(String message, EmbedObject embed, IChannel channel) {
		RequestBuffer.request(() -> {
			try {
				new MessageBuilder(channel.getClient()).appendContent(message).withEmbed(embed).withChannel(channel).build();
			} catch (DiscordException | MissingPermissionsException ignore) {
				//No reason to print exception.
			}
		});
	}

	public static void sendMessageAsync(String message, MessageReceivedEvent event) {
		RequestBuffer.request(() -> {
			try {
				new MessageBuilder(event.getClient()).appendContent(message).withChannel(event.getChannel()).build();
			} catch (DiscordException | MissingPermissionsException ignore) {
				//No reason to print exception.
			}
		});
	}

	public static void sendMessageAsync(EmbedObject embed, MessageReceivedEvent event) {
		RequestBuffer.request(() -> {
			try {
				new MessageBuilder(event.getClient()).withEmbed(embed).withChannel(event.getChannel()).build();
			} catch (DiscordException | MissingPermissionsException ignore) {
				//No reason to print exception.
			}
		});
	}

	public static void sendMessageAsync(String message, EmbedObject embed, MessageReceivedEvent event) {
		RequestBuffer.request(() -> {
			try {
				new MessageBuilder(event.getClient()).appendContent(message).withEmbed(embed).withChannel(event.getChannel()).build();
			} catch (DiscordException | MissingPermissionsException ignore) {
				//No reason to print exception.
			}
		});
	}

	public static IMessage sendMessageSync(String message, MessageReceivedEvent event) {
		return RequestBuffer.request(() -> {
			try {
				return new MessageBuilder(event.getClient()).appendContent(message).withChannel(event.getMessage().getChannel()).build();
			} catch (DiscordException | MissingPermissionsException e) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	public static IMessage sendMessageSync(EmbedObject embed, MessageReceivedEvent event) {
		return RequestBuffer.request(() -> {
			try {
				return new MessageBuilder(event.getClient()).withEmbed(embed).withChannel(event.getMessage().getChannel()).build();
			} catch (DiscordException | MissingPermissionsException e) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	public static IMessage sendMessageSync(String message, EmbedObject embed, MessageReceivedEvent event) {
		return RequestBuffer.request(() -> {
			try {
				return new MessageBuilder(event.getClient()).appendContent(message).withEmbed(embed).withChannel(event.getMessage().getChannel()).build();
			} catch (DiscordException | MissingPermissionsException e) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	public static IMessage sendMessageSync(String message, IChannel channel) {
		return RequestBuffer.request(() -> {
			try {
				return new MessageBuilder(channel.getClient()).appendContent(message).withChannel(channel).build();
			} catch (DiscordException | MissingPermissionsException e) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	public static IMessage sendMessageSync(EmbedObject embed, IChannel channel) {
		return RequestBuffer.request(() -> {
			try {
				return new MessageBuilder(channel.getClient()).withEmbed(embed).withChannel(channel).build();
			} catch (DiscordException | MissingPermissionsException e) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	public static IMessage sendMessageSync(String message, EmbedObject embed, IChannel channel) {
		return RequestBuffer.request(() -> {
			try {
				return new MessageBuilder(channel.getClient()).appendContent(message).withEmbed(embed).withChannel(channel).build();
			} catch (DiscordException | MissingPermissionsException e) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	public static void sendDirectMessageAsync(String message, IUser user) {
		RequestBuffer.request(() -> {
			try {
				IPrivateChannel pc = user.getOrCreatePMChannel();
				new MessageBuilder(user.getClient()).withChannel(pc).appendContent(message).build();
			} catch (DiscordException | MissingPermissionsException ignore) {
				//Failed to send message.
			}
		});
	}

	public static void sendDirectMessageAsync(EmbedObject embed, IUser user) {
		RequestBuffer.request(() -> {
			try {
				IPrivateChannel pc = user.getOrCreatePMChannel();
				new MessageBuilder(user.getClient()).withChannel(pc).withEmbed(embed).build();
			} catch (DiscordException | MissingPermissionsException ignore) {
				//Failed to send message.
			}
		});
	}

	public static void sendDirectMessageAsync(String message, EmbedObject embed, IUser user) {
		RequestBuffer.request(() -> {
			try {
				IPrivateChannel pc = user.getOrCreatePMChannel();
				new MessageBuilder(user.getClient()).withChannel(pc).appendContent(message).withEmbed(embed).build();
			} catch (DiscordException | MissingPermissionsException ignore) {
				//Failed to send message.
			}
		});
	}

	public static IMessage sendDirectMessageSync(String message, IUser user) {
		return RequestBuffer.request(() -> {
			try {
				IPrivateChannel pc = user.getOrCreatePMChannel();
				return new MessageBuilder(user.getClient()).withChannel(pc).appendContent(message).build();
			} catch (DiscordException | MissingPermissionsException ignore) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	public static IMessage sendDirectMessageSync(EmbedObject embed, IUser user) {
		return RequestBuffer.request(() -> {
			try {
				IPrivateChannel pc = user.getOrCreatePMChannel();
				return new MessageBuilder(user.getClient()).withChannel(pc).withEmbed(embed).build();
			} catch (DiscordException | MissingPermissionsException ignore) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	public static IMessage sendDirectMessageSync(EmbedObject embed, String message, IUser user) {
		return RequestBuffer.request(() -> {
			try {
				IPrivateChannel pc = user.getOrCreatePMChannel();
				return new MessageBuilder(user.getClient()).withChannel(pc).appendContent(message).withEmbed(embed).build();
			} catch (DiscordException | MissingPermissionsException ignore) {
				//Failed to send message.
				return null;
			}
		}).get();
	}

	//Message editing
	public static void editMessage(String content, IMessage message) {
		try {
			RequestBuffer.request(() -> {
				try {
					if (message != null && !message.isDeleted())
						message.edit(content);
				} catch (DiscordException | MissingPermissionsException e) {
					//Failed to edit.
				}
			});
		} catch (NullPointerException ignore) {
		}
	}

	public static void editMessage(String content, EmbedObject embed, IMessage message) {
		try {
			RequestBuffer.request(() -> {
				try {
					if (message != null && !message.isDeleted())
						message.edit(content, embed);
				} catch (DiscordException | MissingPermissionsException e) {
					//Failed to edit.
				}
			});
		} catch (NullPointerException ignore) {
		}
	}

	public static void editMessage(String content, MessageReceivedEvent event) {
		try {
			RequestBuffer.request(() -> {
				try {
					if (event.getMessage() != null && !event.getMessage().isDeleted())
						event.getMessage().edit(content);

				} catch (DiscordException | MissingPermissionsException e) {
					//Failed to edit.
				}
			});
		} catch (NullPointerException ignore) {
		}
	}

	public static void editMessage(String content, EmbedObject embed, MessageReceivedEvent event) {
		try {
			RequestBuffer.request(() -> {
				try {
					if (event.getMessage() != null && !event.getMessage().isDeleted())
						event.getMessage().edit(content, embed);

				} catch (DiscordException | MissingPermissionsException e) {
					//Failed to edit.
				}
			});
		} catch (NullPointerException ignore) {
		}
	}

	//Message deleting
	public static void deleteMessage(IMessage message) {
		try {
			RequestBuffer.request(() -> {
				try {
					if (!message.isDeleted())
						message.delete();
				} catch (DiscordException | MissingPermissionsException e) {
					//Failed to delete.
				}
			});
		} catch (NullPointerException ignore) {
		}
	}

	public static void deleteMessage(MessageReceivedEvent event) {
		try {
			RequestBuffer.request(() -> {
				try {
					if (!event.getMessage().isDeleted())
						event.getMessage().delete();
				} catch (DiscordException | MissingPermissionsException e) {
					//Failed to delete.
				}
			});
		} catch (NullPointerException ignore) {
		}
	}
}