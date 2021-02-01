import {GuildSettings} from "@/objects/guild/GuildSettings";
import {WebRole} from "@/objects/guild/WebRole";
import {WebChannel} from "@/objects/guild/WebChannel";
import {Announcement} from "@/objects/announcement/Announcement";
import {WebCalendar} from "@/objects/calendar/WebCalendar";

export class WebGuild {
    private readonly _id: string;
    private _name: string = "";
    private _icon: string = "";

    private _settings: GuildSettings = new GuildSettings("");
    private _botNick: string = "";

    private _canManageServer: boolean = false;
    private _hasDisCalRole: boolean = false;

    private readonly _roles: WebRole[] = [];
    private readonly _channels: WebChannel[] = [];
    private readonly _announcements: Announcement[] = [];
    private readonly _availableLangs: string[] = [];

    private _calendar: WebCalendar = new WebCalendar();

    private _shard: number = 0;

    constructor(id: string) {
        this._id = id;
    }

    //Getter/Setter pairs
    get id() {
        return this._id;
    }

    get name() {
        return this._name;
    }

    set name(name) {
        this._name = name;
    }

    get icon() {
        return this._icon;
    }

    set icon(iconUrl) {
        this._icon = iconUrl;
    }

    get settings() {
        return this._settings;
    }

    set settings(settings) {
        this._settings = settings;
    }

    get botNick() {
        return this._botNick;
    }

    set botNick(nick) {
        this._botNick = nick;
    }

    get canManageServer() {
        return this._canManageServer;
    }

    set canManageServer(manage) {
        this._canManageServer = manage;
    }

    get hasDisCalRole() {
        return this._hasDisCalRole;
    }

    set hasDisCalRole(hasRole) {
        this._hasDisCalRole = hasRole;
    }

    get roles() {
        return this._roles;
    }

    get channels() {
        return this._channels;
    }

    get announcements() {
        return this._announcements;
    }

    get availableLangs() {
        return this._availableLangs;
    }

    get calendar() {
        return this._calendar;
    }

    set calendar(calendar) {
        this._calendar = calendar;
    }

    get shard() {
        return this._shard;
    }

    set shard(shard) {
        this._shard = shard;
    }

    //Json conversions
    toJson() {
        let json: any = {
            "id": this.id,
            "name": this.name,
            "settings": this.settings.toJson(),
            "manage_server": this.canManageServer,
            "discal_role": this.hasDisCalRole,
            "calendar": this.calendar.toJson()
        };
        if (this.icon != null) {
            json.icon = this.icon;
        }
        if (this.botNick != null && this.botNick !== "") {
            json.bot_nick = this.botNick;
        }

        let jRoles = [];
        for (let i = 0; i < this.roles.length; i++) {
            let role = this.roles[i];
            jRoles.push(role.toJson());
        }
        json.roles = jRoles;

        let jChannels = [];
        for (let i = 0; i < this.channels.length; i++) {
            let channel = this.channels[i];
            jChannels.push(channel.toJson());
        }
        json.channels = jChannels;

        let jAnnouncements = [];
        for (let i = 0; i < this.announcements.length; i++) {
            let announcement = this.announcements[i];
            jAnnouncements.push(announcement.toJson());
        }
        json.announcements = jAnnouncements;

        json.shard = this.shard;

        return json;
    }

    fromJson(json: any) {
        //We don't need to set ID, this is already done...
        this.name = json.name;

        if (json.hasOwnProperty("icon_url")) {
            this.icon = json.icon_url;
        }

        this.settings = new GuildSettings(this.id).fromJson(json.settings);

        if (json.hasOwnProperty("bot_nick")) {
            this.botNick = json.bot_nick;
        }

        this.canManageServer = json.manage_server;
        this.hasDisCalRole = json.discal_role;

        for (let i = 0; i < json.roles.length; i++) {
            this.roles.push(new WebRole().fromJson(json.roles[i]));
        }

        for (let i = 0; i < json.channels.length; i++) {
            this.channels.push(new WebChannel().fromJson(json.channels[i]));
        }

        for (let i = 0; i < json.announcements.length; i++) {
            this.roles.push(new WebRole().fromJson(json.roles[i]));
        }

        for (let i = 0; i < json.available_langs.length; i++) {
            this.availableLangs.push(json.available_langs[i]);
        }

        this.calendar = new WebCalendar().fromJson(json.calendar);

        this.shard = json.shard;

        return this;
    }
}
