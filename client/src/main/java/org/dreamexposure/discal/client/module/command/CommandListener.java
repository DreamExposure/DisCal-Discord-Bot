package org.dreamexposure.discal.client.module.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.utils.PermissionChecker;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings({"unused", "ToArrayCallWithZeroLengthArrayArgument", "ConstantConditions", "OptionalGetWithoutIsPresent"})
class CommandListener {
	private static CommandExecutor cmd;

	/**
	 * Creates a new CommandListener listener.
	 *
	 * @param _cmd The CommandExecutor instance to use.
	 */
	CommandListener(CommandExecutor _cmd) {
		cmd = _cmd;
	}

	/**
	 * Checks for command validity and calls the command executor if valid.
	 *
	 * @param event The event received to check for a command.
	 */
	public static void onMessageEvent(MessageCreateEvent event) {
		try {
			if (event.getMessage().getContent().isPresent() && event.getMessage().getContent().get().isEmpty() && event.getMember().isPresent() && !event.getMember().get().isBot()) {
				//Message is a valid guild message (not DM and not from a bot). Check if in correct channel.
				GuildSettings settings = DatabaseManager.getManager().getSettings(event.getGuild().block().getId());
				if (event.getMessage().getContent().get().startsWith(settings.getPrefix())) {
					if (PermissionChecker.isCorrectChannel(event)) {
						//Prefixed with ! which should mean it is a command, convert and confirm.
						String[] argsOr = event.getMessage().getContent().get().split("\\s+");
						if (argsOr.length > 1) {
							ArrayList<String> argsOr2 = new ArrayList<>(Arrays.asList(argsOr).subList(1, argsOr.length));
							String[] args = argsOr2.toArray(new String[argsOr2.size()]);

							String command = argsOr[0].replace(settings.getPrefix(), "");
							cmd.issueCommand(command, args, event, settings);
						} else if (argsOr.length == 1) {
							//Only command... no args.
							cmd.issueCommand(argsOr[0].replace(settings.getPrefix(), ""), new String[0], event, settings);
						}
					}
				} else if (!event.getMessage().mentionsEveryone() && !event.getMessage().getContent().get().contains("@here") && (event.getMessage().toString().startsWith("<@" + DisCalClient.getClient().getSelfId().get() + ">") || event.getMessage().toString().startsWith("<@!" + DisCalClient.getClient().getSelfId().get() + ">"))) {
					if (PermissionChecker.isCorrectChannel(event)) {
						String[] argsOr = event.getMessage().getContent().get().split("\\s+");
						if (argsOr.length > 2) {
							ArrayList<String> argsOr2 = new ArrayList<>(Arrays.asList(argsOr).subList(2, argsOr.length));
							String[] args = argsOr2.toArray(new String[argsOr2.size()]);

							String command = argsOr[1];
							cmd.issueCommand(command, args, event, settings);
						} else if (argsOr.length == 2) {
							//No args...
							cmd.issueCommand(argsOr[1], new String[0], event, settings);
						} else if (argsOr.length == 1) {
							//Only disCal mentioned...
							cmd.issueCommand("DisCal", new String[0], event, settings);
						}
					}
				}
			}
		} catch (Exception e) {
			Logger.getLogger().exception(event.getMember().get(), "Command error; event message: " + event.getMessage().getContent(), e, CommandListener.class);
		}
	}
}