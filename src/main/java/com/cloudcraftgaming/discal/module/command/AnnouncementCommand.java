package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.event.EventUtils;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.module.announcement.*;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import com.cloudcraftgaming.discal.utils.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;
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
	public Boolean issueCommand(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		if (args.length < 1) {
			Message.sendMessage(MessageManager.getMessage("Notification.Args.Few", settings), event);
		} else {
			switch (args[0].toLowerCase()) {
				case "create":
					if (PermissionChecker.hasSufficientRole(event)) {
						moduleCreate(event, settings);
					} else {
						Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					}
					break;
				case "confirm":
					if (PermissionChecker.hasSufficientRole(event)) {
						moduleConfirm(event, settings);
					} else {
						Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					}
					break;
				case "cancel":
					if (PermissionChecker.hasSufficientRole(event)) {
						moduleCancel(event, settings);
					} else {
						Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					}
					break;
				case "delete":
					if (PermissionChecker.hasSufficientRole(event)) {
						moduleDelete(args, event, settings);
					} else {
						Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					}
					break;
				case "view":
				case "review":
					moduleView(args, event, settings);
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
					moduleType(args, event, settings);
					break;
				case "hours":
					moduleHours(args, event, settings);
					break;
				case "minutes":
					moduleMinutes(args, event, settings);
					break;
				case "list":
					moduleList(args, event, settings);
					break;
				case "event":
					moduleEvent(args, event, settings);
					break;
				case "info":
					moduleInfo(args, event, settings);
					break;
				case "channel":
					moduleChannel(args, event, settings);
					break;
				case "color":
				case "colour":
					moduleColor(args, event, settings);
					break;
				case "copy":
					if (PermissionChecker.hasSufficientRole(event)) {
						moduleCopy(args, event, settings);
					} else {
						Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					}
					break;
				case "edit":
					if (PermissionChecker.hasSufficientRole(event)) {
						if (settings.isDevGuild()) {
							moduleEdit(args, event, settings);
						} else {
							Message.sendMessage(MessageManager.getMessage("Notification.Disabled", settings), event);
						}
					} else {
						Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
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
					Message.sendMessage(MessageManager.getMessage("Notification.Args.Invalid", settings), event);
					break;
			}
		}
		return false;
	}


	private void moduleCreate(MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();

		if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
			AnnouncementCreator.getCreator().init(event, settings);
			if (!AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Create.Init", settings), event);
			} else {
				Message.deleteMessage(event);
			}
		} else {
			if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
				Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
				Message.deleteMessage(event);
				AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.AlreadyInit", settings), event));

			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.AlreadyInit", settings), event);
			}
		}
	}

	private void moduleEdit(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
			if (args.length == 2) {
				String anId = args[1];
				if (AnnouncementUtils.announcementExists(anId, event)) {
					Announcement announcement = AnnouncementCreator.getCreator().edit(event, anId, settings);

					if (announcement.getCreatorMessage() == null) {
						Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(announcement, settings), MessageManager.getMessage("Creator.Announcement.Edit.Init", settings), event);
					} else {
						Message.deleteMessage(event);
					}
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
				}
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Edit.Specify", settings), event);
			}
		} else {
			if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
				Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
				Message.deleteMessage(event);
				AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.AlreadyInit", settings), event));
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.AlreadyInit", settings), event);
			}
		}
	}

	private void moduleConfirm(MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
			AnnouncementCreatorResponse acr = AnnouncementCreator.getCreator().confirmAnnouncement(event);
			if (acr.isSuccessful()) {
				if (acr.getAnnouncement().isEditing()) {
					if (acr.getAnnouncement().getCreatorMessage() != null) {
						Message.deleteMessage(acr.getAnnouncement().getCreatorMessage());
						Message.deleteMessage(event);
					}
					Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(acr.getAnnouncement(), settings), MessageManager.getMessage("Creator.Announcement.Confirm.Edit.Success", settings), event);
				} else {
					if (acr.getAnnouncement().getCreatorMessage() != null) {
						Message.deleteMessage(acr.getAnnouncement().getCreatorMessage());
						Message.deleteMessage(event);
					}
					Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(acr.getAnnouncement(), settings), MessageManager.getMessage("Creator.Announcement.Confirm.Create.Success", settings), event);
				}
			} else {
				if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
					Message.deleteMessage(event);
					AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.Confirm.Failure", settings), event));
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Confirm.Failure", settings), event);
				}
			}
		} else {
			Message.sendMessage(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
		}
	}

	private void moduleCancel(MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();

		if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
			IMessage creatorMessage = AnnouncementCreator.getCreator().getCreatorMessage(guildId);
			AnnouncementCreator.getCreator().terminate(event);

			if (creatorMessage != null) {
				Message.deleteMessage(creatorMessage);
				Message.deleteMessage(event);
			}
			Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Cancel.Success", settings), event);
		} else {
			Message.sendMessage(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
		}
	}

	private void moduleDelete(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (args.length == 1) {
			if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Delete.Specify", settings), event);
			} else {
				if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
					Message.deleteMessage(event);
					AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.Delete.InCreator", settings), event));
				}
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Delete.InCreator", settings), event);
			}
		} else if (args.length == 2) {
			String value = args[1];
			if (AnnouncementUtils.announcementExists(value, event)) {
				if (DatabaseManager.getManager().deleteAnnouncement(value)) {
					Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Delete.Success", settings), event);
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Delete.Failure", settings), event);
				}
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
			}
		} else {
			Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Delete.Specify", settings), event);
		}
	}

	private void moduleView(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (args.length == 1) {
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
					Message.deleteMessage(event);
					AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), event));
				} else {
					Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), event);
				}
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.View.Specify", settings), event);
			}
		} else if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
					Message.deleteMessage(event);
					AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.View.InCreator", settings), event));
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Announcement.View.InCreator", settings), event);
				}
			} else {
				try {
					Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(value), guildId);
					if (a != null) {
						Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), AnnouncementMessageFormatter.getSubscriberNames(a), event);
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
					}
				} catch (Exception e) {
					Message.sendMessage(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
				}
			}
		} else {
			Message.sendMessage(MessageManager.getMessage("Creator.Announcement.View.Specify", settings), event);
		}
	}

	//TODO: Use sub rewrite in new creator.
	private void moduleSubscribe(String[] args, MessageReceivedEvent event) {
		long guildId = event.getGuild().getLongID();
		if (args.length == 1) {
			Message.sendMessage("Please specify the ID of the announcement you wish to subscribe to!",
					event);
		} else if (args.length == 2) {
			String value = args[1];
			if (AnnouncementUtils.announcementExists(value, event)) {
				Announcement a = DatabaseManager.getManager()
						.getAnnouncement(UUID.fromString(value), guildId);
				String senderId = event.getMessage().getAuthor().getStringID();
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
					if (!a.getSubscriberUserIds().contains(user.getStringID())) {
						String username = user.getDisplayName(event.getMessage().getGuild());
						a.getSubscriberUserIds().add(user.getStringID());
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
						if (!a.getSubscriberRoleIds().contains(role.getStringID())) {
							String roleName = role.getName();
							a.getSubscriberRoleIds().add(role.getStringID());
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

	private void moduleSubscribeRewriteArgsOne(String[] args, MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		IGuild guild = message.getGuild();
		IUser user = message.getAuthor();
		if (AnnouncementCreator.getCreator().hasAnnouncement(guild.getLongID())) {
			UUID announcementId = AnnouncementCreator.getCreator().getAnnouncement(guild.getLongID())
					.getAnnouncementId();
			Announcement a = AnnouncementCreator.getCreator().getAnnouncement(guild.getLongID());
			String senderId = user.getStringID();
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
	}

	private void moduleSubscribeRewriteArgsTwo(String[] args, MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		IGuild guild = message.getGuild();
		IUser user = message.getAuthor();
		String value = args[1];
		if (args[1].length() <= 32) {
			if (AnnouncementCreator.getCreator().hasAnnouncement(guild.getLongID())) {
				Announcement a = AnnouncementCreator.getCreator().getAnnouncement(guild.getLongID());
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
					if (!a.getSubscriberUserIds().contains(u.getStringID())) {
						a.getSubscriberUserIds().add(u.getStringID());
						String username = u.getName();
						Message.sendMessage(
								"`" + username + "` has been subscribed to the announcement with the ID `" + a
										.getAnnouncementId() + "`" + Message.lineBreak
										+ "To unsubscribe them use `!announcement unsubscribe <announcement ID> <value>",
								event);
					}
				} else if (r != null) {
					if (!a.getSubscriberRoleIds().contains(r.getStringID())) {
						a.getSubscriberRoleIds().add(r.getStringID());
						String username = r.getName();
						Message.sendMessage(
								"The role `" + username
										+ "` has been subscribed to the announcement with the ID `" + a
										.getAnnouncementId() + "`" + Message.lineBreak
										+ "To unsubscribe them use `!announcement unsubscribe <announcement ID> <value>",
								event);
					}
				}
			} else {
				Message.sendMessage(
						"You are not currently creating an announcement! Please specify an announcement ID!",
						event);
			}
		} else {
			if (AnnouncementUtils.announcementExists(value, event)) {
				String senderId = user.getStringID();
				Announcement a = DatabaseManager.getManager()
						.getAnnouncement(UUID.fromString(value), guild.getLongID());
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
	}

	private void moduleSubscribeRewriteArgsThree(String[] args, MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		IGuild guild = message.getGuild();
		IUser user = message.getAuthor();
		List<String> subscribedUsers = new ArrayList<>();
		List<String> subscribedRoles = new ArrayList<>();

		String announcementID;
		boolean updateDb;
		if (args[1].length() > 32) {
			AnnouncementCreator.getCreator().getAnnouncement(guild.getLongID());
			updateDb = true;
			announcementID = args[1];
		} else {
			updateDb = false;
			if (AnnouncementCreator.getCreator().hasAnnouncement(guild.getLongID())) {
				announcementID = AnnouncementCreator.getCreator().getAnnouncement(guild.getLongID())
						.getAnnouncementId().toString();
			} else {
				Message.sendMessage("You must specify an announcement ID as your first argument!", event);
				return;
			}
		}

		if (AnnouncementUtils.announcementExists(announcementID, event) || !updateDb) {
			Announcement a = updateDb ? DatabaseManager.getManager()
					.getAnnouncement(UUID.fromString(announcementID), guild.getLongID())
					: AnnouncementCreator.getCreator().getAnnouncement(guild.getLongID());

			for (int i = 1; i < args.length; i++) {
				IUser u = guild.getUserByID(UserUtils.getUser(args[i], message));
				IRole r = guild.getRoleByID(RoleUtils.getRole(args[i], message));
				if (args[i].toLowerCase().contains("everyone") || args[i].toLowerCase().contains("here")) {
					//Here or everyone is to be subscribed...
					String men = args[i].toLowerCase();
					if (!a.getSubscriberRoleIds().contains(men)) {
						a.getSubscriberRoleIds().add(men);
						Message.sendMessage(
								"`" + men + "` has been subscribed to the announcement with the ID `" + a
										.getAnnouncementId().toString() + "`" + Message.lineBreak
										+ "To unsubscribe them use `!announcement unsubscribe <announcement ID> <value>",
								event);
						subscribedUsers.add(men);
					} else {
						Message.sendMessage(men
										+ " is already subscribed to the specified announcement! To unsubscribe them use `!announcement unsubscribe <announcement ID> <value>`",
								event);
					}
				}

				if (u != null) {
					if (!a.getSubscriberUserIds().contains(u.getStringID())) {
						subscribedUsers.add(u.getName());
						a.getSubscriberUserIds().add(u.getStringID());
					}
				} else if (r != null) {
					if (!a.getSubscriberRoleIds().contains(r.getStringID())) {
						subscribedRoles.add(r.getName());
						a.getSubscriberRoleIds().add(r.getStringID());
					}
				}
			}

			EmbedBuilder em = new EmbedBuilder();
			em.withColor(EventColor.TURQUOISE.getR(), EventColor.TURQUOISE.getG(),
					EventColor.TURQUOISE.getB());
			em.withAuthorIcon(event.getClient().getApplicationIconURL());
			em.withAuthorName("Announcement Subscribe");
			em.withDesc("Subscribed Users: " + subscribedUsers + "\nSubscribed Roles: " + subscribedRoles);
			em.withFooterText("Announcement ID: " + announcementID);
			Message.sendMessage(em.build(), event);
			if (updateDb) {
				DatabaseManager.getManager().updateAnnouncement(a);
			}
		} else {
			Message.sendMessage("There is no announcement by the ID: " + announcementID + "!", event);
		}

	}

	private void moduleSubscribeRewrite(String[] args, MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		IGuild guild = message.getGuild();
		IUser user = message.getAuthor();
		if (args.length == 1) {
			moduleSubscribeRewriteArgsOne(args, event);
		} else if (args.length == 2) {
			moduleSubscribeRewriteArgsTwo(args, event);
		} else {
			moduleSubscribeRewriteArgsThree(args, event);
		}
	}

	private void moduleUnsubscribeRewriteArgsOne(String[] args, MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		IGuild guild = message.getGuild();
		IUser user = message.getAuthor();
		if (AnnouncementCreator.getCreator().hasAnnouncement(guild.getLongID())) {
			UUID announcementId = AnnouncementCreator.getCreator().getAnnouncement(guild.getLongID())
					.getAnnouncementId();
			Announcement a = AnnouncementCreator.getCreator().getAnnouncement(guild.getLongID());
			String senderId = user.getStringID();
			if (a.getSubscriberUserIds().contains(senderId)) {
				a.getSubscriberUserIds().remove(senderId);
				Message.sendMessage(
						"You have unsubscribed from the announcement with the ID: `" + announcementId.toString()
								+ "`" + Message.lineBreak + "To subscribe use `!announcement subscribe <id>`",
						event);
			} else { // Announcement does not contain user ID
				Message.sendMessage("You not subscribed to that event!", event);
			}
		} else { // User not creating an announcement
			Message.sendMessage("Please specify the ID of the announcement you wish to unsubscribe from!",
					event);
		}
	}

	private void moduleUnsubscribeRewriteArgsTwo(String[] args, MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		IGuild guild = message.getGuild();
		IUser user = message.getAuthor();
		String value = args[1];
		if (args[1].length() <= 32) {
			if (AnnouncementCreator.getCreator().hasAnnouncement(guild.getLongID())) {
				Announcement a = AnnouncementCreator.getCreator().getAnnouncement(guild.getLongID());
				IUser u = guild.getUserByID(UserUtils.getUser(value, message));
				IRole r = guild.getRoleByID(RoleUtils.getRole(value, message));
				if (value.equalsIgnoreCase("everyone") || value.equalsIgnoreCase("here")) {
					String men = value.toLowerCase();
					if (a.getSubscriberRoleIds().contains(men)) {
						a.getSubscriberRoleIds().remove(men);
						Message.sendMessage(
								"`" + men + "` has been unsubscribed from the announcement with the ID `" + a
										.getAnnouncementId() + "`" + Message.lineBreak
										+ "To subscribe them use `!announcement subscribe <announcement ID/value> [value]",
								event);
					} else {
						Message.sendMessage(men
										+ " is not subscribed to the specified announcement! To subscribe them use `!announcement subscribe <announcement ID/value> [value]`",
								event);
					}
				} else if (u != null) {
					if (a.getSubscriberUserIds().contains(u.getStringID())) {
						a.getSubscriberUserIds().remove(u.getStringID());
						String username = u.getName();
						Message.sendMessage(
								"`" + username + "` has been unsubscribed from the announcement with the ID `" + a
										.getAnnouncementId() + "`" + Message.lineBreak
										+ "To subscribe them use `!announcement subscribe <announcement ID/value> [value]",
								event);
					}
				} else if (r != null) {
					if (a.getSubscriberRoleIds().contains(r.getStringID())) {
						a.getSubscriberRoleIds().remove(r.getStringID());
						String username = r.getName();
						Message.sendMessage(
								"The role `" + username
										+ "` has been unsubscribed from the announcement with the ID `" + a
										.getAnnouncementId() + "`" + Message.lineBreak
										+ "To subscribe them use `!announcement subscribe <announcement ID/value> [value]",
								event);
					}
				}
			} else {
				Message.sendMessage(
						"You are not currently creating an announcement! Please specify an announcement ID!",
						event);
			}
		} else {
			if (AnnouncementUtils.announcementExists(value, event)) {
				String senderId = user.getStringID();
				Announcement a = DatabaseManager.getManager()
						.getAnnouncement(UUID.fromString(value), guild.getLongID());
				if (!a.getSubscriberUserIds().contains(senderId)) {
					a.getSubscriberUserIds().remove(senderId);
					Message.sendMessage(
							"You have unsubscribed from the announcement with the ID: `" + value + "`"
									+ Message.lineBreak + "To subscribe use `!announcement subscribe <id>`",
							event);
				} else {
					Message.sendMessage("You are not subscribed to that event!", event);
				}
			} else {
				Message.sendMessage(
						"Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?",
						event);
			}
		}
	}

	private void moduleUnsubscribeRewriteArgsThree(String[] args, MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		IGuild guild = message.getGuild();
		IUser user = message.getAuthor();
		List<String> subscribedUsers = new ArrayList<>();
		List<String> subscribedRoles = new ArrayList<>();

		String announcementID;
		boolean updateDb;
		if (args[1].length() > 32) {
			AnnouncementCreator.getCreator().getAnnouncement(guild.getLongID());
			updateDb = true;
			announcementID = args[1];
		} else {
			updateDb = false;
			if (AnnouncementCreator.getCreator().hasAnnouncement(guild.getLongID())) {
				announcementID = AnnouncementCreator.getCreator().getAnnouncement(guild.getLongID())
						.getAnnouncementId().toString();
			} else {
				Message.sendMessage("You must specify an announcement ID as your first argument!", event);
				return;
			}
		}

		if (AnnouncementUtils.announcementExists(announcementID, event) || !updateDb) {
			Announcement a = updateDb ? DatabaseManager.getManager()
					.getAnnouncement(UUID.fromString(announcementID), guild.getLongID())
					: AnnouncementCreator.getCreator().getAnnouncement(guild.getLongID());

			for (int i = 1; i < args.length; i++) {
				IUser u = guild.getUserByID(UserUtils.getUser(args[i], message));
				IRole r = guild.getRoleByID(RoleUtils.getRole(args[i], message));
				if (args[i].toLowerCase().contains("everyone") || args[i].toLowerCase().contains("here")) {
					//Here or everyone is to be subscribed...
					String men = args[i].toLowerCase();
					if (a.getSubscriberRoleIds().contains(men)) {
						a.getSubscriberRoleIds().remove(men);
						Message.sendMessage(
								"`" + men + "` has been unsubscribed from the announcement with the ID `" + a
										.getAnnouncementId().toString() + "`" + Message.lineBreak
										+ "To subscribe them use `!announcement subscribe <announcement ID> <value>",
								event);
						subscribedUsers.add(men);
					} else {
						Message.sendMessage(men
										+ " is not subscribed to the specified announcement! To unsubscribe them use `!announcement unsubscribe <announcement ID> <value>`",
								event);
					}
				}

				if (u != null) {
					if (a.getSubscriberUserIds().contains(u.getStringID())) {
						subscribedUsers.remove(u.getName());
						a.getSubscriberUserIds().remove(u.getStringID());
					}
				} else if (r != null) {
					if (a.getSubscriberRoleIds().contains(r.getStringID())) {
						subscribedRoles.remove(r.getName());
						a.getSubscriberRoleIds().remove(r.getStringID());
					}
				}
			}

			EmbedBuilder em = new EmbedBuilder();
			em.withColor(EventColor.TURQUOISE.getR(), EventColor.TURQUOISE.getG(),
					EventColor.TURQUOISE.getB());
			em.withAuthorIcon(event.getClient().getApplicationIconURL());
			em.withAuthorName("Announcement Unsubscribe");
			em.withDesc("Unsubscribed Users: " + subscribedUsers + "\nUnsubscribed Roles: " + subscribedRoles);
			em.withFooterText("Announcement ID: " + announcementID);
			Message.sendMessage(em.build(), event);
			if (updateDb) {
				DatabaseManager.getManager().updateAnnouncement(a);
			}
		} else {
			Message.sendMessage("There is no announcement by the ID: " + announcementID + "!", event);
		}

	}

	private void moduleUnsubscribeRewrite(String[] args, MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		IGuild guild = message.getGuild();
		IUser user = message.getAuthor();
		if (args.length == 1) {
			moduleUnsubscribeRewriteArgsOne(args, event);
		} else if (args.length == 2) {
			moduleUnsubscribeRewriteArgsTwo(args, event);
		} else {
			moduleUnsubscribeRewriteArgsThree(args, event);
		}
	}

	//TODO: Use unsub rewrite in new creator.
	private void moduleUnsubscribe(String[] args, MessageReceivedEvent event) {
		long guildId = event.getGuild().getLongID();
		if (args.length == 1) {
			Message.sendMessage("Please specify the ID of the announcement you wish to unsubscribe from!",
					event);
		} else if (args.length == 2) {
			String value = args[1];
			if (AnnouncementUtils.announcementExists(value, event)) {
				Announcement a = DatabaseManager.getManager()
						.getAnnouncement(UUID.fromString(value), guildId);
				String senderId = event.getMessage().getAuthor().getStringID();
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
					if (a.getSubscriberUserIds().contains(user.getStringID())) {
						String username = user.getDisplayName(event.getMessage().getGuild());
						a.getSubscriberUserIds().remove(user.getStringID());
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
						if (a.getSubscriberRoleIds().contains(role.getStringID())) {
							String roleName = role.getName();
							a.getSubscriberRoleIds().remove(role.getStringID());
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

	private void moduleType(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				if (AnnouncementType.isValid(value)) {
					AnnouncementType type = AnnouncementType.fromValue(value);
					AnnouncementCreator.getCreator().getAnnouncement(guildId).setAnnouncementType(type);
					if (type.equals(AnnouncementType.SPECIFIC)) {
						if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
							Message.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.Type.Success.Specific", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Type.Success.Specific", settings), event);
						}
					} else if (type.equals(AnnouncementType.COLOR)) {
						if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
							Message.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.Type.Success.Color", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Type.Success.Color", settings), event);
						}
					} else if (type.equals(AnnouncementType.RECUR)) {
						if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
							Message.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.Type.Success.Recur", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Type.Success.Recur", settings), event);
						}
					} else {
						if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
							Message.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.Type.Success.Universal", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Type.Success.Universal", settings), event);
						}
					}
				} else {
					if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
						Message.deleteMessage(event);
						AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.Type.Specify", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Type.Specify", settings), event);
					}
				}
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		} else {
			Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Type.Specify", settings), event);
		}
	}

	private void moduleHours(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				try {
					Integer hoursOr = Integer.valueOf(value);
					Integer hours = Math.abs(hoursOr);
					AnnouncementCreator.getCreator().getAnnouncement(guildId).setHoursBefore(hours);
					if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
						Message.deleteMessage(event);
						AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.Hours.Success.New", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Hours.Success", "%hours%", hours + "", settings), event);
					}
				} catch (NumberFormatException e) {
					if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
						Message.deleteMessage(event);
						AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.Hours.NotInt", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Hours.NotInt", settings), event);
					}
				}
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		} else {
			Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Hours.Specify", settings), event);
		}
	}

	private void moduleMinutes(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				try {
					Integer minutesOr = Integer.valueOf(value);
					Integer minutes = Math.abs(minutesOr);
					AnnouncementCreator.getCreator().getAnnouncement(guildId).setMinutesBefore(minutes);
					if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
						Message.deleteMessage(event);
						AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.Minutes.Success.New", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Minutes.Success", "%minutes%", minutes + "", settings), event);
					}
				} catch (NumberFormatException e) {
					if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
						Message.deleteMessage(event);
						AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.Minutes.NotInt", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Minutes.NotInt", settings), event);
					}
				}
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		} else {
			Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Minutes.Specify", settings), event);
		}
	}

	private void moduleList(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (args.length == 1) {
			if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.List.Specify", settings), event);
			} else {
				if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
					Message.deleteMessage(event);
					AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.List.InCreator", settings), event));
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Announcement.List.InCreator", settings), event);
				}
			}
		} else if (args.length == 2) {
			String value = args[1];
			if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				if (value.equalsIgnoreCase("all")) {
					ArrayList<Announcement> announcements = DatabaseManager.getManager().getAnnouncements(guildId);
					Message.sendMessage(MessageManager.getMessage("Creator.Announcement.List.All", "%amount%", announcements.size() + "", settings), event);
					//Loop and add embeds
					for (Announcement a : announcements) {
						Message.sendMessage(AnnouncementMessageFormatter.getCondensedAnnouncementEmbed(a, settings), event);
					}
				} else {
					//List specific amount of announcements
					try {
						Integer amount = Integer.valueOf(value);
						Message.sendMessage(MessageManager.getMessage("Creator.Announcement.List.Some", "%amount%", amount + "", settings), event);

						int posted = 0;
						for (Announcement a : DatabaseManager.getManager().getAnnouncements(guildId)) {
							if (posted < amount) {
								Message.sendMessage(AnnouncementMessageFormatter.getCondensedAnnouncementEmbed(a, settings), event);

								posted++;
							} else {
								break;
							}
						}
					} catch (NumberFormatException e) {
						Message.sendMessage(MessageManager.getMessage("Creator.Announcement.List.NotInt", settings), event);
					}
				}
			} else {
				if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
					Message.deleteMessage(event);
					AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.List.InCreator", settings), event));
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Announcement.List.InCreator", settings), event);
				}
			}
		} else {
			Message.sendMessage(MessageManager.getMessage("Creator.Announcement.List.Specify", settings), event);
		}
	}

	private void moduleEvent(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				if (AnnouncementCreator.getCreator().getAnnouncement(guildId).getAnnouncementType()
						.equals(AnnouncementType.SPECIFIC)) {
					if (EventUtils.eventExists(guildId, value)) {
						AnnouncementCreator.getCreator().getAnnouncement(guildId).setEventId(value);
						if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
							Message.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.Event.Success.New", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Event.Success", "%id%", value, settings), event);
						}
					} else {
						if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
							Message.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.CannotFind.Event", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Announcement.CannotFind.Event", settings), event);
						}
					}
				} else if (AnnouncementCreator.getCreator().getAnnouncement(guildId).getAnnouncementType()
						.equals(AnnouncementType.RECUR)) {
					if (EventUtils.eventExists(guildId, value)) {
						if (value.contains("_")) {
							String[] stuff = value.split("_");
							value = stuff[0];
						}
						AnnouncementCreator.getCreator().getAnnouncement(guildId).setEventId(value);
						if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
							Message.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.Event.Success.New", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Event.Success", "%id%", value, settings), event);
						}
					} else {
						if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
							Message.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.CannotFind.Event", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Announcement.CannotFind.Event", settings), event);
						}
					}
				} else {
					if (AnnouncementCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(guildId));
						Message.deleteMessage(event);
						AnnouncementCreator.getCreator().setCreatorMessage(Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId), settings), MessageManager.getMessage("Creator.Announcement.Event.Failure.Type", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Event.Failure.Type", settings), event);
					}
				}
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		} else {
			Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Event.Specify", settings), event);
		}
	}

	private void moduleInfo(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (args.length < 2) {
			Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Info.Specify", settings), event);
		} else if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				AnnouncementCreator.getCreator().getAnnouncement(guildId).setInfo(value);
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Info.Success", "%info%", value, settings), event);
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		} else {
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				String value = GeneralUtils.getContent(args, 1);
				AnnouncementCreator.getCreator().getAnnouncement(guildId).setInfo(value);
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Info.Success", "%info%", value, settings), event);
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		}
	}

	private void moduleChannel(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				if (ChannelUtils.channelExists(value, event)) {
					IChannel c = ChannelUtils.getChannelFromNameOrId(value, event);
					if (c != null) {
						AnnouncementCreator.getCreator().getAnnouncement(guildId)
								.setAnnouncementChannelId(c.getStringID());
						Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Channel.Success",  "%channel%", c.getName(), settings), event);
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Announcement.CannotFind.Channel", settings), event);
					}
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Announcement.CannotFind.Channel", settings), event);
				}
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		} else {
			Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Channel.Specify", settings), event);
		}
	}

	private void moduleColor(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				if (AnnouncementCreator.getCreator().getAnnouncement(guildId).getAnnouncementType()
						.equals(AnnouncementType.COLOR)) {
					if (EventColor.exists(value)) {
						EventColor color = EventColor.fromNameOrHexOrID(value);
						AnnouncementCreator.getCreator().getAnnouncement(guildId).setEventColor(color);
						Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Color.Success", "%color%", color.name(), settings), event);
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Color.Specify", settings), event);
					}
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Color.Failure.Type", settings), event);
				}
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		} else {
			Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Color.Specify", settings), event);
		}
	}

	private void moduleCopy(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (args.length == 2) {
			String value = args[1];
			if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
				if (AnnouncementUtils.announcementExists(value, event)) {
					Announcement a = AnnouncementCreator.getCreator().init(event, value);

					Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), MessageManager.getMessage("Creator.Announcement.Copy.Success", settings), event);
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
				}
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Announcement.AlreadyInit", settings), event);
			}
		} else {
			Message.sendMessage(MessageManager.getMessage("Creator.Announcement.Copy.Specify", settings), event);
		}
	}
}