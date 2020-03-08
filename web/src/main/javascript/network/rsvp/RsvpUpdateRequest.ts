import {AsyncTask} from "@/objects/task/AsyncTask";
import {TaskCallback} from "@/objects/task/TaskCallback";
import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";
import {TaskType} from "@/enums/TaskType";
import {RsvpData} from "@/objects/event/RsvpData";
import jqXHR = JQuery.jqXHR;

export class RsvpUpdateRequest implements AsyncTask {
    private readonly rsvpToAdd: RsvpData;
    private readonly rsvpToRemove: RsvpData;

    readonly callback: TaskCallback;

    apiKey: string;
    apiUrl: string;


    constructor(toAdd: RsvpData, toRemove: RsvpData, callback: TaskCallback) {
        this.rsvpToAdd = toAdd;
        this.rsvpToRemove = toRemove;
        this.callback = callback;
    }

    provideApiDetails(apiKey: string, apiUrl: string): void {
        this.apiKey = apiKey;
		this.apiUrl = apiUrl;
    }

    execute(): void {
        let bodyRaw: any = {
            "guild_id": this.rsvpToAdd.guildId,
            "event_id": this.rsvpToAdd.eventId
        };

        //add the lists here... (technically duplicated event/guild ID data, but its easier...)
        bodyRaw.to_add = this.rsvpToAdd.toJson();
        bodyRaw.to_remove = this.rsvpToRemove.toJson();

        $.ajax({
            url: this.apiUrl + "/v2/rsvp/update",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.apiKey
            },
            method: "POST",
            dataType: "json",
            data: JSON.stringify(bodyRaw),
            success: function (json: any) {
                let status = new NetworkCallStatus(true, TaskType.RSVP_UPDATE);
                status.code = 200;
                status.body = json;
                status.message = "Success";

                this.onComplete(status);

            }.bind(this),
            error: function (jqXHR: jqXHR) {

                let status = new NetworkCallStatus(false, TaskType.RSVP_UPDATE);
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