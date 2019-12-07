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
		frequency = EventFrequency.DAILY;
		interval = 1;
		count = -1;
	}

	/**
	 * The frequency of the event recurrence.
	 *
	 * @return The frequency of the the event recurrence.
	 */
	public EventFrequency getFrequency() {
		return frequency;
	}

	/**
	 * The interval at which it recurs.
	 *
	 * @return The interval at which it recurs.
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * The count or amount of events.
	 *
	 * @return The count of amount of events.
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Converts settings to a Google Accepted RRule
	 *
	 * @return The RRule accepted by Google.
	 */
	public String toRRule() {
		String rrule = "RRULE:FREQ=" + frequency.name() + ";INTERVAL=" + interval;
		if (count < 1)
			return rrule; //Infinite
		else
			return rrule + ";COUNT=" + count;
	}

	/**
	 * Converts settings to a human readable string.
	 *
	 * @return Human readable string of the settings.
	 */
	public String toHumanReadable() {
		String humanRead = "Frequency: " + frequency.name() + GlobalConst.lineBreak + "Interval: " + interval;
		if (count < 1)
			return humanRead + GlobalConst.lineBreak + "Amount: Infinite";
		else
			return humanRead + GlobalConst.lineBreak + "Amount: " + count;
	}

	/**
	 * Sets the frequency of the event recurrence.
	 *
	 * @param _frequency The frequency of the event recurrence.
	 */
	public void setFrequency(EventFrequency _frequency) {
		frequency = _frequency;
	}

	/**
	 * Sets the interval of the recurrence.
	 *
	 * @param _interval The interval of the recurrence.
	 */
	public void setInterval(Integer _interval) {
		interval = _interval;
	}

	/**
	 * Sets the count or amount of events to create following the rules set.
	 *
	 * @param _count The amount of events to create.
	 */
	public void setCount(Integer _count) {
		count = _count;
	}

	/**
	 * Converts a Google RRule to the accepted settings in this object.
	 *
	 * @param rrule The RRules to convert from.
	 */
	public Recurrence fromRRule(String rrule) {
		rrule = rrule.replaceAll("RRULE:", "");
		String[] contents = rrule.split(";");
		for (String c: contents) {
			if (c.contains("FREQ=")) {
				String freq = c.replaceAll("FREQ=", "");
				if (EventFrequency.isValid(freq))
					frequency = EventFrequency.fromValue(freq);

			} else if (c.contains("INTERVAL=")) {
				String inter = c.replaceAll("INTERVAL=", "");
				try {
					interval = Integer.parseInt(inter);
				} catch (NumberFormatException e) {
					//Not valid number, safe to ignore error.
					interval = 1;
				}
			} else if (c.contains("COUNT=")) {
				String con = c.replaceAll("COUNT=", "");
				try {
					count = Integer.parseInt(con);
				} catch (NumberFormatException e) {
					//Not valid number, can ignore.
					count = -1;
				}
			}
		}
		return this;
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();

		json.put("frequency", frequency.getName());
		json.put("interval", interval);
		json.put("count", count);

		return json;
	}

	public Recurrence fromJson(JSONObject json) {
		frequency = EventFrequency.fromValue(json.getString("frequency"));
		interval = json.getInt("interval");
		count = json.getInt("count");

		return this;
	}
}