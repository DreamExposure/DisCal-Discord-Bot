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

	public static WebCalendar fromCalendar(CalendarData cd, GuildSettings gs) {
		if (cd.getCalendarAddress().equalsIgnoreCase("primary")) {
			return new WebCalendar("primary", "primary", "N/a", "N/a", "N/a", "N/a", false);
		} else {
			String id = cd.getCalendarId();
			String address = cd.getCalendarAddress();
			String link = "https://www.discalbot.com/embed/calendar/" + gs.getGuildID().asString();

			String name;
			String description;
			String timezone;
			try {
				Calendar cal = CalendarAuth.getCalendarService(gs).calendars().get(id).execute();
				name = cal.getSummary();
				description = cal.getDescription();
				timezone = cal.getTimeZone().replaceAll("/", "___");
			} catch (Exception e) {
				Logger.getLogger().exception(null, "[WEB] Failed to get calendar!", e, true, WebCalendar.class);
				name = "ERROR!";
				description = "ERROR";
				timezone = "ERROR";
			}
			return new WebCalendar(id, address, link, name, description, timezone, cd.isExternal());
		}
	}

	public static WebCalendar fromJson(JSONObject data) {
		String id = data.getString("id");
		String address = data.getString("address");
		String link = data.getString("link");
		String name = data.getString("name");
		String description = "";
		if (data.has("description"))
			description = data.getString("description");
		String timezone = data.getString("timezone");
		boolean external = data.getBoolean("external");

		return new WebCalendar(id, address, link, name, description, timezone, external);
	}

	private final String id;
	private final String address;
	private final String link;
	private final String name;
	private final String description;
	private final String timezone;
	private final boolean external;

	private WebCalendar(String id, String address, String link, String name, String description,
						String timezone, boolean external) {
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

	//Functions
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
}