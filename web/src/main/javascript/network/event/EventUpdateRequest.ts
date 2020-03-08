import {AsyncTask} from "@/objects/task/AsyncTask";
import {TaskCallback} from "@/objects/task/TaskCallback";
import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";
import {Event} from "@/objects/event/Event";
import {TaskType} from "@/enums/TaskType";
import {EventColor} from "@/enums/EventColor";
import jqXHR = JQuery.jqXHR;

export class EventUpdateRequest implements AsyncTask {
    private readonly guildId: number;
    private readonly calNum: number;
    private readonly event: Event;

    readonly callback: TaskCallback;

    apiKey: string;
    apiUrl: string;

    constructor(guildId: number, calNum: number, event: Event, callback: TaskCallback) {
        this.guildId = guildId;
        this.calNum = calNum;
        this.event = event;
        this.callback = callback;
    }

    provideApiDetails(apiKey: string, apiUrl: string): void {
		this.apiKey = apiKey;
		this.apiUrl = apiUrl;
	}


    execute(): void {
        let bodyRaw: any = {
            "guild_id": this.guildId,
            "calendar_number": this.calNum,
            "event_id": this.event.eventId,
            "epoch_start": this.event.epochStart,
            "epoch_end": this.event.epochEnd,
            "color": EventColor[this.event.color],
            "recur": this.event.doesRecur
        };

        //Add the optional stuffs
        if (this.event.summary.length > 0) {
            bodyRaw.summary = this.event.summary;
        }
        if (this.event.description.length > 0) {
            bodyRaw.description = this.event.description;
        }
        if (this.event.location.length > 0) {
            bodyRaw.location = this.event.location;
        }
        if (this.event.image.length > 0) {
            bodyRaw.image = this.event.image; //Backend will verify if link is valid.
        }
        if (this.event.doesRecur) {
            bodyRaw.recurrence = this.event.recurrence.toJson();
        }


        $.ajax({
            url: this.apiUrl + "/v2/events/update",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.apiKey
            },
            method: "POST",
            dataType: "json",
            data: JSON.stringify(bodyRaw),
            success: function (json: any) {
                let status = new NetworkCallStatus(true, TaskType.EVENT_UPDATE);
                status.code = 200;
                status.body = json;
                status.message = json.message;

                this.onComplete(status);

            }.bind(this),
            error: function (jqXHR: jqXHR) {
                let status = new NetworkCallStatus(false, TaskType.EVENT_UPDATE);
                status.code = jqXHR.status;
                status.body = jqXHR.responseJSON;
                status.message = jqXHR.responseJSON.message;

                this.onComplete(status);
            }.bind(this)
        });
    }

    onComplete(status: NetworkCallStatus): void {
        this.callback.onCallback(status);
    }
}