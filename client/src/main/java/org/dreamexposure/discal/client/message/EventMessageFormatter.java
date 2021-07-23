package org.dreamexposure.discal.client.message;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import discord4j.core.object.entity.Guild;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Image;
import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.event.EventCreatorResponse;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.object.event.PreEvent;
import org.dreamexposure.discal.core.utils.GlobalVal;
import org.dreamexposure.discal.core.utils.ImageUtils;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("MagicNumber")
public class EventMessageFormatter {
    public static Mono<EmbedCreateSpec> getEventEmbed(Event event, int calNum, GuildSettings settings) {
        final Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());
        Mono<EventData> data = DatabaseManager.INSTANCE.getEventData(settings.getGuildID(), event.getId())
            .defaultIfEmpty(new EventData())
            .cache();
        Mono<String> sDate = getHumanReadableDate(event.getStart(), calNum, false, settings);
        Mono<String> sTime = getHumanReadableTime(event.getStart(), calNum, false, settings);
        Mono<String> eDate = getHumanReadableDate(event.getEnd(), calNum, false, settings);
        Mono<String> eTime = getHumanReadableTime(event.getEnd(), calNum, false, settings);
        Mono<Boolean> img = data.filter(EventData::shouldBeSaved)
            .flatMap(d -> ImageUtils.validate(d.getImageLink(), settings.getPatronGuild()))
            .defaultIfEmpty(false);
        Mono<String> timezone = DatabaseManager.INSTANCE.getCalendar(settings.getGuildID(), calNum)
            .flatMap(CalendarWrapper::getCalendar)
            .map(com.google.api.services.calendar.model.Calendar::getTimeZone)
            .defaultIfEmpty("Error/Unknown");

        return Mono.zip(guild, data, sDate, sTime, eDate, eTime, img, timezone)
            .map(TupleUtils.function((g, ed, startDate, startTime, endDate, endTime, hasImg, tz) -> {
                var builder = EmbedCreateSpec.builder();

                if (settings.getBranded())
                    builder.author(g.getName(), BotSettings.BASE_URL.get(),
                        g.getIconUrl(Image.Format.PNG).orElse(GlobalVal.getIconUrl()));
                else
                    builder.author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl());

                builder.title(Messages.getMessage("Embed.Event.Info.Title", settings));
                if (hasImg) {
                    builder.image(ed.getImageLink());
                }
                if (event.getSummary() != null) {
                    String summary = event.getSummary();
                    if (summary.length() > 250) {
                        summary = summary.substring(0, 250);
                        summary = summary + " (continues on Google Calendar View)";
                    }
                    builder.addField(Messages.getMessage("Embed.Event.Info.Summary", settings), summary, false);
                }
                if (event.getDescription() != null) {
                    String description = event.getDescription();
                    if (description.length() > 500) {
                        description = description.substring(0, 500);
                        description = description + " (continues on Google Calendar View)";
                    }
                    builder.addField(Messages.getMessage("Embed.Event.Info.Description", settings), description, false);
                }


                //Start time
                builder.addField(Messages.getMessage("Embed.Event.Info.StartDate", settings), startDate, true);
                builder.addField(Messages.getMessage("Embed.Event.Info.StartTime", settings), startTime, true);

                //Timezone so that start/end times are split up cleanly on discord
                builder.addField(Messages.getMessage("Embed.Event.Info.TimeZone", settings), tz, false);

                //End time
                builder.addField(Messages.getMessage("Embed.Event.Info.EndDate", settings), endDate, true);
                builder.addField(Messages.getMessage("Embed.Event.Info.EndTime", settings), endTime, true);

                //Location handling
                if (event.getLocation() != null && !"".equalsIgnoreCase(event.getLocation())) {
                    if (event.getLocation().length() > 300) {
                        final String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
                        builder.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), location, false);
                    } else {
                        builder.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), false);
                    }
                }
                //TODO: Add info on recurrence here.
                builder.url(event.getHtmlLink());
                builder.footer(Messages.getMessage("Embed.Event.Info.ID", "%id%", event.getId(), settings), null);

                if (event.getColorId() != null) {
                    final EventColor ec = EventColor.Companion.fromId(Integer.parseInt(event.getColorId()));
                    builder.color(ec.asColor());
                } else {
                    builder.color(GlobalVal.getDiscalColor());
                }

                return builder.build();
            }));
    }

    public static Mono<EmbedCreateSpec> getCondensedEventEmbed(Event event, int calNum, GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());
        Mono<EventData> data = DatabaseManager.INSTANCE.getEventData(settings.getGuildID(), event.getId())
            .defaultIfEmpty(new EventData())
            .cache();
        Mono<String> date = getHumanReadableDate(event.getStart(), calNum, false, settings);
        Mono<String> time = getHumanReadableTime(event.getStart(), 1, false, settings);
        Mono<Boolean> img = data.filter(EventData::shouldBeSaved)
            .flatMap(d -> ImageUtils.validate(d.getImageLink(), settings.getPatronGuild()))
            .defaultIfEmpty(false);

        return Mono.zip(guild, data, date, time, img)
            .map(TupleUtils.function((g, ed, startData, startTime, hasImg) -> {
                var builder = EmbedCreateSpec.builder();

                if (settings.getBranded())
                    builder.author(g.getName(), BotSettings.BASE_URL.get(),
                        g.getIconUrl(Image.Format.PNG).orElse(GlobalVal.getIconUrl()));
                else
                    builder.author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl());

                builder.title(Messages.getMessage("Embed.Event.Condensed.Title", settings));
                if (hasImg)
                    builder.thumbnail(ed.getImageLink());

                if (event.getSummary() != null) {
                    String summary = event.getSummary();
                    if (summary.length() > 250) {
                        summary = summary.substring(0, 250);
                        summary = summary + " (continues on Google Calendar View)";
                    }
                    builder.addField(Messages.getMessage("Embed.Event.Condensed.Summary", settings), summary, false);
                }
                builder.addField(Messages.getMessage("Embed.Event.Condensed.Date", settings), startData, true);
                builder.addField(Messages.getMessage("Embed.Event.Condensed.Time", settings), startTime, true);
                if (event.getLocation() != null && !"".equalsIgnoreCase(event.getLocation())) {
                    if (event.getLocation().length() > 300) {
                        final String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
                        builder.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), location, false);
                    } else {
                        builder.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), false);
                    }
                }
                builder.addField(Messages.getMessage("Embed.Event.Condensed.ID", settings), event.getId(), false);
                builder.url(event.getHtmlLink());

                if (event.getColorId() != null) {
                    final EventColor ec = EventColor.Companion.fromId(Integer.parseInt(event.getColorId()));
                    builder.color(ec.asColor());
                } else {
                    builder.color(GlobalVal.getDiscalColor());
                }

                return builder.build();
            }));
    }

    public static Mono<EmbedCreateSpec> getPreEventEmbed(PreEvent event, GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());
        Mono<String> sDate = getHumanReadableDate(event.getStartDateTime(), event.getCalNumber(), false, settings);
        Mono<String> sTime = getHumanReadableTime(event.getStartDateTime(), event.getCalNumber(), false, settings);
        Mono<String> eDate = getHumanReadableDate(event.getEndDateTime(), event.getCalNumber(), false, settings);
        Mono<String> eTime = getHumanReadableTime(event.getEndDateTime(), event.getCalNumber(), false, settings);
        Mono<Boolean> img = Mono.justOrEmpty(event.getEventData()).filter(EventData::shouldBeSaved)
            .flatMap(d -> ImageUtils.validate(d.getImageLink(), settings.getPatronGuild()))
            .defaultIfEmpty(false);

        return Mono.zip(guild, sDate, sTime, eDate, eTime, img)
            .map(TupleUtils.function((g, startDate, startTime, endDate, endTime, hasImg) -> {
                var builder = EmbedCreateSpec.builder();

                if (settings.getBranded())
                    builder.author(g.getName(), BotSettings.BASE_URL.get(),
                        g.getIconUrl(Image.Format.PNG).orElse(GlobalVal.getIconUrl()));
                else
                    builder.author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl());

                if (hasImg)
                    builder.image(event.getEventData().getImageLink());
                if (event.getEditing())
                    builder.addField(Messages.getMessage("Embed.Event.Pre.Id", settings), event.getEventId(), false);

                if (event.getSummary() != null) {
                    String summary = event.getSummary();
                    if (summary.length() > 250) {
                        summary = summary.substring(0, 250);
                        summary = summary + " (continues on Google Calendar View)";
                    }
                    builder.addField(Messages.getMessage("Embed.Event.Pre.Summary", settings), summary, false);
                } else {
                    builder.addField(Messages.getMessage("Embed.Event.Pre.Summary", settings), "NOT SET", true);
                }
                if (event.getDescription() != null) {
                    String description = event.getDescription();
                    if (description.length() > 500) {
                        description = description.substring(0, 500);
                        description = description + " (continues on Google Calendar View)";
                    }
                    builder.addField(Messages.getMessage("Embed.Event.Pre.Description", settings), description, false);
                } else {
                    builder.addField(Messages.getMessage("Embed.Event.Pre.Description", settings), "NOT SET", true);
                }
                if (event.getRecur()) {
                    builder.addField(Messages.getMessage("Embed.Event.Pre.Recurrence", settings), event.getRecurrence().toHumanReadable(), false);
                } else {
                    builder.addField(Messages.getMessage("Embed.Event.Pre.Recurrence", settings), "N/a", true);
                }
                builder.addField(Messages.getMessage("Embed.Event.Pre.StartDate", settings), startDate, true);
                builder.addField(Messages.getMessage("Embed.Event.Pre.StartTime", settings), startTime, true);
                builder.addField(Messages.getMessage("Embed.Event.Pre.TimeZone", settings), event.getTimezone(), false);
                builder.addField(Messages.getMessage("Embed.Event.Pre.EndDate", settings), endDate, true);
                builder.addField(Messages.getMessage("Embed.Event.Pre.EndTime", settings), endTime, true);

                if (event.getLocation() != null && !"".equalsIgnoreCase(event.getLocation())) {
                    if (event.getLocation().length() > 300) {
                        final String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
                        builder.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), location, false);
                    } else {
                        builder.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), false);
                    }
                } else {
                    builder.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), "N/a", true);
                }

                builder.footer(Messages.getMessage("Embed.Event.Pre.Key", settings), null);
                builder.color(event.getColor().asColor());

                return builder.build();
            }));
    }

    public static Mono<EmbedCreateSpec> getEventConfirmationEmbed(EventCreatorResponse ecr, int calNum,
                                                                  GuildSettings settings) {
        Mono<Guild> guild = DisCalClient.getClient().getGuildById(settings.getGuildID());
        Mono<EventData> data = DatabaseManager.INSTANCE.getEventData(settings.getGuildID(), ecr.getEvent().getId())
            .defaultIfEmpty(new EventData())
            .cache();
        Mono<String> date = getHumanReadableDate(ecr.getEvent().getStart(), calNum, false, settings);
        Mono<String> time = getHumanReadableTime(ecr.getEvent().getStart(), calNum, false, settings);
        Mono<Boolean> img = data.filter(EventData::shouldBeSaved)
            .flatMap(d -> ImageUtils.validate(d.getImageLink(), settings.getPatronGuild()))
            .defaultIfEmpty(false);

        return Mono.zip(guild, data, date, time, img)
            .map(TupleUtils.function((g, ed, d, t, hasImg) -> {
                var builder = EmbedCreateSpec.builder();

                if (settings.getBranded())
                    builder.author(g.getName(), BotSettings.BASE_URL.get(),
                        g.getIconUrl(Image.Format.PNG).orElse(GlobalVal.getIconUrl()));
                else
                    builder.author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl());

                builder.title(Messages.getMessage("Embed.Event.Confirm.Title", settings));

                if (hasImg) builder.image(ed.getImageLink());

                builder.addField(Messages.getMessage("Embed.Event.Confirm.ID", settings), ecr.getEvent().getId(), false);
                builder.addField(Messages.getMessage("Embed.Event.Condensed.Date", settings), d, true);
                builder.addField(Messages.getMessage("Embed.Event.Condensed.Time", settings), t, true);

                if (ecr.getEvent().getLocation() != null && !"".equalsIgnoreCase(ecr.getEvent().getLocation())) {
                    if (ecr.getEvent().getLocation().length() > 300) {
                        final String location = ecr.getEvent().getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
                        builder.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings), location, false);
                    } else {
                        builder.addField(Messages.getMessage("Embed.Event.Confirm.Location", settings),
                            ecr.getEvent().getLocation(), false);
                    }
                }
                builder.footer(Messages.getMessage("Embed.Event.Confirm.Footer", settings), null);
                builder.url(ecr.getEvent().getHtmlLink());

                if (ecr.getEvent().getColorId() != null) {
                    final EventColor ec = EventColor.Companion.fromId(Integer.parseInt(ecr.getEvent().getColorId()));
                    builder.color(ec.asColor());
                } else {
                    builder.color(GlobalVal.getDiscalColor());
                }

                return builder.build();
            }));
    }

    public static Mono<String> getHumanReadableDate(@Nullable EventDateTime eventDateTime, int calNum,
                                                    boolean preEvent, GuildSettings settings) {
        return Mono.justOrEmpty(eventDateTime).flatMap(dateTime -> {
                if (!preEvent) {
                    return DatabaseManager.INSTANCE.getCalendar(settings.getGuildID(), calNum)
                        .flatMap(CalendarWrapper::getCalendar)
                        .map(com.google.api.services.calendar.model.Calendar::getTimeZone);
                } else {
                    return Mono.just("UTC");
                }
            }
        ).map(ZoneId::of).map(tz -> {
            final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                .withZone(tz);

            if (eventDateTime.getDateTime() != null) { //Date with time
                return format.format(Instant.ofEpochMilli(eventDateTime.getDateTime().getValue()));
            } else { //Date only
                Instant date = Instant.ofEpochMilli(eventDateTime.getDate().getValue())
                    .plus(1, ChronoUnit.DAYS);

                return format.format(date);
            }
        }).defaultIfEmpty("NOT SET");
    }

    public static Mono<String> getHumanReadableTime(@Nullable EventDateTime eventDateTime, int calNum,
                                                    boolean preEvent, GuildSettings settings) {
        return Mono.justOrEmpty(eventDateTime).flatMap(dateTime -> {
                if (!preEvent) {
                    return DatabaseManager.INSTANCE.getCalendar(settings.getGuildID(), calNum)
                        .flatMap(CalendarWrapper::getCalendar)
                        .map(com.google.api.services.calendar.model.Calendar::getTimeZone);
                } else {
                    return Mono.just("UTC");
                }
            }
        ).map(ZoneId::of).map(tz -> {
            DateTimeFormatter format;
            if (settings.getTwelveHour()) format = DateTimeFormatter.ofPattern("hh:mm:ss a").withZone(tz);
            else format = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(tz);

            if (eventDateTime.getDateTime() != null) { //Date with time
                return format.format(Instant.ofEpochMilli(eventDateTime.getDateTime().getValue()));
            } else { //Just date
                // I guess just return 0 essentially?
                Instant date = Instant.ofEpochMilli(eventDateTime.getDate().getValue())
                    .plus(1, ChronoUnit.DAYS);

                //Go to beginning of day
                return format.format(date.atZone(tz).truncatedTo(ChronoUnit.DAYS).toLocalDate().atStartOfDay());
            }
        }).defaultIfEmpty("NOT SET");
    }
}
