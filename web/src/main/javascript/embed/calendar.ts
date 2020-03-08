import {EmbedCalendar} from "@/objects/calendar/EmbedCalendar";

export class EmbedCalendarRunner {
	private embedCalendar: EmbedCalendar;


	constructor() {
		this.embedCalendar = new EmbedCalendar();
	}

	init(key: string, url: string) {
		this.embedCalendar.init(key, url);

		/**loop through stuff and assign onclick to the functions here since we can't do it in html
		 *due to how the code is compiled and minified, making it impossible to call these functions
		 **/
		document.getElementById("previous-month")!.onclick = function () {
			this.previousMonth();
		}.bind(this);
		document.getElementById("next-month")!.onclick = function () {
			this.nextMonth();
		}.bind(this);

		let dateDisplays = document.getElementsByClassName("cal-date");
		for (let i = 0; i < dateDisplays.length; i++) {
			let e = (<HTMLElement>dateDisplays[i]);
			e.onclick = function () {
				this.selectDate(e.id);
			}.bind(this);
		}
	}

	//Handle user input for the calendar....
	previousMonth() {
		this.embedCalendar.selectedDate.setMonth(this.embedCalendar.selectedDate.getMonth() - 1);
		this.embedCalendar.selectedDate.setDate(1);

		this.embedCalendar.setMonth(this.embedCalendar.selectedDate);

		this.embedCalendar.getEventsForMonth();
	}

	nextMonth() {
		this.embedCalendar.selectedDate.setMonth(this.embedCalendar.selectedDate.getMonth() + 1);
		this.embedCalendar.selectedDate.setDate(1);

		this.embedCalendar.setMonth(this.embedCalendar.selectedDate);

		this.embedCalendar.getEventsForMonth();
	}

	selectDate(clickedId: string) {
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