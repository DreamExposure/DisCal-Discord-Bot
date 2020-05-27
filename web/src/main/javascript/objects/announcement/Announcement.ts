import {EventColor} from "@/enums/EventColor";
import {AnnouncementType} from "@/enums/AnnouncementType";
import {AnnouncementModifier} from "@/enums/AnnouncementModifier";

export class Announcement {
    private readonly _guildId: string = "";
    private _announcementId: string = "";

    private readonly _subscriberRoleIds: string[];
    private readonly _subscriberUserIds: string[];

    private _announcementChannelId: number = 0;
    private _announcementType: AnnouncementType = AnnouncementType.SPECIFIC;
    private _modifier: AnnouncementModifier = AnnouncementModifier.BEFORE;
    private _eventId: string = "";
    private _eventColor: EventColor = EventColor.NONE;
    private _hoursBefore: number = 0;
    private _minutesBefore: number = 0;
    private _info: string = "N/a";

    private _enabled: boolean = false;
    private _infoOnly: boolean = false;

    constructor(guildId: string) {
		this._guildId = guildId;

		this._subscriberRoleIds = [];
		this._subscriberUserIds = [];
	}

	//Getter/setter pairs
	get guildId() {
		return this._guildId;
	}

    get announcementId() {
        return this._announcementId;
    }

    set announcementId(id) {
        this._announcementId = id;
    }

    get subscriberRoleIds() {
        return this._subscriberRoleIds;
    }

    get subscriberUserIds() {
        return this._subscriberUserIds;
    }

    get announcementChannelId() {
        return this._announcementChannelId;
    }

    set announcementChannelId(id) {
        this._announcementChannelId = id;
    }

    get announcementType() {
        return this._announcementType;
    }

    set announcementType(type) {
        this._announcementType = type;
    }

    get modifier() {
        return this._modifier;
    }

    set modifier(mod) {
        this._modifier = mod;
    }

    get eventId() {
        return this._eventId;
    }

    set eventId(id) {
        this._eventId = id;
    }

    get eventColor() {
        return this._eventColor;
    }

    set eventColor(color) {
        this._eventColor = color;
    }

    get hoursBefore() {
        return this._hoursBefore;
    }

    set hoursBefore(hours) {
        this._hoursBefore = hours;
    }

    get minutesBefore() {
        return this._minutesBefore;
    }

    set minutesBefore(minutes) {
        this._minutesBefore = minutes;
    }

    get info() {
        return this._info;
    }

    set info(info) {
        this._info = info;
    }

    get enabled() {
        return this._enabled;
    }

    set enabled(enabled) {
        this._enabled = enabled;
    }

    get infoOnly() {
        return this._infoOnly;
    }

    set infoOnly(infoOnly) {
        this._infoOnly = infoOnly;
    }

    //Json conversions
    toJson() {
        let json: any = {
            "guild_id": this.guildId,
            "id": this.announcementId,
            "channel_id": this.announcementChannelId,
            "type": AnnouncementType[this.announcementType],
            "modifier": AnnouncementModifier[this.modifier],
            "event_id": this.eventId,
            "event_color": EventColor[this.eventColor],
            "hours": this.hoursBefore,
            "minutes": this.minutesBefore,
            "info": this.info,
            "enabled": this.enabled,
            "info_only": this.infoOnly
        };

        let jRoles = [];
        for (let i = 0; i < this.subscriberRoleIds.length; i++) {
            jRoles.push(this.subscriberRoleIds[i]);
        }
        json.subscriber_roles = jRoles;

        let jUsers = [];
        for (let i = 0; i < this.subscriberUserIds.length; i++) {
            jUsers.push(this.subscriberUserIds[i]);
        }
        json.subscrober_users = jUsers;
    }

    fromJson(json: any) {
        this.announcementId = json.id;

        for (let i = 0; i < json.subscriber_roles.length; i++) {
            this.subscriberRoleIds.push(json.subscriber_roles[i]);
        }

        for (let i = 0; i < json.subscriber_users.length; i++) {
            this.subscriberUserIds.push(json.subscriber_users[i]);
        }

        this.announcementChannelId = json.channel_id;
        this.announcementType = (<any>AnnouncementType)[json.type];
        this.modifier = (<any>AnnouncementModifier)[json.modifier];
        this.eventId = json.event_id;
        this.eventColor = (<any>EventColor)[json.event_color];
        this.hoursBefore = json.hours;
        this.minutesBefore = json.minutes;
        this.info = json.info;
        this.enabled = json.enabled;
        this.infoOnly = json.info_only;

        return this;
    }
}