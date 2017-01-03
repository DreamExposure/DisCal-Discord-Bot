package com.cloudcraftgaming.module.command;

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
public class MessageListener {
    private CommandExecutor cmd;

    MessageListener(CommandExecutor _cmd) {
        cmd = _cmd;
    }

    @EventSubscriber
    public void onMessageEvent(MessageReceivedEvent event) {
        IMessage msg = event.getMessage();
        if (msg.getContent().startsWith("!")) {
            //Command supported by DisCal, try commands.
            String[] argsOr = msg.getContent().split(" ");
            ArrayList<String> argsOr2 = new ArrayList<>();
            argsOr2.addAll(Arrays.asList(argsOr).subList(1, argsOr.length));
            String[] args = argsOr2.toArray(new String[argsOr2.size()]);

            String command = argsOr[0].replaceAll("!", "");
            cmd.issueCommand(command, args, event);
        }
    }
}