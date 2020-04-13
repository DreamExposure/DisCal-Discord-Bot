package org.dreamexposure.discal.core.object.web;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.utils.GuildUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.GuildUpdateData;
import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.entity.RestGuild;
import discord4j.rest.util.Image;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

/**
 * Created by Nova Fox on 12/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class WebGuild {

	public static WebGuild fromGuild(RestGuild g) {
		GuildUpdateData data = g.getData().block();

		Snowflake id = Snowflake.of(data.id());
		String name = data.name();
		String iconUrl = data.icon().orElse("");

		Mono<String> botNick = g.member(Long.parseLong(BotSettings.ID.get()))
				.getData()
				.map(MemberData::nick)
				.map(Possible::flatOpt)
				.flatMap(Mono::justOrEmpty)
				.defaultIfEmpty("DisCal");

		Mono<GuildSettings> settings = DatabaseManager.getSettings(id).cache();

		Mono<List<WebRole>> roles = settings.flatMapMany(s ->
				g.getRoles().map(role -> WebRole.fromRole(role, s)))
				.collectList();

		Mono<List<WebChannel>> webChannels = settings.flatMapMany(s ->
				g.getChannels()
						.ofType(TextChannel.class)
						.map(channel -> WebChannel.fromChannel(channel, s)))
				.collectList();

		Mono<List<Announcement>> announcements = DatabaseManager.getAnnouncements(id);

		Mono<WebCalendar> calendar = settings.flatMap(s ->
				DatabaseManager.getMainCalendar(id)
						.flatMap(d -> Mono.just(WebCalendar.fromCalendar(d, s)))
		);

		return Mono.zip(botNick, settings, roles, webChannels, announcements, calendar)
				.map(TupleUtils.function((bn, s, r, wc, a, c) -> {
					WebGuild wg = new WebGuild(id.asLong(), name, iconUrl, s, bn, false, false, c);

					wg.getChannels().add(WebChannel.all(s));

					wg.getRoles().addAll(r);
					wg.getChannels().addAll(wc);
					wg.getAnnouncements().addAll(a);
					return wg;
				})).block();
	}

	public static WebGuild fromGuild(Guild g) {
		long id = g.getId().asLong();
		String name = g.getName();
		String iconUrl = g.getIconUrl(Image.Format.PNG).orElse(null);
		Mono<String> botNick = g.getMemberById(Snowflake.of(BotSettings.ID.get()))
				.map(Member::getNickname)
				.flatMap(Mono::justOrEmpty)
				.defaultIfEmpty("DisCal");

		Mono<GuildSettings> settings = DatabaseManager.getSettings(g.getId()).cache();

		Mono<List<WebRole>> roles = settings.flatMapMany(s ->
				g.getRoles().map(role -> WebRole.fromRole(role, s)))
				.collectList();

		Mono<List<WebChannel>> webChannels = settings.flatMapMany(s ->
				g.getChannels()
						.ofType(TextChannel.class)
						.map(channel -> WebChannel.fromChannel(channel, s)))
				.collectList();

		Mono<List<Announcement>> announcements = DatabaseManager.getAnnouncements(g.getId());

		Mono<WebCalendar> calendar = settings.flatMap(s ->
				DatabaseManager.getMainCalendar(Snowflake.of(id))
						.flatMap(d -> Mono.just(WebCalendar.fromCalendar(d, s)))
		);

		return Mono.zip(botNick, settings, roles, webChannels, announcements, calendar)
				.map(TupleUtils.function((bn, s, r, wc, a, c) -> {
					WebGuild wg = new WebGuild(id, name, iconUrl, s, bn, false, false, c);

					wg.getChannels().add(WebChannel.all(s));

					wg.getRoles().addAll(r);
					wg.getChannels().addAll(wc);
					wg.getAnnouncements().addAll(a);
					return wg;
				})).block();
	}

	public static WebGuild fromJson(JSONObject data) {
		long id = Long.parseLong(data.getString("id"));
		GuildSettings settings = new GuildSettings(
				Snowflake.of(id)).fromJson(data.getJSONObject("settings"));

		WebGuild webGuild = new WebGuild(
				id,
				data.getString("name"),
				data.optString("icon_url"),
				settings,
				data.optString("bot_nick"),
				data.getBoolean("manage_server"),
				data.getBoolean("discal_role"),
				WebCalendar.fromJson(data.getJSONObject("calendar")));

		JSONArray jRoles = data.getJSONArray("roles");
		for (int i = 0; i < jRoles.length(); i++) {
			webGuild.getRoles().add(WebRole.fromJson(jRoles.getJSONObject(i)));
		}

		JSONArray jChannels = data.getJSONArray("channels");
		for (int i = 0; i < jChannels.length(); i++) {
			webGuild.getChannels().add(WebChannel.fromJson(jChannels.getJSONObject(i)));
		}

		JSONArray jAnnouncements = data.getJSONArray("announcements");
		for (int i = 0; i < jAnnouncements.length(); i++) {
			webGuild.getAnnouncements().add(new Announcement(Snowflake.of(id)).fromJson(jAnnouncements.getJSONObject(i)));
		}

		return webGuild;
	}

	private final long id;
	private final String name;
	private final String iconUrl;

	//Bot settings
	private final GuildSettings settings;
	private final String botNick;

	//User info
	private boolean manageServer;
	private boolean discalRole;

	//Lists and stuffs
	private final List<WebRole> roles = new ArrayList<>();
	private final List<WebChannel> channels = new ArrayList<>();
	private final List<Announcement> announcements = new ArrayList<>();

	private final List<String> availableLangs = new ArrayList<>();

	private final WebCalendar calendar;

	private WebGuild(long id, String name, String iconUrl, GuildSettings settings, String botNick,
					 boolean manageServer, boolean discalRole, WebCalendar calendar) {
		this.id = id;
		this.name = name;
		this.iconUrl = iconUrl;
		this.settings = settings;
		this.botNick = botNick;
		this.manageServer = manageServer;
		this.discalRole = discalRole;
		this.calendar = calendar;
	}

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

	//Setties
	public void setManageServer(boolean ms) {
		manageServer = ms;
	}

	public void setDiscalRole(boolean dr) {
		discalRole = dr;
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
}