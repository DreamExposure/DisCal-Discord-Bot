package org.dreamexposure.discal.client.module.command;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.object.event.RsvpData;
import org.dreamexposure.discal.core.utils.EventUtils;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.TimeUtils;
import org.dreamexposure.discal.core.utils.UserUtils;

import java.util.ArrayList;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Image;
import discord4j.core.spec.EmbedCreateSpec;

/**
 * Created by Nova Fox on 8/31/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class RsvpCommand implements ICommand {
	/**
	 * Gets the command this Object is responsible for.
	 *
	 * @return The command this Object is responsible for.
	 */
	@Override
	public String getCommand() {
		return "rsvp";
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
		return new ArrayList<>();
	}

	/**
	 * Gets the info on the command (not sub command) to be used in help menus.
	 *
	 * @return The command info.
	 */
	@Override
	public CommandInfo getCommandInfo() {
		CommandInfo info = new CommandInfo(
				"rsvp",
				"Confirms attendance to an event",
				"!rsvp <subCommand> <eventId>"
		);

		info.getSubCommands().put("onTime", "Marks you are going to event");
		info.getSubCommands().put("late", "Marks that you will be late to event");
		info.getSubCommands().put("not", "Marks that you are NOT going to event");
		info.getSubCommands().put("unsure", "Marks that you may or may not go to event");
		info.getSubCommands().put("remove", "Removes your RSVP from the event");
		info.getSubCommands().put("list", "Lists who has RSVPed to event");

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
		if (args.length > 0) {
			switch (args[0].toLowerCase()) {
				case "ontime":
					moduleGoing(args, event, settings);
					break;
				case "late":
					moduleGoingLate(args, event, settings);
					break;
				case "not":
					moduleNotGoing(args, event, settings);
					break;
				case "unsure":
					moduleUnsure(args, event, settings);
					break;
				case "remove":
					moduleRemove(args, event, settings);
					break;
				case "list":
					moduleList(args, event, settings);
					break;
				default:
					MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Args.InvalidSubCmd", settings), event);
					break;
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Args.Few", settings), event);
		}
		return false;
	}

	private void moduleGoing(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 2) {
			String eventId = args[1];
			if (EventUtils.eventExists(settings, eventId)) {
				if (!TimeUtils.inPast(eventId, settings)) {
					RsvpData data = DatabaseManager.getManager().getRsvpData(settings.getGuildID(), eventId);
					data.removeCompletely(event.getMember().get().getId().asString());
					data.getGoingOnTime().add(event.getMember().get().getId().asString());

					DatabaseManager.getManager().updateRsvpData(data);
					MessageManager.sendMessageAsync(MessageManager.getMessage("RSVP.going.success", settings), getRsvpEmbed(data, settings), event);
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Notifications.Event.InPast", settings), event);
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Notifications.Event.NotExist", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("RSVP.going.specify", settings), event);
		}
	}

	private void moduleGoingLate(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 2) {
			String eventId = args[1];
			if (EventUtils.eventExists(settings, eventId)) {
				if (!TimeUtils.inPast(eventId, settings)) {
					RsvpData data = DatabaseManager.getManager().getRsvpData(settings.getGuildID(), eventId);
					data.removeCompletely(event.getMember().get().getId().asString());
					data.getGoingLate().add(event.getMember().get().getId().asString());

					DatabaseManager.getManager().updateRsvpData(data);
					MessageManager.sendMessageAsync(MessageManager.getMessage("RSVP.late.success", settings), getRsvpEmbed(data, settings), event);
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Notifications.Event.InPast", settings), event);
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Notifications.Event.NotExist", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("RSVP.late.specify", settings), event);
		}
	}

	private void moduleNotGoing(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 2) {
			String eventId = args[1];
			if (EventUtils.eventExists(settings, eventId)) {
				if (!TimeUtils.inPast(eventId, settings)) {
					RsvpData data = DatabaseManager.getManager().getRsvpData(settings.getGuildID(), eventId);
					data.removeCompletely(event.getMember().get().getId().asString());
					data.getNotGoing().add(event.getMember().get().getId().asString());

					DatabaseManager.getManager().updateRsvpData(data);
					MessageManager.sendMessageAsync(MessageManager.getMessage("RSVP.not.success", settings), getRsvpEmbed(data, settings), event);
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Notifications.Event.InPast", settings), event);
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Notifications.Event.NotExist", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("RSVP.not.specify", settings), event);
		}
	}

	private void moduleRemove(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 2) {
			String eventId = args[1];
			if (EventUtils.eventExists(settings, eventId)) {
				if (!TimeUtils.inPast(eventId, settings)) {
					RsvpData data = DatabaseManager.getManager().getRsvpData(settings.getGuildID(), eventId);
					data.removeCompletely(event.getMember().get().getId().asString());

					DatabaseManager.getManager().updateRsvpData(data);
					MessageManager.sendMessageAsync(MessageManager.getMessage("RSVP.remove.success", settings), getRsvpEmbed(data, settings), event);
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Notifications.Event.InPast", settings), event);
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Notifications.Event.NotExist", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("RSVP.remove.specify", settings), event);
		}
	}

	private void moduleUnsure(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 2) {
			String eventId = args[1];
			if (EventUtils.eventExists(settings, eventId)) {
				if (!TimeUtils.inPast(eventId, settings)) {
					RsvpData data = DatabaseManager.getManager().getRsvpData(settings.getGuildID(), eventId);
					data.removeCompletely(event.getMember().get().getId().asString());
					data.getUndecided().add(event.getMember().get().getId().asString());

					DatabaseManager.getManager().updateRsvpData(data);
					MessageManager.sendMessageAsync(MessageManager.getMessage("RSVP.unsure.success", settings), getRsvpEmbed(data, settings), event);
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Notifications.Event.InPast", settings), event);
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Notifications.Event.NotExist", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("RSVP.unsure.specify", settings), event);
		}
	}

	private void moduleList(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (args.length == 2) {
			String eventId = args[1];
			if (EventUtils.eventExists(settings, eventId)) {
				RsvpData data = DatabaseManager.getManager().getRsvpData(settings.getGuildID(), eventId);

				MessageManager.sendMessageAsync(getRsvpEmbed(data, settings), event);
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Notifications.Event.NoExist", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("RSVP.list.specify", settings), event);
		}
	}


	private Consumer<EmbedCreateSpec> getRsvpEmbed(RsvpData data, GuildSettings settings) {
		return spec -> {
			Guild g = DisCalClient.getClient().getGuildById(settings.getGuildID()).block();

			if (settings.isBranded() && g != null)
				spec.setAuthor(g.getName(), GlobalConst.discalSite, g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
			else
				spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

			spec.setTitle(MessageManager.getMessage("Embed.RSVP.List.Title", settings));
			spec.addField("Event ID", data.getEventId(), false);

			StringBuilder onTime = new StringBuilder();
			for (Member u : UserUtils.getUsers(data.getGoingOnTime(), g)) {
				onTime.append(u.getDisplayName()).append(", ");
			}

			StringBuilder late = new StringBuilder();
			for (Member u : UserUtils.getUsers(data.getGoingLate(), g)) {
				late.append(u.getDisplayName()).append(", ");
			}

			StringBuilder unsure = new StringBuilder();
			for (Member u : UserUtils.getUsers(data.getUndecided(), g)) {
				unsure.append(u.getDisplayName()).append(", ");
			}

			StringBuilder notGoing = new StringBuilder();
			for (Member u : UserUtils.getUsers(data.getNotGoing(), g)) {
				notGoing.append(u.getDisplayName()).append(", ");
			}

			if (onTime.toString().isEmpty())
				spec.addField("On time", "N/a", true);
			else
				spec.addField("On Time", onTime.toString(), true);

			if (late.toString().isEmpty())
				spec.addField("Late", "N/a", true);
			else
				spec.addField("Late", late.toString(), true);

			if (unsure.toString().isEmpty())
				spec.addField("Unsure", "N/a", true);
			else
				spec.addField("Unsure", unsure.toString(), true);

			if (notGoing.toString().isEmpty())
				spec.addField("Not Going", "N/a", true);
			else
				spec.addField("Not Going", notGoing.toString(), true);

			spec.setFooter(MessageManager.getMessage("Embed.RSVP.List.Footer", settings), null);
			spec.setColor(GlobalConst.discalColor);

		};
	}
}