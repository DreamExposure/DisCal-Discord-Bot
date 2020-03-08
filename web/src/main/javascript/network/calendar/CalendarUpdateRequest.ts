import {AsyncTask} from "@/objects/task/AsyncTask";
import {TaskCallback} from "@/objects/task/TaskCallback";
import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";
import {TaskType} from "@/enums/TaskType";
import jqXHR = JQuery.jqXHR;

export class CalendarUpdateRequest implements AsyncTask {
    private readonly guildId: number;
    private readonly calNum: number;

    readonly callback: TaskCallback;

    apiKey: string;
    apiUrl: string;

    private _summary: string;
    private _description: string;
    private _timezone: string;

    //Booleans for tracking what is updated.
    private updateSummary: boolean;
    private updateDescription: boolean;
    private updateTimezone: boolean;

    constructor(guildId: number, calNum: number, callback: TaskCallback) {
        this.guildId = guildId;
        this.calNum = calNum;
        this.callback = callback;
    }

    provideApiDetails(apiKey: string, apiUrl: string): void {
		this.apiKey = apiKey;
		this.apiUrl = apiUrl;
	}

    //Setters
    set summary(summary: string) {
        this._summary = summary;
        this.updateSummary = true;
    }

    set description(desc: string) {
        this._description = desc;
        this.updateDescription = true;
    }

    set timezone(tz: string) {
        this._timezone = tz;
        this.updateTimezone = true;
    }


    execute(): void {
        let bodyRaw: any = {
            "guild_id": this.guildId,
            "calendar_number": this.calNum
        };

        if (this.updateSummary) {
            bodyRaw.summary = this._summary;
        }
        if (this.updateDescription) {
            bodyRaw.description = this._description;
        }
        if (this.updateTimezone) {
            bodyRaw.timezone = this._timezone;
        }

        $.ajax({
            url: this.apiUrl + "/v2/calendar/update",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.apiKey
            },
            method: "POST",
            dataType: "json",
            data: JSON.stringify(bodyRaw),
            success: function (json: any) {
                let status = new NetworkCallStatus(true, TaskType.CALENDAR_UPDATE);
                status.code = 200;
                status.body = json;
                status.message = json.message;

                this.onComplete(status);

            }.bind(this),
            error: function (jqXHR: jqXHR) {
                let status = new NetworkCallStatus(false, TaskType.CALENDAR_UPDATE);
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