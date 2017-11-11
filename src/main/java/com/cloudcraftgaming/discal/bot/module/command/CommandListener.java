package com.cloudcraftgaming.discal.bot.module.command;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.bot.utils.PermissionChecker;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

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

    /**
     * Creates a new CommandListener listener.
     * @param _cmd The CommandExecutor instance to use.
     */
    CommandListener(CommandExecutor _cmd) {
        cmd = _cmd;
    }

    /**
     * Checks for command validity and calls the command executor if valid.
     * @param event The event received to check for a command.
     */
    @EventSubscriber
    public void onMessageEvent(MessageReceivedEvent event) {
        try {
            if (event.getMessage() != null && event.getGuild() != null && event.getChannel() != null && !event.getChannel().isPrivate() && event.getMessage().getContent() != null && event.getMessage().getContent().length() > 0) {
                //Message is a valid guild message (not DM). Check if in correct channel.
				GuildSettings settings = DatabaseManager.getManager().getSettings(event.getGuild().getLongID());
                if (event.getMessage().getContent().startsWith(settings.getPrefix())) {
                	if (PermissionChecker.isCorrectChannel(event)) {
						//Prefixed with ! which should mean it is a command, convert and confirm.
						String[] argsOr = event.getMessage().getContent().split(" ");
						if (argsOr.length > 1) {
							ArrayList<String> argsOr2 = new ArrayList<>(Arrays.asList(argsOr).subList(1, argsOr.length));
							String[] args = argsOr2.toArray(new String[argsOr2.size()]);

							String command = argsOr[0].replace(settings.getPrefix(), "");
							cmd.issueCommand(command, args, event, settings);
						} else if (argsOr.length == 1) {
							//Only command... no args.
							cmd.issueCommand(argsOr[0].replace(settings.getPrefix(), ""), new String[0], event, settings);
						}
					}
                } else if (!event.getMessage().mentionsEveryone() && !event.getMessage().mentionsHere() && (event.getMessage().toString().startsWith("<@" + Main.getSelfUser().getStringID() + ">") || event.getMessage().toString().startsWith("<@!" + Main.getSelfUser().getStringID() + ">"))) {
					if (PermissionChecker.isCorrectChannel(event)) {
						String[] argsOr = event.getMessage().getContent().split(" ");
						if (argsOr.length > 2) {
							ArrayList<String> argsOr2 = new ArrayList<>(Arrays.asList(argsOr).subList(2, argsOr.length));
							String[] args = argsOr2.toArray(new String[argsOr2.size()]);

							String command = argsOr[1];
							cmd.issueCommand(command, args, event, settings);
						} else if (argsOr.length == 2) {
							//No args...
							cmd.issueCommand(argsOr[1], new String[0], event, settings);
						} else if (argsOr.length == 1) {
							//Only disCal mentioned...
							cmd.issueCommand("DisCal", new String[0], event, settings);
						}
					}
                }
            }
        } catch (Exception e) {
            ExceptionHandler.sendException(event.getAuthor(), "Command error; event message: " + event.getMessage().getContent(), e, this.getClass());
        }
    }
}