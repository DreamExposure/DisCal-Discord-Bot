package org.dreamexposure.discal.client.announcement;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.dreamexposure.discal.client.message.AnnouncementMessageFormatter;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.announcement.AnnouncementCreatorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.dreamexposure.discal.core.utils.GlobalVal.getDEFAULT;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementCreator {
    static {
        instance = new AnnouncementCreator();
    }

    private static final AnnouncementCreator instance;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final List<Announcement> announcements = new CopyOnWriteArrayList<>();

    private AnnouncementCreator() {
    } //Prevent initialization

    public static AnnouncementCreator getCreator() {
        return instance;
    }

    //Functional
    public Mono<Announcement> init(final MessageCreateEvent e, final GuildSettings settings) {
        if (!this.hasAnnouncement(settings.getGuildID())) {
            return e.getMessage().getChannel().flatMap(channel -> {
                final Announcement a = new Announcement(settings.getGuildID());
                a.setAnnouncementChannelId(channel.getId().asString());
                this.announcements.add(a);

                return AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings)
                    .flatMap(em ->
                        Messages.sendMessage(Messages.getMessage("Creator.Announcement.Create.Init", settings), em, e))
                    .doOnNext(a::setCreatorMessage)
                    .then(Messages.deleteMessage(e))
                    .thenReturn(a);
            });
        }
        return Mono.justOrEmpty(this.getAnnouncement(settings.getGuildID()));
    }

    public Mono<Announcement> init(final MessageCreateEvent e, final String announcementId, final GuildSettings settings) {
        if (!this.hasAnnouncement(settings.getGuildID())) {
            return DatabaseManager.INSTANCE.getAnnouncement(UUID.fromString(announcementId), settings.getGuildID())
                .flatMap(toCopy -> {
                    final Announcement a = Announcement.Companion.copy(toCopy, false);
                    this.announcements.add(a);

                    return AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings)
                        .flatMap(em ->
                            Messages.sendMessage(Messages.getMessage("Creator.Announcement.Copy.Success", settings), em, e))
                        .doOnNext(a::setCreatorMessage)
                        .then(Messages.deleteMessage(e))
                        .thenReturn(a)
                        .doOnError(err -> LOGGER.error(getDEFAULT(), "Failed to copy", err))
                        .onErrorResume(err -> Mono.empty());
                });
        }
        return Mono.justOrEmpty(this.getAnnouncement(settings.getGuildID()));
    }

    public Mono<Announcement> edit(final MessageCreateEvent e, final String announcementId, final GuildSettings settings) {
        if (!this.hasAnnouncement(settings.getGuildID())) {
            return DatabaseManager.INSTANCE.getAnnouncement(UUID.fromString(announcementId), settings.getGuildID())
                .flatMap(edit -> {
                    edit.setEditing(true);
                    this.announcements.add(edit);

                    return AnnouncementMessageFormatter.getFormatAnnouncementEmbed(edit, settings)
                        .flatMap(em ->
                            Messages.sendMessage(Messages.getMessage("Creator.Announcement.Edit.Init", settings), em, e))
                        .doOnNext(edit::setCreatorMessage)
                        .then(Messages.deleteMessage(e))
                        .thenReturn(edit)
                        .doOnError(err -> LOGGER.error(getDEFAULT(), "Failed to init editor", err))
                        .onErrorResume(err -> Mono.empty());
                });
        } else {
            return Mono.justOrEmpty(this.getAnnouncement(settings.getGuildID()));
        }
    }

    public void terminate(final Snowflake guildId) {
        if (this.hasAnnouncement(guildId))
            this.announcements.remove(this.getAnnouncement(guildId));
    }

    public Mono<AnnouncementCreatorResponse> confirmAnnouncement(final Snowflake guildId) {
        return Mono.justOrEmpty(this.getAnnouncement(guildId))
            .filter(Announcement::hasRequiredValues)
            .flatMap(a ->
                DatabaseManager.INSTANCE.updateAnnouncement(a).map(success -> {
                    if (success) {
                        this.terminate(guildId);
                        return new AnnouncementCreatorResponse(true, a);
                    } else {
                        return new AnnouncementCreatorResponse(false, null);
                    }
                })
            )
            .defaultIfEmpty(new AnnouncementCreatorResponse(false, null));
    }

    //Getters
    public Announcement getAnnouncement(final Snowflake guildId) {
        for (final Announcement a : this.announcements) {
            if (a.getGuildId().equals(guildId)) {
                a.setLastEdit(System.currentTimeMillis());
                return a;
            }
        }
        return null;
    }

    public List<Announcement> getAllAnnouncements() {
        return this.announcements;
    }

    //Booleans/Checkers
    public boolean hasAnnouncement(final Snowflake guildId) {
        for (final Announcement a : this.announcements) {
            if (a.getGuildId().equals(guildId))
                return true;
        }
        return false;
    }
}
