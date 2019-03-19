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
				Logger.getLogger().exception(null, "[WEB] Failed to get calendar!", e, this.getClass());
				name = "ERROR!";
				description = "ERROR";
				timezone = "ERROR";
			}
		}
		return this;
	}

	public JSONObject toJson() {
		JSONObject data = new JSONObject();

		data.put("Id", id);
		data.put("Address", address);
		data.put("Link", link);
		data.put("Name", name);
		if (description != null)
			data.put("Description", description);
		data.put("Timezone", timezone);
		data.put("External", external);

		return data;
	}

	public WebCalendar fromJson(JSONObject data) {
		id = data.getString("Id");
		address = data.getString("Address");
		link = data.getString("Link");
		name = data.getString("Name");
		if (data.has("Description"))
			description = data.getString("Description");
		timezone = data.getString("Timezone");
		external = data.getBoolean("External");

		return this;
	}
}