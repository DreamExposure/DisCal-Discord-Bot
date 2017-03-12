package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.internal.data.BotData;
import com.cloudcraftgaming.module.announcement.*;
import com.cloudcraftgaming.utils.Message;
import com.cloudcraftgaming.utils.PermissionChecker;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

import java.io.IOException;
import java.util.UUID;

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
                        Message.sendMessage("Announcement creator initialized!" + Message.lineBreak + "Please specify the type:" + Message.lineBreak + "Either `UNIVERSAL` for all events, or `SPECIFIC` for a specific event", event, client);
                    } else {
                        Message.sendMessage("Announcement creator has already been started!", event, client);
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
                        Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId)), event, client);
                    } else {
                        Message.sendMessage("You must specify the ID of the announcement you wish to view!", event, client);
                    }
                } else if (function.equalsIgnoreCase("confirm")) {
                    if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                        AnnouncementCreatorResponse acr = AnnouncementCreator.getCreator().confirmEvent(event);
                        if (acr.isSuccessful()) {
                            Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(acr.getAnnouncement()), "Announcement created " + Message.lineBreak + Message.lineBreak + "Use `!announcement subscribe <id>` to subscribe to the announcement!", event, client);
                        } else {
                            Message.sendMessage("Oops! Something went wrong! Are you sure all of the info is correct?", event, client);
                        }
                    }
                } else if (function.equalsIgnoreCase("delete")) {
                    if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                        Message.sendMessage("Please specify the Id of the announcement to delete!", event, client);
                    } else {
                        Message.sendMessage("You cannot delete an announcement while in the creator!", event, client);
                    }
                } else if (function.equalsIgnoreCase("list")) {
                    if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                        Message.sendMessage("Please specify how many announcements you wish to list or `all`", event, client);
                    } else {
                        Message.sendMessage("You cannot list existing announcements while in the creator!", event, client);
                    }
                } else {
                    //TODO: Add useful info about what you /can/ do.
                    Message.sendMessage("Invalid arguments. Useful info here coming soon!", event, client);
                }
            } else if (args.length == 2) {
                String function = args[0];
                String value = args[1];
                String guildId = event.getMessage().getGuild().getID();
                if (function.equalsIgnoreCase("type")) {
                    if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                        if (AnnouncementType.isValid(value)) {
                            AnnouncementType type = AnnouncementType.fromValue(value);
                            AnnouncementCreator.getCreator().getAnnouncement(guildId).setAnnouncementType(type);
                            Message.sendMessage("Announcement type set to: " + type.name() + Message.lineBreak + "If type is `SPECIFIC` please set the specific event ID to fire for with `!announcement event <id>`" + Message.lineBreak + "Please specify the NAME (not ID) of the channel this announcement will post in with `!announcement channel <name>`!", event, client);
                        } else {
                            Message.sendMessage("Valid types are only `UNIVERSAL` or `SPECIFIC`!", event, client);
                        }
                    } else {
                        Message.sendMessage("Announcement creator has not been initialized!", event, client);
                    }
                } else if (function.equalsIgnoreCase("channel")) {
                    if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                        if (channelExists(value, event)) {
                            IChannel c = getChannelFromName(value, event);
                            if (c != null) {
                                AnnouncementCreator.getCreator().getAnnouncement(guildId).setAnnouncementChannelId(c.getID());
                                Message.sendMessage("Announcement channel set to: `" + c.getName() + "`" + Message.lineBreak + "Please specify the amount of hours before the event this is to fire!", event, client);
                            } else {
                                Message.sendMessage("Are you sure you typed the channel name correctly? I can't seem to find it.", event, client);
                            }
                        } else {
                            Message.sendMessage("Are you sure you typed the channel name correctly? I can't seem to find it.", event, client);
                        }
                    } else {
                        Message.sendMessage("Announcement creator has not been initialized!", event, client);
                    }
                } else if (function.equalsIgnoreCase("view") || function.equalsIgnoreCase("review")) {
                    if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                        Message.sendMessage("You cannot view another announcement while one is in the creator!", event, client);
                    } else {
                        Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(value), guildId);
                        if (a != null) {
                            Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a), event, client);
                        } else {
                            Message.sendMessage("That announcement does not exist! Are you sure you typed the ID correctly?", event, client);
                        }
                    }
                } else if (function.equalsIgnoreCase("hours")) {
                    if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                        try {
                            Integer hours = Integer.valueOf(value);
                            AnnouncementCreator.getCreator().getAnnouncement(guildId).setHoursBefore(hours);
                            Message.sendMessage("Announcement hours before set to: `" + hours + "`" + Message.lineBreak + "Please specify the amount of minutes before the event to fire!", event, client);
                        } catch (NumberFormatException e) {
                            Message.sendMessage("Hours must be a valid integer! (Ex: `1` or `10`)", event, client);
                        }
                    } else {
                        Message.sendMessage("Announcement creator has not been initialized!", event, client);
                    }
                } else if (function.equalsIgnoreCase("minutes")) {
                    if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                        try {
                            Integer minutes = Integer.valueOf(value);
                            AnnouncementCreator.getCreator().getAnnouncement(guildId).setMinutesBefore(minutes);
                            Message.sendMessage("Announcement minutes before set to: `" + minutes + "`" + Message.lineBreak + "Announcement creation halted! Please use `!announcement review` to make sure everything is correct and then use `!announcement confirm` to create your announcement!", event, client);
                        } catch (NumberFormatException e) {
                            Message.sendMessage("Minutes must be a valid integer! (Ex: `1` or `10`)", event, client);
                        }
                    } else {
                        Message.sendMessage("Announcement creator has not been initialized!", event, client);
                    }
                } else if (function.equalsIgnoreCase("event")) {
                    if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                        if (eventExists(value, event)) {
                            AnnouncementCreator.getCreator().getAnnouncement(guildId).setEventId(value);
                            Message.sendMessage("Event ID set to: `" + value + "`" + Message.lineBreak + "Please use `!announcement review` to make sure everything is correct and then use `!announcement confirm` to create your announcement!", event, client);
                        } else {
                            Message.sendMessage("Hmm... I can't seem to find an event with that ID, are you sure its correct?", event, client);
                        }
                    } else {
                        Message.sendMessage("Announcement creator has not been initialized!", event, client);
                    }
                } else if (function.equalsIgnoreCase("subscribe")) {
                    if (announcementExists(value, event)) {
                        Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(value), guildId);
                        String senderId = event.getMessage().getAuthor().getID();
                        if (!a.getSubscriberUserIds().contains(senderId)) {
                            a.getSubscriberUserIds().add(senderId);
                            DatabaseManager.getManager().updateAnnouncement(a);
                            Message.sendMessage("You have subscribed to the announcement with the ID: `" + value + "`" + Message.lineBreak + "To unsubscribe use `!announcement unsubscribe <id>`", event, client);
                        } else {
                            Message.sendMessage("You are already subscribed to that event!", event, client);
                        }
                    } else {
                        Message.sendMessage("Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?", event, client);
                    }
                } else if (function.equalsIgnoreCase("unsubscribe")) {
                    if (announcementExists(value, event)) {
                        Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(value), guildId);
                        String senderId = event.getMessage().getAuthor().getID();
                        if (a.getSubscriberUserIds().contains(senderId)) {
                            a.getSubscriberUserIds().remove(senderId);
                            DatabaseManager.getManager().updateAnnouncement(a);
                            Message.sendMessage("You have unsubscribed to the announcement with the ID: `" + value + "`" + Message.lineBreak + "To re-subscribe use `!announcement subscribe <id>`", event, client);
                        } else {
                            Message.sendMessage("You are not subscribed to this event!", event, client);
                        }
                    } else {
                        Message.sendMessage("Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?", event, client);
                    }
                } else if (function.equalsIgnoreCase("delete")) {
                    if (announcementExists(value, event)) {
                        if (DatabaseManager.getManager().deleteAnnouncement(value)) {
                            Message.sendMessage("Announcement successfully deleted!", event, client);
                        } else {
                            Message.sendMessage("Failed to delete announcement! Something may have gone wrong, the dev has been emailed!", event, client);
                        }
                    } else {
                        Message.sendMessage("Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?", event, client);
                    }
                } else if (function.equalsIgnoreCase("list")) {
                    if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                        if (value.equalsIgnoreCase("all")) {
                            Message.sendMessage("All announcements, use `!announcement view <id` for more info.", event, client);
                            //Loop and add embeds
                            for (Announcement a : DatabaseManager.getManager().getAnnouncements(
                                    guildId)) {
                                Message.sendMessage(AnnouncementMessageFormatter.getCondensedAnnouncementEmbed(a), event, client);
                            }
                        } else {
                            //List specific amount of announcements
                            try {
                                Integer amount = Integer.valueOf(value);
                                Message.sendMessage("Displaying the first `" + amount + "` announcements found, use `!announcement view <id>` for more info.", event, client);

                                int posted = 0;
                                for (Announcement a : DatabaseManager.getManager().getAnnouncements(guildId)) {
                                    if (posted < amount) {
                                        Message.sendMessage(AnnouncementMessageFormatter.getCondensedAnnouncementEmbed(a), event, client);
                                        
                                        posted++;
                                    } else {
                                        break;
                                    }
                                }
                            } catch (NumberFormatException e) {
                                Message.sendMessage("Amount must either be `all` or a valid integer!", event, client);
                            }
                        }
                    } else {
                        Message.sendMessage("You cannot list announcements while in the creator!", event, client);
                    }
                } else {
                    //TODO: Add useful info about what you /can/ do.
                    Message.sendMessage("Invalid arguments. Useful info here coming soon!", event, client);
                }
            } else {
                //TODO: Add useful info about what you /can/ do.
                Message.sendMessage("Invalid arguments. Useful info here coming soon!", event, client);
            }
        } else {
            Message.sendMessage("You do not have sufficient permissions to use this DisCal command!", event, client);
        }
        return false;
    }

    private Boolean channelExists(String value, MessageReceivedEvent event) {
        for (IChannel c : event.getMessage().getGuild().getChannels()) {
            if (c.getName().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private IChannel getChannelFromName(String value, MessageReceivedEvent event) {
        for (IChannel c : event.getMessage().getGuild().getChannels()) {
            if (c.getName().equalsIgnoreCase(value)) {
                return c;
            }
        }
        return null;
    }

    private Boolean eventExists(String value, MessageReceivedEvent event) {
        try {
            Calendar service = CalendarAuth.getCalendarService();
            BotData data = DatabaseManager.getManager().getData(event.getMessage().getGuild().getID());
            Event calEvent = service.events().get(data.getCalendarAddress(), value).execute();
            if (calEvent != null) {
                return true;
            }
        } catch (IOException e) {
            //Something went wrong... event probably doesn't exist.
        }
        return false;
    }

    private Boolean announcementExists(String value, MessageReceivedEvent event) {
        for (Announcement a : DatabaseManager.getManager().getAnnouncements(event.getMessage().getGuild().getID())) {
            if (a.getAnnouncementId().toString().equals(value)) {
                return true;
            }
        }
        return false;
    }
}