import {TimeFormat} from "@/enums/TimeFormat";
import {AnnouncementStyle} from "@/enums/AnnouncementStyle";

export class GuildSettings {
    private _id: string;
    private _controlRole: string = "everyone";
    private _disCalChannel: string = "all";
    private _announcementStyle: AnnouncementStyle = AnnouncementStyle.FULL;
    private _lang: string = "";
    private _prefix: string = "";
    private _isPatronGuild: boolean = false;
    private _isDevGuild: boolean = false;
    private _maxCalendars: number = 1;
    private _timeFormat: TimeFormat = TimeFormat.TWENTY_FOUR_HOUR;
    private _isBranded: boolean = false;

    constructor(id: string) {
        this._id = id;
    }

    //Getter/setter pairs
    get id() {
        return this._id;
    }

    get controlRole() {
        return this._controlRole;
    }

    set controlRole(role) {
        this._controlRole = role;
    }

    get disCalChannel() {
        return this._disCalChannel;
    }

    set disCalChannel(channel) {
        this._disCalChannel = channel;
    }

    get announcementStyle() {
        return this._announcementStyle;
    }

    set announcementStyle(style) {
        this._announcementStyle = style;
    }

    get lang() {
        return this._lang;
    }

    set lang(lang) {
        this._lang = lang;
    }

    get prefix() {
        return this._prefix;
    }

    set prefix(prefix) {
        this._prefix = prefix;
    }

    get isPatronGuild() {
        return this._isPatronGuild;
    }

    set isPatronGuild(patron) {
        this._isPatronGuild = patron;
    }

    get isDevGuild() {
        return this._isDevGuild;
    }

    set isDevGuild(dev) {
        this._isDevGuild = dev;
    }

    get maxCalendars() {
        return this._maxCalendars;
    }

    set maxCalendars(max) {
        this._maxCalendars = max;
    }

    get timeFormat() {
        return this._timeFormat;
    }

    set timeFormat(format) {
        this._timeFormat = format;
    }

    get isBranded() {
        return this._isBranded;
    }

    set isBranded(branded) {
        this._isBranded = branded;
    }

    //Json conversions
    toJson() {
        return {
            "guild_id": this.id,
            "control_role": this.controlRole,
            "discal_channel": this.disCalChannel,
            "announcement_style": this.announcementStyle,
            "lang": this.lang,
            "prefix": this.prefix,
            "patron_guild": this.isPatronGuild,
            "dev_guild": this.isDevGuild,
            "max_calendars": this.maxCalendars,
            "time_format": this.timeFormat,
            "branded": this.isBranded
        };
    }

    fromJson(json: any) {
        this._id = json.guild_id;
        this.controlRole = json.control_role;
        this.disCalChannel = json.discal_channel;
        this.announcementStyle = json.announcement_style;
        this.lang = json.lang;
        this.prefix = json.prefix;
        this.isPatronGuild = json.patron_guild;
        this.isDevGuild = json.dev_guild;
        this.maxCalendars = json.max_calendars;
        this.timeFormat = json.time_format;
        this.isBranded = json.branded;

        return this;
    }
}
