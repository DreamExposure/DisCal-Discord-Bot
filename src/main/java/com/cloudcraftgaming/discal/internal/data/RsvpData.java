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

	private final ArrayList<String> going = new ArrayList<>();
	private final ArrayList<String> notGoing = new ArrayList<>();
	private final ArrayList<String> undecided = new ArrayList<>();

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

	public ArrayList<String> getGoing() {
		return going;
	}

	public ArrayList<String> getNotGoing() {
		return notGoing;
	}

	public ArrayList<String> getUndecided() {
		return undecided;
	}

	public String getGoingString() {
		StringBuilder goingString = new StringBuilder();
		Integer i = 0;
		for (String u : going) {
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
		Integer i = 0;
		for (String u : notGoing) {
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
		Integer i = 0;
		for (String u : undecided) {
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

	public void setGoingFromString(String goingList) {
		if (goingList != null) {
			String[] subs = goingList.split(",");
			Collections.addAll(going, subs);
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

	//Boolean/Checkers
	public boolean shouldBeSaved() {
		return going.size() > 0 || notGoing.size() > 0 || undecided.size() > 0;
	}
}