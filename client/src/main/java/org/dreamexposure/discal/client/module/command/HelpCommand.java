package org.dreamexposure.discal.client.module.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.GlobalConst;

import java.util.ArrayList;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
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
		CommandInfo info = new CommandInfo("help");
		info.setDescription("Displays help (duh).");
		info.setExample("!help (command) (sub-command)");

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
			EmbedCreateSpec em = new EmbedCreateSpec();
			em.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);
			em.setTitle("DisCal Command Help");
			for (ICommand c: CommandExecutor.getExecutor().getCommands()) {
				if (c.getAliases().size() > 0) {
					String al = c.getAliases().toString();
					em.addField(c.getCommand() + " " + al, c.getCommandInfo().getDescription(), true);
				} else {
					em.addField(c.getCommand(), c.getCommandInfo().getDescription(), true);
				}
			}
			em.setFooter("Check out the official site for more command info!", null);
			em.setUrl("https://www.discalbot.com/commands");
			em.setColor(GlobalConst.discalColor);
			MessageManager.sendMessageAsync(em, event);
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
	private EmbedCreateSpec getCommandInfoEmbed(ICommand cmd) {
		EmbedCreateSpec em = new EmbedCreateSpec();
		em.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);
		em.addField("Command", cmd.getCommand(), true);
		em.addField("Description", cmd.getCommandInfo().getDescription(), true);
		em.addField("Example", cmd.getCommandInfo().getExample(), true);

		//Loop through sub commands
		if (cmd.getCommandInfo().getSubCommands().size() > 0) {
			String subs = cmd.getCommandInfo().getSubCommands().keySet().toString();
			subs = subs.replace("[", "").replace("]", "");
			em.addField("Sub-Commands", subs, false);
		}

		em.setFooter("<> = required | () = optional", null);

		em.setUrl("https://www.discalbot.com/commands");

		em.setColor(GlobalConst.discalColor);

		return em;
	}

	private EmbedCreateSpec getSubCommandEmbed(ICommand cmd, String subCommand) {
		EmbedCreateSpec em = new EmbedCreateSpec();
		em.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);
		em.addField("Command", cmd.getCommand(), true);
		em.addField("Sub Command", subCommand, true);

		em.addField("Usage", cmd.getCommandInfo().getSubCommands().get(subCommand), false);

		em.setFooter("<> = required | () = optional", null);

		em.setUrl("https://www.discalbot.com/commands");

		em.setColor(GlobalConst.discalColor);

		return em;
	}
}