package org.dreamexposure.discal.core.object.web;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.utils.GuildUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Image;
import discord4j.core.object.util.Snowflake;

/**
 * Created by Nova Fox on 12/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("ConstantConditions")
public class WebGuild {
	private long id;
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

	private List<String> availableLangs = new ArrayList<>();

	private WebCalendar calendar;

	//Getters
	public long getId() {
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

	public List<String> getAvailableLangs() {
		return availableLangs;
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
	public void setId(long _id) {
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
	public WebGuild fromGuild(Guild g) {
		Logger.getLogger().debug("web guild conversion p1", true);
		id = g.getId().asLong();
		name = g.getName();
		Logger.getLogger().debug("web guild conversion p2", true);
		if (g.getIconUrl(Image.Format.PNG).isPresent())
			iconUrl = g.getIconUrl(Image.Format.PNG).get();
		Logger.getLogger().debug("web guild conversion p3", true);
		botNick = g.getClient().getSelf().flatMap(user -> user.asMember(g.getId())).map(Member::getNickname).block().orElse("DisCal");
		Logger.getLogger().debug("web guild conversion p4", true);

		settings = DatabaseManager.getManager().getSettings(g.getId());
		Logger.getLogger().debug("web guild conversion p5", true);

		//Handle lists and stuffs
		/*
		for (Role r : g.getRoles().toIterable()) {
			roles.add(new WebRole().fromRole(r, settings));
		}
		 */
		g.getRoles().doOnNext(role -> roles.add(new WebRole().fromRole(role, settings))).subscribe();

		Logger.getLogger().debug("web guild conversion p6", true);

		WebChannel all = new WebChannel();
		all.setId(0);
		all.setName("All Channels");
		all.setDiscalChannel(settings.getDiscalChannel().equalsIgnoreCase("all"));
		channels.add(all);
		Logger.getLogger().debug("web guild conversion p7", true);
		for (GuildChannel c : g.getChannels().toIterable()) {
			if (c instanceof TextChannel)
				channels.add(new WebChannel().fromChannel((TextChannel) c, settings));
		}
		Logger.getLogger().debug("web guild conversion p8", true);
		announcements.addAll(DatabaseManager.getManager().getAnnouncements(g.getId()));
		Logger.getLogger().debug("web guild conversion p9", true);

		calendar = new WebCalendar().fromCalendar(DatabaseManager.getManager().getMainCalendar(Snowflake.of(id)), settings);
		Logger.getLogger().debug("web guild conversion p10", true);

		return this;
	}

	public JSONObject toJson(boolean secure) {
		JSONObject data = new JSONObject();

		data.put("id", String.valueOf(id));
		data.put("name", name);
		if (iconUrl != null)
			data.put("icon_url", iconUrl);
		if (secure)
			data.put("settings", settings.toJsonSecure());
		else
			data.put("settings", settings.toJson());

		if (botNick != null && !botNick.equals(""))
			data.put("bot_nick", botNick);
		data.put("manage_server", manageServer);
		data.put("discal_role", discalRole);

		JSONArray jRoles = new JSONArray();
		for (WebRole wr : roles) {
			jRoles.put(wr.toJson());
		}
		data.put("roles", jRoles);

		JSONArray jChannels = new JSONArray();
		for (WebChannel wc : channels) {
			jChannels.put(wc.toJson());
		}
		data.put("channels", jChannels);

		JSONArray jAnnouncements = new JSONArray();
		for (Announcement a : announcements) {
			jAnnouncements.put(a.toJson());
		}
		data.put("announcements", jAnnouncements);

		data.put("calendar", calendar.toJson());

		//Add data about shard this guild is expected to be on
		data.put("shard", GuildUtils.findShard(Snowflake.of(getId())));

		//Available langs to allow web editing of lang to be possible
		data.put("available_langs", availableLangs);

		return data;
	}

	public WebGuild fromJson(JSONObject data) {
		id = Long.parseLong(data.getString("id"));
		name = data.getString("name");
		if (data.has("icon_url"))
			iconUrl = data.getString("icon_url");
		settings = new GuildSettings(Snowflake.of(id)).fromJson(data.getJSONObject("settings"));
		if (data.has("bot_nick"))
			botNick = data.getString("bot_nick");
		else
			botNick = "";
		manageServer = data.getBoolean("manage_server");
		discalRole = data.getBoolean("discal_role");

		JSONArray jRoles = data.getJSONArray("roles");
		for (int i = 0; i < jRoles.length(); i++) {
			roles.add(new WebRole().fromJson(jRoles.getJSONObject(i)));
		}

		JSONArray jChannels = data.getJSONArray("channels");
		for (int i = 0; i < jChannels.length(); i++) {
			channels.add(new WebChannel().fromJson(jChannels.getJSONObject(i)));
		}

		JSONArray jAnnouncements = data.getJSONArray("announcements");
		for (int i = 0; i < jAnnouncements.length(); i++) {
			announcements.add(new Announcement(Snowflake.of(id)).fromJson(jAnnouncements.getJSONObject(i)));
		}

		calendar = new WebCalendar().fromJson(data.getJSONObject("calendar"));

		return this;
	}
}