package org.dreamexposure.discal.client.message;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.utils.ChannelUtils;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.ImageUtils;

import java.util.function.Consumer;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Image;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings({"Duplicates", "ConstantConditions"})
public class AnnouncementMessageFormatter {

    /**
     * Gets the EmbedObject for an Announcement.
     *
     * @param a The Announcement to embed.
     * @return The EmbedObject for the Announcement.
     */
    public static Consumer<EmbedCreateSpec> getFormatAnnouncementEmbed(Announcement a, GuildSettings settings) {
        return spec -> {
            Guild guild = DisCalClient.getClient().getGuildById(settings.getGuildID()).block();

            if (settings.isBranded())
                spec.setAuthor(guild.getName(), GlobalConst.discalSite, guild.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
            else
                spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

            spec.setTitle(MessageManager.getMessage("Embed.Announcement.Info.Title", settings));
            try {
                spec.addField(MessageManager.getMessage("Embed.Announcement.Info.ID", settings), a.getAnnouncementId().toString(), true);
            } catch (NullPointerException e) {
                spec.addField(MessageManager.getMessage("Embed.Announcement.Info.ID", settings), "ID IS NULL???", true);
            }

            spec.addField(MessageManager.getMessage("Embed.Announcement.Info.Type", settings), a.getAnnouncementType().name(), true);


            if (a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
                spec.addField(MessageManager.getMessage("Embed.Announcement.Info.EventID", settings), a.getEventId(), true);
                EventData ed = DatabaseManager.getEventData(a.getGuildId(), a.getEventId()).block();
                if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink(), settings.isPatronGuild()))
                    spec.setImage(ed.getImageLink());

            } else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
                spec.addField(MessageManager.getMessage("Embed.Announcement.Info.Color", settings), a.getEventColor().name(), true);
            } else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
                spec.addField(MessageManager.getMessage("Embed.Announcement.Info.RecurID", settings), a.getEventId(), true);
                EventData ed = DatabaseManager.getEventData(a.getGuildId(), a.getEventId()).block();
                if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink(), settings.isPatronGuild()))
                    spec.setImage(ed.getImageLink());
            }
            spec.addField(MessageManager.getMessage("Embed.Announcement.Info.Hours", settings), String.valueOf(a.getHoursBefore()), true);
            spec.addField(MessageManager.getMessage("Embed.Announcement.Info.Minutes", settings), String.valueOf(a.getMinutesBefore()), true);
            spec.addField(MessageManager.getMessage("Embed.Announcement.Info.Channel", settings), ChannelUtils.getChannelNameFromNameOrId(a.getAnnouncementChannelId(), guild), true);
            spec.addField(MessageManager.getMessage("Embed.Announcement.Info.Info", settings), a.getInfo(), false);
            if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
                spec.setColor(a.getEventColor().asColor());
            } else {
                spec.setColor(GlobalConst.discalColor);
            }

            spec.addField(MessageManager.getMessage("Embed.Announcement.Info.Enabled", settings), a.isEnabled() + "", true);
        };
    }

    /**
     * Gets the EmbedObject for a Condensed Announcement.
     *
     * @param a The Announcement to embed.
     * @return The EmbedObject for a Condensed Announcement.
     */
    public static Consumer<EmbedCreateSpec> getCondensedAnnouncementEmbed(Announcement a, GuildSettings settings) {
        return spec -> {
            Guild guild = DisCalClient.getClient().getGuildById(settings.getGuildID()).block();

            if (settings.isBranded())
                spec.setAuthor(guild.getName(), GlobalConst.discalSite, guild.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
            else
                spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

            spec.setTitle(MessageManager.getMessage("Embed.Announcement.Condensed.Title", settings));
            spec.addField(MessageManager.getMessage("Embed.Announcement.Condensed.ID", settings), a.getAnnouncementId().toString(), false);
            spec.addField(MessageManager.getMessage("Embed.Announcement.Condensed.Time", settings), condensedTime(a), false);

            if (a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
                spec.addField(MessageManager.getMessage("Embed.Announcement.Condensed.EventID", settings), a.getEventId(), false);
                try {
                    Calendar service = CalendarAuth.getCalendarService(settings);

                    //TODO: Handle multiple calendars...

                    CalendarData data = DatabaseManager.getMainCalendar(a.getGuildId()).block();
                    Event event = service.events().get(data.getCalendarAddress(), a.getEventId()).execute();
                    EventData ed = DatabaseManager.getEventData(settings.getGuildID(), event.getId()).block();
                    if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink(), settings.isPatronGuild()))
                        spec.setThumbnail(ed.getImageLink());

                    if (event.getSummary() != null) {
                        String summary = event.getSummary();
                        if (summary.length() > 250) {
                            summary = summary.substring(0, 250);
                            summary = summary + " (continues on Google Calendar View)";
                        }
                        spec.addField(MessageManager.getMessage("Embed.Announcement.Condensed.Summary", settings), summary, true);
                    }
                } catch (Exception e) {
                    //Failed to get from google cal.
                    LogFeed.log(LogObject
                            .forException("Failed to get event for announcement", e,
                                    AnnouncementMessageFormatter.class));
                }
            } else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
                spec.addField(MessageManager.getMessage("Embed.Announcement.Condensed.Color", settings), a.getEventColor().name(), true);
            } else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
                spec.addField(MessageManager.getMessage("Embed.Announcement.Condensed.RecurID", settings), a.getEventId(), true);
            }
            spec.setFooter(MessageManager.getMessage("Embed.Announcement.Condensed.Type", "%type%", a.getAnnouncementType().name(), settings), null);

            if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
                spec.setColor(a.getEventColor().asColor());
            } else {
                spec.setColor(GlobalConst.discalColor);
            }

            spec.addField(MessageManager.getMessage("Embed.Announcement.Info.Enabled", settings), a.isEnabled() + "", true);

        };
    }

    /**
     * Sends an embed with the announcement info in a proper format.
     *
     * @param announcement The announcement to send info about.
     * @param event        the calendar event the announcement is for.
     * @param data         The BotData belonging to the guild.
     */
    public static void sendAnnouncementMessage(Announcement announcement, Event event, CalendarData data, GuildSettings settings) {
        Guild guild = DisCalClient.getClient().getGuildById(announcement.getGuildId()).block();

        Consumer<EmbedCreateSpec> embed = spec -> {
            if (guild != null) {
                if (settings.isBranded())
                    spec.setAuthor(guild.getName(), GlobalConst.discalSite, guild.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                else
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                spec.setTitle(MessageManager.getMessage("Embed.Announcement.Announce.Title", settings));
                EventData ed = DatabaseManager.getEventData(announcement.getGuildId(), event.getId()).block();
                if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink(), settings.isPatronGuild()))
                    spec.setImage(ed.getImageLink());

                spec.setUrl(event.getHtmlLink());

                try {
                    EventColor ec = EventColor.fromNameOrHexOrID(event.getColorId());
                    spec.setColor(ec.asColor());
                } catch (Exception e) {
                    //I dunno, color probably null.
                    spec.setColor(GlobalConst.discalColor);
                }

                if (!settings.usingSimpleAnnouncements()) {
                    spec.setFooter(MessageManager.getMessage("Embed.Announcement.Announce.ID", "%id%", announcement.getAnnouncementId().toString(), settings), null);
                }

                if (announcement.isInfoOnly() && announcement.getInfo() != null && !announcement.getInfo().equalsIgnoreCase("none")) {
                    //Only send info...
                    spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.Info", settings), announcement.getInfo(), false);
                } else {
                    //Requires all announcement data
                    if (event.getSummary() != null) {
                        String summary = event.getSummary();
                        if (summary.length() > 250) {
                            summary = summary.substring(0, 250);
                            summary = summary + " (continues on Google Calendar View)";
                        }
                        spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.Summary", settings), summary, true);
                    }
                    if (event.getDescription() != null) {
                        String description = event.getDescription();
                        if (description.length() > 250) {
                            description = description.substring(0, 250);
                            description = description + " (continues on Google Calendar View)";
                        }
                        spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.Description", settings), description, true);
                    }
                    if (!settings.usingSimpleAnnouncements()) {
                        spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.Date", settings), EventMessageFormatter.getHumanReadableDate(event.getStart(), settings, false), true);
                        spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.Time", settings), EventMessageFormatter.getHumanReadableTime(event.getStart(), settings, false), true);
                        try {
                            Calendar service = CalendarAuth.getCalendarService(settings);
                            String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
                            spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.TimeZone", settings), tz, true);
                        } catch (Exception e1) {
                            spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.TimeZone", settings), "Unknown *Error Occurred", true);
                        }
                    } else {
                        String start = EventMessageFormatter.getHumanReadableDate(event.getStart(), settings, false) + " at " + EventMessageFormatter.getHumanReadableTime(event.getStart(), settings, false);
                        try {
                            Calendar service = CalendarAuth.getCalendarService(settings);
                            String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
                            start = start + " " + tz;
                        } catch (Exception e1) {
                            start = start + " (TZ UNKNOWN/ERROR)";
                        }

                        spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.Start", settings), start, false);
                    }

                    if (event.getLocation() != null && !event.getLocation().equalsIgnoreCase("")) {
                        if (event.getLocation().length() > 300) {
                            String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
                            spec.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), location, true);
                        } else {
                            spec.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), true);
                        }
                    }

                    if (!settings.usingSimpleAnnouncements())
                        spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.EventID", settings), event.getId(), false);
                    if (!announcement.getInfo().equalsIgnoreCase("None") && !announcement.getInfo().equalsIgnoreCase(""))
                        spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.Info", settings), announcement.getInfo(), false);
                }
            }
        };


        TextChannel channel = null;

        try {
            if (guild != null)
                channel = guild.getChannelById(Snowflake.of(announcement.getAnnouncementChannelId())).ofType(TextChannel.class).onErrorResume(e -> Mono.empty()).block();
        } catch (Exception e) {
            LogFeed.log(LogObject
                    .forException("An error occurred when looking for announcement channel! " +
                                    "| Announcement: " + announcement.getAnnouncementId() + " | TYPE: "
                                    + announcement.getAnnouncementType() + " | Guild: " +
                                    announcement.getGuildId().asString(), e,
                            AnnouncementMessageFormatter.class));
        }

        if (channel == null) {
            //Channel does not exist or could not be found, automatically delete announcement to prevent issues.
            DatabaseManager.deleteAnnouncement(announcement.getAnnouncementId().toString()).subscribe();
            return;
        }

        MessageManager.sendMessageAsync(getSubscriberMentions(announcement, guild), embed, channel);
    }

    public static void sendAnnouncementDM(Announcement announcement, Event event, User user, CalendarData data, GuildSettings settings) {
        Guild guild = DisCalClient.getClient().getGuildById(settings.getGuildID()).block();

        Consumer<EmbedCreateSpec> embed = spec -> {
            if (settings.isBranded() && guild != null)
                spec.setAuthor(guild.getName(), GlobalConst.discalSite, guild.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
            else
                spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

            spec.setTitle(MessageManager.getMessage("Embed.Announcement.Announce.Title", settings));
            EventData ed = DatabaseManager.getEventData(announcement.getGuildId(), event.getId()).block();
            if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink(), settings.isPatronGuild())) {
                spec.setImage(ed.getImageLink());
            }
            if (event.getSummary() != null) {
                String summary = event.getSummary();
                if (summary.length() > 250) {
                    summary = summary.substring(0, 250);
                    summary = summary + " (continues on Google Calendar View)";
                }
                spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.Summary", settings), summary, true);
            }
            if (event.getDescription() != null) {
                String description = event.getDescription();
                if (description.length() > 250) {
                    description = description.substring(0, 250);
                    description = description + " (continues on Google Calendar View)";
                }
                spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.Description", settings), description, true);
            }
            if (!settings.usingSimpleAnnouncements()) {
                spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.Date", settings), EventMessageFormatter.getHumanReadableDate(event.getStart(), settings, false), true);
                spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.Time", settings), EventMessageFormatter.getHumanReadableTime(event.getStart(), settings, false), true);
                try {
                    Calendar service = CalendarAuth.getCalendarService(settings);
                    String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
                    spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.TimeZone", settings), tz, true);
                } catch (Exception e1) {
                    spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.TimeZone", settings), "Unknown *Error Occurred", true);
                }
            } else {
                String start = EventMessageFormatter.getHumanReadableDate(event.getStart(), settings, false) + " at " + EventMessageFormatter.getHumanReadableTime(event.getStart(), settings, false);
                try {
                    Calendar service = CalendarAuth.getCalendarService(settings);
                    String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
                    start = start + " " + tz;
                } catch (Exception e1) {
                    start = start + " (TZ UNKNOWN/ERROR)";
                }

                spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.Start", settings), start, false);
            }

            if (event.getLocation() != null && !event.getLocation().equalsIgnoreCase("")) {
                if (event.getLocation().length() > 300) {
                    String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
                    spec.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), location, true);
                } else {
                    spec.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), true);
                }
            }

            if (!settings.usingSimpleAnnouncements()) {
                spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.EventID", settings), event.getId(), false);
            }
            spec.addField(MessageManager.getMessage("Embed.Announcement.Announce.Info", settings), announcement.getInfo(), false);
            spec.setUrl(event.getHtmlLink());
            if (!settings.usingSimpleAnnouncements()) {
                spec.setFooter(MessageManager.getMessage("Embed.Announcement.Announce.ID", "%id%", announcement.getAnnouncementId().toString(), settings), null);
            }
            try {
                EventColor ec = EventColor.fromNameOrHexOrID(event.getColorId());
                spec.setColor(ec.asColor());
            } catch (Exception e) {
                //I dunno, color probably null.
                spec.setColor(GlobalConst.discalColor);
            }

        };

        if (guild != null) {
            String msg = MessageManager.getMessage("Embed.Announcement.Announce.Dm.Message", "%guild%", guild.getName(), settings);

            MessageManager.sendDirectMessageAsync(msg, embed, user);
        }
    }

    /**
     * Gets the formatted time from an Announcement.
     *
     * @param a The Announcement.
     * @return The formatted time from an Announcement.
     */
    private static String condensedTime(Announcement a) {
        return a.getHoursBefore() + "H" + a.getMinutesBefore() + "m";
    }

    public static String getSubscriberNames(Announcement a) {
        //Loop and get subs without mentions...
        Guild guild = DisCalClient.getClient().getGuildById(a.getGuildId()).block();
        if (guild == null)
            return "Error";

        StringBuilder userMentions = new StringBuilder();
        for (String userId : a.getSubscriberUserIds()) {
            try {
                Member user = guild.getMemberById(Snowflake.of(userId)).block();
                if (user != null)
                    userMentions.append(user.getUsername()).append("#").append(user.getDiscriminator()).append(" ");
            } catch (Exception e) {
                //User does not exist, safely ignore.
            }
        }

        StringBuilder roleMentions = new StringBuilder();
        boolean mentionEveryone = false;
        boolean mentionHere = false;
        for (String roleId : a.getSubscriberRoleIds()) {
            if (roleId.equalsIgnoreCase("everyone")) {
                mentionEveryone = true;
            } else if (roleId.equalsIgnoreCase("here")) {
                mentionHere = true;
            } else {
                try {
                    Role role = guild.getRoleById(Snowflake.of(roleId)).block();
                    if (role != null)
                        roleMentions.append(role.getName()).append(" ");
                } catch (Exception ignore) {
                    //Role does not exist, safely ignore.
                }
            }
        }

        String message = "Subscribers: " + userMentions + " " + roleMentions;
        if (mentionEveryone)
            message = message + " " + guild.getEveryoneRole().block().getName();

        if (mentionHere)
            message = message + " here";


        //Sanitize even tho this shouldn't be needed....
        message = message.replaceAll("@", "");

        return message;
    }

    private static String getSubscriberMentions(Announcement a, Guild guild) {
        StringBuilder userMentions = new StringBuilder();
        for (String userId : a.getSubscriberUserIds()) {
            try {
                Member user = guild.getMemberById(Snowflake.of(userId)).block();
                if (user != null)
                    userMentions.append(user.getMention()).append(" ");

            } catch (Exception e) {
                //User does not exist, safely ignore.
            }
        }

        StringBuilder roleMentions = new StringBuilder();
        boolean mentionEveryone = false;
        boolean mentionHere = false;
        for (String roleId : a.getSubscriberRoleIds()) {
            if (roleId.equalsIgnoreCase("everyone")) {
                mentionEveryone = true;
            } else if (roleId.equalsIgnoreCase("here")) {
                mentionHere = true;
            } else {
                try {
                    Role role = guild.getRoleById(Snowflake.of(roleId)).block();
                    if (role != null)
                        roleMentions.append(role.getMention()).append(" ");
                } catch (Exception e) {
                    //Role does not exist, safely ignore.
                }
            }
        }
        if (!mentionEveryone && !mentionHere && userMentions.toString().equals("") && roleMentions.toString().equals(""))
            return "";


        String message = "Subscribers: " + userMentions + " " + roleMentions;
        if (mentionEveryone)
            message = message + " " + guild.getEveryoneRole().block().getMention();

        if (mentionHere)
            message = message + " @here";

        return message;
    }
}