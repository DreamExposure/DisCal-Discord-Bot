import {EventFrequency} from "@/enums/EventFrequency";

export class Recurrence {
    private _frequency: EventFrequency;
    private _interval: number;
    private _count: number;

    constructor() {
        this.frequency = EventFrequency.DAILY;
        this.interval = 1;
        this.count = -1;
    }

    //Getter/setter pairs
    get frequency() {
        return this._frequency;
    }

    set frequency(freq) {
        this._frequency = freq;
    }

    get interval() {
        return this._interval;
    }

    set interval(int) {
        this._interval = int;
    }

    get count() {
        return this._count;
    }

    set count(c) {
        this._count = c;
    }

    //Json conversions
    toJson() {
        return {
            "frequency": EventFrequency[this.frequency],
            "interval": this.interval,
            "count": this.count
        }
    }

    fromJson(json: any) {
        this.frequency = (<any>EventFrequency)[json.frequency];
        this.interval = json.interval;
        this.count = json.count;

        return this;
    }
}