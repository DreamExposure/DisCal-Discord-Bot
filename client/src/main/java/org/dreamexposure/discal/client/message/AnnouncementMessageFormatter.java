package org.dreamexposure.discal.client.message;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Image;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.utils.ChannelUtils;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.ImageUtils;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings({"Duplicates", "OptionalGetWithoutIsPresent", "ConstantConditions"})
public class AnnouncementMessageFormatter {

	/**
	 * Gets the EmbedObject for an Announcement.
	 *
	 * @param a The Announcement to embed.
	 * @return The EmbedObject for the Announcement.
	 */
	public static EmbedCreateSpec getFormatAnnouncementEmbed(Announcement a, GuildSettings settings, Guild guild) {
		EmbedCreateSpec em = new EmbedCreateSpec();
		em.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);
		em.setTitle(MessageManager.getMessage("Embed.Announcement.Info.Title", settings));
		try {
			em.addField(MessageManager.getMessage("Embed.Announcement.Info.ID", settings), a.getAnnouncementId().toString(), true);
		} catch (NullPointerException e) {
			em.addField(MessageManager.getMessage("Embed.Announcement.Info.ID", settings), "ID IS NULL???", true);
		}

		em.addField(MessageManager.getMessage("Embed.Announcement.Info.Type", settings), a.getAnnouncementType().name(), true);


		if (a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
			em.addField(MessageManager.getMessage("Embed.Announcement.Info.EventID", settings), a.getEventId(), true);
			EventData ed = DatabaseManager.getManager().getEventData(a.getGuildId(), a.getEventId());
			if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink()))
				em.setImage(ed.getImageLink());

		} else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
			em.addField(MessageManager.getMessage("Embed.Announcement.Info.Color", settings), a.getEventColor().name(), true);
		} else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
			em.addField(MessageManager.getMessage("Embed.Announcement.Info.RecurID", settings), a.getEventId(), true);
			EventData ed = DatabaseManager.getManager().getEventData(a.getGuildId(), a.getEventId());
			if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink()))
				em.setImage(ed.getImageLink());
		}
		em.addField(MessageManager.getMessage("Embed.Announcement.Info.Hours", settings), String.valueOf(a.getHoursBefore()), true);
		em.addField(MessageManager.getMessage("Embed.Announcement.Info.Minutes", settings), String.valueOf(a.getMinutesBefore()), true);
		em.addField(MessageManager.getMessage("Embed.Announcement.Info.Channel", settings), ChannelUtils.getChannelNameFromNameOrId(a.getAnnouncementChannelId(), guild), true);
		em.addField(MessageManager.getMessage("Embed.Announcement.Info.Info", settings), a.getInfo(), false);
		if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
			EventColor c = a.getEventColor();
			em.setColor(c.asColor());
		} else {
			em.setColor(GlobalConst.discalColor);
		}

		em.addField(MessageManager.getMessage("Embed.Announcement.Info.Enabled", settings), a.isEnabled() + "", true);

		return em;
	}

	/**
	 * Gets the EmbedObject for a Condensed Announcement.
	 *
	 * @param a The Announcement to embed.
	 * @return The EmbedObject for a Condensed Announcement.
	 */
	public static EmbedCreateSpec getCondensedAnnouncementEmbed(Announcement a, GuildSettings settings) {
		EmbedCreateSpec em = new EmbedCreateSpec();
		em.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);
		em.setTitle(MessageManager.getMessage("Embed.Announcement.Condensed.Title", settings));
		em.addField(MessageManager.getMessage("Embed.Announcement.Condensed.ID", settings), a.getAnnouncementId().toString(), false);
		em.addField(MessageManager.getMessage("Embed.Announcement.Condensed.Time", settings), condensedTime(a), false);

		if (a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
			em.addField(MessageManager.getMessage("Embed.Announcement.Condensed.EventID", settings), a.getEventId(), false);
			try {
				Calendar service = CalendarAuth.getCalendarService(settings);

				//TODO: Handle multiple calendars...

				CalendarData data = DatabaseManager.getManager().getMainCalendar(a.getGuildId());
				Event event = service.events().get(data.getCalendarAddress(), a.getEventId()).execute();
				EventData ed = DatabaseManager.getManager().getEventData(settings.getGuildID(), event.getId());
				if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink()))
					em.setThumbnail(ed.getImageLink());

				if (event.getSummary() != null) {
					String summary = event.getSummary();
					if (summary.length() > 250) {
						summary = summary.substring(0, 250);
						summary = summary + " (continues on Google Calendar View)";
					}
					em.addField(MessageManager.getMessage("Embed.Announcement.Condensed.Summary", settings), summary, true);
				}
			} catch (Exception e) {
				//Failed to get from google cal.
				Logger.getLogger().exception(null, "Failed to get event for announcement.", e, AnnouncementMessageFormatter.class);
			}
		} else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
			em.addField(MessageManager.getMessage("Embed.Announcement.Condensed.Color", settings), a.getEventColor().name(), true);
		} else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
			em.addField(MessageManager.getMessage("Embed.Announcement.Condensed.RecurID", settings), a.getEventId(), true);
		}
		em.setFooter(MessageManager.getMessage("Embed.Announcement.Condensed.Type", "%type%", a.getAnnouncementType().name(), settings), null);

		if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
			EventColor c = a.getEventColor();
			em.setColor(c.asColor());
		} else {
			em.setColor(GlobalConst.discalColor);
		}

		em.addField(MessageManager.getMessage("Embed.Announcement.Info.Enabled", settings), a.isEnabled() + "", true);

		return em;
	}

	/**
	 * Sends an embed with the announcement info in a proper format.
	 *
	 * @param announcement The announcement to send info about.
	 * @param event        the calendar event the announcement is for.
	 * @param data         The BotData belonging to the guild.
	 */
	public static void sendAnnouncementMessage(Announcement announcement, Event event, CalendarData data, GuildSettings settings) {
		EmbedCreateSpec em = new EmbedCreateSpec();
		Guild guild = DisCalClient.getClient().getGuildById(announcement.getGuildId()).block();

		if (guild != null) {
			//Set all of the stuff for embeds regardless of announcement settings
			if (settings.isBranded())
				em.setAuthor(guild.getName(), null, guild.getIconUrl(Image.Format.PNG).get());
			else
				em.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

			em.setTitle(MessageManager.getMessage("Embed.Announcement.Announce.Title", settings));
			EventData ed = DatabaseManager.getManager().getEventData(announcement.getGuildId(), event.getId());
			if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink()))
				em.setImage(ed.getImageLink());

			em.setUrl(event.getHtmlLink());

			try {
				EventColor ec = EventColor.fromNameOrHexOrID(event.getColorId());
				em.setColor(ec.asColor());
			} catch (Exception e) {
				//I dunno, color probably null.
				em.setColor(GlobalConst.discalColor);
			}

			if (!settings.usingSimpleAnnouncements()) {
				em.setFooter(MessageManager.getMessage("Embed.Announcement.Announce.ID", "%id%", announcement.getAnnouncementId().toString(), settings), null);
			}

			if (announcement.isInfoOnly() && announcement.getInfo() != null && !announcement.getInfo().equalsIgnoreCase("none")) {
				//Only send info...
				em.addField(MessageManager.getMessage("Embed.Announcement.Announce.Info", settings), announcement.getInfo(), false);
			} else {
				//Requires all announcement data
				if (event.getSummary() != null) {
					String summary = event.getSummary();
					if (summary.length() > 250) {
						summary = summary.substring(0, 250);
						summary = summary + " (continues on Google Calendar View)";
					}
					em.addField(MessageManager.getMessage("Embed.Announcement.Announce.Summary", settings), summary, true);
				}
				if (event.getDescription() != null) {
					String description = event.getDescription();
					if (description.length() > 250) {
						description = description.substring(0, 250);
						description = description + " (continues on Google Calendar View)";
					}
					em.addField(MessageManager.getMessage("Embed.Announcement.Announce.Description", settings), description, true);
				}
				if (!settings.usingSimpleAnnouncements()) {
					em.addField(MessageManager.getMessage("Embed.Announcement.Announce.Date", settings), EventMessageFormatter.getHumanReadableDate(event.getStart(), settings, false), true);
					em.addField(MessageManager.getMessage("Embed.Announcement.Announce.Time", settings), EventMessageFormatter.getHumanReadableTime(event.getStart(), settings, false), true);
					try {
						Calendar service = CalendarAuth.getCalendarService(settings);
						String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
						em.addField(MessageManager.getMessage("Embed.Announcement.Announce.TimeZone", settings), tz, true);
					} catch (Exception e1) {
						em.addField(MessageManager.getMessage("Embed.Announcement.Announce.TimeZone", settings), "Unknown *Error Occurred", true);
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

					em.addField(MessageManager.getMessage("Embed.Announcement.Announce.Start", settings), start, false);
				}

				if (event.getLocation() != null && !event.getLocation().equalsIgnoreCase("")) {
					if (event.getLocation().length() > 300) {
						String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
						em.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), location, true);
					} else {
						em.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), true);
					}
				}

				if (!settings.usingSimpleAnnouncements())
					em.addField(MessageManager.getMessage("Embed.Announcement.Announce.EventID", settings), event.getId(), false);
				if (!announcement.getInfo().equalsIgnoreCase("None") && !announcement.getInfo().equalsIgnoreCase(""))
					em.addField(MessageManager.getMessage("Embed.Announcement.Announce.Info", settings), announcement.getInfo(), false);
			}


			TextChannel channel = null;

			try {
				channel = guild.getChannels().ofType(TextChannel.class).filter(c -> c.getId().asLong() == Long.valueOf(announcement.getAnnouncementChannelId())).blockLast();
			} catch (Exception e) {
				Logger.getLogger().exception(null, "An error occurred when looking for announcement channel! | Announcement: " + announcement.getAnnouncementId() + " | TYPE: " + announcement.getAnnouncementType() + " | Guild: " + announcement.getGuildId(), e, AnnouncementMessageFormatter.class);
			}

			if (channel == null) {
				//Channel does not exist or could not be found, automatically delete announcement to prevent issues.
				DatabaseManager.getManager().deleteAnnouncement(announcement.getAnnouncementId().toString());
				return;
			}

			MessageManager.sendMessageAsync(getSubscriberMentions(announcement, guild), em, channel);
		}
	}

	public static void sendAnnouncementDM(Announcement announcement, Event event, User user, CalendarData data, GuildSettings settings) {
		EmbedCreateSpec em = new EmbedCreateSpec();
		em.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

		em.setTitle(MessageManager.getMessage("Embed.Announcement.Announce.Title", settings));
		EventData ed = DatabaseManager.getManager().getEventData(announcement.getGuildId(), event.getId());
		if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink())) {
			em.setImage(ed.getImageLink());
		}
		if (event.getSummary() != null) {
			String summary = event.getSummary();
			if (summary.length() > 250) {
				summary = summary.substring(0, 250);
				summary = summary + " (continues on Google Calendar View)";
			}
			em.addField(MessageManager.getMessage("Embed.Announcement.Announce.Summary", settings), summary, true);
		}
		if (event.getDescription() != null) {
			String description = event.getDescription();
			if (description.length() > 250) {
				description = description.substring(0, 250);
				description = description + " (continues on Google Calendar View)";
			}
			em.addField(MessageManager.getMessage("Embed.Announcement.Announce.Description", settings), description, true);
		}
		if (!settings.usingSimpleAnnouncements()) {
			em.addField(MessageManager.getMessage("Embed.Announcement.Announce.Date", settings), EventMessageFormatter.getHumanReadableDate(event.getStart(), settings, false), true);
			em.addField(MessageManager.getMessage("Embed.Announcement.Announce.Time", settings), EventMessageFormatter.getHumanReadableTime(event.getStart(), settings, false), true);
			try {
				Calendar service = CalendarAuth.getCalendarService(settings);
				String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
				em.addField(MessageManager.getMessage("Embed.Announcement.Announce.TimeZone", settings), tz, true);
			} catch (Exception e1) {
				em.addField(MessageManager.getMessage("Embed.Announcement.Announce.TimeZone", settings), "Unknown *Error Occurred", true);
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

			em.addField(MessageManager.getMessage("Embed.Announcement.Announce.Start", settings), start, false);
		}

		if (event.getLocation() != null && !event.getLocation().equalsIgnoreCase("")) {
			if (event.getLocation().length() > 300) {
				String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
				em.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), location, true);
			} else {
				em.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), true);
			}
		}

		if (!settings.usingSimpleAnnouncements()) {
			em.addField(MessageManager.getMessage("Embed.Announcement.Announce.EventID", settings), event.getId(), false);
		}
		em.addField(MessageManager.getMessage("Embed.Announcement.Announce.Info", settings), announcement.getInfo(), false);
		em.setUrl(event.getHtmlLink());
		if (!settings.usingSimpleAnnouncements()) {
			em.setFooter(MessageManager.getMessage("Embed.Announcement.Announce.ID", "%id%", announcement.getAnnouncementId().toString(), settings), null);
		}
		try {
			EventColor ec = EventColor.fromNameOrHexOrID(event.getColorId());
			em.setColor(ec.asColor());
		} catch (Exception e) {
			//I dunno, color probably null.
			em.setColor(GlobalConst.discalColor);
		}

		Guild guild = DisCalClient.getClient().getGuildById(announcement.getGuildId()).block();

		String msg = MessageManager.getMessage("Embed.Announcement.Announce.Dm.Message", "%guild%", guild.getName(), settings);

		MessageManager.sendDirectMessageAsync(msg, em, user);
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

		StringBuilder userMentions = new StringBuilder();
		for (String userId: a.getSubscriberUserIds()) {
			try {
				User user = DisCalClient.getClient().getUserById(Snowflake.of(userId)).block();
				if (user != null)
					userMentions.append(user.asMember(a.getGuildId()).block().getDisplayName()).append(" ");
			} catch (Exception e) {
				//User does not exist, safely ignore.
			}
		}

		StringBuilder roleMentions = new StringBuilder();
		boolean mentionEveryone = false;
		boolean mentionHere = false;
		for (String roleId: a.getSubscriberRoleIds()) {
			if (roleId.equalsIgnoreCase("everyone")) {
				mentionEveryone = true;
			} else if (roleId.equalsIgnoreCase("here")) {
				mentionHere = true;
			} else {
				try {
					Role role = guild.getRoles().filter(r -> r.getId().asLong() == Long.valueOf(roleId)).blockLast();
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
		for (String userId: a.getSubscriberUserIds()) {
			try {
				User user = DisCalClient.getClient().getUserById(Snowflake.of(userId)).block();
				if (user != null)
					userMentions.append(user.getMention()).append(" ");

			} catch (Exception e) {
				//User does not exist, safely ignore.
			}
		}

		StringBuilder roleMentions = new StringBuilder();
		boolean mentionEveryone = false;
		boolean mentionHere = false;
		for (String roleId: a.getSubscriberRoleIds()) {
			if (roleId.equalsIgnoreCase("everyone")) {
				mentionEveryone = true;
			} else if (roleId.equalsIgnoreCase("here")) {
				mentionHere = true;
			} else {
				try {
					Role role = guild.getRoles().filter(r -> r.getId().asString().equalsIgnoreCase(roleId)).blockLast();
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