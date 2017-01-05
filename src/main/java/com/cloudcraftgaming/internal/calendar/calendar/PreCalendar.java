package com.cloudcraftgaming.internal.calendar.calendar;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class PreCalendar {
    private final String guildId;

    private String summery;
    private String description;
    private String timezone;

    public PreCalendar(String _guildId, String _summery) {
        guildId = _guildId;
        summery = _summery;
    }

    //Getters
    public String getGuildId() {
        return guildId;
    }

    public String getSummery() {
        return summery;
    }

    public String getDescription() {
        return description;
    }

    public String getTimezone() {
        return timezone;
    }

    //Setters
    public void setSummery(String _summery) {
        summery = _summery;
    }

    public void setDescription(String _description) {
        description = _description;
    }

    public void setTimezone(String _timezone) {
        timezone = _timezone;
    }

    //Booleans/Checkers
    public Boolean hasRequiredValues() {
        return summery != null && timezone != null;
    }
}