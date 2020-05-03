package org.dreamexposure.discal.client.message;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.event.EventCreatorResponse;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.object.event.PreEvent;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.ImageUtils;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import discord4j.core.object.entity.Guild;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EventMessageFormatter {
    @Deprecated
    public static Mono<Consumer<EmbedCreateSpec>> getEventEmbed(Event event, GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());
        Mono<EventData> data = DatabaseManager
            .getEventData(settings.getGuildID(), event.getId())
            .defaultIfEmpty(EventData.empty()).cache();
        Mono<String> sDate = getHumanReadableDate(event.getStart(), false, settings);
        Mono<String> sTime = getHumanReadableTime(event.getStart(), false, settings);
        Mono<String> eDate = getHumanReadableDate(event.getEnd(), false, settings);
        Mono<String> eTime = getHumanReadableTime(event.getEnd(), false, settings);
        Mono<Boolean> img = data.filter(EventData::shouldBeSaved)
            .flatMap(d -> ImageUtils.validate(d.getImageLink(), settings.isPatronGuild()))
            .defaultIfEmpty(false);
        Mono<String> timezone = DatabaseManager.getMainCalendar(settings.getGuildID())
            .flatMap(d -> CalendarWrapper.getCalendar(d, settings))
            .map(com.google.api.services.calendar.model.Calendar::getTimeZone)
            .defaultIfEmpty("Error/Unknown");

        return Mono.zip(guild, data, sDate, sTime, eDate, eTime, img, timezone)
            .map(TupleUtils.function((g, ed, startDate, startTime, endDate, endTime, hasImg, tz) -> spec -> {
                if (settings.isBranded())
                    spec.setAuthor(g.getName(), GlobalConst.discalSite, g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                else
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                spec.setTitle(Messages.getMessage("Embed.Event.Info.Title", settings));
                if (hasImg) {
                    spec.setImage(ed.getImageLink());
                }
                if (event.getSummary() != null) {
                    String summary = event.getSummary();
                    if (summary.length() > 250) {
                        summary = summary.substring(0, 250);
                        summary = summary + " (continues on Google Calendar View)";
                    }
                    spec.addField(Messages.getMessage("Embed.Event.Info.Summary", settings), summary, true);
                }
                if (event.getDescription() != null) {
                    String description = event.getDescription();
                    if (description.length() > 500) {
                        description = description.substring(0, 500);
                        description = description + " (continues on Google Calendar View)";
                    }
                    spec.addField(Messages.getMessage("Embed.Event.Info.Description", settings), description, true);
                }
                spec.addField(Messages.getMessage("Embed.Event.Info.StartDate", settings), startDate, true);
                spec.addField(Messages.getMessage("Embed.Event.Info.StartTime", settings), startTime, true);
                spec.addField(Messages.getMessage("Embed.Event.Info.EndDate", settings), endDate, true);
                spec.addField(Messages.getMessage("Embed.Event.Info.EndTime", settings), endTime, true);

                spec.addField(Messages.getMessage("Embed.Event.Info.TimeZone", settings), tz, true);
                if (event.getLocation() != null && !event.getLocation().equalsIgnoreCase("")) {
                    if (event.getLocation().length() > 300) {
                        String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), location, true);
                    } else {
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), true);
                    }
                }
                //TODO: Add info on recurrence here.
                spec.setUrl(event.getHtmlLink());
                spec.setFooter(Messages.getMessage("Embed.Event.Info.ID", "%id%", event.getId(), settings), null);

                if (event.getColorId() != null) {
                    EventColor ec = EventColor.fromId(Integer.valueOf(event.getColorId()));
                    spec.setColor(ec.asColor());
                } else {
                    spec.setColor(GlobalConst.discalColor);
                }
            }));
    }

    public static Mono<Consumer<EmbedCreateSpec>> getEventEmbed(Event event, int calNum, GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());
        Mono<EventData> data = DatabaseManager
            .getEventData(settings.getGuildID(), event.getId())
            .defaultIfEmpty(EventData.empty()).cache();
        Mono<String> sDate = getHumanReadableDate(event.getStart(), calNum, false, settings);
        Mono<String> sTime = getHumanReadableTime(event.getStart(), calNum, false, settings);
        Mono<String> eDate = getHumanReadableDate(event.getEnd(), calNum, false, settings);
        Mono<String> eTime = getHumanReadableTime(event.getEnd(), calNum, false, settings);
        Mono<Boolean> img = data.filter(EventData::shouldBeSaved)
            .flatMap(d -> ImageUtils.validate(d.getImageLink(), settings.isPatronGuild()))
            .defaultIfEmpty(false);
        Mono<String> timezone = DatabaseManager.getCalendar(settings.getGuildID(), calNum)
            .flatMap(d -> CalendarWrapper.getCalendar(d, settings))
            .map(com.google.api.services.calendar.model.Calendar::getTimeZone)
            .defaultIfEmpty("Error/Unknown");

        return Mono.zip(guild, data, sDate, sTime, eDate, eTime, img, timezone)
            .map(TupleUtils.function((g, ed, startDate, startTime, endDate, endTime, hasImg, tz) -> spec -> {
                if (settings.isBranded())
                    spec.setAuthor(g.getName(), GlobalConst.discalSite, g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                else
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                spec.setTitle(Messages.getMessage("Embed.Event.Info.Title", settings));
                if (hasImg) {
                    spec.setImage(ed.getImageLink());
                }
                if (event.getSummary() != null) {
                    String summary = event.getSummary();
                    if (summary.length() > 250) {
                        summary = summary.substring(0, 250);
                        summary = summary + " (continues on Google Calendar View)";
                    }
                    spec.addField(Messages.getMessage("Embed.Event.Info.Summary", settings), summary, true);
                }
                if (event.getDescription() != null) {
                    String description = event.getDescription();
                    if (description.length() > 500) {
                        description = description.substring(0, 500);
                        description = description + " (continues on Google Calendar View)";
                    }
                    spec.addField(Messages.getMessage("Embed.Event.Info.Description", settings), description, true);
                }
                spec.addField(Messages.getMessage("Embed.Event.Info.StartDate", settings), startDate, true);
                spec.addField(Messages.getMessage("Embed.Event.Info.StartTime", settings), startTime, true);
                spec.addField(Messages.getMessage("Embed.Event.Info.EndDate", settings), endDate, true);
                spec.addField(Messages.getMessage("Embed.Event.Info.EndTime", settings), endTime, true);

                spec.addField(Messages.getMessage("Embed.Event.Info.TimeZone", settings), tz, true);
                if (event.getLocation() != null && !event.getLocation().equalsIgnoreCase("")) {
                    if (event.getLocation().length() > 300) {
                        String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), location, true);
                    } else {
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), true);
                    }
                }
                //TODO: Add info on recurrence here.
                spec.setUrl(event.getHtmlLink());
                spec.setFooter(Messages.getMessage("Embed.Event.Info.ID", "%id%", event.getId(), settings), null);

                if (event.getColorId() != null) {
                    EventColor ec = EventColor.fromId(Integer.valueOf(event.getColorId()));
                    spec.setColor(ec.asColor());
                } else {
                    spec.setColor(GlobalConst.discalColor);
                }
            }));
    }

    @Deprecated
    public static Mono<Consumer<EmbedCreateSpec>> getCondensedEventEmbed(Event event, GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());
        Mono<EventData> data = DatabaseManager
            .getEventData(settings.getGuildID(), event.getId())
            .defaultIfEmpty(EventData.empty()).cache();
        Mono<String> date = getHumanReadableDate(event.getStart(), false, settings);
        Mono<Boolean> img = data.filter(EventData::shouldBeSaved)
            .flatMap(d -> ImageUtils.validate(d.getImageLink(), settings.isPatronGuild()))
            .defaultIfEmpty(false);

        return Mono.zip(guild, data, date, img)
            .map(TupleUtils.function((g, ed, start, hasImg) -> spec -> {

                if (settings.isBranded())
                    spec.setAuthor(g.getName(), GlobalConst.discalSite, g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                else
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                spec.setTitle(Messages.getMessage("Embed.Event.Condensed.Title", settings));
                if (hasImg)
                    spec.setThumbnail(ed.getImageLink());

                if (event.getSummary() != null) {
                    String summary = event.getSummary();
                    if (summary.length() > 250) {
                        summary = summary.substring(0, 250);
                        summary = summary + " (continues on Google Calendar View)";
                    }
                    spec.addField(Messages.getMessage("Embed.Event.Condensed.Summary", settings), summary, true);
                }
                spec.addField(Messages.getMessage("Embed.Event.Condensed.Date", settings), start, true);
                if (event.getLocation() != null && !event.getLocation().equalsIgnoreCase("")) {
                    if (event.getLocation().length() > 300) {
                        String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), location, true);
                    } else {
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), true);
                    }
                }
                spec.addField(Messages.getMessage("Embed.Event.Condensed.ID", settings), event.getId(), false);
                spec.setUrl(event.getHtmlLink());

                if (event.getColorId() != null) {
                    EventColor ec = EventColor.fromId(Integer.valueOf(event.getColorId()));
                    spec.setColor(ec.asColor());
                } else {
                    spec.setColor(GlobalConst.discalColor);
                }
            }));
    }

    public static Mono<Consumer<EmbedCreateSpec>> getCondensedEventEmbed(Event event, int calNum, GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());
        Mono<EventData> data = DatabaseManager
            .getEventData(settings.getGuildID(), event.getId())
            .defaultIfEmpty(EventData.empty()).cache();
        Mono<String> date = getHumanReadableDate(event.getStart(), calNum, false, settings);
        Mono<Boolean> img = data.filter(EventData::shouldBeSaved)
            .flatMap(d -> ImageUtils.validate(d.getImageLink(), settings.isPatronGuild()))
            .defaultIfEmpty(false);

        return Mono.zip(guild, data, date, img)
            .map(TupleUtils.function((g, ed, start, hasImg) -> spec -> {

                if (settings.isBranded())
                    spec.setAuthor(g.getName(), GlobalConst.discalSite, g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                else
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                spec.setTitle(Messages.getMessage("Embed.Event.Condensed.Title", settings));
                if (hasImg)
                    spec.setThumbnail(ed.getImageLink());

                if (event.getSummary() != null) {
                    String summary = event.getSummary();
                    if (summary.length() > 250) {
                        summary = summary.substring(0, 250);
                        summary = summary + " (continues on Google Calendar View)";
                    }
                    spec.addField(Messages.getMessage("Embed.Event.Condensed.Summary", settings), summary, true);
                }
                spec.addField(Messages.getMessage("Embed.Event.Condensed.Date", settings), start, true);
                if (event.getLocation() != null && !event.getLocation().equalsIgnoreCase("")) {
                    if (event.getLocation().length() > 300) {
                        String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), location, true);
                    } else {
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), true);
                    }
                }
                spec.addField(Messages.getMessage("Embed.Event.Condensed.ID", settings), event.getId(), false);
                spec.setUrl(event.getHtmlLink());

                if (event.getColorId() != null) {
                    EventColor ec = EventColor.fromId(Integer.valueOf(event.getColorId()));
                    spec.setColor(ec.asColor());
                } else {
                    spec.setColor(GlobalConst.discalColor);
                }
            }));
    }

    public static Mono<Consumer<EmbedCreateSpec>> getPreEventEmbed(PreEvent event, int calNum,
                                                                   GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());
        Mono<String> sDate = getHumanReadableDate(event.getViewableStartDate(), calNum, false, settings);
        Mono<String> sTime = getHumanReadableTime(event.getViewableStartDate(), calNum, false, settings);
        Mono<String> eDate = getHumanReadableDate(event.getViewableEndDate(), calNum, false, settings);
        Mono<String> eTime = getHumanReadableTime(event.getViewableEndDate(), calNum, false, settings);
        Mono<Boolean> img = Mono.justOrEmpty(event.getEventData()).filter(EventData::shouldBeSaved)
            .flatMap(d -> ImageUtils.validate(d.getImageLink(), settings.isPatronGuild()))
            .defaultIfEmpty(false);

        return Mono.zip(guild, sDate, sTime, eDate, eTime, img)
            .map(TupleUtils.function((g, startDate, startTime, endDate, endTime, hasImg) -> spec -> {
                if (settings.isBranded())
                    spec.setAuthor(g.getName(), GlobalConst.discalSite, g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                else
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                if (hasImg)
                    spec.setImage(event.getEventData().getImageLink());
                if (event.isEditing())
                    spec.addField(Messages.getMessage("Embed.Event.Pre.Id", settings), event.getEventId(), false);

                if (event.getSummary() != null) {
                    String summary = event.getSummary();
                    if (summary.length() > 250) {
                        summary = summary.substring(0, 250);
                        summary = summary + " (continues on Google Calendar View)";
                    }
                    spec.addField(Messages.getMessage("Embed.Event.Pre.Summary", settings), summary, true);
                } else {
                    spec.addField(Messages.getMessage("Embed.Event.Pre.Summary", settings), "NOT SET", true);
                }
                if (event.getDescription() != null) {
                    String description = event.getDescription();
                    if (description.length() > 500) {
                        description = description.substring(0, 500);
                        description = description + " (continues on Google Calendar View)";
                    }
                    spec.addField(Messages.getMessage("Embed.Event.Pre.Description", settings), description, true);
                } else {
                    spec.addField(Messages.getMessage("Embed.Event.Pre.Description", settings), "NOT SET", true);
                }
                if (event.shouldRecur()) {
                    spec.addField(Messages.getMessage("Embed.Event.Pre.Recurrence", settings), event.getRecurrence().toHumanReadable(), false);
                } else {
                    spec.addField(Messages.getMessage("Embed.Event.Pre.Recurrence", settings), "N/a", true);
                }
                spec.addField(Messages.getMessage("Embed.Event.Pre.StartDate", settings), startDate, true);
                spec.addField(Messages.getMessage("Embed.Event.Pre.StartTime", settings), startTime, true);
                spec.addField(Messages.getMessage("Embed.Event.Pre.EndDate", settings), endDate, true);
                spec.addField(Messages.getMessage("Embed.Event.Pre.EndTime", settings), endTime, true);
                spec.addField(Messages.getMessage("Embed.Event.Pre.TimeZone", settings), event.getTimeZone(), true);

                if (event.getLocation() != null && !event.getLocation().equalsIgnoreCase("")) {
                    if (event.getLocation().length() > 300) {
                        String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), location, true);
                    } else {
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), true);
                    }
                } else {
                    spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), "N/a", true);
                }

                spec.setFooter(Messages.getMessage("Embed.Event.Pre.Key", settings), null);
                spec.setColor(event.getColor().asColor());
            }));
    }

    @Deprecated
    public static Mono<Consumer<EmbedCreateSpec>> getPreEventEmbed(PreEvent event,
                                                                   GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());
        Mono<String> sDate = getHumanReadableDate(event.getViewableStartDate(), false, settings);
        Mono<String> sTime = getHumanReadableTime(event.getViewableStartDate(), false, settings);
        Mono<String> eDate = getHumanReadableDate(event.getViewableEndDate(), false, settings);
        Mono<String> eTime = getHumanReadableTime(event.getViewableEndDate(), false, settings);
        Mono<Boolean> img = Mono.justOrEmpty(event.getEventData()).filter(EventData::shouldBeSaved)
            .flatMap(d -> ImageUtils.validate(d.getImageLink(), settings.isPatronGuild()))
            .defaultIfEmpty(false);

        return Mono.zip(guild, sDate, sTime, eDate, eTime, img)
            .map(TupleUtils.function((g, startDate, startTime, endDate, endTime, hasImg) -> spec -> {
                if (settings.isBranded())
                    spec.setAuthor(g.getName(), GlobalConst.discalSite, g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                else
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                if (hasImg)
                    spec.setImage(event.getEventData().getImageLink());
                if (event.isEditing())
                    spec.addField(Messages.getMessage("Embed.Event.Pre.Id", settings), event.getEventId(), false);

                if (event.getSummary() != null) {
                    String summary = event.getSummary();
                    if (summary.length() > 250) {
                        summary = summary.substring(0, 250);
                        summary = summary + " (continues on Google Calendar View)";
                    }
                    spec.addField(Messages.getMessage("Embed.Event.Pre.Summary", settings), summary, true);
                } else {
                    spec.addField(Messages.getMessage("Embed.Event.Pre.Summary", settings), "NOT SET", true);
                }
                if (event.getDescription() != null) {
                    String description = event.getDescription();
                    if (description.length() > 500) {
                        description = description.substring(0, 500);
                        description = description + " (continues on Google Calendar View)";
                    }
                    spec.addField(Messages.getMessage("Embed.Event.Pre.Description", settings), description, true);
                } else {
                    spec.addField(Messages.getMessage("Embed.Event.Pre.Description", settings), "NOT SET", true);
                }
                if (event.shouldRecur()) {
                    spec.addField(Messages.getMessage("Embed.Event.Pre.Recurrence", settings), event.getRecurrence().toHumanReadable(), false);
                } else {
                    spec.addField(Messages.getMessage("Embed.Event.Pre.Recurrence", settings), "N/a", true);
                }
                spec.addField(Messages.getMessage("Embed.Event.Pre.StartDate", settings), startDate, true);
                spec.addField(Messages.getMessage("Embed.Event.Pre.StartTime", settings), startTime, true);
                spec.addField(Messages.getMessage("Embed.Event.Pre.EndDate", settings), endDate, true);
                spec.addField(Messages.getMessage("Embed.Event.Pre.EndTime", settings), endTime, true);
                spec.addField(Messages.getMessage("Embed.Event.Pre.TimeZone", settings), event.getTimeZone(), true);

                if (event.getLocation() != null && !event.getLocation().equalsIgnoreCase("")) {
                    if (event.getLocation().length() > 300) {
                        String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), location, true);
                    } else {
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), true);
                    }
                } else {
                    spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), "N/a", true);
                }

                spec.setFooter(Messages.getMessage("Embed.Event.Pre.Key", settings), null);
                spec.setColor(event.getColor().asColor());
            }));
    }

    @Deprecated
    public static Mono<Consumer<EmbedCreateSpec>> getEventConfirmationEmbed(EventCreatorResponse ecr,
                                                                            GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());
        Mono<EventData> data = DatabaseManager
            .getEventData(settings.getGuildID(), ecr.getEvent().getId())
            .defaultIfEmpty(EventData.empty()).cache();
        Mono<String> date = getHumanReadableDate(ecr.getEvent().getStart(), false, settings);
        Mono<String> time = getHumanReadableTime(ecr.getEvent().getStart(), false, settings);
        Mono<Boolean> img = data.filter(EventData::shouldBeSaved)
            .flatMap(d -> ImageUtils.validate(d.getImageLink(), settings.isPatronGuild()))
            .defaultIfEmpty(false);

        return Mono.zip(guild, data, date, time, img)
            .map(TupleUtils.function((g, ed, d, t, hasImg) -> spec -> {
                if (settings.isBranded())
                    spec.setAuthor(g.getName(), GlobalConst.discalSite,
                        g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                else
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                spec.setTitle(Messages.getMessage("Embed.Event.Confirm.Title", settings));

                if (hasImg)
                    spec.setImage(ed.getImageLink());

                spec.addField(Messages.getMessage("Embed.Event.Confirm.ID", settings),
                    ecr.getEvent().getId(), false);
                spec.addField(Messages.getMessage("Embed.Event.Confirm.Date", settings),
                    d, false);
                if (ecr.getEvent().getLocation() != null && !ecr.getEvent().getLocation().equalsIgnoreCase("")) {
                    if (ecr.getEvent().getLocation().length() > 300) {
                        String location = ecr.getEvent().getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), location, true);
                    } else {
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), ecr.getEvent().getLocation(), true);
                    }
                }
                spec.setFooter(Messages.getMessage("Embed.Event.Confirm.Footer", settings), null);
                spec.setUrl(ecr.getEvent().getHtmlLink());

                if (ecr.getEvent().getColorId() != null) {
                    EventColor ec = EventColor.fromId(Integer.valueOf(ecr.getEvent().getColorId()));
                    spec.setColor(ec.asColor());
                } else {
                    spec.setColor(GlobalConst.discalColor);
                }
            }));
    }

    public static Mono<Consumer<EmbedCreateSpec>> getEventConfirmationEmbed(EventCreatorResponse ecr,
                                                                            int calNum,
                                                                            GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());
        Mono<EventData> data = DatabaseManager
            .getEventData(settings.getGuildID(), ecr.getEvent().getId())
            .defaultIfEmpty(EventData.empty()).cache();
        Mono<String> date = getHumanReadableDate(ecr.getEvent().getStart(), calNum, false, settings);
        Mono<String> time = getHumanReadableTime(ecr.getEvent().getStart(), calNum, false, settings);
        Mono<Boolean> img = data.filter(EventData::shouldBeSaved)
            .flatMap(d -> ImageUtils.validate(d.getImageLink(), settings.isPatronGuild()))
            .defaultIfEmpty(false);

        return Mono.zip(guild, data, date, time, img)
            .map(TupleUtils.function((g, ed, d, t, hasImg) -> spec -> {
                if (settings.isBranded())
                    spec.setAuthor(g.getName(), GlobalConst.discalSite,
                        g.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                else
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                spec.setTitle(Messages.getMessage("Embed.Event.Confirm.Title", settings));

                if (hasImg) {
                    spec.setImage(ed.getImageLink());
                }

                spec.addField(Messages.getMessage("Embed.Event.Confirm.ID", settings),
                    ecr.getEvent().getId(), false);
                spec.addField(Messages.getMessage("Embed.Event.Confirm.Date", settings),
                    d, false);
                if (ecr.getEvent().getLocation() != null && !ecr.getEvent().getLocation().equalsIgnoreCase("")) {
                    if (ecr.getEvent().getLocation().length() > 300) {
                        String location = ecr.getEvent().getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), location, true);
                    } else {
                        spec.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), ecr.getEvent().getLocation(), true);
                    }
                }
                spec.setFooter(Messages.getMessage("Embed.Event.Confirm.Footer", settings), null);
                spec.setUrl(ecr.getEvent().getHtmlLink());

                if (ecr.getEvent().getColorId() != null) {
                    EventColor ec = EventColor.fromId(Integer.valueOf(ecr.getEvent().getColorId()));
                    spec.setColor(ec.asColor());
                } else {
                    spec.setColor(GlobalConst.discalColor);
                }
            }));
    }

    @Deprecated
    public static Mono<String> getHumanReadableDate(@Nullable EventDateTime eventDateTime,
                                                    boolean preEvent, GuildSettings settings) {
        return Mono.justOrEmpty(eventDateTime).flatMap(dateTime -> {
                if (!preEvent) {
                    return DatabaseManager.getMainCalendar(settings.getGuildID())
                        .flatMap(data -> CalendarWrapper.getCalendar(data, settings))
                        .map(com.google.api.services.calendar.model.Calendar::getTimeZone);
                } else {
                    return Mono.just("UTC");
                }
            }
        ).map(ZoneId::of)
            .map(tz -> {
                DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                if (eventDateTime.getDateTime() != null) {
                    long dateTime = eventDateTime.getDateTime().getValue();
                    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), tz);

                    return format.format(ldt);
                } else {
                    long dateTime = eventDateTime.getDate().getValue();
                    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), tz);
                    return format.format(ldt);
                }
            }).defaultIfEmpty("NOT SET");
    }

    public static Mono<String> getHumanReadableDate(@Nullable EventDateTime eventDateTime,
                                                    int calNum, boolean preEvent,
                                                    GuildSettings settings) {
        return Mono.justOrEmpty(eventDateTime).flatMap(dateTime -> {
                if (!preEvent) {
                    return DatabaseManager.getCalendar(settings.getGuildID(), calNum)
                        .flatMap(data -> CalendarWrapper.getCalendar(data, settings))
                        .map(com.google.api.services.calendar.model.Calendar::getTimeZone);
                } else {
                    return Mono.just("UTC");
                }
            }
        ).map(ZoneId::of)
            .map(tz -> {
                DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                if (eventDateTime.getDateTime() != null) {
                    long dateTime = eventDateTime.getDateTime().getValue();
                    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), tz);

                    return format.format(ldt);
                } else {
                    long dateTime = eventDateTime.getDate().getValue();
                    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), tz);
                    return format.format(ldt);
                }
            }).defaultIfEmpty("NOT SET");
    }

    @Deprecated
    public static Mono<String> getHumanReadableTime(@Nullable EventDateTime eventDateTime,
                                                    boolean preEvent, GuildSettings settings) {
        return Mono.justOrEmpty(eventDateTime).flatMap(dateTime -> {
                if (!preEvent) {
                    return DatabaseManager.getMainCalendar(settings.getGuildID())
                        .flatMap(data -> CalendarWrapper.getCalendar(data, settings))
                        .map(com.google.api.services.calendar.model.Calendar::getTimeZone);
                } else {
                    return Mono.just("UTC");
                }
            }
        ).map(ZoneId::of)
            .map(tz -> {
                DateTimeFormatter format = DateTimeFormatter.ofPattern("hh:mm:ss a");
                if (eventDateTime.getDateTime() != null) {
                    long dateTime = eventDateTime.getDateTime().getValue();
                    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), tz);

                    return format.format(ldt);
                } else {
                    long dateTime = eventDateTime.getDate().getValue();
                    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), tz);
                    return format.format(ldt);
                }
            }).defaultIfEmpty("NOT SET");
    }

    public static Mono<String> getHumanReadableTime(@Nullable EventDateTime eventDateTime,
                                                    int calNum, boolean preEvent,
                                                    GuildSettings settings) {
        return Mono.justOrEmpty(eventDateTime).flatMap(dateTime -> {
                if (!preEvent) {
                    return DatabaseManager.getCalendar(settings.getGuildID(), calNum)
                        .flatMap(data -> CalendarWrapper.getCalendar(data, settings))
                        .map(com.google.api.services.calendar.model.Calendar::getTimeZone);
                } else {
                    return Mono.just("UTC");
                }
            }
        ).map(ZoneId::of)
            .map(tz -> {
                DateTimeFormatter format = DateTimeFormatter.ofPattern("hh:mm:ss a");
                if (eventDateTime.getDateTime() != null) {
                    long dateTime = eventDateTime.getDateTime().getValue();
                    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), tz);

                    return format.format(ldt);
                } else {
                    long dateTime = eventDateTime.getDate().getValue();
                    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), tz);
                    return format.format(ldt);
                }
            }).defaultIfEmpty("NOT SET");
    }
}