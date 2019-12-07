package org.dreamexposure.discal.core.object.web;

import com.google.api.services.calendar.model.Calendar;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.json.JSONObject;

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
			link = "https://www.discalbot.com/embed/calendar/" + gs.getGuildID().asString();
			external = cd.isExternal();
			try {
				Calendar cal = CalendarAuth.getCalendarService(gs).calendars().get(id).execute();
				name = cal.getSummary();
				description = cal.getDescription();
				timezone = cal.getTimeZone().replaceAll("/", "___");
			} catch (Exception e) {
				Logger.getLogger().exception(null, "[WEB] Failed to get calendar!", e, true, this.getClass());
				name = "ERROR!";
				description = "ERROR";
				timezone = "ERROR";
			}
		}
		return this;
	}

	public JSONObject toJson() {
		JSONObject data = new JSONObject();

		data.put("id", id);
		data.put("address", address);
		data.put("link", link);
		data.put("name", name);
		if (description != null)
			data.put("description", description);
		data.put("timezone", timezone);
		data.put("external", external);

		return data;
	}

	public WebCalendar fromJson(JSONObject data) {
		id = data.getString("id");
		address = data.getString("address");
		link = data.getString("link");
		name = data.getString("name");
		if (data.has("description"))
			description = data.getString("description");
		timezone = data.getString("timezone");
		external = data.getBoolean("external");

		return this;
	}
}