package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.utils.Message;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

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
            if (msg.getContent().startsWith("!test")) {
                Message.sendMessage("Bleep bloop", event, cmd.client);
            }
        }
    }
}