export class ElementUtil {
	static showLoader() {
		document.getElementsByClassName("loader")[0].setAttribute("hidden", "show");
	}

	static hideLoader() {
		document.getElementsByClassName("loader")[0].setAttribute("hidden", "hidden");
	}

	static showCalendarContainer() {
		document.getElementById("calendar-container")!.setAttribute("hidden", "show");
	}

	static hideCalendarContainer() {
		document.getElementById("calendar-container")!.setAttribute("hidden", "hidden");
	}

	static showEventsContainer() {
		document.getElementById("events-container")!.setAttribute("hidden", "show");
	}

	static hideEventsContainer() {
		document.getElementById("events-container")!.setAttribute("hidden", "hidden");
	}
}
