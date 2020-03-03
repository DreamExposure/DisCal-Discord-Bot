import {AsyncTask} from "@/objects/task/AsyncTask";
import {TaskCallback} from "@/objects/task/TaskCallback";
import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";
import {TaskType} from "@/enums/TaskType";
import jqXHR = JQuery.jqXHR;

export class GuildSettingsUpdateRequest implements AsyncTask {
    private readonly guildId: number;

    readonly callback: TaskCallback;

    apiKey: string;
    apiUrl: string;

    private _controlRole: string;
    private _discalChannel: string;
    private _simpleAnnouncements: boolean;
    private _lang: string;
    private _prefix: string;

    //Booleans for confirming what gets sent to API...
    private updateControlRole: boolean;
    private updateDiscalChannel: boolean;
    private updateSimpleAnnouncements: boolean;
    private updateLang: boolean;
    private updatePrefix: boolean;

    constructor(guildId: number, callback: TaskCallback) {
        this.guildId = guildId;
        this.callback = callback;
    }

    provideApiDetails(apiKey: string, apiUrl: string): void {
        this.apiKey = apiKey;
        this.apiUrl = apiKey;
    }

    //Setters...
    set controlRole(role: string) {
        this._controlRole = role;
        this.updateControlRole = true;
    }

    set discalChannel(channel: string) {
        this._discalChannel = channel;
        this.updateDiscalChannel = true;
    }

    set simpleAnnouncements(use: boolean) {
        this._simpleAnnouncements = use;
        this.updateSimpleAnnouncements = true;
    }

    set lang(lang: string) {
        this._lang = lang;
        this.updateLang = true;
    }

    set prefix(prefix: string) {
        this._prefix = prefix;
        this.updatePrefix = true;
    }


    execute(): void {
        let bodyRaw: any = {
            "guild_id": this.guildId,
        };

        if (this.updateControlRole) {
            bodyRaw.control_role = this._controlRole;
        }
        if (this.updateDiscalChannel) {
            bodyRaw.discal_channel = this._discalChannel;
        }
        if (this.updateSimpleAnnouncements) {
            bodyRaw.simple_announcements = this._simpleAnnouncements;
        }
        if (this.updateLang) {
            bodyRaw.lang = this._lang;
        }
        if (this.updatePrefix) {
            bodyRaw.prefix = this._prefix;
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