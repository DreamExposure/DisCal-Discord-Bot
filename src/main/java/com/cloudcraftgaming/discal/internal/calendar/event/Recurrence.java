package com.cloudcraftgaming.discal.internal.calendar.event;

import com.cloudcraftgaming.discal.utils.Message;

/**
 * Created by Nova Fox on 3/10/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class Recurrence {
    private EventFrequency frequency;
    private int interval;
    private int count;

    /**
     * Creates new recurrence rules and settings.
     */
    public Recurrence() {
        frequency = EventFrequency.DAILY;
        interval = 1;
        count = -1;
    }

    /**
     * The frequency of the event recurrence.
     * @return The frequency of the the event recurrence.
     */
    public EventFrequency getFrequency() {
        return frequency;
    }

    /**
     * The interval at which it recurs.
     * @return The interval at which it recurs.
     */
    public int getInterval() {
        return interval;
    }

    /**
     * The count or amount of events.
     * @return The count of amount of events.
     */
    public int getCount() {
        return count;
    }

    /**
     * Converts settings to a Google Accepted RRule
     * @return The RRule accepted by Google.
     */
    public String toRRule() {
        String rrule = "RRULE:FREQ=" + frequency.name() + ";INTERVAL=" + interval;
        if (count < 1) {
            //Infinite
            return rrule;
        } else {
            return rrule + ";COUNT=" + count;
        }
    }

    /**
     * Converts settings to a human readable string.
     * @return Human readable string of the settings.
     */
    public String toHumanReadable() {
        String humanRead = "Frequency: " + frequency.name() + Message.lineBreak +
                "Interval: " + interval;
        if (count < 1) {
            return humanRead + Message.lineBreak + "Amount: Infinite";
        } else {
            return humanRead + Message.lineBreak + "Amount: " + count;
        }
    }

    /**
     * Sets the frequency of the event recurrence.
     * @param _frequency The frequency of the event recurrence.
     */
    public void setFrequency(EventFrequency _frequency) {
        frequency = _frequency;
    }

    /**
     * Sets the interval of the recurrence.
     * @param _interval The interval of the recurrence.
     */
    public void setInterval(Integer _interval) {
        interval = _interval;
    }

    /**
     * Sets the count or amount of events to create following the rules set.
     * @param _count The amount of events to create.
     */
    public void setCount(Integer _count) {
        count = _count;
    }

    /**
     * Converts a Google RRule to the accepted settings in this object.
     * @param rrule The RRules to convert from.
     */
    public void fromRRule(String rrule) {
        rrule = rrule.replaceAll("RRULE:", "");
        String[] contents = rrule.split(";");
        for (String c : contents) {
            if (c.contains("FREQ=")) {
                String freq = c.replaceAll("FREQ=", "");
                if (EventFrequency.isValid(freq)) {
                    frequency = EventFrequency.fromValue(freq);
                }
            } else if (c.contains("INTERVAL=")) {
                String inter = c.replaceAll("INTERVAL=", "");
                try {
                    interval = Integer.valueOf(inter);
                } catch (NumberFormatException e) {
                    //Not valid number, safe to ignore error.
                    interval = 1;
                }
            } else if (c.contains("COUNT=")) {
                String con = c.replaceAll("COUNT=", "");
                try {
                    count = Integer.valueOf(con);
                } catch (NumberFormatException e) {
                    //Not valid number, can ignore.
                    count = -1;
                }
            }
        }
    }
}