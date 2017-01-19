package com.cloudcraftgaming.utils;

import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.internal.data.BotData;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

/**
 * Created by Nova Fox on 1/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class PermissionChecker {
    public static boolean hasSufficientRole(MessageReceivedEvent event) {
        BotData bd = DatabaseManager.getManager().getData(event.getMessage().getGuild().getID());
        if (!bd.getControlRole().equalsIgnoreCase("everyone")) {
            IUser sender = event.getMessage().getAuthor();
            String roleId = bd.getControlRole();
            IRole role = event.getMessage().getGuild().getRoleByID(roleId);

            if (role != null) {
                if (!event.getMessage().getGuild().getUsersByRole(role).contains(sender)) {
                    return false;
                }
            }
        }
        return true;
    }
}