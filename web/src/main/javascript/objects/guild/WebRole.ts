export class WebRole {
	private _id: number = 0;
	private _name: string = "";
	private _isManaged: boolean = false;
	private _isControlRole: boolean = false;
	private _isEveryone: boolean = false;

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

    get isManaged() {
        return this._isManaged;
    }

    set isManaged(managed) {
        this._isManaged = managed;
    }

    get isControlRole() {
        return this._isControlRole;
    }

    set isControlRole(controlRole) {
        this._isControlRole = controlRole;
    }

    get isEveryone() {
        return this._isEveryone;
    }

    set isEveryone(everyone) {
        this._isEveryone = everyone;
    }

    //Json conversions
    toJson() {
        return {
            "id": this.id,
            "name": this.name,
            "managed": this.isManaged,
            "control_role": this.isControlRole,
            "everyone": this.isEveryone
        };
    }

    fromJson(json: any) {
        this.id = json.id;
        this.name = json.name;
        this.isManaged = json.managed;
        this.isControlRole = json.control_role;
        this.isEveryone = json.everyone;

        return this;
    }
}