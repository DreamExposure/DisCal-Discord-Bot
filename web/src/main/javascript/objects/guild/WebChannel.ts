export class WebChannel {
    private _id: string = "";
    private _name: string = "";

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

    //Json conversions
    toJson() {
        return {
            "id": this.id,
            "name": this.name
        };
    }

    fromJson(json: any) {
        this.id = json.id;
        this.name = json.name;

        return this;
    }
}
