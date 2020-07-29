export class WebChannel {
	private _id: string = "";
	private _name: string = "";
	private _isDisCalChannel: boolean = false;

	constructor() {
	}

	//Getter/setter pairs

	get id() {
		return this._id;
	}

    set id(id) {
        this._id = id;
    }

    get name() {
        return this._name;
    }

    set name(name) {
        this._name = name;
    }

    get isDisCalChannel() {
        return this._isDisCalChannel;
    }

    set isDisCalChannel(isDisCalChannel) {
        this._isDisCalChannel = isDisCalChannel;
    }

    //Json conversions
    toJson() {
        return {
            "id": this.id,
            "name": this.name,
            "discal_channel": this.isDisCalChannel
        };
    }

    fromJson(json: any) {
        this.id = json.id;
        this.name = json.name;
        this.isDisCalChannel = json.discal_channel;

        return this;
    }
}