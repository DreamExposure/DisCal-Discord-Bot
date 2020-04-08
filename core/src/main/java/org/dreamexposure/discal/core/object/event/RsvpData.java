package org.dreamexposure.discal.core.object.event;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import discord4j.core.object.util.Snowflake;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("Duplicates")
public class RsvpData {
	private final Snowflake guildId;

	private String eventId;
	private long eventEnd;

	private final ArrayList<String> goingOnTime = new ArrayList<>();
	private final ArrayList<String> goingLate = new ArrayList<>();
	private final ArrayList<String> notGoing = new ArrayList<>();
	private final ArrayList<String> undecided = new ArrayList<>();

	public RsvpData(Snowflake guildId, String eventId) {
		this.guildId = guildId;
		this.eventId = eventId;
	}

	//Getters
	public Snowflake getGuildId() {
		return guildId;
	}

	public String getEventId() {
		return eventId;
	}

	public long getEventEnd() {
		return eventEnd;
	}

	public ArrayList<String> getGoingOnTime() {
		return goingOnTime;
	}

	public ArrayList<String> getGoingLate() {
		return goingLate;
	}

	public ArrayList<String> getNotGoing() {
		return notGoing;
	}

	public ArrayList<String> getUndecided() {
		return undecided;
	}

	public String getGoingOnTimeString() {
		StringBuilder goingString = new StringBuilder();
		int i = 0;
		for (String u: goingOnTime) {
			if (i == 0) {
				goingString = new StringBuilder(u);
			} else {
				goingString.append(",").append(u);
			}
			i++;
		}
		return goingString.toString();
	}

	public String getGoingLateString() {
		StringBuilder goingString = new StringBuilder();
		int i = 0;
		for (String u: goingLate) {
			if (i == 0) {
				goingString = new StringBuilder(u);
			} else {
				goingString.append(",").append(u);
			}
			i++;
		}
		return goingString.toString();
	}

	public String getNotGoingString() {
		StringBuilder going = new StringBuilder();
		int i = 0;
		for (String u: notGoing) {
			if (i == 0) {
				going = new StringBuilder(u);
			} else {
				going.append(",").append(u);
			}
			i++;
		}
		return going.toString();
	}

	public String getUndecidedString() {
		StringBuilder going = new StringBuilder();
		int i = 0;
		for (String u: undecided) {
			if (i == 0) {
				going = new StringBuilder(u);
			} else {
				going.append(",").append(u);
			}
			i++;
		}
		return going.toString();
	}

	//Setters
	public void setEventId(String _eventId) {
		eventId = _eventId;
	}

	public void setEventEnd(long _eventEnd) {
		eventEnd = _eventEnd;
	}

	public void setGoingOnTimeFromString(String goingList) {
		if (goingList != null) {
			String[] subs = goingList.split(",");
			Collections.addAll(goingOnTime, subs);
		}
	}

	public void setGoingLateFromString(String goingList) {
		if (goingList != null) {
			String[] subs = goingList.split(",");
			Collections.addAll(goingLate, subs);
		}
	}

	public void setNotGoingFromString(String goingList) {
		if (goingList != null) {
			String[] subs = goingList.split(",");
			Collections.addAll(notGoing, subs);
		}
	}

	public void setUndecidedFromString(String goingList) {
		if (goingList != null) {
			String[] subs = goingList.split(",");
			Collections.addAll(undecided, subs);
		}
	}

	//Functions
	public void removeCompletely(String userId) {
		goingOnTime.remove(userId);
		goingLate.remove(userId);
		notGoing.remove(userId);
		undecided.remove(userId);
	}

	//Boolean/Checkers
	public boolean shouldBeSaved() {
		return goingOnTime.size() > 0 || goingLate.size() > 0 || notGoing.size() > 0 || undecided.size() > 0;
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();

		json.put("guild_id", guildId.asString());
		json.put("event_id", eventId);
		json.put("event_end", eventEnd);

		JSONArray jOnTime = new JSONArray();
		for (String s : goingOnTime)
			jOnTime.put(s);
		json.put("on_time", jOnTime);

		JSONArray jLate = new JSONArray();
		for (String s : goingLate)
			jLate.put(s);
		json.put("late", jLate);

		JSONArray jNot = new JSONArray();
		for (String s : notGoing)
			jNot.put(s);
		json.put("not_going", jNot);

		JSONArray jUndecided = new JSONArray();
		for (String s : undecided)
			jUndecided.put(s);
		json.put("undecided", jUndecided);

		return json;
	}

	public RsvpData fromJson(JSONObject json) {
		eventId = json.getString("event_id");
		eventEnd = json.getLong("event_end");

		JSONArray jOnTime = json.getJSONArray("on_time");
		for (int i = 0; i < jOnTime.length(); i++)
			goingOnTime.add(jOnTime.getString(i));

		JSONArray jLate = json.getJSONArray("late");
		for (int i = 0; i < jLate.length(); i++)
			goingLate.add(jLate.getString(i));

		JSONArray jNot = json.getJSONArray("not_going");
		for (int i = 0; i < jNot.length(); i++)
			notGoing.add(jNot.getString(i));

		JSONArray jUndecided = json.getJSONArray("undecided");
		for (int i = 0; i < jUndecided.length(); i++)
			undecided.add(jUndecided.getString(i));

		return this;
	}
}