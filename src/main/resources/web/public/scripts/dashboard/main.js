function goToGuild(guildId) {
	window.location.href = "/account/dashboard/select?guild=" + guildId;
}

function useSimpleAnnouncements(useSimple) {
	window.location.href = "/account/dashboard/update?simple-ann=" + useSimple;
}