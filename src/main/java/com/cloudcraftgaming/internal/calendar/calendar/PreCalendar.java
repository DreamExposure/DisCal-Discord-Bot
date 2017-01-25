package com.cloudcraftgaming.internal.calendar.calendar;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class PreCalendar {
    private final String guildId;

    private String summary;
    private String description;
    private String timezone;

    PreCalendar(String _guildId, String _summary) {
        guildId = _guildId;
        summary = _summary;
    }

    //Getters
    public String getGuildId() {
        return guildId;
    }

    String getSummary() {
        return summary;
    }

    String getDescription() {
        return description;
    }

    String getTimezone() {
        return timezone;
    }

    //Setters
    public void setSummary(String _summary) {
        summary = _summary;
    }

    public void setDescription(String _description) {
        description = _description;
    }

    public void setTimezone(String _timezone) {
        timezone = _timezone;
    }

    //Booleans/Checkers
    Boolean hasRequiredValues() {
        return summary != null && timezone != null;
    }
}