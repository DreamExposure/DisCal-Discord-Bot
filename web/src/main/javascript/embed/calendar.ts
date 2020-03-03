import {EmbedCalendar} from "@/objects/calendar/EmbedCalendar";

let embedCalendar: EmbedCalendar;

export function loadEmbedCalendar(key: string, url: string) {
	embedCalendar = new EmbedCalendar().init(key, url);
}

//Handle user input for the calendar....
export function previousEmbedMonth() {
	embedCalendar.selectedDate.setMonth(embedCalendar.selectedDate.getMonth() - 1);
	embedCalendar.selectedDate.setDate(1);

	embedCalendar.setMonth(embedCalendar.selectedDate);

	embedCalendar.getEventsForMonth();
}

export function nextEmbedMonth() {
	embedCalendar.selectedDate.setMonth(embedCalendar.selectedDate.getMonth() + 1);
	embedCalendar.selectedDate.setDate(1);

	embedCalendar.setMonth(embedCalendar.selectedDate);

	embedCalendar.getEventsForMonth();
}

export function selectEmbedDate(clickedId: string) {
	let e = document.getElementById(clickedId)!;
	let dateString = e.innerHTML.split("[")[0];
	if (dateString !== "") {
		let dateNum = parseInt(dateString);

		embedCalendar.selectedDate.setDate(dateNum);

		embedCalendar.setMonth(embedCalendar.selectedDate);

		embedCalendar.getEventsForMonth();

		embedCalendar.getEventsForSelectedDate();
	}
}