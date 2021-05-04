import {EventColor, eventColorRGB} from "@/enums/EventColor";
import {Recurrence} from "@/objects/event/Recurrence";

export class Event {
    private _eventId: string = "";
    private _epochStart: number = 0;
    private _epochEnd: number = 0;

    private _summary: string = "";
    private _description: string = "";
    private _location: string = "";

    private _isParent: boolean = false;
    private _color: EventColor = EventColor.NONE;

    private _recur: boolean = false;
    private _recurrence: Recurrence = new Recurrence();
    private _rrule: String = "";

    private _image: string = "";

    constructor() {
    }

    //Setter/getter pairs
    get eventId() {
        return this._eventId;
    }

    set eventId(id) {
        this._eventId = id;
    }

    get epochStart() {
        return this._epochStart;
    }

    set epochStart(epoch) {
        this._epochStart = epoch;
    }

    get epochEnd() {
        return this._epochEnd;
    }

    set epochEnd(epoch) {
        this._epochEnd = epoch;
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

    set description(desc) {
        this._description = desc;
    }

    get location() {
        return this._location;
    }

    set location(loc) {
        this._location = loc;
    }

    get isParent() {
        return this._isParent;
    }

    set isParent(isPar) {
        this._isParent = isPar;
    }

    get color() {
        return this._color;
    }

    set color(col) {
        this._color = col;
    }

    get doesRecur() {
        return this._recur;
    }

    set doesRecur(does) {
        this._recur = does;
    }

    get recurrence() {
        return this._recurrence;
    }

    set recurrence(rec) {
        this._recurrence = rec;
    }

    get rrule() {
        return this._rrule;
    }

    set rrule(rr) {
        this._rrule = rr;
    }

    get image() {
        return this._image;
    }

    set image(img) {
        this._image = img;
    }


    //Json conversion
    toFullCalEvent(editable: boolean) {
        let event: any = {
            id: this.eventId,
            groupId: this.eventId.split("_")[0],
            title: this.summary,
            description: this.description,
            location: this.location,
            image: this.image,
            eventColor: this.color,

            start: this.epochStart,
            end: this.epochEnd,

            overlap: true,
            editable: editable,

            rawEvent: this,
        };

        if (this.color != EventColor.NONE) {
            event.borderColor = eventColorRGB(this.color);
        }
        if (this.doesRecur) {
            event.rrule = this.rrule;
        }

        return event;
    }


    toJson() {
        let json: any = {
            "event_id": this.eventId,
            "epoch_start": this.epochStart,
            "epoch_end": this.epochEnd,
            "color": EventColor[this.color],
            "recur": this.doesRecur
        };

        if (this.summary.length > 0) {
            json.name = this.summary;
        }
        if (this.description.length) {
            json.description = this.description;
        }
        if (this.location.length > 0) {
            json.location = this.location;
        }
        if (this.doesRecur) {
            json.recurrence = this.recurrence.toJson();
            json.rrule = this.rrule
        }
        if (this.image.length > 0) {
            json.image = this.image;
        }

        return json;
    }

    fromJson(json: any) {
        this.eventId = json.event_id;
        this.epochStart = json.epoch_start;
        this.epochEnd = json.epoch_end;

        if (json.hasOwnProperty("name")) {
            this.summary = json.name;
        }
        if (json.hasOwnProperty("description")) {
            this.description = json.description;
        }
        if (json.hasOwnProperty("location")) {
            this.location = json.location;
        }

        this.isParent = json.is_parent;
        this.color = (<any>EventColor)[json.color];

        this.doesRecur = json.recur;
        if (this.doesRecur) {
            this.recurrence = new Recurrence().fromJson(json.recurrence);
            this.rrule = json.rrule;
        }

        if (json.hasOwnProperty("image")) {
            this.image = json.image;
        }

        return this;
    }
}
