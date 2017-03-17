package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.internal.email.EmailSender;
import com.cloudcraftgaming.discal.utils.PermissionChecker;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.Arrays;

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
            if (event.getMessage() != null && event.getMessage().getGuild() != null && event.getMessage().getChannel() != null && !event.getMessage().getChannel().isPrivate() && event.getMessage().getContent() != null && event.getMessage().getContent().length() > 0) {
                //Message is a valid guild message (not DM). Check if in correct channel.
                if (PermissionChecker.inCorrectChannel(event) && event.getMessage().getContent().startsWith("!")) {
                    //Prefixed with ! which should mean it is a command, convert and confirm.
                    String[] argsOr = event.getMessage().getContent().split(" ");
                    if (argsOr.length > 1) {
                        ArrayList<String> argsOr2 = new ArrayList<>();
                        argsOr2.addAll(Arrays.asList(argsOr).subList(1, argsOr.length));
                        String[] args = argsOr2.toArray(new String[argsOr2.size()]);

                        String command = argsOr[0].replaceAll("!", "");
                        cmd.issueCommand(command, args, event);
                    } else if (argsOr.length == 1) {
                        //Only command... no args.
                        cmd.issueCommand(argsOr[0].replaceAll("!", ""), new String[0], event);
                    }
                } else if (!event.getMessage().mentionsEveryone() && !event.getMessage().mentionsHere() && discalMentioned(event) && event.getMessage().toString().startsWith("<@" + Main.getSelfUser().getID() + ">") || event.getMessage().toString().startsWith("<@!" + Main.getSelfUser().getID() + ">")) {
                    String[] argsOr = event.getMessage().getContent().split(" ");
                    if (argsOr.length > 2) {
                        ArrayList<String> argsOr2 = new ArrayList<>();
                        argsOr2.addAll(Arrays.asList(argsOr).subList(2, argsOr.length));
                        String [] args = argsOr2.toArray(new String[argsOr2.size()]);

                        String command = argsOr[1];
                        cmd.issueCommand(command, args, event);
                    } else if (argsOr.length == 2) {
                        //No args...
                        cmd.issueCommand(argsOr[1], new String[0], event);
                    } else if (argsOr.length == 1) {
                        //Only disCal mentioned...
                        cmd.issueCommand("DisCal", new String[0], event);
                    }
                }
            }
        } catch (Exception e) {
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
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