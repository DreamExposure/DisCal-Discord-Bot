package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.ArrayList;

/**
 * Created by Nova Fox on 8/31/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
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
		CommandInfo info = new CommandInfo("rsvp");
		info.setDescription("Confirms attendance to an event");
		info.setExample("!rsvp <subCommand> <eventId>");
		
		return info;
	}
	
	/**
	 * Issues the command this Object is responsible for.
	 *
	 * @param args     The command arguments.
	 * @param event    The event received.
	 * @param settings
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	@Override
	public Boolean issueCommand(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		return false;
	}
}