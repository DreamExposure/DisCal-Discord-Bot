package org.dreamexposure.discal.core.object.event;

import org.dreamexposure.discal.core.enums.event.EventFrequency;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.json.JSONObject;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class Recurrence {
    private EventFrequency frequency;
    private int interval;
    private int count;

    /**
     * Creates new recurrence rules and settings.
     */
    public Recurrence() {
        this.frequency = EventFrequency.DAILY;
        this.interval = 1;
        this.count = -1;
    }

    /**
     * The frequency of the event recurrence.
     *
     * @return The frequency of the the event recurrence.
     */
    public EventFrequency getFrequency() {
        return this.frequency;
    }

    /**
     * The interval at which it recurs.
     *
     * @return The interval at which it recurs.
     */
    public int getInterval() {
        return this.interval;
    }

    /**
     * The count or amount of events.
     *
     * @return The count of amount of events.
     */
    public int getCount() {
        return this.count;
    }

    /**
     * Converts settings to a Google Accepted RRule
     *
     * @return The RRule accepted by Google.
     */
    public String toRRule() {
        final String rrule = "RRULE:FREQ=" + this.frequency.name() + ";INTERVAL=" + this.interval;
        if (this.count < 1)
            return rrule; //Infinite
        else
            return rrule + ";COUNT=" + this.count;
    }

    /**
     * Converts settings to a human readable string.
     *
     * @return Human readable string of the settings.
     */
    public String toHumanReadable() {
        final String humanRead = "Frequency: " + this.frequency.name() + GlobalConst.lineBreak + "Interval: " + this.interval;
        if (this.count < 1)
            return humanRead + GlobalConst.lineBreak + "Amount: Infinite";
        else
            return humanRead + GlobalConst.lineBreak + "Amount: " + this.count;
    }

    /**
     * Sets the frequency of the event recurrence.
     *
     * @param _frequency The frequency of the event recurrence.
     */
    public void setFrequency(final EventFrequency _frequency) {
        this.frequency = _frequency;
    }

    /**
     * Sets the interval of the recurrence.
     *
     * @param _interval The interval of the recurrence.
     */
    public void setInterval(final Integer _interval) {
        this.interval = _interval;
    }

    /**
     * Sets the count or amount of events to create following the rules set.
     *
     * @param _count The amount of events to create.
     */
    public void setCount(final Integer _count) {
        this.count = _count;
    }

    /**
     * Converts a Google RRule to the accepted settings in this object.
     *
     * @param rrule The RRules to convert from.
     */
    public Recurrence fromRRule(String rrule) {
        rrule = rrule.replaceAll("RRULE:", "");
        final String[] contents = rrule.split(";");
        for (final String c : contents) {
            if (c.contains("FREQ=")) {
                final String freq = c.replaceAll("FREQ=", "");
                if (EventFrequency.isValid(freq))
                    this.frequency = EventFrequency.fromValue(freq);

            } else if (c.contains("INTERVAL=")) {
                final String inter = c.replaceAll("INTERVAL=", "");
                try {
                    this.interval = Integer.parseInt(inter);
                } catch (final NumberFormatException e) {
                    //Not valid number, safe to ignore error.
                    this.interval = 1;
                }
            } else if (c.contains("COUNT=")) {
                final String con = c.replaceAll("COUNT=", "");
                try {
                    this.count = Integer.parseInt(con);
                } catch (final NumberFormatException e) {
                    //Not valid number, can ignore.
                    this.count = -1;
                }
            }
        }
        return this;
    }

    public JSONObject toJson() {
        final JSONObject json = new JSONObject();

        json.put("frequency", this.frequency.getName());
        json.put("interval", this.interval);
        json.put("count", this.count);

        return json;
    }

    public Recurrence fromJson(final JSONObject json) {
        this.frequency = EventFrequency.fromValue(json.getString("frequency"));
        this.interval = json.getInt("interval");
        this.count = json.getInt("count");

        return this;
    }
}