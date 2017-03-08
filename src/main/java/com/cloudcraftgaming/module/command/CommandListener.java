package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.Main;
import com.cloudcraftgaming.internal.email.EmailSender;
import com.cloudcraftgaming.utils.PermissionChecker;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

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
            IMessage msg = event.getMessage();
            if (msg.getContent() != null && PermissionChecker.inCorrectChannel(event)) {
                if (!msg.getContent().isEmpty() && msg.getContent().startsWith("!")) {
                    //Command supported by DisCal, try commands.
                    String[] argsOr = msg.getContent().split(" ");
                    ArrayList<String> argsOr2 = new ArrayList<>();
                    argsOr2.addAll(Arrays.asList(argsOr).subList(1, argsOr.length));
                    String[] args = argsOr2.toArray(new String[argsOr2.size()]);

                    String command = argsOr[0].replaceAll("!", "");
                    cmd.issueCommand(command, args, event);
                } else if (msg.getMentions().contains(Main.getSelfUser()) && !(msg.mentionsEveryone() || msg.mentionsHere())) {
                    //DisCal mentioned, see if this is a valid command?
                    String[] argsOr = msg.getContent().split(" ");
                    ArrayList<String> argsOr2 = new ArrayList<>();
                    if (argsOr2.size() > 1) {
                        argsOr2.addAll(Arrays.asList(argsOr).subList(2, argsOr.length));
                        String[] args = argsOr2.toArray(new String[argsOr2.size()]);

                        if (args.length > 0) {
                            String command = argsOr[1];
                            cmd.issueCommand(command, args, event);
                        } else {
                            cmd.issueCommand("DisCal", args, event);
                        }
                    }
                }
            }
        } catch (Exception e) {
            EmailSender.getSender().sendExceptionEmail(e);
        }
    }
}