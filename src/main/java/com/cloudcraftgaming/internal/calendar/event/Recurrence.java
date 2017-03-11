package com.cloudcraftgaming.internal.calendar.event;

import com.cloudcraftgaming.utils.Message;

/**
 * Created by Nova Fox on 3/10/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class Recurrence {
    private EventFrequency frequency;
    private int interval;
    private int count;

    public Recurrence() {
        frequency = EventFrequency.DAILY;
        interval = 1;
        count = -1;
    }

    public EventFrequency getFrequency() {
        return frequency;
    }

    public int getInterval() {
        return interval;
    }

    public int getCount() {
        return count;
    }

    public String toRRule() {
        String rrule = "RRULE:FREQ=" + frequency.name() + ";INTERVAL=" + interval;
        if (count < 1) {
            //Infinite
            return rrule;
        } else {
            return rrule + ";COUNT=" + count;
        }
    }

    public String toHumanReadable() {
        String humanRead = "Frequency: " + frequency.name() + Message.lineBreak +
                "Interval: " + interval;
        if (count < 1) {
            return humanRead + Message.lineBreak + "Amount: Infinite";
        } else {
            return humanRead + Message.lineBreak + "Amount: " + count;
        }
    }

    public void setFrequency(EventFrequency _frequency) {
        frequency = _frequency;
    }

    public void setInterval(Integer _interval) {
        interval = _interval;
    }

    public void setCount(Integer _count) {
        count = _count;
    }

    public void fromRRule(String rrule) {
        rrule = rrule.replaceAll("RRULE", "");
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