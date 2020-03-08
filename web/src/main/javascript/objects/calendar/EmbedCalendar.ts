import {Snackbar} from "@/utils/snackbar";
import {EventColor} from "@/enums/EventColor";
import {EventFrequency} from "@/enums/EventFrequency";
import {TaskCallback} from "@/objects/task/TaskCallback";
import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";
import {TaskType} from "@/enums/TaskType";
import {WebCalendar} from "@/objects/calendar/WebCalendar";
import {CalendarGetRequest} from "@/network/calendar/CalendarGetRequest";
import {EventListMonthRequest} from "@/network/event/list/EventListMonthRequest";
import {EventListDateRequest} from "@/network/event/list/EventListDateRequest";
import {Event} from "@/objects/event/Event";
import {ElementUtil} from "@/utils/ElementUtil";


//The calendar class. This just handles all the stuff inside of the calendar, and keeps it isolated.
export class EmbedCalendar implements TaskCallback {
    private readonly guildId: number;
    private readonly calNumber: number;
    private todaysDate: Date;
    private displays: string[];
    private apiKey: string;
    private apiUrl: string;

    public selectedDate: Date;

    private calendarData: WebCalendar;

    constructor() {
		this.guildId = parseInt(window.location.pathname.split("/")[3]);
		this.calNumber = parseInt(window.location.pathname.split("/")[4]);
		this.todaysDate = new Date();
		this.selectedDate = new Date();
		this.displays = [];
		this.apiKey = "";
		this.apiUrl = "";
	}

    init(key: string, url: string) {
        this.apiKey = key;
        this.apiUrl = url;

        if (this.apiKey === "internal_error") {
			ElementUtil.hideLoader();
			alert("Failed to get a read-only API key to display your calendar. \n" +
				"If you keep receiving this error, please contact the developers");

		} else {
			//Request calendar information
			let calReq = new CalendarGetRequest(this.guildId, this.calNumber, this);
			calReq.provideApiDetails(this.apiKey, this.apiUrl);

			//Execute the calls
			this.setMonth(this.selectedDate);

			calReq.execute();
			this.getEventsForMonth();
		}

        return this;
    }


    //Utility methods for data to human readable conversions
    getMonthName(index: number) {
        return ["January", "February", "March",
            "April", "May", "June",
            "July", "August", "September",
            "October", "November", "December"][index];
    }

    getDayName(index: number) {
        return ["Sunday", "Monday",
            "Tuesday", "Wednesday",
            "Thursday", "Friday",
            "Saturday"][index];
    }

    dateDisplays() {
        //This is all of the IDs for the date displays, in row N, column X shorthand.
        return ["r1c1", "r1c2", "r1c3", "r1c4", "r1c5", "r1c6", "r1c7",
            "r2c1", "r2c2", "r2c3", "r2c4", "r2c5", "r2c6", "r2c7",
            "r3c1", "r3c2", "r3c3", "r3c4", "r3c5", "r3c6", "r3c7",
            "r4c1", "r4c2", "r4c3", "r4c4", "r4c5", "r4c6", "r4c7",
            "r5c1", "r5c2", "r5c3", "r5c4", "r5c5", "r5c6", "r5c7",
            "r6c1", "r6c2", "r6c3", "r6c4", "r6c5", "r6c6", "r6c7"];
    }

    daysInMonth() {
        return new Date(this.selectedDate.getFullYear(), this.selectedDate.getMonth() + 1, 0).getDate();
    }

    //Handling some display nonsense
    dateDisplaysToChange(str: string) {
        return this.dateDisplays().slice(this.dateDisplays().indexOf(str), this.dateDisplays().length - 1);
    }

    findFirstDayOfMonthPosition() {
        let firstDay = new Date(this.selectedDate.getFullYear(), this.selectedDate.getMonth(), 1);

        let firstDayName = this.getDayName(firstDay.getDay());

        switch (firstDayName) {
            case "Sunday":
                return "r1c1";
            case "Monday":
                return "r1c2";
            case "Tuesday":
                return "r1c3";
            case "Wednesday":
                return "r1c4";
            case "Thursday":
                return "r1c5";
            case "Friday":
                return "r1c6";
            case "Saturday":
                return "r1c7";
        }

        return "r1c1";
    }

    changeRecurrenceEditDisplays(checkbox: HTMLInputElement) {
        let eventId = checkbox.id.split("-")[1];
        if (checkbox.checked) {
            //Enable recur input
            (<HTMLInputElement>document.getElementById("editFrequency-" + eventId)).disabled = false;
            (<HTMLInputElement>document.getElementById("editCount-" + eventId)).disabled = false;
            (<HTMLInputElement>document.getElementById("editInterval-" + eventId)).disabled = false;

        } else {
            //Disable recur input
            (<HTMLInputElement>document.getElementById("editFrequency-" + eventId)).disabled = true;
            (<HTMLInputElement>document.getElementById("editCount-" + eventId)).disabled = true;
            (<HTMLInputElement>document.getElementById("editInterval-" + eventId)).disabled = true;
        }
    }

    //Actually handling calendar activities
    setMonth(date: Date) {

        document.getElementById("month-display")!.innerHTML = this.getMonthName(date.getMonth()) + " " + date.getFullYear();

        this.displays = [];

        let tcc = this.dateDisplays();
        for (let ii = 0; ii < tcc.length; ii++) {
            let e = document.getElementById(tcc[ii])!;
            e.innerHTML = "";
            e.className = "";
        }

        let tc = this.dateDisplaysToChange(this.findFirstDayOfMonthPosition());
        let count = this.daysInMonth();
        for (let i = 0; i < tc.length; i++) {
            let d = i + 1;
            if (d <= count) {
                let el = document.getElementById(tc[i])!;
                el.innerHTML = d + "";
                this.displays[d] = tc[i];

                let thisDate = new Date(this.selectedDate.getFullYear(), this.selectedDate.getMonth(), d);
                if (d === this.selectedDate.getDate()) {
                    el.className = "selected";
                }
                if (thisDate.getMonth() === this.todaysDate.getMonth()
                    && thisDate.getFullYear() === this.todaysDate.getFullYear()
                    && thisDate.getDate() === this.todaysDate.getDate()) {
                    el.className = "today";
                }
            }
        }
    }

    getEventsForMonth() {
        let ds = new Date(this.selectedDate.getFullYear(), this.selectedDate.getMonth(), 1);
        ds.setHours(0, 0, 0, 0);

        let eventReq = new EventListMonthRequest(this.guildId, this.calNumber,
            this.daysInMonth(), ds.getTime(), this);

        eventReq.provideApiDetails(this.apiKey, this.apiUrl);

        eventReq.execute();
    }

    getEventsForSelectedDate() {
		let ds = new Date(this.selectedDate.getFullYear(), this.selectedDate.getMonth(), this.selectedDate.getDate());
		ds.setHours(0, 0, 0, 0);

		ElementUtil.hideEventsContainer();

		let eventReq = new EventListDateRequest(this.guildId, this.calNumber, ds.getTime(), this);
		eventReq.provideApiDetails(this.apiKey, this.apiUrl);

		ElementUtil.hideEventsContainer();
		eventReq.execute();
	}

    onCallback(status: NetworkCallStatus): void {
        if (status.isSuccess) {
            switch (status.type) {
                case TaskType.CALENDAR_GET:
                    this.calendarData = new WebCalendar().fromJson(status.body);

                    (<HTMLLinkElement>document.getElementById("view-on-google-button"))
						.href = "https://calendar.google.com/calendar/embed?src="
						+ this.calendarData.address;
					break;
				case TaskType.EVENT_LIST_MONTH:
					//Display the event counts on the calendar...
					for (let i = 0; i < status.body.events.length; i++) {
						let d = new Date(status.body.events[i].epoch_start);

						let e = document.getElementById(this.displays[d.getDate()])!;

						if (e.innerHTML.indexOf("[") === -1) {
							e.innerHTML = d.getDate() + "[1]";
						} else {
							e.innerHTML = d.getDate().toString()
								+ "[" + (parseInt(e.innerHTML.split("[")[1][0]) + 1).toString() + "]";
						}
					}
					ElementUtil.hideLoader();
					ElementUtil.showCalendarContainer();
					break;
				case TaskType.EVENT_LIST_DATE:
					this.loadEventDisplay(status);
					break;
				default:
					break;
			}
        } else {
            Snackbar.showSnackbar("ERROR] " + status.message);
        }
    }

    private loadEventDisplay(status: NetworkCallStatus) {
        //Display the selected day's event details for editing and such.
        let container = document.getElementById("event-container")!;

        while (container.firstChild) {
            container.removeChild(container.firstChild);
        }

        for (let i = 0; i < status.body.events.length; i++) {
            let event = new Event().fromJson(status.body.events[i]);

            //Create View Button
            let viewButton = document.createElement("button");
            viewButton.type = "button";
            viewButton.setAttribute("data-toggle", "modal");
            viewButton.setAttribute("data-target", "#modal-" + event.eventId);
            viewButton.innerHTML = "View Event With ID: " + event.eventId;
            container.appendChild(viewButton);

            container.appendChild(document.createElement("br"));
            container.appendChild(document.createElement("br"));

            //Create modal container
            let modalContainer = document.createElement("div");
            modalContainer.className = "modal fade";
            modalContainer.id = "modal-" + event.eventId;
            // @ts-ignore
            modalContainer.role = "dialog";
            container.appendChild(modalContainer);

            //Create modal-dialog
            let modalDia = document.createElement("div");
            modalDia.className = "modal-dialog";
            modalContainer.appendChild(modalDia);

            //Create Modal Content
            let modalCon = document.createElement("div");
            modalCon.className = "modal-content";
            modalDia.appendChild(modalCon);

            //Create modal header and title
            let modalHeader = document.createElement("div");
            modalHeader.className = "modal-header";
            modalCon.appendChild(modalHeader);
            let modalTitle = document.createElement("h4");
            modalTitle.className = "modal-title";
            modalTitle.innerHTML = "Viewing Event";
            modalHeader.appendChild(modalTitle);

            //Create Modal Body
            let modalBody = document.createElement("div");
            modalBody.className = "modal-body";
            modalCon.appendChild(modalBody);

            let form = document.createElement("form");
            modalBody.appendChild(form);

            //Summary
            let summaryLabel = document.createElement("label");
            summaryLabel.innerHTML = "Summary";
            summaryLabel.appendChild(document.createElement("br"));
            form.appendChild(summaryLabel);
            let summary = document.createElement("input");
            summary.name = "summary";
            summary.type = "text";
            // noinspection JSDeprecatedSymbols
            summary.value = event.summary;
            summary.id = "editSummary-" + event.eventId;
            summaryLabel.appendChild(summary);
            form.appendChild(document.createElement("br"));
            form.appendChild(document.createElement("br"));

            //Description
            let descriptionLabel = document.createElement("label");
            descriptionLabel.innerHTML = "Description";
            descriptionLabel.appendChild(document.createElement("br"));
            form.appendChild(descriptionLabel);
            let description = document.createElement("input");
            description.name = "edit-description";
            description.type = "text";
            description.value = event.description;
            description.id = "editDescription-" + event.eventId;
            descriptionLabel.appendChild(description);
            form.appendChild(document.createElement("br"));
            form.appendChild(document.createElement("br"));

            //Start date and time
            let sd = new Date(event.epochStart);
            let startLabel = document.createElement("label");
            startLabel.innerHTML = "Start Date and Time";
            startLabel.appendChild(document.createElement("br"));
            form.appendChild(startLabel);
            let startDate = document.createElement("input");
            startDate.name = "start-date";
            startDate.type = "date";
            startDate.valueAsDate = sd;
            startDate.id = "editStartDate-" + event.eventId;
            startLabel.appendChild(startDate);
            let startTime = document.createElement("input");
            startTime.name = "start-time";
            startTime.type = "time";
            startTime.value = (sd.getHours() < 10 ? "0" : "") + sd.getHours() + ":" + (sd.getMinutes() < 10 ? "0" : "") + sd.getMinutes();
            startTime.id = "editStartTime-" + event.eventId;
            startLabel.appendChild(startTime);
            form.appendChild(document.createElement("br"));
            form.appendChild(document.createElement("br"));

            //End date and time
            let ed = new Date(event.epochEnd);
            let endLabel = document.createElement("label");
            endLabel.innerHTML = "End Date and Time";
            endLabel.appendChild(document.createElement("br"));
            form.appendChild(endLabel);
            let endDate = document.createElement("input");
            endDate.name = "end-date";
            endDate.type = "date";
            endDate.valueAsDate = ed;
            endDate.id = "editEndDate-" + event.eventId;
            endLabel.appendChild(endDate);
            let endTime = document.createElement("input");
            endTime.name = "end-time";
            endTime.type = "time";
            endTime.value = (ed.getHours() < 10 ? "0" : "") + ed.getHours() + ":" + (ed.getMinutes() < 10 ? "0" : "") + ed.getMinutes();
            endTime.id = "editEndTime-" + event.eventId;
            endLabel.appendChild(endTime);
            form.appendChild(document.createElement("br"));
            form.appendChild(document.createElement("br"));

            //Location
            let locationLabel = document.createElement("label");
            locationLabel.innerHTML = "Location";
            locationLabel.appendChild(document.createElement("br"));
            form.appendChild(locationLabel);
            let location = document.createElement("input");
            location.name = "location";
            location.type = "text";
            location.value = event.location;
            location.id = "editLocation-" + event.eventId;
            locationLabel.appendChild(location);
            form.appendChild(document.createElement("br"));
            form.appendChild(document.createElement("br"));

            //Color
            let colorLabel = document.createElement("label");
            colorLabel.innerHTML = "Color";
            colorLabel.appendChild(document.createElement("br"));
            form.appendChild(colorLabel);
            let colorSelect = document.createElement("select");
            colorSelect.name = "color";
            colorSelect.id = "editColor-" + event.eventId;
            colorLabel.appendChild(colorSelect);

            for (let ec in EventColor) {
                let option = document.createElement("option");
                option.value = EventColor[ec];
                option.text = EventColor[ec];
                option.selected = (EventColor[event.color] === EventColor[ec]);
                colorSelect.appendChild(option);
            }
            form.appendChild(document.createElement("br"));
            form.appendChild(document.createElement("br"));

            if (event.doesRecur) {
                //Recurrence
                let recurrenceLabel = document.createElement("label");
                recurrenceLabel.innerHTML = "Recurrence";
                recurrenceLabel.appendChild(document.createElement("br"));
                form.appendChild(recurrenceLabel);

                if (event.isParent) {
                    let enableRecurrence = document.createElement("input");
                    enableRecurrence.name = "enable-recurrence";
                    enableRecurrence.type = "checkbox";
                    enableRecurrence.checked = false;
                    enableRecurrence.id = "editEnableRecur-" + event.eventId;
                    enableRecurrence.onclick = function () {
                        this.changeRecurrenceEditDisplays(this);
                    }.bind(this);
                    recurrenceLabel.appendChild(enableRecurrence);
                    form.appendChild(document.createElement("br"));
                    form.appendChild(document.createElement("br"));

                    //Frequency
                    let frequencyLabel = document.createElement("label");
                    frequencyLabel.innerHTML = "Recurrence - Frequency";
                    frequencyLabel.appendChild(document.createElement("br"));
                    form.appendChild(frequencyLabel);
                    let freqSelect = document.createElement("select");
                    freqSelect.name = "frequency";
                    freqSelect.id = "editFrequency-" + event.eventId;
                    frequencyLabel.appendChild(freqSelect);

                    for (let f in EventFrequency) {
                        let op = document.createElement("option");
                        op.value = EventFrequency[f];
                        op.text = EventFrequency[f];
                        op.selected = (EventFrequency[event.recurrence.frequency] === EventFrequency[f]);
                        freqSelect.appendChild(op);
                    }

                    freqSelect.disabled = true;
                    frequencyLabel.appendChild(freqSelect);
                    form.appendChild(document.createElement("br"));
                    form.appendChild(document.createElement("br"));

                    //Count
                    let countLabel = document.createElement("label");
                    countLabel.innerHTML = "Recurrence - Count";
                    countLabel.appendChild(document.createElement("br"));
                    form.appendChild(countLabel);
                    let count = document.createElement("input");
                    count.name = "count";
                    count.type = "number";
                    count.valueAsNumber = event.recurrence.count;
                    count.min = "-1";
                    count.id = "editCount-" + event.eventId;
                    count.disabled = true;
                    countLabel.appendChild(count);
                    form.appendChild(document.createElement("br"));
                    form.appendChild(document.createElement("br"));

                    //Interval
                    let intervalLabel = document.createElement("label");
                    intervalLabel.innerHTML = "Recurrence - Interval";
                    intervalLabel.appendChild(document.createElement("br"));
                    form.appendChild(intervalLabel);
                    let interval = document.createElement("input");
                    interval.name = "interval";
                    interval.type = "number";
                    interval.valueAsNumber = event.recurrence.interval;
                    interval.min = "1";
                    interval.id = "editInterval-" + event.eventId;
                    interval.disabled = true;
                    intervalLabel.appendChild(interval);
                    form.appendChild(document.createElement("br"));
                    form.appendChild(document.createElement("br"));

                } else {
                    //Cannot edit recurrence
                    let cannotEditRecur = document.createElement("input");
                    cannotEditRecur.name = "ignore-cer";
                    cannotEditRecur.type = "text";
                    cannotEditRecur.disabled = true;
                    cannotEditRecur.value = "Cannot edit child";
                    recurrenceLabel.appendChild(cannotEditRecur);
                }
                form.appendChild(document.createElement("br"));
                form.appendChild(document.createElement("br"));
            }

            //Image
            let imageLabel = document.createElement("label");
            imageLabel.innerHTML = "Image";
            imageLabel.appendChild(document.createElement("br"));
            form.appendChild(imageLabel);
            let image = document.createElement("input");
            image.name = "image";
            image.type = "text";
            image.value = event.image;
            image.id = "editImage-" + event.eventId;
            imageLabel.appendChild(image);
            form.appendChild(document.createElement("br"));
            form.appendChild(document.createElement("br"));

            //ID (readonly) for API
            let idLabel = document.createElement("label");
            idLabel.innerHTML = "Event ID";
            idLabel.appendChild(document.createElement("br"));
            form.appendChild(idLabel);
            let hiddenId = document.createElement("input");
            hiddenId.type = "text";
			hiddenId.name = "id";
			hiddenId.value = event.eventId;
			hiddenId.id = "editId-" + event.eventId;
			hiddenId.readOnly = true;
			idLabel.appendChild(hiddenId);
			form.appendChild(document.createElement("br"));
			form.appendChild(document.createElement("br"));

			//Create modal footer
			let modalFooter = document.createElement("div");
			modalFooter.className = "modal-footer";
			modalCon.appendChild(modalFooter);

			let closeButton = document.createElement("button");
			closeButton.type = "button";
			closeButton.setAttribute("data-dismiss", "modal");
			closeButton.innerHTML = "Close";
			modalFooter.appendChild(closeButton);
			//Oh my god finally done!!!
		}
		ElementUtil.showEventsContainer();
	}
}