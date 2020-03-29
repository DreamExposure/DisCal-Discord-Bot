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
	guild: WebGuild = new WebGuild(0);

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

		//load in settings data
		(<HTMLInputElement>document.getElementById("nickname-input")).value = this.guild.botNick;
		document.getElementById("nick-update-btn")!.onclick = function () {
			this.updateBotNick();
		}.bind(this);

		(<HTMLInputElement>document.getElementById("prefix-input")).value = this.guild.settings.prefix;
		document.getElementById("prefix-update-btn")!.onclick = function () {
			this.updatePrefix();
		}.bind(this);

		let controlRoleSelect = document.getElementById("control-role-select")!;
		for (let i = 0; i < this.guild.roles.length; i++) {
			let role = this.guild.roles[i];
			let opt = document.createElement("option");

			opt.innerHTML = role.name;
			opt.value = role.id.toString();
			opt.selected = role.id.toString() == this.guild.settings.controlRole;

			controlRoleSelect.appendChild(opt);
		}
		document.getElementById("control-role-update-btn")!.onclick = function () {
			this.updateControlRole();
		}.bind(this);

		let chanSelect = document.getElementById("discal-chan-select")!;
		for (let i = 0; i < this.guild.channels.length; i++) {
			let chan = this.guild.channels[i];
			let opt = document.createElement("option");

			opt.innerHTML = chan.name;
			opt.value = chan.id.toString();
			if (chan.id == 0 && this.guild.settings.disCalChannel === "all") {
				opt.selected = true;
			} else {
				opt.selected = chan.id.toString() == this.guild.settings.disCalChannel;
			}
			chanSelect.appendChild(opt);
		}
		document.getElementById("discal-chan-update-btn")!.onclick = function () {
			this.updateDiscalChannel();
		}.bind(this);

		let langSelect = document.getElementById("discal-lang-select")!;
		for (let i = 0; i < this.guild.availableLangs.length; i++) {
			let lang = this.guild.availableLangs[i];
			let opt = document.createElement("option");

			opt.innerHTML = lang.toUpperCase();
			opt.value = lang.toUpperCase();

			opt.selected = lang.toUpperCase() == this.guild.settings.lang.toUpperCase();
			langSelect.appendChild(opt);
		}
		document.getElementById("discal-lang-update-btn")!.onclick = function () {
			this.updateLang();
		}.bind(this);


		//load data that cannot be edited
		(<HTMLParagraphElement>document.getElementById("shard-display")).innerHTML = "Shard: " + this.guild.shard;

		if (this.guild.settings.isPatronGuild) {
			(<HTMLParagraphElement>document.getElementById("patron-display")).innerHTML = "This guild is a patron guild";
		} else {
			(<HTMLParagraphElement>document.getElementById("patron-display")).innerHTML = "This guild is NOT a patron guild";
		}
		if (this.guild.settings.isDevGuild) {
			(<HTMLParagraphElement>document.getElementById("dev-display")).innerHTML = "This guild is a dev guild";
		} else {
			(<HTMLParagraphElement>document.getElementById("dev-display")).innerHTML = "This guild is NOT a dev guild";
		}
		//display what shard the server is on when we support that...


		//Hide loader and show the data container...
		ElementUtil.hideLoader();
		document.getElementById("guild-settings")!.hidden = false;
		document.getElementById("guild-data")!.hidden = false;
	}

	private updateBotNick() {
		let request = new WebGuildUpdateRequest(this.guildId, this);
		request.provideApiDetails(this.apiKey, this.apiUrl);

		request.botNick = (<HTMLInputElement>document.getElementById("nickname-input")).value;
		this.guild.botNick = (<HTMLInputElement>document.getElementById("nickname-input")).value;

		request.execute();
	}

	private updatePrefix() {
		let request = new GuildSettingsUpdateRequest(this.guildId, this);
		request.provideApiDetails(this.apiKey, this.apiUrl);

		request.prefix = (<HTMLInputElement>document.getElementById("prefix-input")).value;
		this.guild.settings.prefix = request.prefix;

		request.execute();
	}

	private updateControlRole() {
		let request = new GuildSettingsUpdateRequest(this.guildId, this);
		request.provideApiDetails(this.apiKey, this.apiUrl);

		let select = <HTMLSelectElement>document.getElementById("control-role-select");
		request.controlRole = select.selectedOptions[0].value;
		this.guild.settings.controlRole = request.controlRole;

		request.execute();
	}

	private updateDiscalChannel() {
		let request = new GuildSettingsUpdateRequest(this.guildId, this);
		request.provideApiDetails(this.apiKey, this.apiUrl);

		let select = <HTMLSelectElement>document.getElementById("discal-chan-select");
		request.discalChannel = select.selectedOptions[0].value;
		this.guild.settings.disCalChannel = request.discalChannel;

		request.execute();
	}

	private updateLang() {
		let request = new GuildSettingsUpdateRequest(this.guildId, this);
		request.provideApiDetails(this.apiKey, this.apiUrl);

		let select = <HTMLSelectElement>document.getElementById("discal-lang-select");
		request.lang = select.selectedOptions[0].value;
		this.guild.settings.lang = request.lang;

		request.execute();
	}

	onCallback(status: NetworkCallStatus): void {
		if (status.isSuccess) {
			switch (status.type) {
				case TaskType.WEB_GUILD_GET:
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