package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.utils.Message;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class HelpCommand implements ICommand {
    /**
     * Gets the command this Object is responsible for.
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "help";
    }

    /**
     * Issues the command this Object is responsible for.
     * @param args The command arguments.
     * @param event The event received.
     * @param client The Client associated with the Bot.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        StringBuilder cmds = new StringBuilder();

        for (String c : CommandExecutor.getExecutor().getAllCommands()) {
            cmds.append(c).append(", ");
        }
        cmds = new StringBuilder(cmds.substring(0, cmds.length() - 2));

        //TODO: Make this prettier!
        Message.sendMessage("All commands: " + cmds + Message.lineBreak + Message.lineBreak + "For extra help visit: https://www.cloudcraftgaming.com/discal/", event, client);
        return true;
    }
}