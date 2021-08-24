import {AsyncTask} from "@/objects/task/AsyncTask";
import {TaskCallback} from "@/objects/task/TaskCallback";
import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";
import {TaskType} from "@/enums/TaskType";
import jqXHR = JQuery.jqXHR;

export class GuildSettingsUpdateRequest implements AsyncTask {
    private readonly guildId: string;

    readonly callback: TaskCallback;

    apiKey: string = "";
    apiUrl: string = "";

    private _controlRole: string = "";
    private _simpleAnnouncements: boolean = false;
    private _lang: string = "";
    private _prefix: string = "";

    //Booleans for confirming what gets sent to API...
    private updateSimpleAnnouncements: boolean = false;

    constructor(guildId: string, callback: TaskCallback) {
        this.guildId = guildId;
        this.callback = callback;
    }

    provideApiDetails(apiKey: string, apiUrl: string): void {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    //Setter/getter pairs
    set controlRole(role: string) {
        this._controlRole = role;
    }

    get controlRole() {
        return this._controlRole;
    }

    set simpleAnnouncements(use: boolean) {
        this._simpleAnnouncements = use;
    }

    get simpleAnnouncements() {
        return this._simpleAnnouncements;
    }

    set lang(lang: string) {
        this._lang = lang;
    }

    get lang() {
        return this._lang;
    }

    set prefix(prefix: string) {
        this._prefix = prefix;
    }

    get prefix() {
        return this._prefix;
    }


    execute(): void {
        let bodyRaw: any = {
            "guild_id": this.guildId,
        };

        if (this.controlRole.length > 0) {
            bodyRaw.control_role = this.controlRole;
        }
        if (this.updateSimpleAnnouncements) {
            bodyRaw.simple_announcements = this.simpleAnnouncements;
        }
        if (this.lang.length > 0) {
            bodyRaw.lang = this.lang;
        }
        if (this.prefix.length > 0) {
            bodyRaw.prefix = this.prefix;
        }


        $.ajax({
            url: this.apiUrl + "/v2/guild/settings/update",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.apiKey
            },
            method: "POST",
            dataType: "json",
            data: JSON.stringify(bodyRaw),
            success: function (json: any) {
                let status = new NetworkCallStatus(true, TaskType.GUILD_SETTINGS_UPDATE);
                status.code = 200;
                status.body = json;
                status.message = json.message;

                this.onComplete(status);

            }.bind(this),
            error: function (jqXHR: jqXHR) {
                let status = new NetworkCallStatus(false, TaskType.GUILD_SETTINGS_UPDATE);
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
