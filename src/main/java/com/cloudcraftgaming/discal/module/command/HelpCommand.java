package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import com.cloudcraftgaming.discal.utils.Message;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
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
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event) {
        if (args.length < 1) {
            EmbedBuilder em = new EmbedBuilder();
            em.withAuthorIcon(Main.client.getGuildByID(266063520112574464L).getIconURL());
            em.withAuthorName("DisCal");
            em.withTitle("DisCal Command Help");
            for (ICommand c : CommandExecutor.getExecutor().getCommands()) {
                if (c.getAliases().size() > 0) {
                    String al = c.getAliases().toString();
                    em.appendField(c.getCommand() + " " + al, c.getCommandInfo().getDescription(), true);
                } else {
                    em.appendField(c.getCommand(), c.getCommandInfo().getDescription(), true);
                }
            }
            em.withFooterText("Check out the official site for more command info!");
            em.withUrl("https://www.cloudcraftgaming.com/discal/commands");
            em.withColor(56, 138, 237);
            Message.sendMessage(em.build(), event);
        } else if (args.length == 1) {
            String cmdFor = args[0];
            ICommand cmd = CommandExecutor.getExecutor().getCommand(cmdFor);
            if (cmd != null) {
                Message.sendMessage(getCommandInfoEmbed(cmd), event);
            }
        }

        return false;
    }

    //Embed formatters
    private EmbedObject getCommandInfoEmbed(ICommand cmd) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID(266063520112574464L).getIconURL());
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