package org.dreamexposure.discal.core.object.web;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.discordjson.json.GuildUpdateData;
import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.entity.RestGuild;
import discord4j.rest.util.Image;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.exceptions.BotNotInGuildException;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.utils.GuildUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nova Fox on 12/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class WebGuild {

    public static WebGuild fromGuild(final RestGuild g) throws BotNotInGuildException {
        final GuildUpdateData data;

        try {
            data = g.getData().block();
        } catch (final Exception e) {
            throw new BotNotInGuildException();
        }

        final Snowflake id = Snowflake.of(data.id());
        final String name = data.name();
        final String iconUrl = data.icon().orElse("");

        final Mono<String> botNick = g.member(Snowflake.of(BotSettings.ID.get()))
            .getData()
            .map(MemberData::nick)
            .map(Possible::flatOpt)
            .flatMap(Mono::justOrEmpty)
            .defaultIfEmpty("DisCal");

        final Mono<GuildSettings> settings = DatabaseManager.getSettings(id).cache();

        final Mono<List<WebRole>> roles = settings.flatMapMany(s ->
            g.getRoles().map(role -> WebRole.fromRole(role, s)))
            .collectList();

        final Mono<List<WebChannel>> webChannels = settings.flatMapMany(s ->
            g.getChannels()
                .ofType(GuildMessageChannel.class)
                .map(channel -> WebChannel.fromChannel(channel, s)))
            .collectList();

        final Mono<List<Announcement>> announcements = DatabaseManager.getAnnouncements(id);

        final Mono<WebCalendar> calendar = settings.flatMap(s ->
            DatabaseManager.getMainCalendar(id)
                .flatMap(d -> WebCalendar.fromCalendar(d, s))
        )
            .defaultIfEmpty(WebCalendar.empty());

        return Mono.zip(botNick, settings, roles, webChannels, announcements, calendar)
            .map(TupleUtils.function((bn, s, r, wc, a, c) -> {
                final WebGuild wg = new WebGuild(id.asLong(), name, iconUrl, s, bn, false, false, c);

                wg.getChannels().add(WebChannel.all(s));

                wg.getRoles().addAll(r);
                wg.getChannels().addAll(wc);
                wg.getAnnouncements().addAll(a);
                return wg;
            })).block();
    }

    public static WebGuild fromGuild(final Guild g) {
        final long id = g.getId().asLong();
        final String name = g.getName();
        final String iconUrl = g.getIconUrl(Image.Format.PNG).orElse(null);
        final Mono<String> botNick = g.getMemberById(Snowflake.of(BotSettings.ID.get()))
            .map(Member::getNickname)
            .flatMap(Mono::justOrEmpty)
            .defaultIfEmpty("DisCal");

        final Mono<GuildSettings> settings = DatabaseManager.getSettings(g.getId()).cache();

        final Mono<List<WebRole>> roles = settings.flatMapMany(s ->
            g.getRoles().map(role -> WebRole.fromRole(role, s)))
            .collectList();

        final Mono<List<WebChannel>> webChannels = settings.flatMapMany(s ->
            g.getChannels()
                .ofType(GuildMessageChannel.class)
                .map(channel -> WebChannel.fromChannel(channel, s)))
            .collectList();

        final Mono<List<Announcement>> announcements = DatabaseManager.getAnnouncements(g.getId());

        final Mono<WebCalendar> calendar = settings.flatMap(s ->
            DatabaseManager.getMainCalendar(Snowflake.of(id))
                .flatMap(d -> WebCalendar.fromCalendar(d, s))
        );

        return Mono.zip(botNick, settings, roles, webChannels, announcements, calendar)
            .map(TupleUtils.function((bn, s, r, wc, a, c) -> {
                final WebGuild wg = new WebGuild(id, name, iconUrl, s, bn, false, false, c);

                wg.getChannels().add(WebChannel.all(s));

                wg.getRoles().addAll(r);
                wg.getChannels().addAll(wc);
                wg.getAnnouncements().addAll(a);
                return wg;
            })).block();
    }

    public static WebGuild fromJson(final JSONObject data) {
        final long id = Long.parseLong(data.getString("id"));
        final GuildSettings settings = new GuildSettings(
            Snowflake.of(id)).fromJson(data.getJSONObject("settings"));

        final WebGuild webGuild = new WebGuild(
            id,
            data.getString("name"),
            data.optString("icon_url"),
            settings,
            data.optString("bot_nick"),
            data.getBoolean("manage_server"),
            data.getBoolean("discal_role"),
            WebCalendar.fromJson(data.getJSONObject("calendar")));

        final JSONArray jRoles = data.getJSONArray("roles");
        for (int i = 0; i < jRoles.length(); i++) {
            webGuild.getRoles().add(WebRole.fromJson(jRoles.getJSONObject(i)));
        }

        final JSONArray jChannels = data.getJSONArray("channels");
        for (int i = 0; i < jChannels.length(); i++) {
            webGuild.getChannels().add(WebChannel.fromJson(jChannels.getJSONObject(i)));
        }

        final JSONArray jAnnouncements = data.getJSONArray("announcements");
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

    private WebGuild(final long id, final String name, final String iconUrl, final GuildSettings settings, final String botNick,
                     final boolean manageServer, final boolean discalRole, final WebCalendar calendar) {
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
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getIcon() {
        return this.iconUrl;
    }

    public GuildSettings getSettings() {
        return this.settings;
    }

    public String getBotNick() {
        return this.botNick;
    }

    public List<WebRole> getRoles() {
        return this.roles;
    }

    public WebRole getRole(final long id) {
        for (final WebRole wr : this.roles) {
            if (wr.getId() == id)
                return wr;
        }
        return null;
    }

    public List<WebChannel> getChannels() {
        return this.channels;
    }

    public WebChannel getChannel(final long id) {
        for (final WebChannel wc : this.channels) {
            if (wc.getId() == id)
                return wc;
        }
        return null;
    }

    public List<Announcement> getAnnouncements() {
        return this.announcements;
    }

    public List<String> getAvailableLangs() {
        return this.availableLangs;
    }

    public WebCalendar getCalendar() {
        return this.calendar;
    }

    public boolean isManageServer() {
        return this.manageServer;
    }

    public boolean isDiscalRole() {
        return this.discalRole;
    }

    //Setties
    public void setManageServer(final boolean ms) {
        this.manageServer = ms;
    }

    public void setDiscalRole(final boolean dr) {
        this.discalRole = dr;
    }

    public JSONObject toJson(final boolean secure) {
        final JSONObject data = new JSONObject();

        data.put("id", String.valueOf(this.id));
        data.put("name", this.name);
        if (this.iconUrl != null)
            data.put("icon_url", this.iconUrl);
        if (secure)
            data.put("settings", this.settings.toJsonSecure());
        else
            data.put("settings", this.settings.toJson());

        if (this.botNick != null && !this.botNick.isEmpty())
            data.put("bot_nick", this.botNick);
        data.put("manage_server", this.manageServer);
        data.put("discal_role", this.discalRole);

        final JSONArray jRoles = new JSONArray();
        for (final WebRole wr : this.roles) {
            jRoles.put(wr.toJson());
        }
        data.put("roles", jRoles);

        final JSONArray jChannels = new JSONArray();
        for (final WebChannel wc : this.channels) {
            jChannels.put(wc.toJson());
        }
        data.put("channels", jChannels);

        final JSONArray jAnnouncements = new JSONArray();
        for (final Announcement a : this.announcements) {
            jAnnouncements.put(a.toJson());
        }
        data.put("announcements", jAnnouncements);

        data.put("calendar", this.calendar.toJson());

        //Add data about shard this guild is expected to be on
        data.put("shard", GuildUtils.findShard(Snowflake.of(this.getId())));

        //Available langs to allow web editing of lang to be possible
        data.put("available_langs", this.availableLangs);

        return data;
    }
}