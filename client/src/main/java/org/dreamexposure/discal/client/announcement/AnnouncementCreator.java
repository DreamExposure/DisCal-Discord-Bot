package org.dreamexposure.discal.client.announcement;

import org.dreamexposure.discal.client.message.AnnouncementMessageFormatter;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.announcement.AnnouncementCreatorResponse;
import org.dreamexposure.discal.core.utils.AnnouncementUtils;
import org.dreamexposure.discal.core.utils.PermissionChecker;

import java.util.ArrayList;
import java.util.UUID;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("ConstantConditions")
public class AnnouncementCreator {
	private static AnnouncementCreator instance;

	private ArrayList<Announcement> announcements = new ArrayList<>();

	private AnnouncementCreator() {
	} //Prevent initialization

	/**
	 * Gets the instance of the AnnouncementCreator.
	 *
	 * @return The instance of the AnnouncementCreator.
	 */
	public static AnnouncementCreator getCreator() {
		if (instance == null) {
			instance = new AnnouncementCreator();
		}
		return instance;
	}

	//Functional

	/**
	 * Initiates the creator for the guild involved.
	 *
	 * @param e The event received upon init.
	 * @return A new Announcement.
	 */
	public Announcement init(MessageCreateEvent e, GuildSettings settings) {
		if (!hasAnnouncement(settings.getGuildID())) {
			Announcement a = new Announcement(settings.getGuildID());
			a.setAnnouncementChannelId(e.getMessage().getChannel().block().getId().asString());

			if (PermissionChecker.botHasMessageManagePerms(e).blockOptional().orElse(false)) {
				Message msg = MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Create.Init", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), e);
				a.setCreatorMessage(msg);
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Notif.MANAGE_MESSAGES", settings), e);
			}

			announcements.add(a);
			return a;
		}
		return getAnnouncement(settings.getGuildID());
	}

	public Announcement init(MessageCreateEvent e, String announcementId, GuildSettings settings) {
		if (!hasAnnouncement(settings.getGuildID()) && AnnouncementUtils.announcementExists(announcementId, settings.getGuildID())) {
			Announcement toCopy = DatabaseManager.getManager().getAnnouncement(UUID.fromString(announcementId), settings.getGuildID());

			//Copy
			Announcement a = new Announcement(toCopy);

			if (PermissionChecker.botHasMessageManagePerms(e).blockOptional().orElse(false)) {
				Message msg = MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Copy.Success", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), e);
				a.setCreatorMessage(msg);
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Notif.MANAGE_MESSAGES", settings), e);
			}

			announcements.add(a);
			return a;
		}
		return getAnnouncement(settings.getGuildID());
	}

	public Announcement edit(MessageCreateEvent e, String announcementId, GuildSettings settings) {
		if (!hasAnnouncement(settings.getGuildID()) && AnnouncementUtils.announcementExists(announcementId, settings.getGuildID())) {
			Announcement edit = DatabaseManager.getManager().getAnnouncement(UUID.fromString(announcementId), settings.getGuildID());

			//Copy
			Announcement a = new Announcement(edit, true);
			a.setEditing(true);

			if (PermissionChecker.botHasMessageManagePerms(e).blockOptional().orElse(false)) {
				Message msg = MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Announcement.Edit.Init", settings), AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings), e);
				a.setCreatorMessage(msg);
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Notif.MANAGE_MESSAGES", settings), e);
			}

			announcements.add(a);
			return a;
		}
		return getAnnouncement(settings.getGuildID());
	}

	public void terminate(Snowflake guildId) {
		if (hasAnnouncement(guildId))
			announcements.remove(getAnnouncement(guildId));
	}

	public AnnouncementCreatorResponse confirmAnnouncement(Snowflake guildId) {
		if (hasAnnouncement(guildId)) {
			Announcement a = getAnnouncement(guildId);
			if (a.hasRequiredValues()) {
				if (DatabaseManager.getManager().updateAnnouncement(a)) {
					terminate(guildId);
					return new AnnouncementCreatorResponse(true, a);
				}
			}
		}
		return new AnnouncementCreatorResponse(false);
	}

	//Getters

	/**
	 * Gets the Announcement in the creator for the guild.
	 *
	 * @param guildId The ID of the guild
	 * @return The Announcement in the creator for the guild.
	 */
	public Announcement getAnnouncement(Snowflake guildId) {
		for (Announcement a: announcements) {
			if (a.getGuildId().equals(guildId)) {
				a.setLastEdit(System.currentTimeMillis());
				return a;
			}
		}
		return null;
	}

	public Message getCreatorMessage(Snowflake guildId) {
		if (hasAnnouncement(guildId))
			return getAnnouncement(guildId).getCreatorMessage();
		return null;
	}

	public ArrayList<Announcement> getAllAnnouncements() {
		return announcements;
	}

	//Setters
	public void setCreatorMessage(Message message) {
		if (message != null && hasCreatorMessage(message.getGuild().block().getId()))
			getAnnouncement(message.getGuild().block().getId()).setCreatorMessage(message);
	}

	//Booleans/Checkers

	/**
	 * Whether or not the Guild has an announcement in the creator.
	 *
	 * @param guildId The ID of the guild.
	 * @return <code>true</code> if active, else <code>false</code>.
	 */
	public boolean hasAnnouncement(Snowflake guildId) {
		for (Announcement a: announcements) {
			if (a.getGuildId().equals(guildId))
				return true;
		}
		return false;
	}

	public boolean hasCreatorMessage(Snowflake guildId) {
		return hasAnnouncement(guildId) && getAnnouncement(guildId).getCreatorMessage() != null;
	}
}