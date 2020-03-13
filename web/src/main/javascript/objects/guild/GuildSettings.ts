export class GuildSettings {
	private _id: number;
	private _hasExternalCalendar: boolean = false;
	private _controlRole: string = "everyone";
	private _disCalChannel: string = "all";
	private _hasSimpleAnnouncements: boolean = false;
	private _lang: string = "";
	private _prefix: string = "";
	private _isPatronGuild: boolean = false;
	private _isDevGuild: boolean = false;
	private _maxCalendars: number = 1;
	private _usingTwelveHour: boolean = false;
	private _isBranded: boolean = false;

	constructor(id: number) {
		this._id = id;
	}

	//Getter/setter pairs
	get id() {
		return this._id;
	}

    get hasExternalCalendar() {
        return this._hasExternalCalendar;
    }

    set hasExternalCalendar(external) {
        this._hasExternalCalendar = external;
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

    get hasSimpleAnnouncements() {
        return this._hasSimpleAnnouncements;
    }

    set hasSimpleAnnouncements(simpleAnnouncements) {
        this._hasSimpleAnnouncements = simpleAnnouncements;
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

    get usingTwelveHour() {
        return this._usingTwelveHour;
    }

    set usingTwelveHour(using) {
        this._usingTwelveHour = using;
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
            "external_calendar": this.hasExternalCalendar,
            "control_role": this.controlRole,
            "discal_channel": this.disCalChannel,
            "simple_announcement": this.hasSimpleAnnouncements,
            "lang": this.lang,
            "prefix": this.prefix,
            "patron_guild": this.isPatronGuild,
            "dev_guild": this.isDevGuild,
            "max_calendars": this.maxCalendars,
            "twelve_hour": this.usingTwelveHour,
            "branded": this.isBranded
        };
    }

    fromJson(json: any) {
        this._id = json.guild_id;
        this.hasExternalCalendar = json.external_calendar;
        this.controlRole = json.control_role;
        this.disCalChannel = json.discal_channel;
        this.hasSimpleAnnouncements = json.simple_announcement;
        this.lang = json.lang;
        this.prefix = json.prefix;
        this.isPatronGuild = json.patron_guild;
        this.isDevGuild = json.dev_guild;
        this.maxCalendars = json.max_calendars;
        this.usingTwelveHour = json.twelve_hour;
        this.isBranded = json.branded;

        return this;
    }
}