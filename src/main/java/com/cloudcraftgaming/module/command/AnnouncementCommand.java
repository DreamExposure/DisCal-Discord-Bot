package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.module.announcement.AnnouncementCreator;
import com.cloudcraftgaming.module.announcement.AnnouncementMessageFormatter;
import com.cloudcraftgaming.utils.Message;
import com.cloudcraftgaming.utils.PermissionChecker;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementCommand implements ICommand {
    @Override
    public String getCommand() {
        return "Announcement";
    }

    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        if (PermissionChecker.hasSufficientRole(event)) {
            if (args.length < 1) {
                Message.sendMessage("Please specify the function you would like to execute.", event, client);
            } else if (args.length == 1) {
                String guildId = event.getMessage().getGuild().getID();
                String function = args[0];
                if (function.equalsIgnoreCase("create")) {
                    if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                        AnnouncementCreator.getCreator().init(event);
                        Message.sendMessage("Announcement creator initialized!" + Message.lineBreak + "Please specify the type:" + Message.lineBreak + "Either 'UNIVERSAL' for all events, or 'SPECIFIC' for a specific event", event, client);
                    }
                } else if (function.equalsIgnoreCase("subscribe")) {
                    Message.sendMessage("Please specify the ID of the announcement you wish to subscribe to!", event, client);
                } else if (function.equalsIgnoreCase("unsubscribe")) {
                    Message.sendMessage("Please specify the ID of the announcement you wish to unsubscribe from!", event, client);
                } else if (function.equalsIgnoreCase("cancel")) {
                    if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                        AnnouncementCreator.getCreator().terminate(event);
                    } else {
                        Message.sendMessage("Cannot cancel creation when the creator has not been started!", event, client);
                    }
                } else if (function.equalsIgnoreCase("review") || function.equalsIgnoreCase("view")) {
                    if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                        Message.sendMessage(AnnouncementMessageFormatter.getFormatEventMessage(AnnouncementCreator.getCreator().getAnnouncement(guildId)), event, client);
                    } else {
                        Message.sendMessage("You must specify the ID of the announcement you wish to view!", event, client);
                    }
                } //TODO: Finish adding the rest of command args here...
            }
        } else {
            Message.sendMessage("You do not have sufficient permissions to use this DisCal command!", event, client);
        }
        return false;
    }
}