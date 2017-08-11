package com.cloudcraftgaming.discal.module.announcement;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.calendar.event.EventMessageFormatter;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.EventData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.utils.*;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("Duplicates")
public class AnnouncementMessageFormatter {

    /**
     * Gets the EmbedObject for an Announcement.
     * @param a The Announcement to embed.
     * @return The EmbedObject for the Announcement.
     */
    public static EmbedObject getFormatAnnouncementEmbed(Announcement a, GuildSettings settings) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID(266063520112574464L).getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle(MessageManager.getMessage("Embed.Announcement.Info.Title", settings));
        try {
			em.appendField(MessageManager.getMessage("Embed.Announcement.Info.ID", settings), a.getAnnouncementId().toString(), true);
		} catch (NullPointerException e) {
        	em.appendField(MessageManager.getMessage("Embed.Announcement.Info.ID", settings), "ID IS NULL???", true);
		}
        em.appendField(MessageManager.getMessage("Embed.Announcement.Info.Type", settings), a.getAnnouncementType().name(), true);
        if (a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
            em.appendField(MessageManager.getMessage("Embed.Announcement.Info.EventID", settings), a.getEventId(), true);
			EventData ed = DatabaseManager.getManager().getEventData(a.getGuildId(), a.getEventId());
			if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink())) {
				em.withImage(ed.getImageLink());
			}
        } else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
            em.appendField(MessageManager.getMessage("Embed.Announcement.Info.Color", settings), a.getEventColor().name(), true);
        } else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
            em.appendField(MessageManager.getMessage("Embed.Announcement.Info.RecurID", settings), a.getEventId(), true);
			EventData ed = DatabaseManager.getManager().getEventData(a.getGuildId(), a.getEventId());
			if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink())) {
				em.withImage(ed.getImageLink());
			}
        }
        em.appendField(MessageManager.getMessage("Embed.Announcement.Info.Hours", settings), String.valueOf(a.getHoursBefore()), true);
        em.appendField(MessageManager.getMessage("Embed.Announcement.Info.Minutes", settings), String.valueOf(a.getMinutesBefore()), true);
        em.appendField(MessageManager.getMessage("Embed.Announcement.Info.Channel", settings), ChannelUtils.getChannelNameFromNameOrId(a.getAnnouncementChannelId(), a.getGuildId()), true);
        em.appendField(MessageManager.getMessage("Embed.Announcement.Info.Info", settings), a.getInfo(), false);
        if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
            EventColor c = a.getEventColor();
            em.withColor(c.getR(), c.getG(), c.getB());
        } else {
            em.withColor(56, 138, 237);
        }

        return em.build();
    }

    /**
     * Gets the EmbedObject for a Condensed Announcement.
     * @param a The Announcement to embed.
     * @return The EmbedObject for a Condensed Announcement.
     */
    public static EmbedObject getCondensedAnnouncementEmbed(Announcement a, GuildSettings settings)  {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID(266063520112574464L).getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle(MessageManager.getMessage("Embed.Announcement.Condensed.Title", settings));
        em.appendField(MessageManager.getMessage("Embed.Announcement.Condensed.ID", settings), a.getAnnouncementId().toString(), false);
        em.appendField(MessageManager.getMessage("Embed.Announcement.Condensed.Time", settings), condensedTime(a), false);

        if (a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
            em.appendField(MessageManager.getMessage("Embed.Announcement.Condensed.EventID", settings), a.getEventId(), false);
            try {
            	Calendar service;
            	if (settings.useExternalCalendar()) {
            		service = CalendarAuth.getCalendarService(settings);
				} else {
					service = CalendarAuth.getCalendarService();
				}
                //TODO: Handle multiple calendars...
                CalendarData data = DatabaseManager.getManager().getMainCalendar(a.getGuildId());
                Event event = service.events().get(data.getCalendarAddress(), a.getEventId()).execute();
				EventData ed = DatabaseManager.getManager().getEventData(settings.getGuildID(), event.getId());
				if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink())) {
					em.withThumbnail(ed.getImageLink());
				}
                if (event.getSummary() != null) {
					String summary = event.getSummary();
					if (summary.length() > 250) {
						summary = summary.substring(0, 250);
						summary = summary + " (continues on Google Calendar View)";
					}
                    em.appendField(MessageManager.getMessage("Embed.Announcement.Condensed.Summary", settings), summary, true);
                }
            } catch (Exception e) {
                //Failed to get from google cal.
                ExceptionHandler.sendException(null, "Failed to get event for announcement.", e, AnnouncementMessageFormatter.class);
            }
        } else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
            em.appendField(MessageManager.getMessage("Embed.Announcement.Condensed.Color", settings), a.getEventColor().name(), true);
        } else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
            em.appendField(MessageManager.getMessage("Embed.Announcement.Condensed.RecurID", settings), a.getEventId(), true);
        }
        em.withFooterText(MessageManager.getMessage("Embed.Announcement.Condensed.Type", "%type%", a.getAnnouncementType().name(), settings));

        if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
            EventColor c = a.getEventColor();
            em.withColor(c.getR(), c.getG(), c.getB());
        } else {
            em.withColor(56, 138, 237);
        }

        return em.build();
    }

    /**
     * Sends an embed with the announcement info in a proper format.
     *
     * @param announcement The announcement to send info about.
     * @param event        the calendar event the announcement is for.
     * @param data         The BotData belonging to the guild.
     */
    static void sendAnnouncementMessage(Announcement announcement, Event event, CalendarData data, GuildSettings settings) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID(266063520112574464L).getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle(MessageManager.getMessage("Embed.Announcement.Announce.Title", settings));
		EventData ed = DatabaseManager.getManager().getEventData(announcement.getGuildId(), event.getId());
		if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink())) {
			em.withImage(ed.getImageLink());
		}
        if (event.getSummary() != null) {
			String summary = event.getSummary();
			if (summary.length() > 250) {
				summary = summary.substring(0, 250);
				summary = summary + " (continues on Google Calendar View)";
			}
            em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.Summary", settings), summary, true);
        }
        if (event.getDescription() != null) {
			String description = event.getDescription();
			if (description.length() > 250) {
				description = description.substring(0, 250);
				description = description + " (continues on Google Calendar View)";
			}
            em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.Description", settings), description, true);
        }
        if (!settings.usingSimpleAnnouncements()) {
            em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.Date", settings), EventMessageFormatter.getHumanReadableDate(event.getStart(), settings, false), true);
            em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.Time", settings), EventMessageFormatter.getHumanReadableTime(event.getStart(), settings, false), true);
            try {
                Calendar service = CalendarAuth.getCalendarService();
                String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
                em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.TimeZone", settings), tz, true);
            } catch (Exception e1) {
                em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.TimeZone", settings), "Unknown *Error Occurred", true);
            }
        } else {
            String start = EventMessageFormatter.getHumanReadableDate(event.getStart(), settings, false) + " at " + EventMessageFormatter.getHumanReadableTime(event.getStart(), settings, false);
            try {
                Calendar service = CalendarAuth.getCalendarService();
                String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
                start = start + " " + tz;
            } catch (Exception e1) {
                start = start + " (TZ UNKNOWN/ERROR)";
            }

            em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.Start", settings), start, false);
        }

        if (!settings.usingSimpleAnnouncements()) {
            em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.EventID", settings), event.getId(), false);
        }
        if (!announcement.getInfo().equalsIgnoreCase("None")) {
			em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.Info", settings), announcement.getInfo(), false);
		}
        em.withUrl(event.getHtmlLink());
        if (!settings.usingSimpleAnnouncements()) {
            em.withFooterText(MessageManager.getMessage("Embed.Announcement.Announce.ID", "%id%", announcement.getAnnouncementId().toString(), settings));
        }
        try {
            EventColor ec = EventColor.fromNameOrHexOrID(event.getColorId());
            em.withColor(ec.getR(), ec.getG(), ec.getB());
        } catch (Exception e) {
            //I dunno, color probably null.
            em.withColor(56, 138, 237);
        }

        IGuild guild = Main.client.getGuildByID(announcement.getGuildId());

        IChannel channel = guild.getChannelByID(Long.valueOf(announcement.getAnnouncementChannelId()));

	    if (announcement.getSubscriberRoleIds().isEmpty() && announcement.getSubscriberUserIds().isEmpty()) {
	    	Message.sendMessage(em.build(), channel);
	    } else {
	    	Message.sendMessage(em.build(), getSubscriberMentions(announcement, guild), channel);
	    }
    }

    static void sendAnnouncementDM(Announcement announcement, Event event, IUser user, CalendarData data, GuildSettings settings) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID(266063520112574464L).getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle(MessageManager.getMessage("Embed.Announcement.Announce.Title", settings));
		EventData ed = DatabaseManager.getManager().getEventData(announcement.getGuildId(), event.getId());
		if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink())) {
			em.withImage(ed.getImageLink());
		}
		if (event.getSummary() != null) {
			String summary = event.getSummary();
			if (summary.length() > 250) {
				summary = summary.substring(0, 250);
				summary = summary + " (continues on Google Calendar View)";
			}
			em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.Summary", settings), summary, true);
		}
		if (event.getDescription() != null) {
			String description = event.getDescription();
			if (description.length() > 250) {
				description = description.substring(0, 250);
				description = description + " (continues on Google Calendar View)";
			}
			em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.Description", settings), description, true);
		}
        if (!settings.usingSimpleAnnouncements()) {
            em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.Date", settings), EventMessageFormatter.getHumanReadableDate(event.getStart(), settings, false), true);
            em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.Time", settings), EventMessageFormatter.getHumanReadableTime(event.getStart(), settings, false), true);
            try {
                Calendar service = CalendarAuth.getCalendarService();
                String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
                em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.TimeZone", settings), tz, true);
            } catch (Exception e1) {
                em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.TimeZone", settings), "Unknown *Error Occurred", true);
            }
        } else {
            String start = EventMessageFormatter.getHumanReadableDate(event.getStart(), settings, false) + " at " + EventMessageFormatter.getHumanReadableTime(event.getStart(), settings, false);
            try {
                Calendar service = CalendarAuth.getCalendarService();
                String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
                start = start + " " + tz;
            } catch (Exception e1) {
                start = start + " (TZ UNKNOWN/ERROR)";
            }

            em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.Start", settings), start, false);
        }

        if (!settings.usingSimpleAnnouncements()) {
            em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.EventID", settings), event.getId(), false);
        }
        em.appendField(MessageManager.getMessage("Embed.Announcement.Announce.Info", settings), announcement.getInfo(), false);
        em.withUrl(event.getHtmlLink());
        if (!settings.usingSimpleAnnouncements()) {
            em.withFooterText(MessageManager.getMessage("Embed.Announcement.Announce.ID", "%id%", announcement.getAnnouncementId().toString(), settings));
        }
        try {
            EventColor ec = EventColor.fromNameOrHexOrID(event.getColorId());
            em.withColor(ec.getR(), ec.getG(), ec.getB());
        } catch (Exception e) {
            //I dunno, color probably null.
            em.withColor(56, 138, 237);
        }

        IGuild guild = Main.client.getGuildByID(announcement.getGuildId());

        String msg = MessageManager.getMessage("Embed.Announcement.Announce.Dm.Message", "%guild%", guild.getName(), settings);

        Message.sendDirectMessage(msg, em.build(), user);
    }

    /**
     * Gets the formatted time from an Announcement.
     * @param a The Announcement.
     * @return The formatted time from an Announcement.
     */
    private static String condensedTime(Announcement a) {
        return a.getHoursBefore() + "H" + a.getMinutesBefore() + "m";
    }

    public static String getSubscriberNames(Announcement a) {
        //Loop and get subs without mentions...
        IGuild guild = Main.client.getGuildByID(a.getGuildId());

        StringBuilder userMentions = new StringBuilder();
        for (String userId : a.getSubscriberUserIds()) {
            try {
                IUser user = guild.getUserByID(Long.valueOf(userId));
                if (user != null) {
                    userMentions.append(user.getName()).append(" ");
                }
            } catch (Exception e) {
                //User does not exist, safely ignore.
            }
        }

        StringBuilder roleMentions = new StringBuilder();
        Boolean mentionEveryone = false;
        Boolean mentionHere = false;
        for (String roleId : a.getSubscriberRoleIds()) {
            if (roleId.equalsIgnoreCase("everyone")) {
                mentionEveryone = true;
            } else if (roleId.equalsIgnoreCase("here")) {
                mentionHere = true;
            } else {
                try {
                    IRole role = guild.getRoleByID(Long.valueOf(roleId));
                    if (role != null) {
                        roleMentions.append(role.getName()).append(" ");
                    }
                } catch (Exception e) {
                    //Role does not exist, safely ignore.
                }
            }
        }

        String message = "Subscribers: " + userMentions + " " + roleMentions;
        if (mentionEveryone) {
            message = message + " " + guild.getEveryoneRole().getName();
        }
        if (mentionHere) {
            message = message + " here";
        }

        return message;
    }

    public static String getSubscriberMentions(Announcement a, IGuild guild) {
        StringBuilder userMentions = new StringBuilder();
        for (String userId : a.getSubscriberUserIds()) {
            try {
                IUser user = guild.getUserByID(Long.valueOf(userId));
                if (user != null) {
                    userMentions.append(user.mention(true)).append(" ");
                }
            } catch (Exception e) {
                //User does not exist, safely ignore.
            }
        }

        StringBuilder roleMentions = new StringBuilder();
        Boolean mentionEveryone = false;
        Boolean mentionHere = false;
        for (String roleId : a.getSubscriberRoleIds()) {
            if (roleId.equalsIgnoreCase("everyone")) {
                mentionEveryone = true;
            } else if (roleId.equalsIgnoreCase("here")) {
                mentionHere = true;
            } else {
                try {
                    IRole role = guild.getRoleByID(Long.valueOf(roleId));
                    if (role != null) {
                        roleMentions.append(role.mention()).append(" ");
                    }
                } catch (Exception e) {
                    //Role does not exist, safely ignore.
                }
            }
        }

        String message = "Subscribers: " + userMentions + " " + roleMentions;
        if (mentionEveryone) {
            message = message + " " + guild.getEveryoneRole().mention();
        }
        if (mentionHere) {
            message = message + " @here";
        }

        return message;
    }
}