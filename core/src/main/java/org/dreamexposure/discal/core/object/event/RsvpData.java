package org.dreamexposure.discal.core.object.event;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import discord4j.common.util.Snowflake;

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

    public RsvpData(final Snowflake guildId, final String eventId) {
        this.guildId = guildId;
        this.eventId = eventId;
    }

    //Getters
    public Snowflake getGuildId() {
        return this.guildId;
    }

    public String getEventId() {
        return this.eventId;
    }

    public long getEventEnd() {
        return this.eventEnd;
    }

    public ArrayList<String> getGoingOnTime() {
        return this.goingOnTime;
    }

    public ArrayList<String> getGoingLate() {
        return this.goingLate;
    }

    public ArrayList<String> getNotGoing() {
        return this.notGoing;
    }

    public ArrayList<String> getUndecided() {
        return this.undecided;
    }

    public String getGoingOnTimeString() {
        StringBuilder goingString = new StringBuilder();
        int i = 0;
        for (final String u : this.goingOnTime) {
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
        for (final String u : this.goingLate) {
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
        for (final String u : this.notGoing) {
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
        for (final String u : this.undecided) {
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
    public void setEventId(final String _eventId) {
        this.eventId = _eventId;
    }

    public void setEventEnd(final long _eventEnd) {
        this.eventEnd = _eventEnd;
    }

    public void setGoingOnTimeFromString(final String goingList) {
        if (goingList != null) {
            final String[] subs = goingList.split(",");
            Collections.addAll(this.goingOnTime, subs);
        }
    }

    public void setGoingLateFromString(final String goingList) {
        if (goingList != null) {
            final String[] subs = goingList.split(",");
            Collections.addAll(this.goingLate, subs);
        }
    }

    public void setNotGoingFromString(final String goingList) {
        if (goingList != null) {
            final String[] subs = goingList.split(",");
            Collections.addAll(this.notGoing, subs);
        }
    }

    public void setUndecidedFromString(final String goingList) {
        if (goingList != null) {
            final String[] subs = goingList.split(",");
            Collections.addAll(this.undecided, subs);
        }
    }

    //Functions
    public void removeCompletely(final String userId) {
        this.goingOnTime.remove(userId);
        this.goingLate.remove(userId);
        this.notGoing.remove(userId);
        this.undecided.remove(userId);
    }

    //Boolean/Checkers
    public boolean shouldBeSaved() {
        return !this.goingOnTime.isEmpty()
            || !this.goingLate.isEmpty()
            || !this.notGoing.isEmpty()
            || !this.undecided.isEmpty();
    }

    public JSONObject toJson() {
        final JSONObject json = new JSONObject();

        json.put("guild_id", this.guildId.asString());
        json.put("event_id", this.eventId);
        json.put("event_end", this.eventEnd);

        final JSONArray jOnTime = new JSONArray();
        for (final String s : this.goingOnTime)
            jOnTime.put(s);
        json.put("on_time", jOnTime);

        final JSONArray jLate = new JSONArray();
        for (final String s : this.goingLate)
            jLate.put(s);
        json.put("late", jLate);

        final JSONArray jNot = new JSONArray();
        for (final String s : this.notGoing)
            jNot.put(s);
        json.put("not_going", jNot);

        final JSONArray jUndecided = new JSONArray();
        for (final String s : this.undecided)
            jUndecided.put(s);
        json.put("undecided", jUndecided);

        return json;
    }

    public RsvpData fromJson(final JSONObject json) {
        this.eventId = json.getString("event_id");
        this.eventEnd = json.getLong("event_end");

        final JSONArray jOnTime = json.getJSONArray("on_time");
        for (int i = 0; i < jOnTime.length(); i++)
            this.goingOnTime.add(jOnTime.getString(i));

        final JSONArray jLate = json.getJSONArray("late");
        for (int i = 0; i < jLate.length(); i++)
            this.goingLate.add(jLate.getString(i));

        final JSONArray jNot = json.getJSONArray("not_going");
        for (int i = 0; i < jNot.length(); i++)
            this.notGoing.add(jNot.getString(i));

        final JSONArray jUndecided = json.getJSONArray("undecided");
        for (int i = 0; i < jUndecided.length(); i++)
            this.undecided.add(jUndecided.getString(i));

        return this;
    }
}