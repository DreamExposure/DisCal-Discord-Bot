export class ElementUtil {
	static showLoader() {
		document.getElementsByClassName("loader")[0].setAttribute("hidden", "show");
	}

	static hideLoader() {
		document.getElementsByClassName("loader")[0].setAttribute("hidden", "hidden");
	}

	static showCalendarContainer() {
		document.getElementById("calendar-container")!.hidden = false;
	}

	static hideCalendarContainer() {
		document.getElementById("calendar-container")!.hidden = true;
	}

	static showEventsContainer() {
		document.getElementById("events-container")!.hidden = false;
	}

	static hideEventsContainer() {
		document.getElementById("events-container")!.hidden = true;
	}
}
