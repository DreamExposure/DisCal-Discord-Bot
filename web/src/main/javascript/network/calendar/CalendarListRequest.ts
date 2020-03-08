import {AsyncTask} from "@/objects/task/AsyncTask";
import {TaskCallback} from "@/objects/task/TaskCallback";
import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";
import {TaskType} from "@/enums/TaskType";
import jqXHR = JQuery.jqXHR;

export class CalendarListRequest implements AsyncTask {
    private readonly guildId: number;

    readonly callback: TaskCallback;

    apiKey: string;
    apiUrl: string;

    constructor(guildId: number, callback: TaskCallback) {
        this.guildId = guildId;
        this.callback = callback;
    }

    provideApiDetails(apiKey: string, apiUrl: string): void {
        this.apiKey = apiKey;
        this.apiUrl = apiKey;
    }


    execute(): void {
        let bodyRaw: any = {
            "guild_id": this.guildId
        };


        $.ajax({
            url: this.apiUrl + "/v2/calendar/list",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.apiKey
            },
            method: "POST",
            dataType: "json",
            data: JSON.stringify(bodyRaw),
            success: function (json: any) {
                let status = new NetworkCallStatus(true, TaskType.CALENDAR_LIST);
                status.code = 200;
                status.body = json;
                status.message = "Success";

                this.onComplete(status);

            }.bind(this),
            error: function (jqXHR: jqXHR) {
                let status = new NetworkCallStatus(false, TaskType.CALENDAR_LIST);
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