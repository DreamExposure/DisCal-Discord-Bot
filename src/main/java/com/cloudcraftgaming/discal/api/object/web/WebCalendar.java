package com.cloudcraftgaming.discal.api.object.web;

import com.cloudcraftgaming.discal.api.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.calendar.CalendarData;
import com.cloudcraftgaming.discal.bot.internal.calendar.calendar.CalendarMessageFormatter;
import com.cloudcraftgaming.discal.logger.Logger;
import com.google.api.services.calendar.model.Calendar;

/**
 * Created by Nova Fox on 1/7/18.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class WebCalendar {
	private String id;
	private String address;
	private String link;
	private String name;
	private String description;
	private String timezone;

	private boolean external;

	//Getters
	public String getId() {
		return id;
	}

	public String getAddress() {
		return address;
	}

	public String getLink() {
		return link;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getTimezone() {
		return timezone;
	}

	public boolean isExternal() {
		return external;
	}

	//Setters
	public void setId(String _id) {
		id = _id;
	}

	public void setAddress(String _address) {
		address = _address;
	}

	public void setLink(String _link) {
		link = _link;
	}

	public void setName(String _name) {
		name = _name;
	}

	public void setDescription(String _desc) {
		description = _desc;
	}

	public void setTimezone(String _tz) {
		timezone = _tz;
	}

	public void setExternal(boolean _ext) {
		external = _ext;
	}

	//Functions
	public WebCalendar fromCalendar(CalendarData cd, GuildSettings gs) {
		if (cd.getCalendarAddress().equalsIgnoreCase("primary")) {
			id = "primary";
			address = "primary";
			link = "N/a";
			name = "N/a";
			description = "N/a";
			timezone = "N/a";
		} else {
			id = cd.getCalendarId();
			address = cd.getCalendarAddress();
			link = CalendarMessageFormatter.getCalendarLink(id);
			external = cd.isExternal();
			try {
				if (cd.isExternal()) {
					Calendar cal = CalendarAuth.getCalendarService(gs).calendars().get(id).execute();
					name = cal.getSummary();
					description = cal.getDescription();
					timezone = cal.getTimeZone().replaceAll("/", "___");
				} else {
					Calendar cal = CalendarAuth.getCalendarService().calendars().get(id).execute();
					name = cal.getSummary();
					description = cal.getDescription();
					timezone = cal.getTimeZone().replaceAll("/", "___");
				}
			} catch (Exception e) {
				Logger.getLogger().exception(null, "[WEB] Failed to get calendar!", e, this.getClass(), true);
				name = "ERROR!";
				description = "ERROR";
				timezone = "ERROR";
			}
		}
		return this;
	}
}