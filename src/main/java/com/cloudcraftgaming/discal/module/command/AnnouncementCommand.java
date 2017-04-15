package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.event.EventUtils;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.module.announcement.*;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import com.cloudcraftgaming.discal.utils.*;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.*;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementCommand implements ICommand {
    /**
     * Gets the command this Object is responsible for.
     *
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "Announcement";
    }

    /**
     * Gets the short aliases of the command this object is responsible for.
     * </br>
     * This will return an empty ArrayList if none are present
     *
     * @return The aliases of the command.
     */
    @Override
    public ArrayList<String> getAliases() {
        ArrayList<String> aliases = new ArrayList<>();
        aliases.add("announcements");
        aliases.add("announce");
        aliases.add("alert");
        aliases.add("alerts");
        aliases.add("a");
        aliases.add("ann");
        return aliases;
    }

    /**
     * Gets the info on the command (not sub command) to be used in help menus.
     *
     * @return The command info.
     */
    @Override
    public CommandInfo getCommandInfo() {
        CommandInfo info = new CommandInfo("announcement");
        info.setDescription("Used for all announcement functions.");
        info.setExample("!announcement <function> (value(s))");

        info.getSubCommands().add("create");
        info.getSubCommands().add("copy");
        info.getSubCommands().add("edit");
        info.getSubCommands().add("confirm");
        info.getSubCommands().add("cancel");
        info.getSubCommands().add("delete");
        info.getSubCommands().add("view");
        info.getSubCommands().add("review");
        info.getSubCommands().add("subscribe");
        info.getSubCommands().add("unsubscribe");
        info.getSubCommands().add("type");
        info.getSubCommands().add("hours");
        info.getSubCommands().add("minutes");
        info.getSubCommands().add("list");
        info.getSubCommands().add("event");
        info.getSubCommands().add("info");

        return info;
    }

    /**
     * Issues the command this Object is responsible for.
     *
     * @param args   The command arguments.
     * @param event  The event received.
     * @param client The Client associated with the Bot.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        if (PermissionChecker.hasSufficientRole(event)) {
            if (args.length < 1) {
                Message.sendMessage("Please specify the function you would like to execute. To view valid functions use `!help announcement`", event, client);
            } else if (args.length >= 1) {
                String guildId = event.getMessage().getGuild().getID();
                GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);
                switch (args[0].toLowerCase()) {
                    case "create":
                        moduleCreate(event, client);
                        break;
                    case "confirm":
                        moduleConfirm(event, client);
                        break;
                    case "cancel":
                        moduleCancel(event, client);
                        break;
                    case "delete":
                        moduleDelete(args, event, client);
                        break;
                    case "view":
                    case "review":
                        moduleView(args, event, client);
                        break;
                    case "subscribe":
                    case "sub":
                        moduleSubscribe(args, event, client);
                        break;
                    case "unsubscribe":
                    case "unsub":
                        moduleUnsubscribe(args, event, client);
                        break;
                    case "type":
                        moduleType(args, event, client);
                        break;
                    case "hours":
                        moduleHours(args, event, client);
                        break;
                    case "minutes":
                        moduleMinutes(args, event, client);
                        break;
                    case "list":
                        moduleList(args, event, client);
                        break;
                    case "event":
                        moduleEvent(args, event, client);
                        break;
                    case "info":
                        moduleInfo(args, event, client);
                        break;
                    case "channel":
                        moduleChannel(args, event, client);
                        break;
                    case "color":
                    case "colour":
                        moduleColor(args, event, client);
                        break;
                    case "copy":
                        moduleCopy(args, event, client);
                        break;
                    case "edit":
                        if (settings.isDevGuild()) {
                            moduleEdit(args, event, client);
                        } else {
                            Message.sendMessage("This option is disabled for testing only!", event, client);
                        }
                        break;
                    default:
                        Message.sendMessage("Invalid sub command! Use `!help announcement` to view valid sub commands!", event, client);
                        break;
                }
            }
        } else {
            Message.sendMessage("You do not have sufficient permissions to use this DisCal command!", event, client);
        }
        return false;
    }


    private void moduleCreate(MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();

        if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
            AnnouncementCreator.getCreator().init(event);
            Message.sendMessage("Announcement creator initialized!" + Message.lineBreak + "Please specify the type:" + Message.lineBreak + "`UNIVERSAL` for all events, or `SPECIFIC` for a specific event, `COLOR` for events with a specific color, or `RECUR` for recurring events.", event, client);
        } else {
            Message.sendMessage("Announcement creator has already been started!", event, client);
        }
    }

    private void moduleEdit(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
            if (args.length == 2) {
                String anId = args[1];
                if (AnnouncementUtils.announcementExists(anId, event)) {
                    Announcement announcement = AnnouncementCreator.getCreator().edit(event, anId);

                    Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(announcement), "Announcement Editor initiated! Edit the values and then confirm your edits with `!announcement confirm`", event, client);
                } else {
                    Message.sendMessage("I can't seem to find an announcement with that ID. Are you sure you typed it correctly?", event, client);
                }
            } else {
                Message.sendMessage("Please specify the ID of the announcement to edit with `!announcement edit <ID>`", event, client);
            }
        } else {
            Message.sendMessage("Announcement Creator has already been initialized!", event, client);
        }
    }

    private void moduleConfirm(MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
            AnnouncementCreatorResponse acr = AnnouncementCreator.getCreator().confirmAnnouncement(event);
            if (acr.isSuccessful()) {
                if (acr.getAnnouncement().isEditing()) {
                    Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(acr.getAnnouncement()), "Announcement updated!", event, client);
                } else {
                    Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(acr.getAnnouncement()), "Announcement created " + Message.lineBreak + Message.lineBreak + "Use `!announcement subscribe <id>` to subscribe to the announcement!", event, client);
                }
            } else {
                Message.sendMessage("Oops! Something went wrong! Are you sure all of the info is correct?", event, client);
            }
        }
    }

    private void moduleCancel(MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();

        if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
            AnnouncementCreator.getCreator().terminate(event);
            Message.sendMessage("Announcement Creator terminated!", event, client);
        } else {
            Message.sendMessage("Cannot cancel creation when the Creator has not been started!", event, client);
        }
    }

    private void moduleDelete(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 1) {
            if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                Message.sendMessage("Please specify the Id of the announcement to delete!", event, client);
            } else {
                Message.sendMessage("You cannot delete an announcement while in the Creator!", event, client);
            }
        } else if (args.length == 2) {
            String value = args[1];
            if (AnnouncementUtils.announcementExists(value, event)) {
                if (DatabaseManager.getManager().deleteAnnouncement(value)) {
                    Message.sendMessage("Announcement successfully deleted!", event, client);
                } else {
                    Message.sendMessage("Failed to delete announcement! Something may have gone wrong, the dev has been emailed!", event, client);
                }
            } else {
                Message.sendMessage("Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?", event, client);
            }
        } else {
            Message.sendMessage("Please use `!announcement delete <ID>`", event, client);
        }
    }

    private void moduleView(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 1) {
            if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(AnnouncementCreator.getCreator().getAnnouncement(guildId)), event, client);
            } else {
                Message.sendMessage("You must specify the ID of the announcement you wish to view!", event, client);
            }
        } else if (args.length == 2) {
            String value = args[1];
            if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                Message.sendMessage("You cannot view another announcement while one is in the creator!", event, client);
            } else {
                try {
                    Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(value), guildId);
                    if (a != null) {
                        Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a), AnnouncementMessageFormatter.getSubscriberNames(a), event, client);
                    } else {
                        Message.sendMessage("That announcement does not exist! Are you sure you typed the ID correctly?", event, client);
                    }
                } catch (NumberFormatException e) {
                    Message.sendMessage("Hmm... is the ID correct? I seem to be having issues parsing it..", event, client);
                }
            }
        } else {
            Message.sendMessage("Please use `!announcement view` or `!announcement view <ID>`", event, client);
        }
    }

    private void moduleSubscribe(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 1) {
            Message.sendMessage("Please specify the ID of the announcement you wish to subscribe to!", event, client);
        } else if (args.length == 2) {
            String value = args[1];
            if (AnnouncementUtils.announcementExists(value, event)) {
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
        } else if (args.length == 3) {
            String value1 = args[1];
            String value2 = args[2];
            if (AnnouncementUtils.announcementExists(value1, event)) {
                Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(value1), guildId);
                IUser user = event.getMessage().getGuild().getUserByID(UserUtils.getUser(value2, event.getMessage()));
                if (user != null) {
                    //Valid user, let's add that user to the announcement.
                    if (!a.getSubscriberUserIds().contains(user.getID())) {
                        String username = user.getDisplayName(event.getMessage().getGuild());
                        a.getSubscriberUserIds().add(user.getID());
                        DatabaseManager.getManager().updateAnnouncement(a);
                        Message.sendMessage("`" + username + "` has been subscribed to the announcement with the ID `" + a.getAnnouncementId() + "`" + Message.lineBreak + "To unsubscribe them use `!announcement unsubscribe <announcement ID> <mention>", event, client);
                    } else {
                        Message.sendMessage("That user is already subscribed to the specified announcement! To unsubscribe them use `!announcement unsubscribe <announcement ID> <mention>`", event, client);
                    }
                } else if (value2.equalsIgnoreCase("everyone") || value2.equalsIgnoreCase("here")) {
                    //Here or everyone is to be subscribed...
                    String men = value2.toLowerCase();
                    if (!a.getSubscriberRoleIds().contains(men)) {
                        a.getSubscriberRoleIds().add(men);
                        DatabaseManager.getManager().updateAnnouncement(a);
                        Message.sendMessage("`" + men + "` has been subscribed to the announcement with the ID `" + a.getAnnouncementId() + "`" + Message.lineBreak + "To unsubscribe them use `!announcement unsubscribe <announcement ID> <value>", event, client);
                    } else {
                        Message.sendMessage(men + " is already subscribed to the specified announcement! To unsubscribe them use `!announcement unsubscribe <announcement ID> <value>`", event, client);
                    }
                } else {
                    //User does not exist, see if a role.
                    IRole role = RoleUtils.getRoleFromMention(value2, event);
                    if (role != null) {
                        //Role valid, let's add that role to the announcement.
                        if (!a.getSubscriberRoleIds().contains(role.getID())) {
                            String roleName = role.getName();
                            a.getSubscriberRoleIds().add(role.getID());
                            DatabaseManager.getManager().updateAnnouncement(a);
                            Message.sendMessage("`" + roleName + "` has been subscribed to the announcement with the ID `" + a.getAnnouncementId() + "`" + Message.lineBreak + "To unsubscribe them use `!announcement unsubscribe <announcement ID> <mention>", event, client);
                        } else {
                            Message.sendMessage("That role is already subscribed to the specified announcement! To unsubscribe them use `!announcement unsubscribe <announcement ID> <mention>`", event, client);
                        }
                    } else {
                        //Role does not exist...
                        Message.sendMessage("Role or user not found! Are you sure you typed them correctly?", event, client);
                    }
                }
            } else {
                Message.sendMessage("Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?", event, client);
            }
        } else {
            Message.sendMessage("Please use `!announcement subscribe <ID>` or `!announcement subscribe <ID> <user mention/role mention/here/everyone>`", event, client);
        }
    }

    private void moduleSubscribeRewrite(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        if (!DatabaseManager.getManager().getSettings(event.getMessage().getGuild().getID()).isDevGuild()) {
            return;
        }
        IMessage message = event.getMessage();
        IGuild guild = message.getGuild();
        IUser user = message.getAuthor();
        if (args.length == 1) {
            if (AnnouncementCreator.getCreator().hasAnnouncement(guild.getID())) {
                UUID announcementId = AnnouncementCreator.getCreator().getAnnouncement(guild.getID()).getAnnouncementId();
                Announcement a = DatabaseManager.getManager().getAnnouncement(announcementId, guild.getID());
                String senderId = user.getID();
                if (!a.getSubscriberUserIds().contains(senderId)) {
                    a.getSubscriberUserIds().add(senderId);
                    DatabaseManager.getManager().updateAnnouncement(a);
                    Message.sendMessage("You have subscribed to the announcement with the ID: `" + announcementId.toString() + "`" + Message.lineBreak + "To unsubscribe use `!announcement unsubscribe <id>`", event, client);
                } else { // Announcement contains user ID
                    Message.sendMessage("You are already subscribed to that event!", event, client);
                }
            } else { // User not creating an announcement
                Message.sendMessage("Please specify the ID of the announcement you wish to subscribe to!", event, client);
            }
        } else if (args.length == 2) {
            String value = args[1];
            if (AnnouncementUtils.announcementExists(value, event)) {
                String senderId = user.getID();
                Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(value), guild.getID());
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
        } else {

        }

    }

    private void moduleUnsubscribe(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 1) {
            Message.sendMessage("Please specify the ID of the announcement you wish to unsubscribe from!", event, client);
        } else if (args.length == 2) {
            String value = args[1];
            if (AnnouncementUtils.announcementExists(value, event)) {
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
        } else if (args.length == 3) {
            String value1 = args[1];
            String value2 = args[2];
            if (AnnouncementUtils.announcementExists(value1, event)) {
                Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(value1), guildId);
                IUser user = UserUtils.getUserFromMention(value2, event);
                if (user != null) {
                    //Valid user, let's add that user to the announcement.
                    if (a.getSubscriberUserIds().contains(user.getID())) {
                        String username = user.getDisplayName(event.getMessage().getGuild());
                        a.getSubscriberUserIds().remove(user.getID());
                        DatabaseManager.getManager().updateAnnouncement(a);
                        Message.sendMessage("`" + username + "` has been unsubscribed from the announcement with the ID `" + a.getAnnouncementId() + "`" + Message.lineBreak + "To re-subscribe them use `!announcement subscribe <announcement ID> <mention>", event, client);
                    } else {
                        Message.sendMessage("That user is not subscribed to the specified announcement! To subscribe them use `!announcement unsubscribe <announcement ID> <mention>`", event, client);
                    }
                } else if (value2.equalsIgnoreCase("everyone") || value2.equalsIgnoreCase("here")) {
                    //Here or everyone is to be mentioned...
                    String men = value2.toLowerCase();
                    if (a.getSubscriberRoleIds().contains(men)) {
                        a.getSubscriberRoleIds().remove(men);
                        DatabaseManager.getManager().updateAnnouncement(a);
                        Message.sendMessage("`" + men + "` has been unsubscribed from the announcement with the ID `" + a.getAnnouncementId() + "`" + Message.lineBreak + "To re-subscribe them use `!announcement subscribe <announcement ID> <value>", event, client);
                    } else {
                        Message.sendMessage(men + " is not subscribed to the specified announcement! To subscribe them use `!announcement unsubscribe <announcement ID> <value>`", event, client);
                    }
                } else {
                    //User does not exist, see if a role.
                    IRole role = RoleUtils.getRoleFromMention(value2, event);
                    if (role != null) {
                        //Role valid, let's add that role to the announcement.
                        if (a.getSubscriberRoleIds().contains(role.getID())) {
                            String roleName = role.getName();
                            a.getSubscriberRoleIds().remove(role.getID());
                            DatabaseManager.getManager().updateAnnouncement(a);
                            Message.sendMessage("`" + roleName + "` has been unsubscribed from the announcement with the ID `" + a.getAnnouncementId() + "`" + Message.lineBreak + "To re-subscribe them use `!announcement subscribe <announcement ID> <mention>", event, client);
                        } else {
                            Message.sendMessage("That role is not subscribed to the specified announcement! To subscribe them use `!announcement unsubscribe <announcement ID> <mention>`", event, client);
                        }
                    } else {
                        //Role does not exist...
                        Message.sendMessage("Role or user not found! Are you sure you typed them correctly?", event, client);
                    }
                }
            } else {
                Message.sendMessage("Hmm.. it seems the specified announcement does not exist, are you sure you wrote the ID correctly?", event, client);
            }
        } else {
            Message.sendMessage("Please use `!announcement unsubscribe <ID>` or `!announcement unsubscribe <ID> <user mention/role mention/here/everyone>`", event, client);
        }
    }

    private void moduleType(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            String value = args[1];
            if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                if (AnnouncementType.isValid(value)) {
                    AnnouncementType type = AnnouncementType.fromValue(value);
                    AnnouncementCreator.getCreator().getAnnouncement(guildId).setAnnouncementType(type);
                    if (type.equals(AnnouncementType.SPECIFIC)) {
                        Message.sendMessage("Announcement type set to: `" + type.name() + "`" + Message.lineBreak + "Please set the specific event ID to fire for with `!announcement event <id>`", event, client);
                    } else if (type.equals(AnnouncementType.COLOR)) {
                        Message.sendMessage("Announcement type set to: `" + type.name() + "`" + Message.lineBreak + "Please set the specific event color to fire for with `!announcement color <name or ID>`", event, client);
                    } else if (type.equals(AnnouncementType.RECUR)) {
                        Message.sendMessage("Announcement type set to: `" + type.name() + "`" + Message.lineBreak + "Please set the recurring event to fire for with `!announcement event  <ID>`", event, client);
                    } else {
                        Message.sendMessage("Announcement type set to: `" + type.name() + "`" + Message.lineBreak + "Please specify the NAME (not ID) of the channel this announcement will post in with `!announcement channel <name>`!", event, client);
                    }
                } else {
                    Message.sendMessage("Valid types are only `UNIVERSAL`, `SPECIFIC`, `COLOR`, or `RECUR`!", event, client);
                }
            } else {
                Message.sendMessage("Announcement creator has not been initialized!", event, client);
            }
        } else {
            Message.sendMessage("Please use `!announcement type <TYPE>`", event, client);
        }
    }

    private void moduleHours(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            String value = args[1];
            if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                try {
                    Integer hoursOr = Integer.valueOf(value);
                    Integer hours = Math.abs(hoursOr);
                    AnnouncementCreator.getCreator().getAnnouncement(guildId).setHoursBefore(hours);
                    Message.sendMessage("Announcement hours before set to: `" + hours + "`" + Message.lineBreak + "Please specify the amount of minutes before the event to fire!", event, client);
                } catch (NumberFormatException e) {
                    Message.sendMessage("Hours must be a valid integer! (Ex: `1` or `10`)", event, client);
                }
            } else {
                Message.sendMessage("Announcement creator has not been initialized!", event, client);
            }
        } else {
            Message.sendMessage("Please use `!announcement hours <amount>`", event, client);
        }
    }

    private void moduleMinutes(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            String value = args[1];
            if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                try {
                    Integer minutesOr = Integer.valueOf(value);
                    Integer minutes = Math.abs(minutesOr);
                    AnnouncementCreator.getCreator().getAnnouncement(guildId).setMinutesBefore(minutes);
                    Message.sendMessage("Announcement minutes before set to: `" + minutes + "`" + Message.lineBreak + "Announcement creation halted! " +
                            "If you would like to add some info text, use `!announcement info <text>` otherwise, review your announcement with `!announcement review`", event, client);
                } catch (NumberFormatException e) {
                    Message.sendMessage("Minutes must be a valid integer! (Ex: `1` or `10`)", event, client);
                }
            } else {
                Message.sendMessage("Announcement creator has not been initialized!", event, client);
            }
        } else {
            Message.sendMessage("Please use `!announcement minutes <amount>`", event, client);
        }
    }

    private void moduleList(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 1) {
            if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                Message.sendMessage("Please specify how many announcements you wish to list or `all`", event, client);
            } else {
                Message.sendMessage("You cannot list existing announcements while in the creator!", event, client);
            }
        } else if (args.length == 2) {
            String value = args[1];
            if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                if (value.equalsIgnoreCase("all")) {
                    ArrayList<Announcement> announcements = DatabaseManager.getManager().getAnnouncements(guildId);
                    Message.sendMessage("All announcements, use `!announcement view <id>` for more info." + Message.lineBreak + "`" + announcements.size() + "`" + Message.lineBreak + Message.lineBreak + "Please note that this list may be delayed due to rate limiting...", event, client);
                    //Loop and add embeds
                    for (Announcement a : announcements) {
                        Message.sendMessage(AnnouncementMessageFormatter.getCondensedAnnouncementEmbed(a), event, client);
                    }
                } else {
                    //List specific amount of announcements
                    try {
                        Integer amount = Integer.valueOf(value);
                        Message.sendMessage("Displaying the first `" + amount + "` announcements found, use `!announcement view <id>` for more info." + Message.lineBreak + Message.lineBreak + "Please note that this list may be delayed due to rate limiting...", event, client);

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
            Message.sendMessage("Please use `!announcement list <amount>` or `!announcement list all`", event, client);
        }
    }

    private void moduleEvent(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            String value = args[1];
            if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                if (AnnouncementCreator.getCreator().getAnnouncement(guildId).getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
                    if (EventUtils.eventExists(guildId, value)) {
                        AnnouncementCreator.getCreator().getAnnouncement(guildId).setEventId(value);
                        Message.sendMessage("Event ID set to: `" + value + "`" + Message.lineBreak + "Please specify the NAME (not ID) of the channel this announcement will post in with `!announcement channel <name>`!", event, client);
                    } else {
                        Message.sendMessage("Hmm... I can't seem to find an event with that ID, are you sure its correct?", event, client);
                    }
                } else if (AnnouncementCreator.getCreator().getAnnouncement(guildId).getAnnouncementType().equals(AnnouncementType.RECUR)) {
                    if (EventUtils.eventExists(guildId, value)) {
                        if (value.contains("_")) {
                            String[] stuff = value.split("_");
                            value = stuff[0];
                        }
                        AnnouncementCreator.getCreator().getAnnouncement(guildId).setEventId(value);

                        Message.sendMessage("Event ID set to: `" + value + "`" + Message.lineBreak + "Please specify the NAME (not ID) of the channel this announcement will post in with `!announcement channel <name>`!", event, client);
                    } else {
                        Message.sendMessage("Hmm... I can't seem to find an event with that ID, are you sure its correct?", event, client);
                    }
                } else {
                    Message.sendMessage("You cannot set an event while the announcement Type is NOT set to `SPECIFIC`", event, client);
                }
            } else {
                Message.sendMessage("Announcement creator has not been initialized!", event, client);
            }
        } else {
            Message.sendMessage("Please use `!announcement event <Event ID>`", event, client);
        }
    }

    private void moduleInfo(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length < 2) {
            Message.sendMessage("Please use `!announcement info <your info here>`", event, client);
        } else if (args.length == 2) {
            String value = args[1];
            if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                AnnouncementCreator.getCreator().getAnnouncement(guildId).setInfo(value);
                Message.sendMessage("Announcement info set to: ```" + value + "```" + Message.lineBreak + "Please review the announcement with `!announcement review` to confirm it is correct and then use `!announcement confirm` to create the announcement!", event, client);
            } else {
                Message.sendMessage("Announcement Creator not initialized!", event, client);
            }
        } else if (args.length > 2) {
            if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                String value = GeneralUtils.getContent(args, 1);
                AnnouncementCreator.getCreator().getAnnouncement(guildId).setInfo(value);
                Message.sendMessage("Announcement info set to: ```" + value + "```" + Message.lineBreak + "Please review the announcement with `!announcement review` to confirm it is correct and then use `!announcement confirm` to create the announcement!", event, client);
            } else {
                Message.sendMessage("Announcement Creator not initialized!", event, client);
            }
        }
    }

    private void moduleChannel(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            String value = args[1];
            if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                if (ChannelUtils.channelExists(value, event)) {
                    IChannel c = ChannelUtils.getChannelFromNameOrId(value, event);
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
        } else {
            Message.sendMessage("Please use `!announcement channel <ChannelName>`", event, client);
        }
    }

    private void moduleColor(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            String value = args[1];
            if (AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                if (AnnouncementCreator.getCreator().getAnnouncement(guildId).getAnnouncementType().equals(AnnouncementType.COLOR)) {
                    if (EventColor.exists(value)) {
                        EventColor color = EventColor.fromNameOrHexOrID(value);
                        AnnouncementCreator.getCreator().getAnnouncement(guildId).setEventColor(color);
                        Message.sendMessage("Announcement Color set to: `" + color.name() + "`" + Message.lineBreak + Message.lineBreak + "Please specify the NAME (not ID) of the channel this announcement will post in with `!announcement channel <name>`!", event, client);
                    } else {
                        Message.sendMessage("Please specify a valid color NAME, ID, or HEX!", event, client);
                    }
                } else {
                    Message.sendMessage("You cannot set announcement color while announcement type is NOT `COLOR`", event, client);
                }
            } else {
                Message.sendMessage("Announcement creator not initiated!", event, client);
            }
        } else {
            Message.sendMessage("Please specify a color with `!announcement color <color>`", event, client);
        }
    }

    private void moduleCopy(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            String value = args[1];
            if (!AnnouncementCreator.getCreator().hasAnnouncement(guildId)) {
                if (AnnouncementUtils.announcementExists(value, event)) {
                    Announcement a = AnnouncementCreator.getCreator().init(event, value);

                    Message.sendMessage(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a), "Announcement copied! Edit any values you wish and confirm the announcement with the command `!announcement confirm`", event, client);
                } else {
                    Message.sendMessage("Hmm... is the ID correct? I seem to be having issues parsing it..", event, client);
                }
            } else {
                Message.sendMessage("Announcement creator already initialized!", event, client);
            }
        } else {
            Message.sendMessage("Please specify the announcement to copy with `!announcement copy <ID>`", event, client);
        }
    }
}