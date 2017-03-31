package com.cloudcraftgaming.discal.utils;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.internal.email.EmailSender;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

/**
 * Created by Nova Fox on 1/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class PermissionChecker {
    /**
     * Checks if the user who sent the received message has the proper role to use a command.
     * @param event The Event received to check for the user and guild.
     * @return <code>true</code> if the user has the proper role, otherwise <code>false</code>.
     */
    public static boolean hasSufficientRole(MessageReceivedEvent event) {
        //TODO: Figure out exactly what is causing a NPE here...
        try {
            GuildSettings settings = DatabaseManager.getManager().getSettings(event.getMessage().getGuild().getID());
            if (!settings.getControlRole().equalsIgnoreCase("everyone")) {
                IUser sender = event.getMessage().getAuthor();
                String roleId = settings.getControlRole();
                IRole role = null;

                for (IRole r :  event.getMessage().getGuild().getRoles()) {
                    if (r.getID().equals(roleId)) {
                        role = r;
                        break;
                    }
                }

                if (role != null) {
                    for (IRole r : sender.getRolesForGuild(event.getMessage().getGuild())) {
                        if (r.getID().equals(role.getID())) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    //Role not found... reset Db...
                    settings.setControlRole("everyone");
                    DatabaseManager.getManager().updateSettings(settings);
                    return true;
                }
            }
        } catch (Exception e) {
            //Something broke so we will harmlessly allow access and email the dev.
            EmailSender.getSender().sendExceptionEmail(e, PermissionChecker.class);
            return true;
        }
        return true;
    }

    /**
     * Checks if the user sent the command in a DisCal channel (if set).
     * @param event The event received to check for the correct channel.
     * @return <code>true</code> if in correct channel, otherwise <code>false</code>.
     */
    public static boolean inCorrectChannel(MessageReceivedEvent event) {
        try {
            GuildSettings settings = DatabaseManager.getManager().getSettings(event.getMessage().getGuild().getID());
            if (settings.getDiscalChannel().equalsIgnoreCase("all")) {
                return true;
            }

            IChannel channel = null;
            for (IChannel c : event.getMessage().getGuild().getChannels()) {
                if (c.getID().equals(settings.getDiscalChannel())) {
                    channel = c;
                    break;
                }
            }

            if (channel != null) {
                return event.getMessage().getChannel().getID().equals(channel.getID());
            }

            //If we got here, the channel no longer exists, reset data and return true.
            settings.setDiscalChannel("all");
            DatabaseManager.getManager().updateSettings(settings);
            return true;
        } catch (Exception e) {
            //Catch any errors so that the bot always responds...
            EmailSender.getSender().sendExceptionEmail(e, PermissionChecker.class);
            return true;
        }
    }
}