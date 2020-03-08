import {WebGuild} from "@/objects/guild/WebGuild";
import {TaskCallback} from "@/objects/task/TaskCallback";
import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";
import {Snackbar} from "@/utils/snackbar";
import {TaskType} from "@/enums/TaskType";
import {ElementUtil} from "@/utils/ElementUtil";
import {WebGuildGetRequest} from "@/network/guild/WebGuildGetRequest";
import {GuildSettingsUpdateRequest} from "@/network/guild/settings/GuildSettingsUpdateRequest";
import {WebGuildUpdateRequest} from "@/network/guild/WebGuildUpdateRequest";

export class DashboardGuildRunner implements TaskCallback {
	guild: WebGuild;

	apiUrl: string;
	apiKey: string;

	guildId: number;
	userId: number;

	constructor(apiKey: string, apiUrl: string, userId: number) {
		this.apiKey = apiKey;
		this.apiUrl = apiUrl;
		this.userId = userId;

		this.guildId = parseInt(window.location.pathname.split("/")[2]);
	}

	startDashboardGuildPage() {
		//First thing we need to do is load the web guild data so we can display it for editing.
		let wgr = new WebGuildGetRequest(this.guildId, this.userId, this);
		wgr.provideApiDetails(this.apiKey, this.apiUrl);

		wgr.execute();
	}

	private handleWebGuildGet(status: NetworkCallStatus) {
		this.guild = new WebGuild(this.guildId).fromJson(status.body);

		//TODO: load in settings data
		(<HTMLInputElement>document.getElementById("nickname-input")).value = this.guild.botNick;
		document.getElementById("nick-update-btn")!.onclick = function () {
			this.updateBotNick()
		}.bind(this);

		(<HTMLInputElement>document.getElementById("prefix-input")).value = this.guild.settings.prefix;
		document.getElementById("prefix-update-btn")!.onclick = function () {
			this.updatePrefix()
		}.bind(this);

		//load data that cannot be edited
		if (this.guild.settings.isPatronGuild) {
			(<HTMLParagraphElement>document.getElementById("patron-display")).innerText = "This guild is a patron guild";
		} else {
			(<HTMLParagraphElement>document.getElementById("patron-display")).innerText = "This guild is NOT a patron guild";
		}
		if (this.guild.settings.isDevGuild) {
			(<HTMLParagraphElement>document.getElementById("dev-display")).innerText = "This guild is a dev guild";
		} else {
			(<HTMLParagraphElement>document.getElementById("dev-display")).innerText = "This guild is NOT a dev guild";
		}
		//display what shard the server is on when we support that...


		//Hide loader and show the data container...
		ElementUtil.hideLoader();
		document.getElementById("guild-settings")!.hidden = false;
	}

	private updateBotNick() {
		let request = new WebGuildUpdateRequest(this.guildId, this);
		request.provideApiDetails(this.apiKey, this.apiUrl);

		request.botNick = (<HTMLInputElement>document.getElementById("nickname-input")).value;
		this.guild.botNick = request.botNick;

		request.execute();
	}

	private updatePrefix() {
		let request = new GuildSettingsUpdateRequest(this.guildId, this);
		request.provideApiDetails(this.apiKey, this.apiUrl);

		request.prefix = (<HTMLInputElement>document.getElementById("prefix-input")).value;
		this.guild.settings.prefix = request.prefix;

		request.execute();

	}

	onCallback(status: NetworkCallStatus): void {
		if (status.isSuccess) {
			switch (status.type) {
				case TaskType.GUILD_SETTINGS_GET:
					this.handleWebGuildGet(status);
					break;
				case TaskType.GUILD_SETTINGS_UPDATE:
					Snackbar.showSnackbar(status.message);
					break;
				case TaskType.WEB_GUILD_UPDATE:
					Snackbar.showSnackbar(status.message);
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