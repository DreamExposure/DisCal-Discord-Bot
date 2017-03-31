package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import com.cloudcraftgaming.discal.utils.Message;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;

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
     * @param args The command arguments.
     * @param event The event received.
     * @param client The Client associated with the Bot.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        StringBuilder cmds = new StringBuilder();

        if (args.length < 1) {
            for (ICommand c : CommandExecutor.getExecutor().getCommands()) {
                if (c.getAliases().size() > 0) {
                    cmds.append(c.getCommand()).append(c.getAliases().toString()).append(", ");
                } else {
                    cmds.append(c.getCommand()).append(", ");
                }
            }
            cmds = new StringBuilder(cmds.substring(0, cmds.length() - 2));

            //TODO: Make this prettier!
            Message.sendMessage("All commands: " + cmds + Message.lineBreak + Message.lineBreak + "Use `!help (command) (sub-command)` for more info on the command!" + Message.lineBreak + Message.lineBreak + "For extra help visit: https://www.cloudcraftgaming.com/discal/", event, client);
            return true;
        } else if (args.length == 1) {
            String cmdFor = args[0];
            ICommand cmd = CommandExecutor.getExecutor().getCommand(cmdFor);
            if (cmd != null) {
                Message.sendMessage(getCommandInfoEmbed(cmd), event, client);
            }
        }

        return false;
    }

    //Embed formatters
    private EmbedObject getCommandInfoEmbed(ICommand cmd) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.appendField("Command", cmd.getCommand(), true);
        em.appendField("Description", cmd.getCommandInfo().getDescription(), true);
        em.appendField("Example", cmd.getCommandInfo().getExample(), true);

        //Loop through sub commands
        if (cmd.getCommandInfo().getSubCommands().size() > 0) {
            String subs = cmd.getCommandInfo().getSubCommands().toString();
            subs = subs.replace("[", "").replace("]", "");
            em.appendField("Sub-Commands", subs, false);
        }

        em.withFooterText("<> = required | () = optional");

        em.withColor(56, 138, 237);

        return em.build();
    }
}