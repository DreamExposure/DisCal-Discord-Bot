package org.dreamexposure.discal.core.object.web;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nova Fox on 12/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"OptionalGetWithoutIsPresent", "ConstantConditions"})
public class WebGuild {
	private String id;
	private String name;
	private String iconUrl;

	//Bot settings
	private GuildSettings settings;
	private String botNick;

	//User info
	private boolean manageServer;
	private boolean discalRole;

	//Lists and stuffs
	private List<WebRole> roles = new ArrayList<>();
	private List<WebChannel> channels = new ArrayList<>();
	private List<Announcement> announcements = new ArrayList<>();

	private WebCalendar calendar;

	//Getters
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getIcon() {
		return iconUrl;
	}

	public GuildSettings getSettings() {
		return settings;
	}

	public String getBotNick() {
		return botNick;
	}

	public List<WebRole> getRoles() {
		return roles;
	}

	public WebRole getRole(long id) {
		for (WebRole wr : roles) {
			if (wr.getId() == id)
				return wr;
		}
		return null;
	}

	public List<WebChannel> getChannels() {
		return channels;
	}

	public WebChannel getChannel(long id) {
		for (WebChannel wc : channels) {
			if (wc.getId() == id)
				return wc;
		}
		return null;
	}

	public List<Announcement> getAnnouncements() {
		return announcements;
	}

	public WebCalendar getCalendar() {
		return calendar;
	}

	public boolean isManageServer() {
		return manageServer;
	}

	public boolean isDiscalRole() {
		return discalRole;
	}

	//Setters
	public void setId(String _id) {
		id = _id;
	}

	public void setName(String _name) {
		name = _name;
	}

	public void setIcon(String _url) {
		iconUrl = _url;
	}

	public void setSettings(GuildSettings _settings) {
		settings = _settings;
	}

	public void setBotNick(String _nick) {
		botNick = _nick;
	}

	public void setCalendar(WebCalendar _cal) {
		calendar = _cal;
	}

	public void setManageServer(boolean _ms) {
		manageServer = _ms;
	}

	public void setDiscalRole(boolean _dr) {
		discalRole = _dr;
	}


	//Functions
	public WebGuild fromGuild(IGuild g) {
		id = g.getStringID();
		name = g.getName();
		iconUrl = g.getIconURL();
		botNick = g.getClient().getOurUser().getNicknameForGuild(g);

		settings = DatabaseManager.getManager().getSettings(g.getLongID());

		//Handle lists and stuffs
		for (IRole r : g.getRoles()) {
			roles.add(new WebRole().fromRole(r, settings));
		}

		WebChannel all = new WebChannel();
		all.setId(0);
		all.setName("All Channels");
		all.setDiscalChannel(settings.getDiscalChannel().equalsIgnoreCase("all"));
		channels.add(all);
		for (IChannel c : g.getChannels()) {
			channels.add(new WebChannel().fromChannel(c, settings));
		}
		announcements.addAll(DatabaseManager.getManager().getAnnouncements(g.getLongID()));

		calendar = new WebCalendar().fromCalendar(DatabaseManager.getManager().getMainCalendar(Long.valueOf(id)), settings);

		return this;
	}

	public JSONObject toJson() {
		JSONObject data = new JSONObject();

		data.put("Id", id);
		data.put("Name", name);
		data.put("IconUrl", iconUrl);
		data.put("Settings", settings.toJson());
		if (botNick != null && !botNick.equals(""))
			data.put("BotNick", botNick);
		data.put("ManageServer", manageServer);
		data.put("DiscalRole", discalRole);

		JSONArray jRoles = new JSONArray();
		for (WebRole wr : roles) {
			jRoles.put(wr.toJson());
		}
		data.put("Roles", jRoles);

		JSONArray jChannels = new JSONArray();
		for (WebChannel wc : channels) {
			jChannels.put(wc.toJson());
		}
		data.put("Channels", jChannels);

		JSONArray jAnnouncements = new JSONArray();
		for (Announcement a : announcements) {
			jAnnouncements.put(a.toJson());
		}
		data.put("Announcements", jAnnouncements);

		data.put("Calendar", calendar.toJson());

		return data;
	}

	public WebGuild fromJson(JSONObject data) {
		id = data.getString("Id");
		name = data.getString("Name");
		iconUrl = data.getString("IconUrl");
		settings = new GuildSettings(Long.valueOf(id)).fromJson(data.getJSONObject("Settings"));
		if (data.has("BotNick"))
			botNick = data.getString("BotNick");
		else
			botNick = "";
		manageServer = data.getBoolean("ManageServer");
		discalRole = data.getBoolean("DiscalRole");

		JSONArray jRoles = data.getJSONArray("Roles");
		for (int i = 0; i < jRoles.length(); i++) {
			roles.add(new WebRole().fromJson(jRoles.getJSONObject(i)));
		}

		JSONArray jChannels = data.getJSONArray("Channels");
		for (int i = 0; i < jChannels.length(); i++) {
			channels.add(new WebChannel().fromJson(jChannels.getJSONObject(i)));
		}

		JSONArray jAnnouncements = data.getJSONArray("Announcements");
		for (int i = 0; i < jAnnouncements.length(); i++) {
			announcements.add(new Announcement(Long.valueOf(id)).fromJson(jAnnouncements.getJSONObject(i)));
		}

		calendar = new WebCalendar().fromJson(data.getJSONObject("Calendar"));

		return this;
	}
}