package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.Main;
import com.cloudcraftgaming.internal.email.EmailSender;
import com.cloudcraftgaming.utils.PermissionChecker;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("unused")
class CommandListener {
    private CommandExecutor cmd;

    CommandListener(CommandExecutor _cmd) {
        cmd = _cmd;
    }

    @EventSubscriber
    public void onMessageEvent(MessageReceivedEvent event) {
        try {
            if (event.getMessage() != null && event.getMessage().getGuild() != null && event.getMessage().getChannel() != null) {
                //Message is a valid guild message (not DM). Check if in correct channel.
                if (PermissionChecker.inCorrectChannel(event)) {
                    //In correct channel for this guild, let's see if valid command.
                    if (event.getMessage().getContent().startsWith("!")) {
                        //Prefixed with ! which should mean it is a command, convert and confirm.

                        String[] argsOr = event.getMessage().getContent().split(" ");
                        String[] args = new String[argsOr.length - 1];
                        System.arraycopy(argsOr, 1, args, 0, argsOr.length);

                        String command = argsOr[0];
                        cmd.issueCommand(command, args, event);

                    } else if (!event.getMessage().mentionsEveryone() && !event.getMessage().mentionsHere() && discalMentioned(event)) {
                        //DisCal is mentioned, everyone and here were not, check if valid command.

                        String[] argsOr = event.getMessage().getContent().split(" ");
                        String[] args = new String[argsOr.length - 2];
                        System.arraycopy(argsOr, 2, args, 0, argsOr.length);

                        //TODO: Confirm mention is first arg.

                        String command = argsOr[0];
                        cmd.issueCommand(command, args, event);
                    }
                }
            }
        } catch (Exception e) {
            EmailSender.getSender().sendExceptionEmail(e);
        }
    }

    private boolean discalMentioned(MessageReceivedEvent event) {
        for (IUser u : event.getMessage().getMentions()) {
            if (u.getID().equals(Main.getSelfUser().getID())) {
                return true;
            }
        }
        return false;
    }
}