package com.cloudcraftgaming.discal.internal.data;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Nova Fox on 6/1/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class RsvpData {
	private final long guildId;

	private String eventId;
	private long eventEnd;

	private final ArrayList<String> rsvpGoing = new ArrayList<>();
	private final ArrayList<String> rsvpNotGoing = new ArrayList<>();
	private final ArrayList<String> rsvpUndecided = new ArrayList<>();

	public RsvpData(long _guildId) {
		guildId = _guildId;
	}

	//Getters
	public long getGuildId() {
		return guildId;
	}

	public String getEventId() {
		return eventId;
	}

	public long getEventEnd() {
		return eventEnd;
	}

	public ArrayList<String> getRsvpGoing() {
		return rsvpGoing;
	}

	public ArrayList<String> getRsvpNotGoing() {
		return rsvpNotGoing;
	}

	public ArrayList<String> getRsvpUndecided() {
		return rsvpUndecided;
	}

	public String getRsvpGoingString() {
		StringBuilder going = new StringBuilder();
		Integer i = 0;
		for (String u : rsvpGoing) {
			if (i == 0) {
				going = new StringBuilder(u);
			} else {
				going.append(",").append(u);
			}
			i++;
		}
		return going.toString();
	}

	public String getRsvpNotGoingString() {
		StringBuilder going = new StringBuilder();
		Integer i = 0;
		for (String u : rsvpNotGoing) {
			if (i == 0) {
				going = new StringBuilder(u);
			} else {
				going.append(",").append(u);
			}
			i++;
		}
		return going.toString();
	}

	public String getRsvpUndecidedString() {
		StringBuilder going = new StringBuilder();
		Integer i = 0;
		for (String u : rsvpUndecided) {
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

	public void setRsvpGoingFromString(String goingList) {
		if (goingList != null) {
			String[] subs = goingList.split(",");
			Collections.addAll(rsvpGoing, subs);
		}
	}

	public void setRsvpNotGoingFromString(String goingList) {
		if (goingList != null) {
			String[] subs = goingList.split(",");
			Collections.addAll(rsvpNotGoing, subs);
		}
	}

	public void setRsvpUndecidedFromString(String goingList) {
		if (goingList != null) {
			String[] subs = goingList.split(",");
			Collections.addAll(rsvpUndecided, subs);
		}
	}

	//Boolean/Checkers
	public boolean shouldBeSaved() {
		return rsvpGoing.size() > 0 || rsvpNotGoing.size() > 0 || rsvpUndecided.size() > 0;
	}
}