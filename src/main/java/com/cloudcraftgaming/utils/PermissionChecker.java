package com.cloudcraftgaming.utils;

import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.internal.data.BotData;
import com.cloudcraftgaming.internal.email.EmailSender;
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
    public static boolean hasSufficientRole(MessageReceivedEvent event) {
        //TODO: Figure out exactly what is causing a NPE here...
        try {
            BotData bd = DatabaseManager.getManager().getData(event.getMessage().getGuild().getID());
            if (!bd.getControlRole().equalsIgnoreCase("everyone")) {
                IUser sender = event.getMessage().getAuthor();
                String roleId = bd.getControlRole();
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
                    bd.setControlRole("everyone");
                    DatabaseManager.getManager().updateData(bd);
                    return true;
                }
            }
        } catch (Exception e) {
            //Something broke so we will harmlessly allow access and email the dev.
            EmailSender.getSender().sendExceptionEmail(e);
            return true;
        }
        return true;
    }

    public static boolean inCorrectChannel(MessageReceivedEvent event) {
        try {
            BotData data = DatabaseManager.getManager().getData(event.getMessage().getGuild().getID());
            if (data.getChannel().equalsIgnoreCase("all")) {
                return true;
            }

            IChannel channel = null;
            for (IChannel c : event.getMessage().getGuild().getChannels()) {
                if (c.getID().equals(data.getChannel())) {
                    channel = c;
                    break;
                }
            }

            if (channel != null) {
                return event.getMessage().getChannel().getID().equals(channel.getID());
            }

            //If we got here, the channel no longer exists, reset data and return true.
            data.setChannel("all");
            DatabaseManager.getManager().updateData(data);
            return true;
        } catch (Exception e) {
            //Catch any errors so that the bot always responds...
            EmailSender.getSender().sendExceptionEmail(e);
            return true;
        }
    }
}