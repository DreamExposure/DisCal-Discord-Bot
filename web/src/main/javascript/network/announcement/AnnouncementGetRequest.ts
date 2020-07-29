import {AsyncTask} from "@/objects/task/AsyncTask";
import {TaskCallback} from "@/objects/task/TaskCallback";
import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";
import {TaskType} from "@/enums/TaskType";
import jqXHR = JQuery.jqXHR;

export class AnnouncementGetRequest implements AsyncTask {
	private readonly guildId: string;
	private readonly anId: string;

	readonly callback: TaskCallback;

	apiKey: string = "";
	apiUrl: string = "";

	constructor(guildId: string, anId: string, callback: TaskCallback) {
		this.guildId = guildId;
		this.anId = anId;
		this.callback = callback;
	}

	provideApiDetails(apiKey: string, apiUrl: string): void {
		this.apiKey = apiKey;
		this.apiUrl = apiUrl;
	}


    execute(): void {
        let bodyRaw: any = {
            "guild_id": this.guildId,
            "announcement_id": this.anId
        };


        $.ajax({
            url: this.apiUrl + "/v2/announcement/get",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.apiKey
            },
            method: "POST",
            dataType: "json",
            data: JSON.stringify(bodyRaw),
            success: function (json: any) {
                let status = new NetworkCallStatus(true, TaskType.ANNOUNCEMENT_GET);
                status.code = 200;
                status.body = json;
                status.message = "Success";

                this.onComplete(status);

            }.bind(this),
            error: function (jqXHR: jqXHR) {
                let status = new NetworkCallStatus(false, TaskType.ANNOUNCEMENT_GET);
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