package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.event.EventUtils;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.module.announcement.*;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import com.cloudcraftgaming.discal.utils.*;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementCommand implements ICommand {

	/**
	 * Gets the command this Object is responsible for.
	 *
	 * @return The command this Object is responsible for.
	 */
	@Override
	public String getCommand() {
		return "Announcement";
	}

	/**
	 * Gets the short aliases of the command this object is responsible for.
	 * </br>
	 * This will return an empty ArrayList if none are present
	 *
	 * @return The aliases of the command.
	 */
	@Override
	public ArrayList<String> getAliases() {
		ArrayList<String> aliases = new ArrayList<>();
		aliases.add("announcements");
		aliases.add("announce");
		aliases.add("alert");
		aliases.add("alerts");
		aliases.add("a");
		aliases.add("ann");
		return aliases;
	}

	/**
	 * Gets the info on the command (not sub command) to be used in help menus.
	 *
	 * @return The command info.
	 */
	@Override
	public CommandInfo getCommandInfo() {
		CommandInfo info = new CommandInfo("announcement");
		info.setDescription("Used for all announcement functions.");
		info.setExample("!announcement <function> (value(s))");

		info.getSubCommands().add("create");
		info.getSubCommands().add("copy");
		info.getSubCommands().add("edit");
		info.getSubCommands().add("confirm");
		info.getSubCommands().add("cancel");
		info.getSubCommands().add("delete");
		info.getSubCommands().add("view");
		info.getSubCommands().add("review");
		info.getSubCommands().add("subscribe");
		info.getSubCommands().add("unsubscribe");
		info.getSubCommands().add("type");
		info.getSubCommands().add("hours");
		info.getSubCommands().add("minutes");
		info.getSubCommands().add("list");
		info.getSubCommands().add("event");
		info.getSubCommands().add("info");

		return info;
	}

	/**
	 * Issues the command this Object is responsible for.
	 *
	 * @param args  The command arguments.
	 * @param event The event received.
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	@Override
	public Boolean issueCommand(String[] args, MessageReceivedEvent event) {
		if (PermissionChecker.hasSufficientRole(event)) {
			if (args.length < 1) {
				Message.sendMessage(MessageManager.getMessage("Notification.Args.Few", event), event);
			} else if (args.length >= 1) {
				String guildId = event.getMessage().getGuild().getID();
				GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);
				switch (args[0].toLowerCase()) {
					case "create":
						moduleCreate(event);
						break;
					case "confirm":
						moduleConfirm(event);
						break;
					case "cancel":
						moduleCancel(event);
						break;
					case "delete":
						moduleDelete(args, event);
						break;
					case "view":
					case "review":
						moduleView(args, event);
						break;
					case "subscribe":
					case "sub":
						moduleSubscribe(args, event);
						break;
					case "unsubscribe":
					case "unsub":
						moduleUnsubscribe(args, event);
						break;
					case "type":
						moduleType(args, event);
						break;
					case "hours":
						moduleHours(args, event);
						break;
					case "minutes":
						moduleMinutes(args, event);
						break;
					case "list":
						moduleList(args, event);
						break;
					case "event":
						moduleEvent(args, event);
						break;
					case "info":
						moduleInfo(args, event);
						break;
					case "channel":
						moduleChannel(args, event);
						break;
					case "color":
					case "colour":
						moduleColor(args, event);
						break;
					case "copy":
						moduleCopy(args, event);
						break;
					case "edit":
						if (settings.isDevGuild()) {
							moduleEdit(args, event);
						} else {
							Message.sendMessage(MessageManager.getMessage("Notification.Disabled", settings), event);
						}
						break;
					case "devsub":
						if (settings.isDevGuild()) {
							moduleSubscribeRewrite(args, event);
						} else {
							Message.sendMessage(MessageManager.getMessage("Notification.Disabled", settings), event);
						}
						break;
					case "devunsub":
						if (settings.isDevGuild()) {
							moduleUnsubscribeRewrite(args, event);
						} else {
							Message.sendMessage(MessageManager.getMessage("Notification.Disabled", settings), event);
						}
						break;
					default:
						Message.sendMessage(MessageManager.getMessage("Notification.Args.Invalid", event), event);
						break;
				}
			}
		} else {
			Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", event), event);
		}
		return false;
	}


	private void moduleCreate(MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();

		if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
			AnnouncementCreator.getCreator().init(event);
			Message.sendMessage(
					"Announcement creator initialized!" + Message.lineBreak + "Please specify the type:"
							+ Message.lineBreak
							+ "`UNIVERSAL` for all events, or `SPECIFIC` for a specific event, `COLOR` for events with a specific color, or `RECUR` for recurring events.",
					event);
		} else {
			Message.sendMessage("Announcement creator has already been started!", event);
		}
	}

	private void moduleEdit(String[] args, MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();
		if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
			if (args.length == 2) {
				String anId = args[1];
				if (AnnouncementUtils.announcementExists(anId, event)) {
					Announcement announcement = AnnouncementCreator.getCreator().edit(event, anId);

					Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(announcement),
							"Announcement Editor initiated! Edit the values and then confirm your edits with `!announcement confirm`",
							event);
				} else {
					Message.sendMessage(
							"I can't seem to find an announcement with that ID. Are you sure you typed it correctly?",
							event);
				}
			} else {
				Message.sendMessage(
						"Please specify the ID of the announcement to edit with `!announcement edit <ID>`",
						event);
			}
		} else {
			Message.sendMessage("Announcement Creator has already been initialized!", event);
		}
	}

	private void moduleConfirm(MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();
		if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
			AnnouncementCreatorResponse acr = AnnouncementCreator.getCreator().confirmAnnouncement(event);
			if (acr.isSuccessful()) {
				if (acr.getAnnouncement().isEditing()) {
					Message.sendMessage(
							AnnouncementMessageFormatter.getFormatAnnouncementEmbed(acr.getAnnouncement()),
							"Announcement updated!", event);
				} else {
					Message.sendMessage(
							AnnouncementMessageFormatter.getFormatAnnouncementEmbed(acr.getAnnouncement()),
							"Announcement created " + Message.lineBreak + Message.lineBreak
									+ "Use `!announcement subscribe <id>` to subscribe to the announcement!", event);
				}
			} else {
				Message.sendMessage("Oops! Something went wrong! Are you sure all of the info is correct?",
						event);
			}
		}
	}

	private void moduleCancel(MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();

		if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
			AnnouncementCreator.getCreator().terminate(event);
			Message.sendMessage("Announcement Creator terminated!", event);
		} else {
			Message.sendMessage("Cannot cancel creation when the Creator has not been started!", event);
		}
	}

	private void moduleDelete(String[] args, MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();
		if (args.length == 1) {
			if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				Message.sendMessage("Please specify the Id of the announcement to delete!", event);
			} else {
				Message.sendMessage("You cannot delete an announcement while in the Creator!", event);
			}
		} else if (args.length == 2) {
			String value = args[1];
			if (AnnouncementUtils.announcementExists(value, event)) {
				if (DatabaseManager.getManager().deleteAnnouncement(value)) {
					Message.sendMessage("Announcement successfully deleted!", event);
				} else {
					Message.sendMessage(
							"Failed to delete announcement! Something may have gone wrong, the dev has been emailed!",
							event);
				}
			} else {
				Message.sendMessage(
						"Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?",
						event);
			}
		} else {
			Message.sendMessage("Please use `!announcement delete <ID>`", event);
		}
	}

	private void moduleView(String[] args, MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();
		if (args.length == 1) {
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				Message.sendMessage(AnnouncementMessageFormatter
								.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId)),
						event);
			} else {
				Message.sendMessage("You must specify the ID of the announcement you wish to view!", event);
			}
		} else if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				Message.sendMessage("You cannot view another announcement while one is in the creator!",
						event);
			} else {
				try {
					Announcement a = DatabaseManager.getManager()
							.getAnnouncement(UUID.fromString(value), guildId);
					if (a != null) {
						Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a),
								AnnouncementMessageFormatter.getSubscriberNames(a), event);
					} else {
						Message.sendMessage(
								"That announcement does not exist! Are you sure you typed the ID correctly?",
								event);
					}
				} catch (NumberFormatException e) {
					Message.sendMessage("Hmm... is the ID correct? I seem to be having issues parsing it..",
							event);
				}
			}
		} else {
			Message.sendMessage("Please use `!announcement view` or `!announcement view <ID>`", event);
		}
	}

	private void moduleSubscribe(String[] args, MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();
		if (args.length == 1) {
			Message.sendMessage("Please specify the ID of the announcement you wish to subscribe to!",
					event);
		} else if (args.length == 2) {
			String value = args[1];
			if (AnnouncementUtils.announcementExists(value, event)) {
				Announcement a = DatabaseManager.getManager()
						.getAnnouncement(UUID.fromString(value), guildId);
				String senderId = event.getMessage().getAuthor().getID();
				if (!a.getSubscriberUserIds().contains(senderId)) {
					a.getSubscriberUserIds().add(senderId);
					DatabaseManager.getManager().updateAnnouncement(a);
					Message.sendMessage("You have subscribed to the announcement with the ID: `" + value + "`"
							+ Message.lineBreak + "To unsubscribe use `!announcement unsubscribe <id>`", event);
				} else {
					Message.sendMessage("You are already subscribed to that event!", event);
				}
			} else {
				Message.sendMessage(
						"Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?",
						event);
			}
		} else if (args.length == 3) {
			String value1 = args[1];
			String value2 = args[2];
			if (AnnouncementUtils.announcementExists(value1, event)) {
				Announcement a = DatabaseManager.getManager()
						.getAnnouncement(UUID.fromString(value1), guildId);
				IUser user = event.getMessage().getGuild()
						.getUserByID(UserUtils.getUser(value2, event.getMessage()));
				if (user != null) {
					//Valid user, let's add that user to the announcement.
					if (!a.getSubscriberUserIds().contains(user.getID())) {
						String username = user.getDisplayName(event.getMessage().getGuild());
						a.getSubscriberUserIds().add(user.getID());
						DatabaseManager.getManager().updateAnnouncement(a);
						Message.sendMessage(
								"`" + username + "` has been subscribed to the announcement with the ID `" + a
										.getAnnouncementId() + "`" + Message.lineBreak
										+ "To unsubscribe them use `!announcement unsubscribe <announcement ID> <mention>",
								event);
					} else {
						Message.sendMessage(
								"That user is already subscribed to the specified announcement! To unsubscribe them use `!announcement unsubscribe <announcement ID> <mention>`",
								event);
					}
				} else if (value2.equalsIgnoreCase("everyone") || value2.equalsIgnoreCase("here")) {
					//Here or everyone is to be subscribed...
					String men = value2.toLowerCase();
					if (!a.getSubscriberRoleIds().contains(men)) {
						a.getSubscriberRoleIds().add(men);
						DatabaseManager.getManager().updateAnnouncement(a);
						Message.sendMessage(
								"`" + men + "` has been subscribed to the announcement with the ID `" + a
										.getAnnouncementId() + "`" + Message.lineBreak
										+ "To unsubscribe them use `!announcement unsubscribe <announcement ID> <value>",
								event);
					} else {
						Message.sendMessage(men
										+ " is already subscribed to the specified announcement! To unsubscribe them use `!announcement unsubscribe <announcement ID> <value>`",
								event);
					}
				} else {
					//User does not exist, see if a role.
					IRole role = RoleUtils.getRoleFromMention(value2, event);
					if (role != null) {
						//Role valid, let's add that role to the announcement.
						if (!a.getSubscriberRoleIds().contains(role.getID())) {
							String roleName = role.getName();
							a.getSubscriberRoleIds().add(role.getID());
							DatabaseManager.getManager().updateAnnouncement(a);
							Message.sendMessage(
									"`" + roleName + "` has been subscribed to the announcement with the ID `" + a
											.getAnnouncementId() + "`" + Message.lineBreak
											+ "To unsubscribe them use `!announcement unsubscribe <announcement ID> <mention>",
									event);
						} else {
							Message.sendMessage(
									"That role is already subscribed to the specified announcement! To unsubscribe them use `!announcement unsubscribe <announcement ID> <mention>`",
									event);
						}
					} else {
						//Role does not exist...
						Message.sendMessage("Role or user not found! Are you sure you typed them correctly?",
								event);
					}
				}
			} else {
				Message.sendMessage(
						"Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?",
						event);
			}
		} else {
			Message.sendMessage(
					"Please use `!announcement subscribe <ID>` or `!announcement subscribe <ID> <user mention/role mention/here/everyone>`",
					event);
		}
	}

	private void moduleSubscribeRewrite(String[] args, MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		IGuild guild = message.getGuild();
		IUser user = message.getAuthor();
		if (args.length == 1) {
			if (AnnouncementCreator.getCreator().hasAnnouncement(guild.getID())) {
				UUID announcementId = AnnouncementCreator.getCreator().getAnnouncement(guild.getID())
						.getAnnouncementId();
				Announcement a = AnnouncementCreator.getCreator().getAnnouncement(guild.getID());
				String senderId = user.getID();
				if (!a.getSubscriberUserIds().contains(senderId)) {
					a.getSubscriberUserIds().add(senderId);
					Message.sendMessage(
							"You have subscribed to the announcement with the ID: `" + announcementId.toString()
									+ "`" + Message.lineBreak + "To unsubscribe use `!announcement unsubscribe <id>`",
							event);
				} else { // Announcement contains user ID
					Message.sendMessage("You are already subscribed to that event!", event);
				}
			} else { // User not creating an announcement
				Message.sendMessage("Please specify the ID of the announcement you wish to subscribe to!",
						event);
			}
		} else if (args.length == 2) {
			String value = args[1];
			if (args[1].length() <= 32) {
				if (AnnouncementCreator.getCreator().hasAnnouncement(guild.getID())) {
					Announcement a = AnnouncementCreator.getCreator().getAnnouncement(guild.getOwnerID());
					IUser u = guild.getUserByID(UserUtils.getUser(value, message));
					IRole r = guild.getRoleByID(RoleUtils.getRole(value, message));
					if (value.equalsIgnoreCase("everyone") || value.equalsIgnoreCase("here")) {
						String men = value.toLowerCase();
						if (!a.getSubscriberRoleIds().contains(men)) {
							a.getSubscriberRoleIds().add(men);
							Message.sendMessage(
									"`" + men + "` has been subscribed to the announcement with the ID `" + a
											.getAnnouncementId() + "`" + Message.lineBreak
											+ "To unsubscribe them use `!announcement unsubscribe <announcement ID> <value>",
									event);
						} else {
							Message.sendMessage(men
											+ " is already subscribed to the specified announcement! To unsubscribe them use `!announcement unsubscribe <announcement ID> <value>`",
									event);
						}
					} else if (u != null) {
						if (!a.getSubscriberUserIds().contains(u.getID())) {
							a.getSubscriberUserIds().add(u.getID());
							String username = u.getName();
							Message.sendMessage(
									"`" + username + "` has been subscribed to the announcement with the ID `" + a
											.getAnnouncementId() + "`" + Message.lineBreak
											+ "To unsubscribe them use `!announcement unsubscribe <announcement ID> <value>",
									event);
						}
					} else if (r != null) {
						if (!a.getSubscriberRoleIds().contains(r.getID())) {
							a.getSubscriberUserIds().add(u.getID());
							String username = r.getName();
							Message.sendMessage(
									"The role `" + username + "` has been subscribed to the announcement with the ID `" + a
											.getAnnouncementId() + "`" + Message.lineBreak
											+ "To unsubscribe them use `!announcement unsubscribe <announcement ID> <value>",
									event);
						}
					} else {

					}
				} else {
					Message.sendMessage(
							"You are not currently creating an announcement! Please specify an announcement ID!",
							event);
				}
			} else {
				if (AnnouncementUtils.announcementExists(value, event)) {
					String senderId = user.getID();
					Announcement a = DatabaseManager.getManager()
							.getAnnouncement(UUID.fromString(value), guild.getID());
					if (!a.getSubscriberUserIds().contains(senderId)) {
						a.getSubscriberUserIds().add(senderId);
						Message.sendMessage(
								"You have subscribed to the announcement with the ID: `" + value + "`"
										+ Message.lineBreak + "To unsubscribe use `!announcement unsubscribe <id>`",
								event);
					} else {
						Message.sendMessage("You are already subscribed to that event!", event);
					}
				} else {
					Message.sendMessage(
							"Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?",
							event);
				}
			}
		} else {
			List<String> subscribedUsers = new ArrayList<>();
			List<String> subscribedRoles = new ArrayList<>();

			String announcementId;
			int start = 1;
			if (args[1].length() > 32) {
				announcementId = args[1];
			} else {
				if (AnnouncementCreator.getCreator().hasAnnouncement(guild.getID())) {
					announcementId = AnnouncementCreator.getCreator().getAnnouncement(guild.getID())
							.getAnnouncementId().toString();
					start++;
				} else {
					Message.sendMessage("You must specify an announcement ID as your first argument!", event);
					return;
				}
			}

			if (AnnouncementUtils.announcementExists(announcementId, event)) {
				Announcement a = start == 1 ? DatabaseManager.getManager()
						.getAnnouncement(UUID.fromString(announcementId), guild.getID())
						: AnnouncementCreator.getCreator().getAnnouncement(guild.getID());
				for (int i = start; i < args.length; i++) {
					IUser u = guild.getUserByID(UserUtils.getUser(args[i], message));
					IRole r = guild.getRoleByID(RoleUtils.getRole(args[i], message));
					if (args[i].equalsIgnoreCase("everyone") || args[i].equalsIgnoreCase("here")) {
						//Here or everyone is to be subscribed...
						String men = args[i].toLowerCase();
						if (!a.getSubscriberRoleIds().contains(men)) {
							a.getSubscriberRoleIds().add(men);
							Message.sendMessage(
									"`" + men + "` has been subscribed to the announcement with the ID `" + a
											.getAnnouncementId() + "`" + Message.lineBreak
											+ "To unsubscribe them use `!announcement unsubscribe <announcement ID> <value>",
									event);
							subscribedUsers.add(men);
						} else {
							Message.sendMessage(men
											+ " is already subscribed to the specified announcement! To unsubscribe them use `!announcement unsubscribe <announcement ID> <value>`",
									event);
						}
					} else {
						if (u != null) {
							if (!a.getSubscriberUserIds().contains(user.getID())) {
								String username = user.getDisplayName(event.getMessage().getGuild());
								a.getSubscriberUserIds().add(user.getID());
								subscribedUsers.add(username);
							}
						} else if (r != null) {
							if (!a.getSubscriberRoleIds().contains(r.getID())) {
								String roleName = r.getName();
								a.getSubscriberRoleIds().add(r.getID());
								subscribedRoles.add(roleName);
							}
						}


					}
				}

				EmbedBuilder em = new EmbedBuilder();
				try {
					em.withAuthorIcon(Main.client.getApplicationIconURL());
				} catch (DiscordException ex) {
					ExceptionHandler
							.sendException(user, "Client author icon failed on subscribing members", ex,
									ex.getClass());
				}
				em.withAuthorName("Announcement Subscribe");
				em.withDesc(
						"Users subscribed: " + subscribedUsers + "\nRoles subscribed: " + subscribedRoles);
				em.withFooterText("Announcement ID: " + announcementId);
				Message.sendMessage(em.build(), event);

				if (start == 1) {
					DatabaseManager.getManager().updateAnnouncement(a)
					;
				}
			} else {
				Message.sendMessage(
						"Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?",
						event);
			}
		}

	}

	private void moduleUnsubscribeRewrite(String[] args, MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		IGuild guild = message.getGuild();
		IUser user = message.getAuthor();
		IChannel debug = guild.getChannelByID("267685015016570880");
		Message.sendMessage(debugEmbed("Args: " + Arrays.toString(args)), event);

		if (args.length == 1) {
			Message.sendMessage(debugEmbed("Args.length == 1"), event);
			if (AnnouncementCreator.getCreator().hasAnnouncement(guild.getID())) {
				Message.sendMessage(debugEmbed("Announcement being created."), event);
				UUID announcementId = AnnouncementCreator.getCreator().getAnnouncement(guild.getID())
						.getAnnouncementId();
				Announcement a = AnnouncementCreator.getCreator().getAnnouncement(guild.getID());
				String senderId = user.getID();
				if (a.getSubscriberUserIds().contains(senderId)) {
					Message.sendMessage(debugEmbed("User in announcement"), event);
					a.getSubscriberUserIds().remove(senderId);
					DatabaseManager.getManager().updateAnnouncement(a);
					Message.sendMessage(
							"You have unsubscribed from the announcement with the ID: `" + announcementId
									.toString() + "`" + Message.lineBreak
									+ "To subscribe use `!announcement subsribe <id>`", event);
				} else { // Announcement contains user ID
					Message.sendMessage("You are not subscribed to that event!", event);
				}
			} else { // User not creating an announcement
				Message.sendMessage(debugEmbed("Announcement not being created."), event);
				Message
						.sendMessage("Please specify the ID of the announcement you wish to unsubscribe from!",
								event);
			}
		} else if (args.length == 2) {
			String value = args[1];
			if (AnnouncementUtils.announcementExists(value, event)) {
				Message.sendMessage(debugEmbed("Valid UID."), event);
				String senderId = user.getID();
				Announcement a = DatabaseManager.getManager()
						.getAnnouncement(UUID.fromString(value), guild.getID());
				if (a.getSubscriberUserIds().contains(senderId)) {
					Message.sendMessage(debugEmbed("User subscribed"), event);
					a.getSubscriberUserIds().remove(senderId);
					DatabaseManager.getManager().updateAnnouncement(a);
					Message.sendMessage(
							"You have unsubscribed from the announcement with the ID: `" + value + "`"
									+ Message.lineBreak + "To subscribe use `!announcement unsubscribe <id>`", event);
				} else {
					Message.sendMessage("You are already unsubscribed to that event!", event);
				}
			} else {
				Message.sendMessage(debugEmbed("Invalid UID."), event);
				Message.sendMessage(
						"Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?",
						event);
			}
		} else {
			List<String> subscribedUsers = new ArrayList<>();
			List<String> subscribedRoles = new ArrayList<>();

			String announcementId;
			int start = 1;
			if (args[1].length() > 32) {
				announcementId = args[1];
				Message.sendMessage(
						debugEmbed("Args[1].legth = " + args[1].length() + "\nArgs[1] = " + args[1]), event);
			} else {
				if (AnnouncementCreator.getCreator().hasAnnouncement(guild.getID())) {
					announcementId = AnnouncementCreator.getCreator().getAnnouncement(guild.getID())
							.getAnnouncementId().toString();
					Message.sendMessage(debugEmbed("Announcement being created"), event);
					start++;
				} else {
					Message.sendMessage("You must specify an announcement ID as your first argument!", event);
					return;
				}
			}

			if (AnnouncementUtils.announcementExists(announcementId, event)) {
				Message.sendMessage(debugEmbed("Announcement exists."), event);
				Announcement a = start == 1 ? DatabaseManager.getManager()
						.getAnnouncement(UUID.fromString(announcementId), guild.getID())
						: AnnouncementCreator.getCreator().getAnnouncement(guild.getID());
				Message.sendMessage(
						debugEmbed("Args at point of subscribing multiple users: " + Arrays.toString(args)),
						event);
				for (int i = start; i < args.length; i++) {
					Message.sendMessage(debugEmbed("ARGS[I]: " + args[i]), event);
					IUser u = guild.getUserByID(UserUtils.getUser(args[i], message));
					IRole r = guild.getRoleByID(RoleUtils.getRole(args[i], message));
					Message.sendMessage(
							debugEmbed("u == null: " + (u == null) + "\n\nr == null: " + (r == null)), event);
					if (args[i].equalsIgnoreCase("everyone") || args[i].equalsIgnoreCase("here")) {
						//Here or everyone is to be subscribed...
						Message.sendMessage(debugEmbed("args[i] = " + args[i]), event);
						String men = args[i].toLowerCase();
						if (a.getSubscriberRoleIds().contains(men)) {
							a.getSubscriberRoleIds().remove(men);
							DatabaseManager.getManager().updateAnnouncement(a);
							subscribedUsers.add(men);
						}
					} else {
						if (u != null) {
							Message.sendMessage(debugEmbed("u is not null"), event);
							if (a.getSubscriberUserIds().contains(user.getID())) {
								String username = user.getDisplayName(event.getMessage().getGuild());
								a.getSubscriberUserIds().remove(user.getID());
								DatabaseManager.getManager().updateAnnouncement(a);
								subscribedUsers.add(username);
							}
						} else if (r != null) {
							Message.sendMessage(debugEmbed("r is not null"), event);
							if (a.getSubscriberRoleIds().contains(r.getID())) {
								String roleName = r.getName();
								a.getSubscriberRoleIds().remove(r.getID());
								DatabaseManager.getManager().updateAnnouncement(a);
								subscribedRoles.add(roleName);
							}
						}


					}
				}

				EmbedBuilder em = new EmbedBuilder();
				try {
					em.withAuthorIcon(Main.client.getApplicationIconURL());
				} catch (DiscordException ex) {
					ExceptionHandler
							.sendException(user, "Client author icon failed on unsubscribing members", ex,
									ex.getClass());
				}
				em.withAuthorName("Announcement Subscribe");
				em.withDesc(
						"Users unsubscribed: " + subscribedUsers + "\nRoles unsubscribed: " + subscribedRoles);
				em.withFooterText("Announcement ID: " + announcementId);
				Message.sendMessage(em.build(), event);
			} else {
				Message.sendMessage(
						"Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?",
						event);
			}
		}

	}

	private EmbedObject debugEmbed(String str) {
		return new EmbedBuilder().withAuthorName("DEBUG EMBED").withDesc(str).build();
	}

	private void moduleUnsubscribe(String[] args, MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();
		if (args.length == 1) {
			Message.sendMessage("Please specify the ID of the announcement you wish to unsubscribe from!",
					event);
		} else if (args.length == 2) {
			String value = args[1];
			if (AnnouncementUtils.announcementExists(value, event)) {
				Announcement a = DatabaseManager.getManager()
						.getAnnouncement(UUID.fromString(value), guildId);
				String senderId = event.getMessage().getAuthor().getID();
				if (a.getSubscriberUserIds().contains(senderId)) {
					a.getSubscriberUserIds().remove(senderId);
					DatabaseManager.getManager().updateAnnouncement(a);
					Message.sendMessage(
							"You have unsubscribed to the announcement with the ID: `" + value + "`"
									+ Message.lineBreak + "To re-subscribe use `!announcement subscribe <id>`",
							event);
				} else {
					Message.sendMessage("You are not subscribed to this event!", event);
				}
			} else {
				Message.sendMessage(
						"Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?",
						event);
			}
		} else if (args.length == 3) {
			String value1 = args[1];
			String value2 = args[2];
			if (AnnouncementUtils.announcementExists(value1, event)) {
				Announcement a = DatabaseManager.getManager()
						.getAnnouncement(UUID.fromString(value1), guildId);
				IUser user = UserUtils.getUserFromMention(value2, event);
				if (user != null) {
					//Valid user, let's add that user to the announcement.
					if (a.getSubscriberUserIds().contains(user.getID())) {
						String username = user.getDisplayName(event.getMessage().getGuild());
						a.getSubscriberUserIds().remove(user.getID());
						DatabaseManager.getManager().updateAnnouncement(a);
						Message.sendMessage(
								"`" + username + "` has been unsubscribed from the announcement with the ID `" + a
										.getAnnouncementId() + "`" + Message.lineBreak
										+ "To re-subscribe them use `!announcement subscribe <announcement ID> <mention>",
								event);
					} else {
						Message.sendMessage(
								"That user is not subscribed to the specified announcement! To subscribe them use `!announcement unsubscribe <announcement ID> <mention>`",
								event);
					}
				} else if (value2.equalsIgnoreCase("everyone") || value2.equalsIgnoreCase("here")) {
					//Here or everyone is to be mentioned...
					String men = value2.toLowerCase();
					if (a.getSubscriberRoleIds().contains(men)) {
						a.getSubscriberRoleIds().remove(men);
						DatabaseManager.getManager().updateAnnouncement(a);
						Message.sendMessage(
								"`" + men + "` has been unsubscribed from the announcement with the ID `" + a
										.getAnnouncementId() + "`" + Message.lineBreak
										+ "To re-subscribe them use `!announcement subscribe <announcement ID> <value>",
								event);
					} else {
						Message.sendMessage(men
										+ " is not subscribed to the specified announcement! To subscribe them use `!announcement unsubscribe <announcement ID> <value>`",
								event);
					}
				} else {
					//User does not exist, see if a role.
					IRole role = RoleUtils.getRoleFromMention(value2, event);
					if (role != null) {
						//Role valid, let's add that role to the announcement.
						if (a.getSubscriberRoleIds().contains(role.getID())) {
							String roleName = role.getName();
							a.getSubscriberRoleIds().remove(role.getID());
							DatabaseManager.getManager().updateAnnouncement(a);
							Message.sendMessage(
									"`" + roleName + "` has been unsubscribed from the announcement with the ID `" + a
											.getAnnouncementId() + "`" + Message.lineBreak
											+ "To re-subscribe them use `!announcement subscribe <announcement ID> <mention>",
									event);
						} else {
							Message.sendMessage(
									"That role is not subscribed to the specified announcement! To subscribe them use `!announcement unsubscribe <announcement ID> <mention>`",
									event);
						}
					} else {
						//Role does not exist...
						Message.sendMessage("Role or user not found! Are you sure you typed them correctly?",
								event);
					}
				}
			} else {
				Message.sendMessage(
						"Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?",
						event);
			}
		} else {
			Message.sendMessage(
					"Please use `!announcement unsubscribe <ID>` or `!announcement unsubscribe <ID> <user mention/role mention/here/everyone>`",
					event);
		}
	}

	private void moduleType(String[] args, MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				if (AnnouncementType.isValid(value)) {
					AnnouncementType type = AnnouncementType.fromValue(value);
					AnnouncementCreator.getCreator().getAnnouncement(guildId).setAnnouncementType(type);
					if (type.equals(AnnouncementType.SPECIFIC)) {
						Message.sendMessage(
								"Announcement type set to: `" + type.name() + "`" + Message.lineBreak
										+ "Please set the specific event ID to fire for with `!announcement event <id>`",
								event);
					} else if (type.equals(AnnouncementType.COLOR)) {
						Message.sendMessage(
								"Announcement type set to: `" + type.name() + "`" + Message.lineBreak
										+ "Please set the specific event color to fire for with `!announcement color <name or ID>`",
								event);
					} else if (type.equals(AnnouncementType.RECUR)) {
						Message.sendMessage(
								"Announcement type set to: `" + type.name() + "`" + Message.lineBreak
										+ "Please set the recurring event to fire for with `!announcement event  <ID>`",
								event);
					} else {
						Message.sendMessage(
								"Announcement type set to: `" + type.name() + "`" + Message.lineBreak
										+ "Please specify the NAME (not ID) of the channel this announcement will post in with `!announcement channel <name>`!",
								event);
					}
				} else {
					Message.sendMessage("Valid types are only `UNIVERSAL`, `SPECIFIC`, `COLOR`, or `RECUR`!",
							event);
				}
			} else {
				Message.sendMessage("Announcement creator has not been initialized!", event);
			}
		} else {
			Message.sendMessage("Please use `!announcement type <TYPE>`", event);
		}
	}

	private void moduleHours(String[] args, MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				try {
					Integer hoursOr = Integer.valueOf(value);
					Integer hours = Math.abs(hoursOr);
					AnnouncementCreator.getCreator().getAnnouncement(guildId).setHoursBefore(hours);
					Message.sendMessage(
							"Announcement hours before set to: `" + hours + "`" + Message.lineBreak
									+ "Please specify the amount of minutes before the event to fire!", event);
				} catch (NumberFormatException e) {
					Message.sendMessage("Hours must be a valid integer! (Ex: `1` or `10`)", event);
				}
			} else {
				Message.sendMessage("Announcement creator has not been initialized!", event);
			}
		} else {
			Message.sendMessage("Please use `!announcement hours <amount>`", event);
		}
	}

	private void moduleMinutes(String[] args, MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				try {
					Integer minutesOr = Integer.valueOf(value);
					Integer minutes = Math.abs(minutesOr);
					AnnouncementCreator.getCreator().getAnnouncement(guildId).setMinutesBefore(minutes);
					Message.sendMessage(
							"Announcement minutes before set to: `" + minutes + "`" + Message.lineBreak
									+ "Announcement creation halted! " +
									"If you would like to add some info text, use `!announcement info <text>` otherwise, review your announcement with `!announcement review`",
							event);
				} catch (NumberFormatException e) {
					Message.sendMessage("Minutes must be a valid integer! (Ex: `1` or `10`)", event);
				}
			} else {
				Message.sendMessage("Announcement creator has not been initialized!", event);
			}
		} else {
			Message.sendMessage("Please use `!announcement minutes <amount>`", event);
		}
	}

	private void moduleList(String[] args, MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();
		if (args.length == 1) {
			if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				Message
						.sendMessage("Please specify how many announcements you wish to list or `all`", event);
			} else {
				Message.sendMessage("You cannot list existing announcements while in the creator!", event);
			}
		} else if (args.length == 2) {
			String value = args[1];
			if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				if (value.equalsIgnoreCase("all")) {
					ArrayList<Announcement> announcements = DatabaseManager.getManager()
							.getAnnouncements(guildId);
					Message.sendMessage(
							"All announcements, use `!announcement view <id>` for more info." + Message.lineBreak
									+ "`" + announcements.size() + "`" + Message.lineBreak + Message.lineBreak
									+ "Please note that this list may be delayed due to rate limiting...", event);
					//Loop and add embeds
					for (Announcement a : announcements) {
						Message
								.sendMessage(AnnouncementMessageFormatter.getCondensedAnnouncementEmbed(a), event);
					}
				} else {
					//List specific amount of announcements
					try {
						Integer amount = Integer.valueOf(value);
						Message.sendMessage("Displaying the first `" + amount
								+ "` announcements found, use `!announcement view <id>` for more info."
								+ Message.lineBreak + Message.lineBreak
								+ "Please note that this list may be delayed due to rate limiting...", event);

						int posted = 0;
						for (Announcement a : DatabaseManager.getManager().getAnnouncements(guildId)) {
							if (posted < amount) {
								Message.sendMessage(AnnouncementMessageFormatter.getCondensedAnnouncementEmbed(a),
										event);

								posted++;
							} else {
								break;
							}
						}
					} catch (NumberFormatException e) {
						Message.sendMessage("Amount must either be `all` or a valid integer!", event);
					}
				}
			} else {
				Message.sendMessage("You cannot list announcements while in the creator!", event);
			}
		} else {
			Message.sendMessage("Please use `!announcement list <amount>` or `!announcement list all`",
					event);
		}
	}

	private void moduleEvent(String[] args, MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				if (AnnouncementCreator.getCreator().getAnnouncement(guildId).getAnnouncementType()
						.equals(AnnouncementType.SPECIFIC)) {
					if (EventUtils.eventExists(guildId, value)) {
						AnnouncementCreator.getCreator().getAnnouncement(guildId).setEventId(value);
						Message.sendMessage("Event ID set to: `" + value + "`" + Message.lineBreak
										+ "Please specify the NAME (not ID) of the channel this announcement will post in with `!announcement channel <name>`!",
								event);
					} else {
						Message.sendMessage(
								"Hmm... I can't seem to find an event with that ID, are you sure its correct?",
								event);
					}
				} else if (AnnouncementCreator.getCreator().getAnnouncement(guildId).getAnnouncementType()
						.equals(AnnouncementType.RECUR)) {
					if (EventUtils.eventExists(guildId, value)) {
						if (value.contains("_")) {
							String[] stuff = value.split("_");
							value = stuff[0];
						}
						AnnouncementCreator.getCreator().getAnnouncement(guildId).setEventId(value);

						Message.sendMessage("Event ID set to: `" + value + "`" + Message.lineBreak
										+ "Please specify the NAME (not ID) of the channel this announcement will post in with `!announcement channel <name>`!",
								event);
					} else {
						Message.sendMessage(
								"Hmm... I can't seem to find an event with that ID, are you sure its correct?",
								event);
					}
				} else {
					Message.sendMessage(
							"You cannot set an event while the announcement Type is NOT set to `SPECIFIC`",
							event);
				}
			} else {
				Message.sendMessage("Announcement creator has not been initialized!", event);
			}
		} else {
			Message.sendMessage("Please use `!announcement event <Event ID>`", event);
		}
	}

	private void moduleInfo(String[] args, MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();
		if (args.length < 2) {
			Message.sendMessage("Please use `!announcement info <your info here>`", event);
		} else if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				AnnouncementCreator.getCreator().getAnnouncement(guildId).setInfo(value);
				Message.sendMessage("Announcement info set to: ```" + value + "```" + Message.lineBreak
								+ "Please review the announcement with `!announcement review` to confirm it is correct and then use `!announcement confirm` to create the announcement!",
						event);
			} else {
				Message.sendMessage("Announcement Creator not initialized!", event);
			}
		} else if (args.length > 2) {
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				String value = GeneralUtils.getContent(args, 1);
				AnnouncementCreator.getCreator().getAnnouncement(guildId).setInfo(value);
				Message.sendMessage("Announcement info set to: ```" + value + "```" + Message.lineBreak
								+ "Please review the announcement with `!announcement review` to confirm it is correct and then use `!announcement confirm` to create the announcement!",
						event);
			} else {
				Message.sendMessage("Announcement Creator not initialized!", event);
			}
		}
	}

	private void moduleChannel(String[] args, MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				if (ChannelUtils.channelExists(value, event)) {
					IChannel c = ChannelUtils.getChannelFromNameOrId(value, event);
					if (c != null) {
						AnnouncementCreator.getCreator().getAnnouncement(guildId)
								.setAnnouncementChannelId(c.getID());
						Message.sendMessage(
								"Announcement channel set to: `" + c.getName() + "`" + Message.lineBreak
										+ "Please specify the amount of hours before the event this is to fire!",
								event);
					} else {
						Message.sendMessage(
								"Are you sure you typed the channel name correctly? I can't seem to find it.",
								event);
					}
				} else {
					Message.sendMessage(
							"Are you sure you typed the channel name correctly? I can't seem to find it.", event);
				}
			} else {
				Message.sendMessage("Announcement creator has not been initialized!", event);
			}
		} else {
			Message.sendMessage("Please use `!announcement channel <ChannelName>`", event);
		}
	}

	private void moduleColor(String[] args, MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				if (AnnouncementCreator.getCreator().getAnnouncement(guildId).getAnnouncementType()
						.equals(AnnouncementType.COLOR)) {
					if (EventColor.exists(value)) {
						EventColor color = EventColor.fromNameOrHexOrID(value);
						AnnouncementCreator.getCreator().getAnnouncement(guildId).setEventColor(color);
						Message.sendMessage(
								"Announcement Color set to: `" + color.name() + "`" + Message.lineBreak
										+ Message.lineBreak
										+ "Please specify the NAME (not ID) of the channel this announcement will post in with `!announcement channel <name>`!",
								event);
					} else {
						Message.sendMessage("Please specify a valid color NAME, ID, or HEX!", event);
					}
				} else {
					Message.sendMessage(
							"You cannot set announcement color while announcement type is NOT `COLOR`", event);
				}
			} else {
				Message.sendMessage("Announcement creator not initiated!", event);
			}
		} else {
			Message.sendMessage("Please specify a color with `!announcement color <color>`", event);
		}
	}

	private void moduleCopy(String[] args, MessageReceivedEvent event) {
		String guildId = event.getMessage().getGuild().getID();
		if (args.length == 2) {
			String value = args[1];
			if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				if (AnnouncementUtils.announcementExists(value, event)) {
					Announcement a = AnnouncementCreator.getCreator().init(event, value);

					Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a),
							"Announcement copied! Edit any values you wish and confirm the announcement with the command `!announcement confirm`",
							event);
				} else {
					Message.sendMessage("Hmm... is the ID correct? I seem to be having issues parsing it..",
							event);
				}
			} else {
				Message.sendMessage("Announcement creator already initialized!", event);
			}
		} else {
			Message.sendMessage("Please specify the announcement to copy with `!announcement copy <ID>`",
					event);
		}
	}
}