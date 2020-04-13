package org.dreamexposure.discal.client.module.command;

import org.dreamexposure.discal.client.announcement.AnnouncementCreator;
import org.dreamexposure.discal.client.message.AnnouncementMessageFormatter;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.announcement.AnnouncementCreatorResponse;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.AnnouncementUtils;
import org.dreamexposure.discal.core.utils.ChannelUtils;
import org.dreamexposure.discal.core.utils.EventUtils;
import org.dreamexposure.discal.core.utils.GeneralUtils;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.PermissionChecker;
import org.dreamexposure.discal.core.utils.RoleUtils;
import org.dreamexposure.discal.core.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings({"Duplicates", "OptionalGetWithoutIsPresent", "ConstantConditions", "DuplicateExpressions"})
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
		CommandInfo info = new CommandInfo(
				"announcement",
				"Used for all announcement functions.",
				"!announcement <function> (value(s))"
		);

		info.getSubCommands().put("create", "Starts the announcement creator.");
		info.getSubCommands().put("copy", "Copies an existing announcement.");
		info.getSubCommands().put("edit", "Edits an existing announcement.");
		info.getSubCommands().put("confirm", "Confirms and creates/edits the announcement.");
		info.getSubCommands().put("cancel", "Cancels the current announcement creator/editor");
		info.getSubCommands().put("delete", "Deletes an existing announcement.");
		info.getSubCommands().put("view", "Views the specified announcement.");
		info.getSubCommands().put("review", "Reviews the announcement in the creator/editor.");
		info.getSubCommands().put("subscribe", "Subscribes users/roles to the announcement.");
		info.getSubCommands().put("sub", "Subscribes users/roles to the announcement.");
		info.getSubCommands().put("unsubscribe", "Unsubscribes users/roles to the announcement.");
		info.getSubCommands().put("unsub", "Unsubscribes users/roles to the announcement.");
		info.getSubCommands().put("channel", "Sets the channel the announcement will be sent in.");
		info.getSubCommands().put("type", "Sets the announcement's type.");
		info.getSubCommands().put("hours", "Sets the amount of hours before the event to fire (added to minutes)");
		info.getSubCommands().put("minutes", "Sets the amount of minutes before the event to fire (added to hours)");
		info.getSubCommands().put("list", "Lists an amount of events.");
		info.getSubCommands().put("event", "Sets the event the announcement is for (if applicable)");
		info.getSubCommands().put("color", "Sets the color the announcement is for (if applicable)");
		info.getSubCommands().put("info", "Sets an additional info.");
		info.getSubCommands().put("enable", "Enables or Disables the announcement (alias for `disable`)");
		info.getSubCommands().put("disable", "Enables or Disables the announcement (alias for `enable`)");
		info.getSubCommands().put("infoOnly", "Allows for setting an announcement to ONLY display the ' extra info'");

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
	public boolean issueCommand(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length < 1) {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Args.Few", settings), event);
		} else {
			switch (args[0].toLowerCase()) {
				case "create":
					if (PermissionChecker.hasSufficientRole(event, settings).blockOptional().orElse(false))
						moduleCreate(event, settings);
					else
						MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					break;
				case "confirm":
					if (PermissionChecker.hasSufficientRole(event, settings).blockOptional().orElse(false))
						moduleConfirm(event, settings);
					else
						MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					break;
				case "cancel":
					if (PermissionChecker.hasSufficientRole(event, settings).blockOptional().orElse(false))
						moduleCancel(event, settings);
					else
						MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					break;
				case "delete":
					if (PermissionChecker.hasSufficientRole(event, settings).blockOptional().orElse(false))
						moduleDelete(args, event, settings);
					else
						MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					break;
				case "view":
				case "review":
					moduleView(args, event, settings);
					break;
				case "subscribe":
				case "sub":
					moduleSubscribeRewrite(args, event, settings);
					break;
				case "unsubscribe":
				case "unsub":
					moduleUnsubscribeRewrite(args, event, settings);
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
				case "enable":
				case "enabled":
				case "disable":
				case "disabled":
					moduleEnable(args, event, settings);
					break;
				case "infoonly":
					moduleInfoOnly(args, event, settings);
				case "channel":
					moduleChannel(args, event, settings);
					break;
				case "color":
				case "colour":
					moduleColor(args, event, settings);
					break;
				case "copy":
					if (PermissionChecker.hasSufficientRole(event, settings).blockOptional().orElse(false))
						moduleCopy(args, event, settings);
					else
						MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					break;
				case "edit":
					if (PermissionChecker.hasSufficientRole(event, settings).blockOptional().orElse(false))
						moduleEdit(args, event, settings);
					else
						MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					break;
				default:
					MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Args.Invalid", settings), event);
					break;
			}
		}
		return false;
	}


	private void moduleCreate(MessageCreateEvent event, GuildSettings settings) {

		if (!AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
			AnnouncementCreator.getCreator().init(event, settings);
			if (!AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID()))
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Create.Init", settings), event);
			else
				MessageManager.deleteMessage(event);
		} else {
			if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
				MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
				MessageManager.deleteMessage(event);
				AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.AlreadyInit", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));

			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.AlreadyInit", settings), event);
			}
		}
	}

	private void moduleEdit(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (!AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
			if (args.length == 2) {
				String anId = args[1];
				if (AnnouncementUtils.announcementExists(anId, settings.getGuildID())) {
					Announcement announcement = AnnouncementCreator.getCreator().edit(event, anId, settings);

					if (announcement.getCreatorMessage() == null) {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Edit.Init", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(announcement, settings), event);
					} else {
						MessageManager.deleteMessage(event);
					}
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Edit.Specify", settings), event);
			}
		} else {
			if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
				MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
				MessageManager.deleteMessage(event);
				AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.AlreadyInit", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.AlreadyInit", settings), event);
			}
		}
	}

	private void moduleConfirm(MessageCreateEvent event, GuildSettings settings) {
		if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
			AnnouncementCreatorResponse acr = AnnouncementCreator.getCreator().confirmAnnouncement(settings.getGuildID());
			if (acr.isSuccessful()) {
				if (acr.getAnnouncement().isEditing()) {
					if (acr.getAnnouncement().getCreatorMessage() != null) {
						MessageManager.deleteMessage(acr.getAnnouncement().getCreatorMessage());
						MessageManager.deleteMessage(event);
					}
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Confirm.Edit.Success", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(acr.getAnnouncement(), settings), event);
				} else {
					if (acr.getAnnouncement().getCreatorMessage() != null) {
						MessageManager.deleteMessage(acr.getAnnouncement().getCreatorMessage());
						MessageManager.deleteMessage(event);
					}
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Confirm.Create.Success", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(acr.getAnnouncement(), settings), event);
				}
			} else {
				if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
					MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
					MessageManager.deleteMessage(event);
					AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Confirm.Failure", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Confirm.Failure", settings), event);
				}
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
		}
	}

	private void moduleCancel(MessageCreateEvent event, GuildSettings settings) {
		if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
			Message creatorMessage = AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID());
			AnnouncementCreator.getCreator().terminate(settings.getGuildID());

			if (creatorMessage != null) {
				MessageManager.deleteMessage(creatorMessage);
				MessageManager.deleteMessage(event);
			}

			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Cancel.Success", settings), event);
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
		}
	}

	private void moduleDelete(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 1) {
			if (!AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Delete.Specify", settings), event);
			} else {
				if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
					MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
					MessageManager.deleteMessage(event);
					AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Delete.InCreator", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
				}
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Delete.InCreator", settings), event);
			}
		} else if (args.length == 2) {
			String value = args[1];
			if (AnnouncementUtils.announcementExists(value, settings.getGuildID())) {
				if (DatabaseManager.deleteAnnouncement(value).block()) {

					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Delete.Success", settings), event);
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Delete.Failure", settings), event);
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Delete.Specify", settings), event);
		}
	}

	private void moduleView(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 1) {
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
					MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
					MessageManager.deleteMessage(event);
					AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
				} else {
					MessageManager.sendMessageAsync(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event);
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.View.Specify", settings), event);
			}
		} else if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
					MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
					MessageManager.deleteMessage(event);
					AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.View.InCreator", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.View.InCreator", settings), event);
				}
			} else {
				try {
					Announcement a = DatabaseManager.getAnnouncement(UUID.fromString(value), settings.getGuildID()).block();
					if (a != null) {
						MessageManager.sendMessageAsync(AnnouncementMessageFormatter.getSubscriberNames(a), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event);
					} else {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
					}
				} catch (Exception e) {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
				}
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.View.Specify", settings), event);
		}
	}

	private void moduleSubscribeRewriteArgsOne(MessageCreateEvent event, GuildSettings settings) {
		Member user = event.getMember().get();
		if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
			Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());
			if (!a.getSubscriberUserIds().contains(user.getId().asString())) {
				a.getSubscriberUserIds().add(user.getId().asString());
				if (a.getCreatorMessage() != null) {
					MessageManager.deleteMessage(a.getCreatorMessage());
					MessageManager.deleteMessage(event);
					a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Subscribe.Self.Success", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Subscribe.Self.Success", settings), event);
				}
			} else {
				//Announcement contains user ID
				if (a.getCreatorMessage() != null) {
					MessageManager.deleteMessage(a.getCreatorMessage());
					MessageManager.deleteMessage(event);
					a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Subscribe.Self.Already", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Subscribe.Self.Already", settings), event);
				}
			}
		} else {
			//User not creating an announcement
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Subscribe.Self.Specify", settings), event);
		}
	}

	private void moduleSubscribeRewriteArgsTwo(String[] args, MessageCreateEvent event, GuildSettings settings) {
		Message message = event.getMessage();
		Guild guild = event.getGuild().block();
		Member user = event.getMember().get();
		String value = args[1];
		if (args[1].length() <= 32) {
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());
				Member u = guild.getMemberById(UserUtils.getUser(value, message)).onErrorResume(e -> Mono.empty()).block();
				Role r = guild.getRoleById(RoleUtils.getRole(value, message)).onErrorResume(e -> Mono.empty()).block();
				if (value.equalsIgnoreCase("everyone") || value.equalsIgnoreCase("here")) {
					String men = value.toLowerCase();
					if (!a.getSubscriberRoleIds().contains(men)) {
						a.getSubscriberRoleIds().add(men);
						if (a.getCreatorMessage() != null) {
							MessageManager.deleteMessage(a.getCreatorMessage());
							MessageManager.deleteMessage(event);
							a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Subscribe.Other.Success", "%value%", men, settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Subscribe.Other.Success", "%value%", men, settings), event);
						}
					} else {
						if (a.getCreatorMessage() != null) {
							MessageManager.deleteMessage(a.getCreatorMessage());
							MessageManager.deleteMessage(event);
							a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Subscribe.Other.Already", "%value%", men, settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Subscribe.Other.Already", "%value%", men, settings), event);
						}
					}
				} else if (u != null) {
					String username = u.getUsername();
					if (!a.getSubscriberUserIds().contains(u.getId().asString())) {
						a.getSubscriberUserIds().add(u.getId().asString());
						if (a.getCreatorMessage() != null) {
							MessageManager.deleteMessage(a.getCreatorMessage());
							MessageManager.deleteMessage(event);
							a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Subscribe.Other.Success", "%value%", username, settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Subscribe.Other.Success", "%value%", username, settings), event);
						}
					} else {
						if (a.getCreatorMessage() != null) {
							MessageManager.deleteMessage(a.getCreatorMessage());
							MessageManager.deleteMessage(event);
							a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Subscribe.Other.Already", "%value%", username, settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Subscribe.Other.Already", "%value%", username, settings), event);
						}
					}
				} else if (r != null) {
					String username = r.getName();
					if (!a.getSubscriberRoleIds().contains(r.getId().asString())) {
						a.getSubscriberRoleIds().add(r.getId().asString());
						if (a.getCreatorMessage() != null) {
							MessageManager.deleteMessage(a.getCreatorMessage());
							MessageManager.deleteMessage(event);
							a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Subscribe.Other.Success", "%value%", username, settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Subscribe.Other.Success", "%value%", username, settings), event);
						}
					} else {
						if (a.getCreatorMessage() != null) {
							MessageManager.deleteMessage(a.getCreatorMessage());
							MessageManager.deleteMessage(event);
							a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Subscribe.Other.Already", "%value%", username, settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Subscribe.Other.Already", "%value%", username, settings), event);
						}
					}
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Subscribe.Self.Specify", settings), event);
			}
		} else {
			if (AnnouncementUtils.announcementExists(value, settings.getGuildID())) {
				Announcement a = DatabaseManager.getAnnouncement(UUID.fromString(value), settings.getGuildID()).block();
				if (!a.getSubscriberUserIds().contains(user.getId().asString())) {
					a.getSubscriberUserIds().add(user.getId().asString());
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Subscribe.Self.Success", settings), event);
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Subscribe.Self.Already", settings), event);
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
			}
		}
	}

	private void moduleSubscribeRewriteArgsThree(String[] args, MessageCreateEvent event, GuildSettings settings) {
		Guild guild = event.getGuild().block();
		List<String> subscribedUsers = new ArrayList<>();
		List<String> subscribedRoles = new ArrayList<>();

		String announcementID;
		boolean updateDb;
		if (args[1].length() > 32) {
			AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());
			updateDb = true;
			announcementID = args[1];
		} else {
			updateDb = false;
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				announcementID = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()).getAnnouncementId().toString();
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Subscribe.Other.Specify", settings), event);
				return;
			}
		}

		if (AnnouncementUtils.announcementExists(announcementID, settings.getGuildID()) || !updateDb) {
			Announcement a = updateDb ? DatabaseManager.getAnnouncement(UUID.fromString(announcementID), settings.getGuildID()).block() : AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

			for (int i = 1; i < args.length; i++) {
				Member u = null;
				Role r = null;

				Snowflake usf = UserUtils.getUser(args[i].matches("<@?!?#?&?[0-9]+>") ? args[i].replaceAll("<@?!?#?&?[0-9]+>", "") : args[i], guild);
				if (usf != null)
					u = guild.getMemberById(usf).onErrorResume(e -> Mono.empty()).block();

				Snowflake rsf = RoleUtils.getRole(args[i].matches("<@?!?#?&?[0-9]+>") ? args[i].replaceAll("<@?!?#?&?[0-9]+>", "") : args[i], guild);
				if (rsf != null)
					r = guild.getRoleById(rsf).onErrorResume(e -> Mono.empty()).block();

				if (args[i].equalsIgnoreCase("everyone") || args[i].equalsIgnoreCase("here")) {
					//Here or everyone is to be subscribed...
					String men = args[i].toLowerCase();
					if (!a.getSubscriberRoleIds().contains(men)) {
						a.getSubscriberRoleIds().add(men);
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Subscribe.Other.Success", "%value%", men, settings), event);
						subscribedUsers.add(men);
					} else {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Subscribe.Other.Already", "%value%", men, settings), event);
					}
				}

				if (u != null) {
					if (!a.getSubscriberUserIds().contains(u.getId().asString())) {
						subscribedUsers.add(u.getDisplayName());
						a.getSubscriberUserIds().add(u.getId().asString());
					}
				} else if (r != null) {
					if (!a.getSubscriberRoleIds().contains(r.getId().asString())) {
						subscribedRoles.add(r.getName());
						a.getSubscriberRoleIds().add(r.getId().asString());
					}
				}
			}

			Consumer<EmbedCreateSpec> embed = spec -> {
				spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

				spec.setColor(GlobalConst.discalColor);
				spec.setDescription(MessageManager.getMessage("Embed.Announcement.Subscribe.Users", "%users%", subscribedUsers.toString(), settings) + GlobalConst.lineBreak + MessageManager.getMessage("Embed.Announcement.Subscribe.Roles", "%roles%", subscribedRoles.toString(), settings));
				spec.setFooter(MessageManager.getMessage("Embed.Announcement.Subscribe.Footer", "%id%", a.getAnnouncementId().toString(), settings), null);
			};

			MessageManager.sendMessageAsync(embed, event);
			if (updateDb)
				DatabaseManager.updateAnnouncement(a).subscribe();
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
		}
	}

	private void moduleSubscribeRewrite(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 1)
			moduleSubscribeRewriteArgsOne(event, settings);
		else if (args.length == 2)
			moduleSubscribeRewriteArgsTwo(args, event, settings);
		else
			moduleSubscribeRewriteArgsThree(args, event, settings);
	}

	private void moduleUnsubscribeRewriteArgsOne(MessageCreateEvent event, GuildSettings settings) {
		Member user = event.getMember().get();
		if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
			Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());
			if (a.getSubscriberUserIds().contains(user.getId().asString())) {
				a.getSubscriberUserIds().remove(user.getId().asString());
				if (a.getCreatorMessage() != null) {
					MessageManager.deleteMessage(a.getCreatorMessage());
					MessageManager.deleteMessage(event);
					a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Self.Success", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Self.Success", settings), event);
				}
			} else { // Announcement does not contain user ID
				if (a.getCreatorMessage() != null) {
					MessageManager.deleteMessage(a.getCreatorMessage());
					MessageManager.deleteMessage(event);
					a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Self.Not", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Self.Not", settings), event);
				}
			}
		} else { // User not creating an announcement
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Self.Specify", settings), event);
		}
	}

	private void moduleUnsubscribeRewriteArgsTwo(String[] args, MessageCreateEvent event, GuildSettings settings) {
		Guild guild = event.getGuild().block();
		Member user = event.getMember().get();
		String value = args[1];
		if (args[1].length() <= 32) {
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

				Member u = null;
				Role r = null;

				Snowflake usf = UserUtils.getUser(value.matches("<@?!?#?&?[0-9]+>") ? value.replaceAll("<@?!?#?&?[0-9]+>", "") : value, guild);
				if (usf != null)
					u = guild.getMemberById(usf).onErrorResume(e -> Mono.empty()).block();

				Snowflake rsf = RoleUtils.getRole(value.matches("<@?!?#?&?[0-9]+>") ? value.replaceAll("<@?!?#?&?[0-9]+>", "") : value, guild);
				if (rsf != null)
					r = guild.getRoleById(rsf).onErrorResume(e -> Mono.empty()).block();

				if (value.equalsIgnoreCase("everyone") || value.equalsIgnoreCase("here")) {
					String men = value.toLowerCase();
					if (a.getSubscriberRoleIds().contains(men)) {
						a.getSubscriberRoleIds().remove(men);
						if (a.getCreatorMessage() != null) {
							MessageManager.deleteMessage(a.getCreatorMessage());
							MessageManager.deleteMessage(event);
							a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Success", "%value%", men, settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Success", "%value%", men, settings), event);
						}
					} else {
						if (a.getCreatorMessage() != null) {
							MessageManager.deleteMessage(a.getCreatorMessage());
							MessageManager.deleteMessage(event);
							a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Not", "%value%", men, settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Not", "%value%", men, settings), event);
						}
					}
				} else if (u != null) {
					if (a.getSubscriberUserIds().contains(u.getId().asString())) {
						a.getSubscriberUserIds().remove(u.getId().asString());
						if (a.getCreatorMessage() != null) {
							MessageManager.deleteMessage(a.getCreatorMessage());
							MessageManager.deleteMessage(event);
							a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Success", "%value%", u.getUsername(), settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Success", "%value%", u.getUsername(), settings), event);
						}
					} else {
						if (a.getCreatorMessage() != null) {
							MessageManager.deleteMessage(a.getCreatorMessage());
							MessageManager.deleteMessage(event);
							a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Not", "%value%", u.getUsername(), settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Not", "%value%", u.getUsername(), settings), event);
						}
					}
				} else if (r != null) {
					String username = r.getName();
					if (a.getSubscriberRoleIds().contains(r.getId().asString())) {
						a.getSubscriberRoleIds().remove(r.getId().asString());
						if (a.getCreatorMessage() != null) {
							MessageManager.deleteMessage(a.getCreatorMessage());
							MessageManager.deleteMessage(event);
							a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Success", "%value%", username, settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Success", "%value%", username, settings), event);
						}
					} else {
						if (a.getCreatorMessage() != null) {
							MessageManager.deleteMessage(a.getCreatorMessage());
							MessageManager.deleteMessage(event);
							a.setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Not", "%value%", username, settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Not", "%value%", username, settings), event);
						}
					}
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Specify", settings), event);
			}
		} else {
			if (AnnouncementUtils.announcementExists(value, settings.getGuildID())) {
				Announcement a = DatabaseManager.getAnnouncement(UUID.fromString(value), settings.getGuildID()).block();
				if (!a.getSubscriberUserIds().contains(user.getId().asString())) {
					a.getSubscriberUserIds().remove(user.getId().asString());
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Self.Success", settings), event);
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Self.Not", settings), event);
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
			}
		}
	}

	private void moduleUnsubscribeRewriteArgsThree(String[] args, MessageCreateEvent event, GuildSettings settings) {
		Guild guild = event.getGuild().block();
		List<String> subscribedUsers = new ArrayList<>();
		List<String> subscribedRoles = new ArrayList<>();

		String announcementID;
		boolean updateDb;
		if (args[1].length() > 32) {
			AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());
			updateDb = true;
			announcementID = args[1];
		} else {
			updateDb = false;
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				announcementID = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()).getAnnouncementId().toString();
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Specify", settings), event);
				return;
			}
		}

		if (AnnouncementUtils.announcementExists(announcementID, settings.getGuildID()) || !updateDb) {
			Announcement a = updateDb ? DatabaseManager.getAnnouncement(UUID.fromString(announcementID), settings.getGuildID()).block() : AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

			for (int i = 1; i < args.length; i++) {
				Member u = null;
				Role r = null;

				Snowflake usf = UserUtils.getUser(args[i].matches("<@?!?#?&?[0-9]+>") ? args[i].replaceAll("<@?!?#?&?[0-9]+>", "") : args[i], guild);
				if (usf != null)
					u = guild.getMemberById(usf).onErrorResume(e -> Mono.empty()).block();

				Snowflake rsf = RoleUtils.getRole(args[i].matches("<@?!?#?&?[0-9]+>") ? args[i].replaceAll("<@?!?#?&?[0-9]+>", "") : args[i], guild);
				if (rsf != null)
					r = guild.getRoleById(rsf).onErrorResume(e -> Mono.empty()).block();

				if (args[i].toLowerCase().contains("everyone") || args[i].toLowerCase().contains("here")) {
					//Here or everyone is to be subscribed...
					String men = args[i].toLowerCase();
					if (a.getSubscriberRoleIds().contains(men)) {
						a.getSubscriberRoleIds().remove(men);
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Success", "%value%", men, settings), event);
						subscribedUsers.add(men);
					} else {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Unsubscribe.Other.Not", "%value%", men, settings), event);
					}
				}

				if (u != null) {
					if (a.getSubscriberUserIds().contains(u.getId().asString())) {
						subscribedUsers.add(u.getUsername());
						a.getSubscriberUserIds().remove(u.getId().asString());
					}
				} else if (r != null) {
					if (a.getSubscriberRoleIds().contains(r.getId().asString())) {
						subscribedRoles.add(r.getName());
						a.getSubscriberRoleIds().remove(r.getId().asString());
					}
				}
			}

			Consumer<EmbedCreateSpec> embed = spec -> {
				spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

				spec.setColor(GlobalConst.discalColor);
				spec.setDescription(MessageManager.getMessage("Embed.Announcement.Unsubscribe.Users", "%users%", subscribedUsers.toString(), settings) + GlobalConst.lineBreak + MessageManager.getMessage("Embed.Announcement.Unsubscribe.Roles", "%roles%", subscribedRoles.toString(), settings));
				spec.setFooter(MessageManager.getMessage("Embed.Announcement.Unsubscribe.Footer", "%id%", a.getAnnouncementId().toString(), settings), null);
			};

			MessageManager.sendMessageAsync(embed, event);
			if (updateDb)
				DatabaseManager.updateAnnouncement(a).subscribe();
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
		}

	}

	private void moduleUnsubscribeRewrite(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 1)
			moduleUnsubscribeRewriteArgsOne(event, settings);
		else if (args.length == 2)
			moduleUnsubscribeRewriteArgsTwo(args, event, settings);
		else
			moduleUnsubscribeRewriteArgsThree(args, event, settings);
	}

	private void moduleType(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				if (AnnouncementType.isValid(value)) {
					AnnouncementType type = AnnouncementType.fromValue(value);
					AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()).setAnnouncementType(type);
					if (type.equals(AnnouncementType.SPECIFIC)) {
						if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
							MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
							MessageManager.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Type.Success.Specific", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Type.Success.Specific", settings), event);
						}
					} else if (type.equals(AnnouncementType.COLOR)) {
						if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
							MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
							MessageManager.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Type.Success.Color", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Type.Success.Color", settings), event);
						}
					} else if (type.equals(AnnouncementType.RECUR)) {
						if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
							MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
							MessageManager.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Type.Success.Recur", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Type.Success.Recur", settings), event);
						}
					} else {
						if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
							MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
							MessageManager.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Type.Success.Universal", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Type.Success.Universal", settings), event);
						}
					}
				} else {
					if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
						MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
						MessageManager.deleteMessage(event);
						AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Type.Specify", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
					} else {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Type.Specify", settings), event);
					}
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Type.Specify", settings), event);
		}
	}

	private void moduleHours(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				try {
					int hoursOr = Integer.parseInt(value);
					int hours = Math.abs(hoursOr);
					AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()).setHoursBefore(hours);
					if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
						MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
						MessageManager.deleteMessage(event);
						AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Hours.Success.New", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
					} else {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Hours.Success", "%hours%", hours + "", settings), event);
					}
				} catch (NumberFormatException e) {
					if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
						MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
						MessageManager.deleteMessage(event);
						AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Hours.NotInt", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
					} else {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Hours.NotInt", settings), event);
					}
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Hours.Specify", settings), event);
		}
	}

	private void moduleMinutes(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				try {
					int minutesOr = Integer.parseInt(value);
					int minutes = Math.abs(minutesOr);
					AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()).setMinutesBefore(minutes);
					if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
						MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
						MessageManager.deleteMessage(event);
						AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Minutes.Success.New", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
					} else {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Minutes.Success", "%minutes%", minutes + "", settings), event);
					}
				} catch (NumberFormatException e) {
					if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
						MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
						MessageManager.deleteMessage(event);
						AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Minutes.NotInt", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
					} else {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Minutes.NotInt", settings), event);
					}
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Minutes.Specify", settings), event);
		}
	}

	private void moduleList(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 1) {
			if (!AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.List.Specify", settings), event);
			} else {
				if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
					MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
					MessageManager.deleteMessage(event);
					AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.List.InCreator", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.List.InCreator", settings), event);
				}
			}
		} else if (args.length == 2) {
			String value = args[1];
			if (!AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				if (value.equalsIgnoreCase("all")) {
					List<Announcement> announcements = DatabaseManager.getAnnouncements(settings.getGuildID()).block();
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.List.All", "%amount%", announcements.size() + "", settings), event);
					//Loop and add embeds
					for (Announcement a : announcements) {
						MessageManager.sendMessageAsync(AnnouncementMessageFormatter.getCondensedAnnouncementEmbed(a, settings), event);
					}
				} else {
					//List specific amount of announcements
					try {
						int amount = Integer.parseInt(value);
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.List.Some", "%amount%", amount + "", settings), event);

						int posted = 0;
						for (Announcement a : DatabaseManager.getAnnouncements(settings.getGuildID()).block()) {
							if (posted < amount) {
								MessageManager.sendMessageAsync(AnnouncementMessageFormatter.getCondensedAnnouncementEmbed(a, settings), event);

								posted++;
							} else {
								break;
							}
						}
					} catch (NumberFormatException e) {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.List.NotInt", settings), event);
					}
				}
			} else {
				if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
					MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
					MessageManager.deleteMessage(event);
					AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.List.InCreator", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.List.InCreator", settings), event);
				}
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.List.Specify", settings), event);
		}
	}

	private void moduleEvent(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				if (AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()).getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
					if (EventUtils.eventExists(settings, value)) {
						AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()).setEventId(value);
						if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
							MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
							MessageManager.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Event.Success.New", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Event.Success", "%id%", value, settings), event);
						}
					} else {
						if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
							MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
							MessageManager.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.CannotFind.Event", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.CannotFind.Event", settings), event);
						}
					}
				} else if (AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()).getAnnouncementType().equals(AnnouncementType.RECUR)) {
					if (EventUtils.eventExists(settings, value)) {
						if (value.contains("_")) {
							String[] stuff = value.split("_");
							value = stuff[0];
						}
						AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()).setEventId(value);
						if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
							MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
							MessageManager.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Event.Success.New", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Event.Success", "%id%", value, settings), event);
						}
					} else {
						if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
							MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
							MessageManager.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.CannotFind.Event", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.CannotFind.Event", settings), event);
						}
					}
				} else {
					if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
						MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
						MessageManager.deleteMessage(event);
						AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Event.Failure.Type", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
					} else {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Event.Failure.Type", settings), event);
					}
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Event.Specify", settings), event);
		}
	}

	private void moduleInfo(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length < 2) {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Info.Specify", settings), event);
		} else if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()).setInfo(value);
				if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
					MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
					MessageManager.deleteMessage(event);
					AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Info.Success.New", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Info.Success", "%info%", value, settings), event);
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		} else {
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				String value = GeneralUtils.getContent(args, 1);
				AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()).setInfo(value);
				if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
					MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
					MessageManager.deleteMessage(event);
					AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Info.Success.New", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Info.Success", "%info%", value, settings), event);
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		}
	}

	private void moduleEnable(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 2) {
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Announcement.Enable.Creator", settings), event);
			} else {
				String value = args[1];
				if (!AnnouncementUtils.announcementExists(value, settings.getGuildID())) {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
				} else {
					Announcement a = DatabaseManager.getAnnouncement(UUID.fromString(value), settings.getGuildID()).block();
					a.setEnabled(!a.isEnabled());

					DatabaseManager.updateAnnouncement(a).subscribe();

					MessageManager.sendMessageAsync(MessageManager.getMessage("Announcement.Enable.Success", "%value%", a.isEnabled() + "", settings), event);
				}
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Announcement.Enable.Specify", settings), event);
		}
	}

	private void moduleInfoOnly(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 2) {
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Announcement.InfoOnly.Creator", settings), event);
			} else {
				String value = args[1];
				if (!AnnouncementUtils.announcementExists(value, settings.getGuildID())) {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
				} else {
					Announcement a = DatabaseManager.getAnnouncement(UUID.fromString(value), settings.getGuildID()).block();
					a.setInfoOnly(!a.isInfoOnly());

					DatabaseManager.updateAnnouncement(a).subscribe();

					MessageManager.sendMessageAsync(MessageManager.getMessage("Announcement.InfoOnly.Success", "%value%", a.isInfoOnly() + "", settings), event);
				}
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Announcement.InfoOnly.Specify", settings), event);
		}
	}

	private void moduleChannel(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				if (ChannelUtils.channelExists(value, event)) {
					GuildChannel c = ChannelUtils.getChannelFromNameOrId(value, event);
					if (c != null) {
						AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()).setAnnouncementChannelId(c.getId().asString());
						if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
							MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
							MessageManager.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Channel.Success.New", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Channel.Success", "%channel%", c.getName(), settings), event);
						}
					} else {
						if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
							MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
							MessageManager.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.CannotFind.Channel", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.CannotFind.Channel", settings), event);
						}
					}
				} else {
					if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
						MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
						MessageManager.deleteMessage(event);
						AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.CannotFind.Channel", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
					} else {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.CannotFind.Channel", settings), event);
					}
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Channel.Specify", settings), event);
		}
	}

	private void moduleColor(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 2) {
			String value = args[1];
			if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				if (AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()).getAnnouncementType().equals(AnnouncementType.COLOR)) {
					if (EventColor.exists(value)) {
						EventColor color = EventColor.fromNameOrHexOrID(value);
						AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()).setEventColor(color);
						if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
							MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
							MessageManager.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Color.Success.New", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Color.Success", "%color%", color.name(), settings), event);
						}
					} else {
						if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
							MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
							MessageManager.deleteMessage(event);
							AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Color.Specify", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
						} else {
							MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Color.Specify", settings), event);
						}
					}
				} else {
					if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
						MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
						MessageManager.deleteMessage(event);
						AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Color.Failure.Type", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
					} else {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Color.Failure.Type", settings), event);
					}
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.NotInit", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Color.Specify", settings), event);
		}
	}

	private void moduleCopy(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 2) {
			String value = args[1];
			if (!AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
				if (AnnouncementUtils.announcementExists(value, settings.getGuildID())) {
					Announcement a = AnnouncementCreator.getCreator().init(event, value, settings);

					if (a.getCreatorMessage() == null) {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Copy.Success", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), event);
					}
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
				}
			} else {
				if (AnnouncementCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
					MessageManager.deleteMessage(AnnouncementCreator.getCreator().getCreatorMessage(settings.getGuildID()));
					MessageManager.deleteMessage(event);
					AnnouncementCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.AlreadyInit", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID()), settings), event));
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.AlreadyInit", settings), event);
				}
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Announcement.Copy.Specify", settings), event);
		}
	}
}