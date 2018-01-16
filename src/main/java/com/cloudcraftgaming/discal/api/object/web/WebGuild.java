package com.cloudcraftgaming.discal.api.object.web;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.announcement.Announcement;
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

	public List<WebChannel> getChannels() {
		return channels;
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
		botNick = Main.client.getOurUser().getNicknameForGuild(g);

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
}