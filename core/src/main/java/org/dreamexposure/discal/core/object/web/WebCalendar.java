package org.dreamexposure.discal.core.object.web;

import com.google.api.services.calendar.model.Calendar;

import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;
import org.json.JSONObject;

/**
 * Created by Nova Fox on 1/7/18.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class WebCalendar {

    public static WebCalendar fromCalendar(final CalendarData cd, final GuildSettings gs) {
        if ("primary".equalsIgnoreCase(cd.getCalendarAddress())) {
            return new WebCalendar("primary", "primary", "N/a", "N/a", "N/a", "N/a", false);
        } else {
            final String id = cd.getCalendarId();
            final String address = cd.getCalendarAddress();
            final String link = "https://www.discalbot.com/embed/calendar/" + gs.getGuildID().asString();

            String name;
            String description;
            String timezone;
            try {
                final Calendar cal = CalendarWrapper.getCalendar(cd, gs).block();
                name = cal.getSummary();
                description = cal.getDescription();
                timezone = cal.getTimeZone().replaceAll("/", "___");
            } catch (final Exception e) {
                LogFeed.log(LogObject.
                    forException("[WEB] Failed to get calendar!", e, WebCalendar.class));
                name = "ERROR!";
                description = "ERROR";
                timezone = "ERROR";
            }
            return new WebCalendar(id, address, link, name, description, timezone, cd.isExternal());
        }
    }

    public static WebCalendar fromJson(final JSONObject data) {
        final String id = data.getString("id");
        final String address = data.getString("address");
        final String link = data.getString("link");
        final String name = data.getString("name");
        String description = "";
        if (data.has("description"))
            description = data.getString("description");
        final String timezone = data.getString("timezone");
        final boolean external = data.getBoolean("external");

        return new WebCalendar(id, address, link, name, description, timezone, external);
    }

    private final String id;
    private final String address;
    private final String link;
    private final String name;
    private final String description;
    private final String timezone;
    private final boolean external;

    private WebCalendar(final String id, final String address, final String link, final String name, final String description,
                        final String timezone, final boolean external) {
        this.id = id;
        this.address = address;
        this.link = link;
        this.name = name;
        this.description = description;
        this.timezone = timezone;
        this.external = external;
    }

    //Getters
    public String getId() {
        return this.id;
    }

    public String getAddress() {
        return this.address;
    }

    public String getLink() {
        return this.link;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getTimezone() {
        return this.timezone;
    }

    public boolean isExternal() {
        return this.external;
    }

    //Functions
    public JSONObject toJson() {
        final JSONObject data = new JSONObject();

        data.put("id", this.id);
        data.put("address", this.address);
        data.put("link", this.link);
        data.put("name", this.name);
        if (this.description != null)
            data.put("description", this.description);
        data.put("timezone", this.timezone);
        data.put("external", this.external);

        return data;
    }
}