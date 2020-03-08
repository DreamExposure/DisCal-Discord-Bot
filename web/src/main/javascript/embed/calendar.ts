import {EmbedCalendar} from "@/objects/calendar/EmbedCalendar";

export class EmbedCalendarRunner {
	private embedCalendar: EmbedCalendar;


	constructor() {
		this.embedCalendar = new EmbedCalendar();
	}

	init(key: string, url: string) {
		this.embedCalendar.init(key, url);
	}

	//Handle user input for the calendar....
	previousEmbedMonth() {
		this.embedCalendar.selectedDate.setMonth(this.embedCalendar.selectedDate.getMonth() - 1);
		this.embedCalendar.selectedDate.setDate(1);

		this.embedCalendar.setMonth(this.embedCalendar.selectedDate);

		this.embedCalendar.getEventsForMonth();
	}

	nextEmbedMonth() {
		this.embedCalendar.selectedDate.setMonth(this.embedCalendar.selectedDate.getMonth() + 1);
		this.embedCalendar.selectedDate.setDate(1);

		this.embedCalendar.setMonth(this.embedCalendar.selectedDate);

		this.embedCalendar.getEventsForMonth();
	}

	selectEmbedDate(clickedId: string) {
		let e = document.getElementById(clickedId)!;
		let dateString = e.innerHTML.split("[")[0];
		if (dateString !== "") {
			let dateNum = parseInt(dateString);

			this.embedCalendar.selectedDate.setDate(dateNum);

			this.embedCalendar.setMonth(this.embedCalendar.selectedDate);

			this.embedCalendar.getEventsForMonth();

			this.embedCalendar.getEventsForSelectedDate();
		}
	}
}