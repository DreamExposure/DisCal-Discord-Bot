package org.dreamexposure.discal.client.message;

import com.google.api.services.calendar.model.Event;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.utils.ChannelUtils;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.ImageUtils;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;
import org.dreamexposure.discal.core.wrapper.google.EventWrapper;

import java.util.function.Consumer;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.Image;
import discord4j.rest.util.Snowflake;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.function.TupleUtils;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings({"Duplicates", "ConstantConditions"})
public class AnnouncementMessageFormatter {
    public static Mono<Consumer<EmbedCreateSpec>> getFormatAnnouncementEmbed(Announcement a, GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID()).cache();

        Mono<String> channelName = guild
            .flatMap(g -> ChannelUtils.getChannelNameFromNameOrId(a.getAnnouncementChannelId(), g));

        Mono<EventData> eData = Mono.just(a)
            .map(Announcement::getAnnouncementType)
            .filter(t -> t.equals(AnnouncementType.SPECIFIC) || t.equals(AnnouncementType.RECUR))
            .flatMap(t -> DatabaseManager.getEventData(a.getGuildId(), a.getEventId()))
            .defaultIfEmpty(EventData.empty()).cache();

        Mono<Boolean> img = eData.filter(EventData::shouldBeSaved)
            .flatMap(ed -> ImageUtils.validate(ed.getImageLink(), settings.isPatronGuild()))
            .defaultIfEmpty(false);

        return Mono.zip(guild, channelName, eData, img)
            .map(TupleUtils.function((g, chanName, ed, hasImg) -> spec -> {
                if (settings.isBranded())
                    spec.setAuthor(g.getName(), GlobalConst.discalSite, g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                else
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                spec.setTitle(Messages.getMessage("Embed.Announcement.Info.Title", settings));
                if (a.getAnnouncementId() != null)
                    spec.addField(Messages.getMessage("Embed.Announcement.Info.ID", settings), a.getAnnouncementId().toString(), true);
                else
                    spec.addField(Messages.getMessage("Embed.Announcement.Info.ID", settings), "ID IS NULL???", true);

                spec.addField(Messages.getMessage("Embed.Announcement.Info.Type", settings), a.getAnnouncementType().name(), true);


                if (a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
                    spec.addField(Messages.getMessage("Embed.Announcement.Info.EventID", settings), a.getEventId(), true);
                    if (hasImg)
                        spec.setImage(ed.getImageLink());

                } else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
                    spec.addField(Messages.getMessage("Embed.Announcement.Info.Color", settings), a.getEventColor().name(), true);
                } else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
                    spec.addField(Messages.getMessage("Embed.Announcement.Info.RecurID", settings), a.getEventId(), true);
                    if (hasImg)
                        spec.setImage(ed.getImageLink());
                }
                spec.addField(Messages.getMessage("Embed.Announcement.Info.Hours", settings), String.valueOf(a.getHoursBefore()), true);
                spec.addField(Messages.getMessage("Embed.Announcement.Info.Minutes", settings), String.valueOf(a.getMinutesBefore()), true);
                spec.addField(Messages.getMessage("Embed.Announcement.Info.Channel", settings), chanName, true);
                spec.addField(Messages.getMessage("Embed.Announcement.Info.Info", settings), a.getInfo(), false);
                if (a.getAnnouncementType().equals(AnnouncementType.COLOR))
                    spec.setColor(a.getEventColor().asColor());
                else
                    spec.setColor(GlobalConst.discalColor);

                spec.addField(Messages.getMessage("Embed.Announcement.Info.Enabled", settings), a.isEnabled() + "", true);
            }));
    }

    @Deprecated
    public static Mono<Consumer<EmbedCreateSpec>> getCondensedAnnouncementEmbed(Announcement a, GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());

        Mono<Event> event = Mono.just(a)
            .map(Announcement::getAnnouncementType)
            .filter(t -> t.equals(AnnouncementType.SPECIFIC))
            .flatMap(t -> DatabaseManager.getMainCalendar(a.getGuildId()))
            .flatMap(cd -> EventWrapper.getEvent(cd, settings, a.getEventId()))
            .defaultIfEmpty(new Event());

        Mono<EventData> eData = Mono.just(a)
            .map(Announcement::getAnnouncementType)
            .filter(t -> t.equals(AnnouncementType.SPECIFIC) || t.equals(AnnouncementType.RECUR))
            .flatMap(t -> DatabaseManager.getEventData(a.getGuildId(), a.getEventId()))
            .defaultIfEmpty(EventData.empty()).cache();

        Mono<Boolean> img = eData.filter(EventData::shouldBeSaved)
            .flatMap(ed -> ImageUtils.validate(ed.getImageLink(), settings.isPatronGuild()))
            .defaultIfEmpty(false);


        return Mono.zip(guild, event, eData, img)
            .map(TupleUtils.function((g, e, ed, hasImg) -> spec -> {
                if (settings.isBranded())
                    spec.setAuthor(g.getName(), GlobalConst.discalSite, g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                else
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                spec.setTitle(Messages.getMessage("Embed.Announcement.Condensed.Title", settings));
                spec.addField(Messages.getMessage("Embed.Announcement.Condensed.ID", settings), a.getAnnouncementId().toString(), false);
                spec.addField(Messages.getMessage("Embed.Announcement.Condensed.Time", settings), condensedTime(a), false);

                if (a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
                    spec.addField(Messages.getMessage("Embed.Announcement.Condensed.EventID", settings), a.getEventId(), false);

                    if (hasImg)
                        spec.setThumbnail(ed.getImageLink());

                    if (e.getSummary() != null) {
                        String summary = e.getSummary();
                        if (summary.length() > 250) {
                            summary = summary.substring(0, 250);
                            summary = summary + " (continues on Google Calendar View)";
                        }
                        spec.addField(Messages.getMessage("Embed.Announcement.Condensed.Summary", settings), summary, true);
                    }
                } else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
                    spec.addField(Messages.getMessage("Embed.Announcement.Condensed.Color", settings), a.getEventColor().name(), true);
                } else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
                    spec.addField(Messages.getMessage("Embed.Announcement.Condensed.RecurID", settings), a.getEventId(), true);
                }
                spec.setFooter(Messages.getMessage("Embed.Announcement.Condensed.Type", "%type%", a.getAnnouncementType().name(), settings), null);

                if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
                    spec.setColor(a.getEventColor().asColor());
                } else {
                    spec.setColor(GlobalConst.discalColor);
                }

                spec.addField(Messages.getMessage("Embed.Announcement.Info.Enabled", settings), a.isEnabled() + "", true);
            }));
    }

    public static Mono<Consumer<EmbedCreateSpec>> getCondensedAnnouncementEmbed(Announcement a, int calNum,
                                                                                GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());

        Mono<Event> event = Mono.just(a)
            .map(Announcement::getAnnouncementType)
            .filter(t -> t.equals(AnnouncementType.SPECIFIC))
            .flatMap(t -> DatabaseManager.getCalendar(a.getGuildId(), calNum))
            .flatMap(cd -> EventWrapper.getEvent(cd, settings, a.getEventId()))
            .defaultIfEmpty(new Event());

        Mono<EventData> eData = Mono.just(a)
            .map(Announcement::getAnnouncementType)
            .filter(t -> t.equals(AnnouncementType.SPECIFIC) || t.equals(AnnouncementType.RECUR))
            .flatMap(t -> DatabaseManager.getEventData(a.getGuildId(), a.getEventId()))
            .defaultIfEmpty(EventData.empty()).cache();

        Mono<Boolean> img = eData.filter(EventData::shouldBeSaved)
            .flatMap(ed -> ImageUtils.validate(ed.getImageLink(), settings.isPatronGuild()))
            .defaultIfEmpty(false);


        return Mono.zip(guild, event, eData, img)
            .map(TupleUtils.function((g, e, ed, hasImg) -> spec -> {
                if (settings.isBranded())
                    spec.setAuthor(g.getName(), GlobalConst.discalSite, g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                else
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                spec.setTitle(Messages.getMessage("Embed.Announcement.Condensed.Title", settings));
                spec.addField(Messages.getMessage("Embed.Announcement.Condensed.ID", settings), a.getAnnouncementId().toString(), false);
                spec.addField(Messages.getMessage("Embed.Announcement.Condensed.Time", settings), condensedTime(a), false);

                if (a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
                    spec.addField(Messages.getMessage("Embed.Announcement.Condensed.EventID", settings), a.getEventId(), false);

                    if (hasImg)
                        spec.setThumbnail(ed.getImageLink());

                    if (e.getSummary() != null) {
                        String summary = e.getSummary();
                        if (summary.length() > 250) {
                            summary = summary.substring(0, 250);
                            summary = summary + " (continues on Google Calendar View)";
                        }
                        spec.addField(Messages.getMessage("Embed.Announcement.Condensed.Summary", settings), summary, true);
                    }
                } else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
                    spec.addField(Messages.getMessage("Embed.Announcement.Condensed.Color", settings), a.getEventColor().name(), true);
                } else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
                    spec.addField(Messages.getMessage("Embed.Announcement.Condensed.RecurID", settings), a.getEventId(), true);
                }
                spec.setFooter(Messages.getMessage("Embed.Announcement.Condensed.Type", "%type%", a.getAnnouncementType().name(), settings), null);

                if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
                    spec.setColor(a.getEventColor().asColor());
                } else {
                    spec.setColor(GlobalConst.discalColor);
                }

                spec.addField(Messages.getMessage("Embed.Announcement.Info.Enabled", settings), a.isEnabled() + "", true);
            }));
    }

    public static Mono<Consumer<EmbedCreateSpec>> getRealAnnouncementEmbed(Announcement a, Event event, CalendarData cd,
                                                                           GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());

        Mono<String> startDate = EventMessageFormatter
            .getHumanReadableDate(event.getStart(), cd.getCalendarNumber(), false, settings);

        Mono<String> startTime = EventMessageFormatter
            .getHumanReadableTime(event.getStart(), cd.getCalendarNumber(), false, settings);

        Mono<String> timezone = CalendarWrapper.getCalendar(cd, settings)
            .map(com.google.api.services.calendar.model.Calendar::getTimeZone)
            .defaultIfEmpty("TZ Unknown/Error");

        Mono<EventData> eData = DatabaseManager.getEventData(settings.getGuildID(), event.getId())
            .defaultIfEmpty(EventData.empty())
            .cache();

        Mono<Boolean> img = eData.filter(EventData::shouldBeSaved)
            .flatMap(ed -> ImageUtils.validate(ed.getImageLink(), settings.isPatronGuild()))
            .defaultIfEmpty(false);

        return Mono.zip(guild, startDate, startTime, timezone, eData, img)
            .map(TupleUtils.function((g, sDate, sTime, tz, ed, hasImg) -> spec -> {
                if (settings.isBranded())
                    spec.setAuthor(g.getName(), GlobalConst.discalSite, g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                else
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                spec.setTitle(Messages.getMessage("Embed.Announcement.Announce.Title", settings));
                if (hasImg)
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
                    spec.setFooter(Messages.getMessage("Embed.Announcement.Announce.ID", "%id%", a.getAnnouncementId().toString(), settings), null);
                }

                if (a.isInfoOnly() && a.getInfo() != null && !a.getInfo().equalsIgnoreCase("none")) {
                    //Only send info...
                    spec.addField(Messages.getMessage("Embed.Announcement.Announce.Info", settings), a.getInfo(), false);
                } else {
                    //Requires all announcement data
                    if (event.getSummary() != null) {
                        String summary = event.getSummary();
                        if (summary.length() > 250) {
                            summary = summary.substring(0, 250);
                            summary = summary + " (continues on Google Calendar View)";
                        }
                        spec.addField(Messages.getMessage("Embed.Announcement.Announce.Summary", settings), summary, true);
                    }
                    if (event.getDescription() != null) {
                        String description = event.getDescription();
                        if (description.length() > 250) {
                            description = description.substring(0, 250);
                            description = description + " (continues on Google Calendar View)";
                        }
                        spec.addField(Messages.getMessage("Embed.Announcement.Announce.Description", settings), description, true);
                    }
                    if (!settings.usingSimpleAnnouncements()) {
                        spec.addField(Messages.getMessage("Embed.Announcement.Announce.Date", settings), sDate, true);
                        spec.addField(Messages.getMessage("Embed.Announcement.Announce.Time", settings), sTime, true);
                        spec.addField(Messages.getMessage("Embed.Announcement.Announce.TimeZone", settings), tz, true);
                    } else {
                        String start = sDate + " at " + sTime + " " + tz;
                        spec.addField(Messages.getMessage("Embed.Announcement.Announce.Start", settings), start, false);
                    }

                    if (event.getLocation() != null && !event.getLocation().equalsIgnoreCase("")) {
                        if (event.getLocation().length() > 300) {
                            String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
                            spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), location, true);
                        } else {
                            spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), true);
                        }
                    }

                    if (!settings.usingSimpleAnnouncements())
                        spec.addField(Messages.getMessage("Embed.Announcement.Announce.EventID", settings), event.getId(), false);
                    if (!a.getInfo().equalsIgnoreCase("None") && !a.getInfo().equalsIgnoreCase(""))
                        spec.addField(Messages.getMessage("Embed.Announcement.Announce.Info", settings), a.getInfo(), false);
                }
            }));
    }

    @Deprecated
    public static Mono<Void> sendAnnouncementMessage(Announcement a, Event event, CalendarData data,
                                                     GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID()).cache();

        Mono<Consumer<EmbedCreateSpec>> embed = getRealAnnouncementEmbed(a, event, data, settings);
        Mono<String> mentions = guild.flatMap(g -> getSubscriberMentions(a, g));

        return Mono.zip(guild, embed, mentions)
            .map(TupleUtils.function((g, em, men) ->
                g.getChannelById(Snowflake.of(a.getAnnouncementChannelId()))
                    .ofType(TextChannel.class)
                    .onErrorResume(ClientException.class, e ->
                        Mono.just(e.getStatus())
                            .filter(HttpResponseStatus.NOT_FOUND::equals)
                            .flatMap(ignored -> DatabaseManager.deleteAnnouncement(a.getAnnouncementId().toString()))
                            .then(Mono.empty()))
                    .flatMap(chan -> Messages.sendMessage(men, em, chan))
            )).then();
    }

    public static Mono<Void> sendAnnouncementMessage(Guild guild, Announcement a, Event event, CalendarData data,
                                                     GuildSettings settings) {
        Mono<Consumer<EmbedCreateSpec>> embed = getRealAnnouncementEmbed(a, event, data, settings);
        Mono<String> mentions = getSubscriberMentions(a, guild);

        return Mono.zip(embed, mentions)
            .map(TupleUtils.function((em, men) ->
                guild.getChannelById(Snowflake.of(a.getAnnouncementChannelId()))
                    .ofType(TextChannel.class)
                    .onErrorResume(ClientException.class, e ->
                        Mono.just(e.getStatus())
                            .filter(HttpResponseStatus.NOT_FOUND::equals)
                            .flatMap(ignored -> DatabaseManager.deleteAnnouncement(a.getAnnouncementId().toString()))
                            .then(Mono.empty()))
                    .flatMap(chan -> Messages.sendMessage(men, em, chan))
            )).then();
    }

    public static Mono<Void> sendAnnouncementDM(Announcement a, Event event, User user, CalendarData data,
                                                GuildSettings settings) {
        return DisCalClient.getClient().getGuildById(settings.getGuildID())
            .map(g -> Messages.getMessage("Embed.Announcement.Announce.Dm.Message", "%guild%", g.getName(), settings))
            .flatMap(msg -> getRealAnnouncementEmbed(a, event, data, settings)
                .flatMap(em -> Messages.sendDirectMessage(msg, em, user))
            ).then();
    }

    private static String condensedTime(Announcement a) {
        return a.getHoursBefore() + "H" + a.getMinutesBefore() + "m";
    }

    public static Mono<String> getSubscriberNames(Announcement a, Guild guild) {
        return Mono.just(new StringBuilder()).flatMap(mentions -> {
            mentions.append("Subscribers: ");

            //User mentions...
            Mono<Void> userMentions = Flux.fromIterable(a.getSubscriberUserIds())
                .flatMap(s -> guild.getMemberById(Snowflake.of(s)))
                .doOnNext(m -> mentions.append(m.getDisplayName()).append(" "))
                .then();

            //Role and everyone/here mentions
            Mono<Void> roleMentions = Flux.fromIterable(a.getSubscriberRoleIds())
                .flatMap(s -> {
                    if (s.equalsIgnoreCase("everyone")) {
                        mentions.append("everyone").append(" ");
                        return Mono.empty();
                    } else if (s.equalsIgnoreCase("here")) {
                        mentions.append("here").append(" ");
                        return Mono.empty();
                    } else {
                        return guild.getRoleById(Snowflake.of(s))
                            .doOnNext(r -> mentions.append(r.getName()).append(" "));
                    }
                }).then();

            //Return the mentions string once fully built...
            return userMentions.then(roleMentions)
                .thenReturn(mentions.toString().replaceAll("@", "@\\u200B"))
                .subscribeOn(Schedulers.single());
        });
    }

    private static Mono<String> getSubscriberMentions(Announcement a, Guild guild) {
        return Mono.just(new StringBuilder()).flatMap(mentions -> {
            mentions.append("Subscribers: ");

            //User mentions...
            Mono<Void> userMentions = Flux.fromIterable(a.getSubscriberUserIds())
                .flatMap(s -> guild.getMemberById(Snowflake.of(s)))
                .doOnNext(m -> mentions.append(m.getMention()).append(" "))
                .then();

            //Role and everyone/here mentions
            Mono<Void> roleMentions = Flux.fromIterable(a.getSubscriberRoleIds())
                .flatMap(s -> {
                    if (s.equalsIgnoreCase("everyone")) {
                        mentions.append("@everyone").append(" ");
                        return Mono.empty();
                    } else if (s.equalsIgnoreCase("here")) {
                        mentions.append("@here").append(" ");
                        return Mono.empty();
                    } else {
                        return guild.getRoleById(Snowflake.of(s))
                            .doOnNext(r -> mentions.append(r.getMention()).append(" "));
                    }
                }).then();

            //Return the mentions string once fully built...
            return userMentions.then(roleMentions)
                .thenReturn(mentions.toString())
                .subscribeOn(Schedulers.single());
        });
    }
}