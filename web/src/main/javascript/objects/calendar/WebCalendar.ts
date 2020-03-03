export class WebCalendar {
    private _id: number;
    private _address: string;
    private _link: string;
    private _name: string;
    private _description: string;
    private _timezone: string;
    private _isExternal: boolean;

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

    get link() {
        return this._link;
    }

    set link(link) {
        this._link = link;
    }

    get name() {
        return this._name;
    }

    set name(name) {
        this._name = name;
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

    get isExternal() {
        return this._isExternal;
    }

    set isExternal(external) {
        this._isExternal = external;
    }

    //Json conversions
    toJson() {
        let json: any = {
            "id": this.id,
            "address": this.address,
            "link": this.link,
            "name": this.name,
            "timezone": this.timezone,
            "external": this.isExternal
        };

        if (this.description != null) {
            json.description = this.description;
        }

        return json;
    }

    fromJson(json: any) {
        this.id = json.id;
        this.address = json.address;
        this.link = json.link;
        this.name = json.name;
        if (json.hasOwnProperty("description")) {
            this.description = json.description;
        }
        this.timezone = json.timezone;
        this.isExternal = json.external;

        return this;
    }
}