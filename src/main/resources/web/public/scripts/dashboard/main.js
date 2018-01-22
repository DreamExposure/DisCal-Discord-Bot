function useSimpleAnnouncements(useSimple) {
	window.location.href = "/api/v1/dashboard/update/settings?simple-ann=" + useSimple;
}

function enableBranding(useBranding) {
	window.location.href = "/api/v1/dashboard/update/settings?branding=" + useBranding;
}