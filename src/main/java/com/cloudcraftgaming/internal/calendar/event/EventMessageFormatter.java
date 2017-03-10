package com.cloudcraftgaming.internal.calendar.event;

import com.cloudcraftgaming.Main;
import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.internal.data.BotData;
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
    private static String lineBreak = System.getProperty("line.separator");

    public static EmbedObject getEventEmbed(Event event, String guildID) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("Event Info");
        em.appendField("Event Name/Summery", event.getSummary(), true);
        em.appendField("Event Description", event.getDescription(), true);
        em.appendField("Event Start Date", EventMessageFormatter.getHumanReadableDate(event), true);
        em.appendField("Event Start Time", EventMessageFormatter.getHumanReadableTime(event, true), true);
        em.appendField("Event End Date", EventMessageFormatter.getHumanReadableDate(event), true);
        em.appendField("Event End Time", EventMessageFormatter.getHumanReadableTime(event, false), true);
        try {
            em.appendField("TimeZone", event.getStart().getTimeZone(), true);
        } catch (IllegalArgumentException e) {
            try {
                BotData data = DatabaseManager.getManager().getData(guildID);
                Calendar service = CalendarAuth.getCalendarService();
                String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
                em.appendField("TimeZone", tz, true);
            } catch (Exception e1) {
                em.appendField("TimeZone", "Error/Unknown", true);
            }
        }
        em.withUrl(event.getHtmlLink());
        em.withFooterText("Event ID: " + event.getId());
        em.withColor(36, 153, 153);

        return em.build();
    }

    public static EmbedObject getShortenedEventEmbed(Event event) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("Shortened Event Info");
        em.appendField("Event Name/Summery", event.getSummary(), true);
        em.appendField("Event Description", event.getDescription(), true);
        em.withUrl(event.getHtmlLink());
        em.withFooterText("Event ID: " + event.getId());
        em.withColor(36, 153, 153);

        return em.build();
    }

    public static String getFormatEventMessage(PreEvent event) {
        return "~-~-~- Event Info ~-~-~-" + lineBreak
                + "Event ID: null until creation completed" + lineBreak + lineBreak
                + "Summary: " + event.getSummary() + lineBreak + lineBreak
                + "Description: " + event.getDescription() + lineBreak + lineBreak
                + "[REQ] Start Date (yyyy/MM/dd): " + getHumanReadableDate(event.getViewableStartDate()) + lineBreak
                + "[REQ] Start Time (HH:mm): " + getHumanReadableTime(event.getViewableStartDate()) + lineBreak
                + "[REQ] End Date (yyyy/MM/dd): " + getHumanReadableDate(event.getViewableEndDate()) + lineBreak
                + "[REQ] End Time (HH:mm): " + getHumanReadableTime(event.getViewableEndDate()) + lineBreak
                + "TimeZone: " + event.getTimeZone();
    }

    private static String getHumanReadableDate(Event event) {
        String[] dateArray = event.getStart().getDateTime().toStringRfc3339().split("-");
        String year = dateArray[0];
        String month = dateArray[1];
        String day = dateArray[2].substring(0, 2);

        return year + "/" + month + "/" + day;
    }

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

    private static String getHumanReadableTime(Event event, boolean start) {
        if (start) {
            String[] timeArray = event.getStart().getDateTime().toStringRfc3339().split(":");
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
            String[] timeArray = event.getEnd().getDateTime().toStringRfc3339().split(":");
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