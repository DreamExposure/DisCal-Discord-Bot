export class WebCalendar {
	private _id: string;
	private _address: string;
	private _number: number;
	private _external: boolean;
	private _summary: string;
	private _description: string;
	private _timezone: string;

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
			"timezone": this.timezone
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

		return this;
	}
}