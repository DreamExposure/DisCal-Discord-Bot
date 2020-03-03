import {AsyncTask} from "@/objects/task/AsyncTask";
import {TaskCallback} from "@/objects/task/TaskCallback";
import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";
import {TaskType} from "@/enums/TaskType";
import {Announcement} from "@/objects/announcement/Announcement";
import {AnnouncementType} from "@/enums/AnnouncementType";
import {EventColor} from "@/enums/EventColor";
import jqXHR = JQuery.jqXHR;

export class AnnouncementCreateRequest implements AsyncTask {
    private readonly guildId: number;
    private readonly announcement: Announcement;

    readonly callback: TaskCallback;

    apiKey: string;
    apiUrl: string;

    constructor(guildId: number, ann: Announcement, callback: TaskCallback) {
        this.guildId = guildId;
        this.announcement = ann;
        this.callback = callback;
    }

    provideApiDetails(apiKey: string, apiUrl: string): void {
        this.apiKey = apiKey;
        this.apiUrl = apiKey;
    }


    execute(): void {
        let bodyRaw: any = {
            "guild_id": this.guildId,
            "channel": this.announcement.announcementChannelId,
            "type": AnnouncementType[this.announcement.announcementType],
            "color": EventColor[this.announcement.eventColor],
            "event_id": this.announcement.eventId,
            "hours": this.announcement.hoursBefore,
            "minutes": this.announcement.minutesBefore,
            "info": this.announcement.info,
            "info_only": this.announcement.infoOnly
        };


        $.ajax({
            url: this.apiUrl + "/v2/announcement/create",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.apiKey
            },
            method: "POST",
            dataType: "json",
            data: JSON.stringify(bodyRaw),
            success: function (json: any) {
                let status = new NetworkCallStatus(true, TaskType.ANNOUNCEMENT_CREATE);
                status.code = 200;
                status.body = json;
                status.message = json.message;

                this.onComplete(status);

            }.bind(this),
            error: function (jqXHR: jqXHR) {
                let status = new NetworkCallStatus(false, TaskType.ANNOUNCEMENT_CREATE);
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