package com.cloudcraftgaming.discal.internal.calendar.event;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.utils.EventColor;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import javax.annotation.Nullable;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("Duplicates")
public class EventMessageFormatter {

    /**
     * Gets an EmbedObject for the specified event.
     * @param event The event involved.
     * @param guildID The ID of the guild.
     * @return The EmbedObject of the event.
     */
    public static EmbedObject getEventEmbed(Event event, String guildID) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("Event Info");
        if (event.getSummary() != null) {
            em.appendField("Event Name/Summary", event.getSummary(), true);
        }
        if (event.getDescription() != null) {
            em.appendField("Event Description", event.getDescription(), true);
        }
        em.appendField("Event Start Date", getHumanReadableDate(event.getStart()), true);
        em.appendField("Event Start Time", getHumanReadableTime(event.getStart()), true);
        em.appendField("Event End Date", getHumanReadableDate(event.getEnd()), true);

        try {
            //TODO: add support for multiple calendars...
            CalendarData data = DatabaseManager.getManager().getMainCalendar(guildID);
            Calendar service = CalendarAuth.getCalendarService();
            String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
            em.appendField("TimeZone", tz, true);
        } catch (Exception e1) {
            em.appendField("TimeZone", "Error/Unknown", true);
        }
        //TODO: Add info on recurrence here.
        em.withUrl(event.getHtmlLink());
        em.withFooterText("Event ID: " + event.getId());
        try {
            EventColor ec = EventColor.fromId(Integer.valueOf(event.getColorId()));
            em.withColor(ec.getR(), ec.getG(), ec.getB());
        } catch (Exception e) {
            //Color is null, ignore and add our default.
            em.withColor(56, 138, 237);
        }

        return em.build();
    }

    /**
     * Gets an EmbedObject for the specified event.
     * @param event The event involved.
     * @return The EmbedObject of the event.
     */
    public static EmbedObject getCondensedEventEmbed(Event event) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("Condensed Event Info");
        if (event.getSummary() != null) {
            em.appendField("Event Summary", event.getSummary(), true);
        }
        em.appendField("Event Date", getHumanReadableDate(event), true);
        em.appendField("Event ID", event.getId(), true);
        em.withUrl(event.getHtmlLink());
        try {
            EventColor ec = EventColor.fromId(Integer.valueOf(event.getColorId()));
            em.withColor(ec.getR(), ec.getG(), ec.getB());
        } catch (Exception e) {
            //Color is null, ignore and add our default.
            em.withColor(56, 138, 237);
        }

        return em.build();
    }

    /**
     * Gets an EmbedObject for the specified PreEvent.
     * @param event The PreEvent to get an embed for.
     * @return The EmbedObject of the PreEvent.
     */
    public static EmbedObject getPreEventEmbed(PreEvent event) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("Pre-Event Info");
        if (event.isEditing()) {
            em.appendField("Event ID", event.getEventId(), true);
        }
        if (event.getSummary() != null) {
            em.appendField("Event Name/Summary", event.getSummary(), true);
        }
        if (event.getDescription() != null) {
            em.appendField("Event Description", event.getDescription(), true);
        }
        if (event.shouldRecur()) {
            em.appendField("Recurrence", event.getRecurrence().toHumanReadable(), true);
        } else {
            em.appendField("Recurrence", "None or N/a", true);
        }
        em.appendField("[R] Event Start Date", getHumanReadableDate(event.getViewableStartDate()), true);
        em.appendField("[R] Event Start Time", EventMessageFormatter.getHumanReadableTime(event.getViewableStartDate()), true);
        em.appendField("[R] Event End Date", getHumanReadableDate(event.getViewableEndDate()), true);
        em.appendField("[R] Event End Time", EventMessageFormatter.getHumanReadableTime(event.getViewableEndDate()), true);
        em.appendField("TimeZone", event.getTimeZone(), true);
        //TODO: Add info on recurrence here.

        em.withFooterText("[R] means required, field needs a value.");
        EventColor ec = event.getColor();
        em.withColor(ec.getR(), ec.getG(), ec.getB());

        return em.build();
    }

    /**
     * Gets an EmbedObject for the specified CreatorResponse.
     * @param ecr The CreatorResponse involved.
     * @return The EmbedObject for the CreatorResponse.
     */
    public static EmbedObject getEventConfirmationEmbed(EventCreatorResponse ecr) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("Event Confirmation");
        em.appendField("Event ID", ecr.getEvent().getId(), false);
        em.appendField("Event Date", getHumanReadableDate(ecr.getEvent()), false);
        em.withFooterText("Click title to view on Google Calendar!");
        em.withUrl(ecr.getEvent().getHtmlLink());
        try {
            EventColor ec = EventColor.fromId(Integer.valueOf(ecr.getEvent().getColorId()));
            em.withColor(ec.getR(), ec.getG(), ec.getB());
        } catch (Exception e) {
            //Color is null, ignore and add our default.
            em.withColor(56, 138, 237);
        }

        return em.build();
    }

    /**
     * Gets a formatted date from the event.
     * @param event The event to get the date from.
     * @return A formatted date from the event.
     */
    private static String getHumanReadableDate(Event event) {
        String[] dateArray = event.getStart().getDateTime().toStringRfc3339().split("-");
        String year = dateArray[0];
        String month = dateArray[1];
        String day = dateArray[2].substring(0, 2);

        return year + "/" + month + "/" + day;
    }

    /**
     *  Gets a formatted date.
     * @param eventDateTime The object to get the date from.
     * @return A formatted date.
     */
    public static String getHumanReadableDate(@Nullable EventDateTime eventDateTime) {
        if (eventDateTime == null) {
            return "Not Set";
        } else {
            if (eventDateTime.getDateTime() != null) {
                String[] dateArray = eventDateTime.getDateTime().toStringRfc3339().split("-");
                String year = dateArray[0];
                String month = dateArray[1];
                String day = dateArray[2].substring(0, 2);

                return year + "/" + month + "/" + day;
            } else {
                String[] dateArray = eventDateTime.getDate().toStringRfc3339().split("-");
                String year = dateArray[0];
                String month = dateArray[1];
                String day = dateArray[2].substring(0, 2);

                return year + "/" + month + "/" + day;
            }
        }
    }

    /**
     * Gets a formatted time.
     * @param eventDateTime The object to get the time from.
     * @return A formatted time.
     */
    public static String getHumanReadableTime(@Nullable EventDateTime eventDateTime) {
        if (eventDateTime == null) {
            return "Not Set";
        } else {
            if (eventDateTime.getDateTime() != null) {
                String[] timeArray = eventDateTime.getDateTime().toStringRfc3339().split(":");
                String suffix = "";
                String hour = timeArray[0].substring(11, 13);

                //Convert hour from 24 to 12...
                try {
                    Integer hRaw = Integer.valueOf(hour);
                    if (hRaw > 12) {
                        hour = String.valueOf(hRaw - 12);
                        suffix = "PM";
                    } else {
                        suffix = "AM";
                    }
                } catch (NumberFormatException e) {
                    //I Dunno... just should catch the error now and not crash anything...
                }

                String minute = timeArray[1];

                return hour + ":" + minute + suffix;
            } else {
                String[] timeArray = eventDateTime.getDate().toStringRfc3339().split(":");
                String suffix = "";
                String hour = timeArray[0].substring(11, 13);

                //Convert hour from 24 to 12...
                try {
                    Integer hRaw = Integer.valueOf(hour);
                    if (hRaw > 12) {
                        hour = String.valueOf(hRaw - 12);
                        suffix = "PM";
                    } else {
                        suffix = "AM";
                    }
                } catch (NumberFormatException e) {
                    //I Dunno... just should catch the error now and not crash anything...
                }

                String minute = timeArray[1];

                return hour + ":" + minute + suffix;
            }
        }
    }
}