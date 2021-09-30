import {CalendarHost} from "@/enums/CalendarHost";

export class WebCalendar {
    private _id: string = "";
    private _address: string = "";
    private _number: number = 1;
    private _external: boolean = false;
    private _summary: string = "";
    private _description: string = "";
    private _timezone: string = "";
    private _host: CalendarHost = CalendarHost.GOOGLE;
    private _link: string = "";
    private _hostLink: string = "";

    constructor() {
    }

    //Getter/setter pairs
    get id() {
        return this._id;
    }

    set id(id) {
        this._id = id;
    }

    get address() {
        return this._address;
    }

    set address(address) {
        this._address = address;
    }

    get number() {
        return this._number;
    }

    set number(num) {
        this._number = num;
    }

    get summary() {
        return this._summary;
    }

    set summary(summary) {
        this._summary = summary;
    }

    get description() {
        return this._description;
    }

    set description(description) {
        this._description = description;
    }

    get timezone() {
        return this._timezone;
    }

    set timezone(timezone) {
        this._timezone = timezone;
    }

    get host() {
        return this._host;
    }

    set host(host) {
        this._host = host
    }

    get link() {
        return this._link;
    }

    set link(link) {
        this._link = link;
    }

    get hostLink() {
        return this._hostLink;
    }

    set hostLink(link) {
        this._hostLink = link;
    }

    get isExternal() {
        return this._external;
    }

    set isExternal(external) {
        this._external = external;
    }

    //Json conversions
    toJson() {
        let json: any = {
            "calendar_address": this.address,
            "calendar_id": this.id,
            "calendar_number": this.number,
            "external": this.isExternal,
            "summary": this.summary,
            "description": this.description,
            "timezone": this.timezone,
            "host": CalendarHost[this.host],
            "link": this.link,
            "host_link": this.hostLink,
        };

        if (this.description != null) {
            json.description = this.description;
        }

        return json;
    }

    fromJson(json: any) {
        this.address = json.calendar_address;
        this.id = json.calendar_id;
        this.number = json.calendar_number;
        this.isExternal = json.external;
        this.summary = json.summary;
        this.description = json.description;
        this.timezone = json.timezone;
        this.host = (<any>CalendarHost)[json.host];
        this.link = json.link;
        this.hostLink = json.host_link;

        return this;
    }
}
