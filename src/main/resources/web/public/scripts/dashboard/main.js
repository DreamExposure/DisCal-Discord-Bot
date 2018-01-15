function goToGuild(guildId) {
	window.location.href = "/account/dashboard/select?guild=" + guildId;
}

function useSimpleAnnouncements(useSimple) {
	window.location.href = "/account/dashboard/update/get?simple-ann=" + useSimple;
}

function enableBranding(useBranding) {
	window.location.href = "/account/dashboard/update/get?branding=" + useBranding;
}