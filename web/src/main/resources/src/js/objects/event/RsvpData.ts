export class RsvpData {
    private readonly _guildId: number;

    private _eventId: string;
    private _eventEnd: number;

    private readonly _goingOnTime: string[];
    private readonly _goingLate: string[];
    private readonly _notGoing: string[];
    private readonly _undecided: string[];

    constructor(guildId: number) {
        this._guildId = guildId;

        this._goingOnTime = [];
        this._goingLate = [];
        this._notGoing = [];
        this._undecided = [];
    }

    //Getter/setter pairs
    get guildId() {
        return this._guildId;
    }

    get eventId() {
        return this._eventId;
    }

    set eventId(id) {
        this._eventId = id;
    }

    get eventEnd() {
        return this._eventEnd;
    }

    set eventEnd(end) {
        this._eventEnd = end;
    }

    get goingOnTime() {
        return this._goingOnTime;
    }

    get goingLate() {
        return this._goingLate;
    }

    get notGoing() {
        return this._notGoing;
    }

    get undecided() {
        return this._undecided;
    }

    //Json conversions
    toJson() {
        let json: any = {
            "guild_id": this.guildId,
            "event_id": this.eventId,
            "event_end": this.eventEnd
        };

        let jOnTime = [];
        for (let u in this.goingOnTime) {
            jOnTime.push(u);
        }
        json.on_time = jOnTime;

        let jLate = [];
        for (let u in this.goingLate) {
            jLate.push(u);
        }
        json.late = jLate;

        let jNot = [];
        for (let u in this.notGoing) {
            jNot.push(u);
        }
        json.on_time = jNot;

        let jUndecided = [];
        for (let u in this.undecided) {
            jUndecided.push(u);
        }
        json.on_time = jUndecided;


        return json;
    }

    fromJson(json: any) {
        this.eventId = json.event_id;
        this.eventEnd = json.event_end;

        for (let u in json.on_time) {
            this.goingOnTime.push(u);
        }

        for (let u in json.late) {
            this.goingLate.push(u);
        }

        for (let u in json.not_going) {
            this.notGoing.push(u);
        }

        for (let u in json.undecided) {
            this.undecided.push(u);
        }

        return this;
    }
}