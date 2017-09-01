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

	private final ArrayList<String> goingOnTime = new ArrayList<>();
	private final ArrayList<String> goingLate = new ArrayList<>();
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
		Integer i = 0;
		for (String u : goingOnTime) {
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
		Integer i = 0;
		for (String u : goingLate) {
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

	//Boolean/Checkers
	public boolean shouldBeSaved() {
		return goingOnTime.size() > 0 || goingLate.size() > 0 || notGoing.size() > 0 || undecided.size() > 0;
	}
}