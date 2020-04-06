package org.dreamexposure.discal.client.module.command;

import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.GlobalConst;

import java.util.ArrayList;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("Duplicates")
public class HelpCommand implements ICommand {
	/**
	 * Gets the command this Object is responsible for.
	 *
	 * @return The command this Object is responsible for.
	 */
	@Override
	public String getCommand() {
		return "help";
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
		return new CommandInfo("help",
				"Displays help (duh)",
				"!help (command) (sub-command)");
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
			Consumer<EmbedCreateSpec> embed = spec -> {
				spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);
				spec.setTitle("DisCal Command Help");
				for (ICommand c : CommandExecutor.getExecutor().getCommands()) {
					if (c.getAliases().size() > 0) {
						String al = c.getAliases().toString();
						spec.addField(c.getCommand() + " " + al, c.getCommandInfo().getDescription(), true);
					} else {
						spec.addField(c.getCommand(), c.getCommandInfo().getDescription(), true);
					}
				}
				spec.setFooter("Check out the official site for more command info!", null);
				spec.setUrl("https://www.discalbot.com/commands");
				spec.setColor(GlobalConst.discalColor);
			};
			MessageManager.sendMessageAsync(embed, event);
		} else if (args.length == 1) {
			String cmdFor = args[0];
			ICommand cmd = CommandExecutor.getExecutor().getCommand(cmdFor);
			if (cmd != null)
				MessageManager.sendMessageAsync(getCommandInfoEmbed(cmd), event);

		} else if (args.length == 2) {
			//Display sub command info
			String cmdFor = args[0];
			ICommand cmd = CommandExecutor.getExecutor().getCommand(cmdFor);
			if (cmd != null) {
				if (cmd.getCommandInfo().getSubCommands().containsKey(args[1].toLowerCase()))
					MessageManager.sendMessageAsync(getSubCommandEmbed(cmd, args[1].toLowerCase()), event);
				else
					MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Args.InvalidSubCmd", settings), event);
			}
		}

		return false;
	}

	//Embed formatters
	private Consumer<EmbedCreateSpec> getCommandInfoEmbed(ICommand cmd) {
		return spec -> {
			spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);
			spec.addField("Command", cmd.getCommand(), true);
			spec.addField("Description", cmd.getCommandInfo().getDescription(), true);
			spec.addField("Example", cmd.getCommandInfo().getExample(), true);

			//Loop through sub commands
			if (cmd.getCommandInfo().getSubCommands().size() > 0) {
				String subs = cmd.getCommandInfo().getSubCommands().keySet().toString();
				subs = subs.replace("[", "").replace("]", "");
				spec.addField("Sub-Commands", subs, false);
			}

			spec.setFooter("<> = required | () = optional", null);

			spec.setUrl("https://www.discalbot.com/commands");

			spec.setColor(GlobalConst.discalColor);

		};
	}

	private Consumer<EmbedCreateSpec> getSubCommandEmbed(ICommand cmd, String subCommand) {
		return spec -> {
			spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);
			spec.addField("Command", cmd.getCommand(), true);
			spec.addField("Sub Command", subCommand, true);

			spec.addField("Usage", cmd.getCommandInfo().getSubCommands().get(subCommand), false);

			spec.setFooter("<> = required | () = optional", null);

			spec.setUrl("https://www.discalbot.com/commands");

			spec.setColor(GlobalConst.discalColor);

		};
	}
}