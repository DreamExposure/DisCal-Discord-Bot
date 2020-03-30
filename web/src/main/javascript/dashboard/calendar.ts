import {WebGuild} from "@/objects/guild/WebGuild";
import {TaskCallback} from "@/objects/task/TaskCallback";
import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";
import {Snackbar} from "@/utils/snackbar";
import {TaskType} from "@/enums/TaskType";
import {ElementUtil} from "@/utils/ElementUtil";
import {WebGuildGetRequest} from "@/network/guild/WebGuildGetRequest";

export class DashboardCalendarRunner implements TaskCallback {
	guild: WebGuild = new WebGuild("");

	apiUrl: string;
	apiKey: string;

	guildId: string;
	userId: string;

	constructor(apiKey: string, apiUrl: string, userId: string) {
		this.apiKey = apiKey;
		this.apiUrl = apiUrl;
		this.userId = userId;

		this.guildId = window.location.pathname.split("/")[2];
	}

	start() {
		//First thing we need to do is load the web guild data so we can display it for editing.
		let wgr = new WebGuildGetRequest(this.guildId, this.userId, this);
		wgr.provideApiDetails(this.apiKey, this.apiUrl);

		wgr.execute();

		//TODO: Handle calendar not guild!!!!!!!!
	}

	private handleWebGuildGet(status: NetworkCallStatus) {
		this.guild = new WebGuild(this.guildId).fromJson(status.body);

		//Hide loader and show the data container...
		ElementUtil.hideLoader();
	}

	onCallback(status: NetworkCallStatus): void {
		if (status.isSuccess) {
			switch (status.type) {
				case TaskType.WEB_GUILD_GET:
					this.handleWebGuildGet(status);
					break;
				default:
					break;
			}
		} else {
			switch (status.type) {
				case TaskType.WEB_GUILD_GET:
					if (status.code == 404) {
						ElementUtil.hideLoader();
						document.getElementById("not-connected")!.hidden = false;
						Snackbar.showSnackbar("Guild not Found");
					} else {
						Snackbar.showSnackbar("[ERROR] " + status.message);
					}
					break;
				default:
					Snackbar.showSnackbar("[ERROR] " + status.message);
					break;
			}
		}
	}
}