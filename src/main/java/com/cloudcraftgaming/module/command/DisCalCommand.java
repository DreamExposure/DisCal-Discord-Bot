package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.Main;
import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.internal.data.BotData;
import com.cloudcraftgaming.utils.Message;
import com.cloudcraftgaming.utils.PermissionChecker;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;

/**
 * Created by Nova Fox on 1/5/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class DisCalCommand implements ICommand {
    @Override
    public String getCommand() {
        return "Discal";
    }

    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        if (args.length < 1) {
          	EmbedBuilder em = new EmbedBuilder();
				em.withAuthorIcon(client.getGuildByID("266063520112574464").getIconURL());
				em.withAuthorName("DisCal!");
				em.withTitle("DisCal is the official Discord Calendar Bot!");
				em.appendField("Developer", "NovaFox161", true);
				em.appendField("Version", Main.version, true);
				em.appendField("Library", "Discord4J, version 2.7.0", false);
				em.appendField("Total Guilds", client.getGuilds().size() + "", true);
				em.withFooterText(
						"For more information about DisCal, please visit: https://www.cloudcraftgaming.com/discal/");
				em.withColor(36, 153, 153);
				Message.sendMessage(em.build(), event, client);
        } else if (args.length == 1) {
            Message.sendMessage("Please specify a function and value!", event, client);
        } else if (args.length == 2) {
            String function = args[0];
            if (function.equalsIgnoreCase("role")) {
                setControlRole(args, event, client);
            } else {
                Message.sendMessage("Invalid function! Use !help.", event, client);
            }
        } else if (args.length > 2) {
            Message.sendMessage("Invalid function! Use !help.", event, client);
        }
        return false;
    }

    private void setControlRole(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        if (PermissionChecker.hasSufficientRole(event)) {
            String roleName = args[1];
            IGuild guild = event.getMessage().getGuild();
            IRole controlRole = null;

            if (!roleName.equals("everyone")) {
                for (IRole r : guild.getRoles()) {
                    if (r.getName().equals(roleName) || r.getID().equals(roleName)) {
                        controlRole = r;
                        break; //So that it only loops through a limited amount of roles.
                    }
                }

                if (controlRole != null) {
                    BotData botData = DatabaseManager.getManager().getData(event.getMessage().getGuild().getID());
                    botData.setControlRole(controlRole.getID());
                    DatabaseManager.getManager().updateData(botData);
                    //Send message.
                    Message.sendMessage("Required control role set to: " + controlRole.getName(), event, client);

                } else {
                    //Invalid role.
                    Message.sendMessage("Invalid role specified! The role must exist!", event, client);
                }
            } else {
                //Role is @everyone, set this so that anyone can control the bot.
                BotData botData = DatabaseManager.getManager().getData(event.getMessage().getGuild().getID());
                botData.setControlRole("everyone");
                DatabaseManager.getManager().updateData(botData);
                //Send message
                Message.sendMessage("Specific role no longer required! Everyone may edit/create!", event, client);
            }
        } else {
            Message.sendMessage("You do not have sufficient permissions to use this DisCal command!", event, client);
        }
    }
}
