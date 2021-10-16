package org.dreamexposure.discal.client.message;

import com.google.api.services.calendar.model.Event;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Image;
import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.utils.*;
import org.dreamexposure.discal.core.wrapper.google.EventWrapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings({"Duplicates", "MagicNumber", "StringConcatenationMissingWhitespace"})
public class AnnouncementMessageFormatter {
    public static Mono<EmbedCreateSpec> getFormatAnnouncementEmbed(Announcement a, GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID()).cache();

        Mono<String> channelName = guild
            .flatMap(g -> g.getChannelById(Snowflake.of(a.getAnnouncementChannelId())))
            .map(GuildChannel::getName)
            .defaultIfEmpty("!!Channel not found!!");

        Mono<EventData> eData = Mono.just(a)
            .map(Announcement::getType)
            .filter(t -> t.equals(AnnouncementType.SPECIFIC) || t.equals(AnnouncementType.RECUR))
            .flatMap(t -> DatabaseManager.INSTANCE.getEventData(a.getGuildId(), a.getEventId()))
            .defaultIfEmpty(new EventData()).cache();

        Mono<Boolean> img = eData.filter(EventData::shouldBeSaved)
            .flatMap(ed -> ImageValidator.validate(ed.getImageLink(), settings.getPatronGuild()))
            .defaultIfEmpty(false);

        return Mono.zip(guild, channelName, eData, img)
            .map(TupleUtils.function((g, chanName, ed, hasImg) -> {
                var embed = EmbedCreateSpec.builder();

                if (settings.getBranded())
                    embed.author(g.getName(), BotSettings.BASE_URL.get(),
                        g.getIconUrl(Image.Format.PNG).orElse(GlobalVal.getIconUrl()));
                else
                    embed.author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl());

                embed.title(Messages.getMessage("Embed.Announcement.Info.Title", settings));
                embed.addField(Messages.getMessage("Embed.Announcement.Info.ID", settings), a.getId().toString(), true);

                embed.addField(Messages.getMessage("Embed.Announcement.Info.Type", settings), a.getType().name(), true);


                if (a.getType().equals(AnnouncementType.SPECIFIC)) {
                    embed.addField(Messages.getMessage("Embed.Announcement.Info.EventID", settings), a.getEventId(), true);
                    if (hasImg)
                        embed.image(ed.getImageLink());

                } else if (a.getType().equals(AnnouncementType.COLOR)) {
                    embed.addField(Messages.getMessage("Embed.Announcement.Info.Color", settings), a.getEventColor().name(), true);
                } else if (a.getType().equals(AnnouncementType.RECUR)) {
                    embed.addField(Messages.getMessage("Embed.Announcement.Info.RecurID", settings), a.getEventId(), true);
                    if (hasImg)
                        embed.image(ed.getImageLink());
                }
                embed.addField(Messages.getMessage("Embed.Announcement.Info.Hours", settings), String.valueOf(a.getHoursBefore()), true);
                embed.addField(Messages.getMessage("Embed.Announcement.Info.Minutes", settings), String.valueOf(a.getMinutesBefore()), true);
                embed.addField(Messages.getMessage("Embed.Announcement.Info.Channel", settings), chanName, true);
                embed.addField(Messages.getMessage("Embed.Announcement.Info.Info", settings), a.getInfo(), false);
                if (a.getType().equals(AnnouncementType.COLOR))
                    embed.color(a.getEventColor().asColor());
                else
                    embed.color(GlobalVal.getDiscalColor());

                embed.addField(Messages.getMessage("Embed.Announcement.Info.Enabled", settings), a.getEnabled() + "", true);
                if (settings.getDevGuild() || settings.getPatronGuild())
                    embed.addField("Publishable", a.getPublish() + "", true);

                return embed.build();
            }));
    }

    @Deprecated
    public static Mono<EmbedCreateSpec> getCondensedAnnouncementEmbed(Announcement a, GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());

        Mono<Event> event = Mono.just(a)
            .map(Announcement::getType)
            .filter(t -> t.equals(AnnouncementType.SPECIFIC))
            .flatMap(t -> DatabaseManager.INSTANCE.getMainCalendar(a.getGuildId()))
            .flatMap(cd -> EventWrapper.INSTANCE.getEvent(cd, a.getEventId()))
            .defaultIfEmpty(new Event());

        Mono<EventData> eData = Mono.just(a)
            .map(Announcement::getType)
            .filter(t -> t.equals(AnnouncementType.SPECIFIC) || t.equals(AnnouncementType.RECUR))
            .flatMap(t -> DatabaseManager.INSTANCE.getEventData(a.getGuildId(), a.getEventId()))
            .defaultIfEmpty(new EventData()).cache();

        Mono<Boolean> img = eData.filter(EventData::shouldBeSaved)
            .flatMap(ed -> ImageValidator.validate(ed.getImageLink(), settings.getPatronGuild()))
            .defaultIfEmpty(false);


        return Mono.zip(guild, event, eData, img)
            .map(TupleUtils.function((g, e, ed, hasImg) -> {
                var embed = EmbedCreateSpec.builder();

                if (settings.getBranded())
                    embed.author(g.getName(), BotSettings.BASE_URL.get(),
                        g.getIconUrl(Image.Format.PNG).orElse(GlobalVal.getIconUrl()));
                else
                    embed.author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl());

                embed.title(Messages.getMessage("Embed.Announcement.Condensed.Title", settings));
                embed.addField(Messages.getMessage("Embed.Announcement.Condensed.ID", settings), a.getId().toString(), false);
                embed.addField(Messages.getMessage("Embed.Announcement.Condensed.Time", settings), condensedTime(a), false);

                if (a.getType().equals(AnnouncementType.SPECIFIC)) {
                    embed.addField(Messages.getMessage("Embed.Announcement.Condensed.EventID", settings), a.getEventId(), false);

                    if (hasImg)
                        embed.thumbnail(ed.getImageLink());

                    if (e.getSummary() != null) {
                        String summary = e.getSummary();
                        if (summary.length() > 250) {
                            summary = summary.substring(0, 250);
                            summary = summary + " (continues on Google Calendar View)";
                        }
                        embed.addField(Messages.getMessage("Embed.Announcement.Condensed.Summary", settings), summary, true);
                    }
                } else if (a.getType().equals(AnnouncementType.COLOR)) {
                    embed.addField(Messages.getMessage("Embed.Announcement.Condensed.Color", settings), a.getEventColor().name(), true);
                } else if (a.getType().equals(AnnouncementType.RECUR)) {
                    embed.addField(Messages.getMessage("Embed.Announcement.Condensed.RecurID", settings), a.getEventId(), true);
                }
                embed.footer(Messages.getMessage("Embed.Announcement.Condensed.Type", "%type%", a.getType().name(),
                    settings), null);

                if (a.getType().equals(AnnouncementType.COLOR)) {
                    embed.color(a.getEventColor().asColor());
                } else {
                    embed.color(GlobalVal.getDiscalColor());
                }

                embed.addField(Messages.getMessage("Embed.Announcement.Info.Enabled", settings), a.getEnabled() + "", true);

                return embed.build();
            }));
    }

    private static String condensedTime(Announcement a) {
        return a.getHoursBefore() + "H" + a.getMinutesBefore() + "m";
    }

    public static Mono<String> getSubscriberNames(Announcement a, Guild guild) {
        return Mono.defer(() -> {
            Mono<List<String>> userMentions = Flux.fromIterable(a.getSubscriberUserIds())
                .flatMap(s -> UserUtils.getUserFromID(s, guild))
                .map(Member::getDisplayName)
                .onErrorReturn("")
                .collectList()
                .defaultIfEmpty(new ArrayList<>());

            Mono<List<String>> roleMentions = Flux.fromIterable(a.getSubscriberRoleIds())
                .flatMap(s -> {
                    if ("everyone".equalsIgnoreCase(s))
                        return Mono.just("everyone");
                    else if ("here".equalsIgnoreCase(s))
                        return Mono.just("here");
                    else {
                        return RoleUtils.getRoleFromID(s, guild)
                            .map(Role::getName)
                            .onErrorReturn("");
                    }
                }).collectList()
                .defaultIfEmpty(new ArrayList<>());

            return Mono.zip(userMentions, roleMentions).map(TupleUtils.function((users, roles) -> {
                StringBuilder mentions = new StringBuilder();

                mentions.append("Subscribers: ");

                for (String s : users) {
                    mentions.append(s).append(" ");
                }

                for (String s : roles) {
                    mentions.append(s).append(" ");
                }

                return mentions.toString().replaceAll("@", "@\u200B");
            }));
        });
    }
}
