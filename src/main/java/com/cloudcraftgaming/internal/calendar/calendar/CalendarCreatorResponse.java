package com.cloudcraftgaming.internal.calendar.calendar;

import com.google.api.services.calendar.model.Calendar;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CalendarCreatorResponse {
    private final Boolean successful;

    private Calendar calendar;

    public CalendarCreatorResponse(Boolean _successful) {
        successful = _successful;
    }

    public CalendarCreatorResponse(Boolean _successful, Calendar _calendar) {
        successful = _successful;
        calendar = _calendar;
    }

    //Getters
    public Boolean isSuccessful() {
        return successful;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    //Setters
    public void setEvent(Calendar _calendar) {
        calendar = _calendar;
    }
}