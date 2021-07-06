import {Calendar} from "@fullcalendar/core";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import boostrapPlugin from "@fullcalendar/bootstrap";
import interactionPlugin from "@fullcalendar/interaction";
import rrulePlugin from "@fullcalendar/rrule";
import momentTimezonePlugin from "@fullcalendar/moment-timezone";
import {ElementUtil} from "@/utils/ElementUtil";
import {CalendarGetRequest} from "@/network/calendar/CalendarGetRequest";
import {TaskCallback} from "@/objects/task/TaskCallback";
import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";
import {Snackbar} from "@/utils/snackbar";
import {TaskType} from "@/enums/TaskType";
import {WebCalendar} from "@/objects/calendar/WebCalendar";
import {Event} from "@/objects/event/Event";
import moment from "moment-timezone";
import {EventColor, eventColorClass} from "@/enums/EventColor";
import {EventFrequency} from "@/enums/EventFrequency";

export class EmbedCalendarRunner implements TaskCallback {
    private initialTimezone: string = 'local';
    private timezoneSelectorEl = document
        .getElementById('time-zone-selector')! as HTMLSelectElement;
    private calendar?: Calendar;

    private readonly guildId;
    private readonly calNumber;

    private readonly apiKey;
    private readonly apiUrl;

    private calendarData: WebCalendar = new WebCalendar();

    constructor(key: string, url: string) {
        this.guildId = window.location.pathname.split("/")[2];
        this.calNumber = parseInt(window.location.pathname.split("/")[4]);
        this.apiKey = key;
        this.apiUrl = url;

        //Create calendar and its needed components
        let calendarEl = document.getElementById('calendar')!;
        this.calendar = new Calendar(calendarEl, {
            plugins: [
                dayGridPlugin, timeGridPlugin,
                boostrapPlugin, interactionPlugin,
                rrulePlugin, momentTimezonePlugin
            ],
            themeSystem: 'bootstrap',
            initialView: 'dayGridMonth',
            customButtons: {
                viewGoogle: {
                    text: 'View on Google',
                    click: () => {
                        window.open("https://calendar.google.com/calendar/embed?src=" + this.calendarData.id, "_blank");
                    },
                }
            },
            headerToolbar: {
                start: 'prev,next,today',
                center: 'title',
                end: 'dayGridMonth,timeGridWeek,timeGridDay viewGoogle'
            },
            navLinks: true,
            nowIndicator: true,
            slotEventOverlap: true,
            dayMaxEvents: true,
            timeZone: this.initialTimezone,
            displayEventEnd: true,
            eventColor: '#5566c2',
            eventTimeFormat: {
                hour: '2-digit',
                minute: '2-digit',
                omitZeroMinute: true,
                hour12: true, //TODO: Support guild setting for this...
                timeZoneName: 'short'
            },
            eventDidMount: (info) => {
                // So here we will call the method to build the event modal and have that method add to dom
                document.body.appendChild(this.buildModal(info.event.extendedProps.rawEvent));
            },
            eventWillUnmount: (info) => {
                //Remove the event modal from the dom
                document.body.removeChild(document.getElementById("modal-" + info.event.id)!);
            },
            eventClick: (info) => {
                //Open modal to show all the event details instead of just the short info...
                info.jsEvent.preventDefault();
                // @ts-ignore
                $("#modal-" + info.event.id).modal("show");
            },
            events: (fetchInfo, successCallback, failureCallback) => {
                let bodyRaw = {
                    'guild_id': this.guildId,
                    'calendar_number': this.calNumber,
                    'epoch_start': fetchInfo.start.valueOf(),
                    'epoch_end': fetchInfo.end.valueOf(),
                }

                $.ajax({
                    url: this.apiUrl + "/v2/events/list/range",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": this.apiKey,
                    },
                    method: "POST",
                    dataType: "json",
                    data: JSON.stringify(bodyRaw),
                    success: (json: any) => {
                        let events = [];

                        for (let i = 0; i < json.events.length; i++) {

                            events.push(new Event().fromJson(json.events[i]).toFullCalEvent(false));
                        }
                        successCallback(events);
                    },
                    error: (jqXHR) => {
                        Snackbar.showSnackbar("[ERROR] " + jqXHR.responseJSON.message);
                        failureCallback(jqXHR.responseJSON.message);
                    },
                })
            }
        });

        //Add listener for user changing timezone selector
        this.timezoneSelectorEl.addEventListener('change', function () {
            this.calendar.setOption('timeZone', this.timezoneSelectorEl.value)
        }.bind(this))

        //Get timezones for selector
        $.ajax({
            url: "https://fullcalendar.io/demo-timezones.json",
            method: "GET",
            success: function (json: any) {
                for (let timezone in json) {
                    if (json.hasOwnProperty(timezone) && json[timezone] !== 'UTC') {
                        let optionEl = document.createElement('option');
                        optionEl.value = json[timezone];
                        optionEl.innerText = json[timezone];
                        this.timezoneSelectorEl.appendChild(optionEl);
                    }
                }
            }.bind(this),
            error: function (ignore: JQueryXHR) {
                //failure
            }
        });

        //Run init to start other API requests...
        this.init();
    }

    private init() {
        if (this.apiKey === "internal_error") {
            alert("Failed to get a read-only API key to display your calendar.\nIf you keep receiving this error," +
                " please contact the developers.");
        } else {
            //Request calendar information
            let calReq = new CalendarGetRequest(this.guildId, this.calNumber, this);
            calReq.provideApiDetails(this.apiKey, this.apiUrl);

            //Execute the calls
            calReq.execute();
        }
    }

    onCallback(status: NetworkCallStatus) {
        if (status.isSuccess) {
            switch (status.type) {
                case TaskType.CALENDAR_GET: {
                    this.calendarData = new WebCalendar().fromJson(status.body);

                    //Hide loading UI and show calendar...
                    ElementUtil.hideLoader();
                    this.calendar?.render();

                    break;
                }
            }
        } else {
            Snackbar.showSnackbar("[ERROR] " + status.message);
        }
    }

    //Ugh, this actually sucks having to make this stuff...
    buildModal(event: Event): HTMLElement {
        //Create modal container
        let modalContainer = document.createElement("div");
        modalContainer.className = "modal fade";
        modalContainer.id = "modal-" + event.eventId;
        // @ts-ignore
        modalContainer.role = "dialog";

        //Create modal-dialog
        let modalDia = document.createElement("div");
        modalDia.className = "modal-dialog modal-dialog-scrollable";
        modalContainer.appendChild(modalDia);

        //Create modal content
        let modalCon = document.createElement("div");
        modalCon.className = "modal-content bg-discal-not-black";
        modalDia.appendChild(modalCon);

        //Create modal header and title
        let modalHeader = document.createElement("div");
        modalHeader.className = "modal-header bg-discal-not-black";
        modalCon.appendChild(modalHeader);
        let modalTitle = document.createElement("h4");
        modalTitle.className = "modal-title text-discord-blurple text-center";
        modalTitle.innerText = "Viewing Event";
        modalHeader.appendChild(modalTitle);

        //Create modal body
        let modalBody = document.createElement("div");
        modalBody.className = "modal-body";
        modalCon.appendChild(modalBody);

        //summary/name
        let summaryLabel = document.createElement("label");
        summaryLabel.className = "text-discord-full-white form-label";
        summaryLabel.htmlFor = "event-summary-" + event.eventId;
        summaryLabel.innerText = "Summary/Name";
        summaryLabel.appendChild(document.createElement("br"));
        modalBody.appendChild(summaryLabel);
        let summary = document.createElement("textarea");
        summary.className = "form-control";
        summary.id = "event-summary-" + event.eventId;
        summary.name = "summary";
        summary.readOnly = true;
        summary.value = event.summary;
        modalBody.appendChild(summary);
        modalBody.appendChild(document.createElement("br"));
        modalBody.appendChild(document.createElement("br"));

        //Description
        if (event.description.trim().length > 0) {
            let descLabel = document.createElement("label");
            descLabel.className = "text-discord-full-white form-label";
            descLabel.htmlFor = "event-description-" + event.eventId;
            descLabel.innerText = "Description";
            descLabel.appendChild(document.createElement("br"));
            modalBody.appendChild(descLabel);
            let desc = document.createElement("textarea");
            desc.className = "form-control";
            desc.id = "event-description-" + event.eventId;
            desc.name = "description";
            desc.readOnly = true;
            desc.value = event.description;
            modalBody.appendChild(desc);
            modalBody.appendChild(document.createElement("br"));
            modalBody.appendChild(document.createElement("br"));
        }

        //Create correctly formatted start/end date/times
        //TODO: Support 24-hour format guild setting
        const format = "dddd, MMMM, Do YYYY, h:mm a z" //Friday, February 26th 2021, 1:20 AM CST

        let startString;
        let endString;
        if (this.timezoneSelectorEl.value == "local") {
            startString = moment(new Date(event.epochStart)).format(format);
            endString = moment(new Date(event.epochEnd)).format(format);
        } else {
            startString = moment.tz(new Date(event.epochStart), this.timezoneSelectorEl.value).format(format);
            endString = moment.tz(new Date(event.epochEnd), this.timezoneSelectorEl.value).format(format);
        }

        //Start date and time
        let startLabel = document.createElement("label");
        startLabel.className = "text-discord-full-white form-label";
        startLabel.htmlFor = "event-start-" + event.eventId;
        startLabel.innerText = "Event Start";
        startLabel.appendChild(document.createElement("br"));
        modalBody.appendChild(startLabel);
        let start = document.createElement("input");
        start.type = "text";
        start.className = "form-control";
        start.id = "event-start-" + event.eventId;
        start.name = "label";
        start.readOnly = true;
        start.value = startString;
        modalBody.appendChild(start);
        modalBody.appendChild(document.createElement("br"));
        modalBody.appendChild(document.createElement("br"));

        //End date and time
        let endLabel = document.createElement("label");
        endLabel.className = "text-discord-full-white form-label";
        endLabel.htmlFor = "event-end-" + event.eventId;
        endLabel.innerText = "Event End";
        endLabel.appendChild(document.createElement("br"));
        modalBody.appendChild(endLabel);
        let end = document.createElement("input");
        end.type = "text";
        end.className = "form-control";
        end.id = "event-end-" + event.eventId;
        end.name = "label";
        end.readOnly = true;
        end.value = endString;
        modalBody.appendChild(end);
        modalBody.appendChild(document.createElement("br"));
        modalBody.appendChild(document.createElement("br"));

        //Location
        if (event.location.trim().length > 0) {
            let locationLabel = document.createElement("label");
            locationLabel.className = "text-discord-full-white form-label";
            locationLabel.htmlFor = "event-location-" + event.eventId;
            locationLabel.innerText = "Location";
            locationLabel.appendChild(document.createElement("br"));
            modalBody.appendChild(locationLabel);
            let location = document.createElement("textarea");
            location.className = "form-control";
            location.id = "event-location-" + event.eventId;
            location.name = "location";
            location.readOnly = true;
            location.value = event.location;
            modalBody.appendChild(location);
            modalBody.appendChild(document.createElement("br"));
            modalBody.appendChild(document.createElement("br"));
        }

        //Color
        let eventColor = document.createElement("h1");
        eventColor.className = "text-discal-not-black bg-" + eventColorClass(event.color);
        eventColor.id = "event-color-" + event.eventId;
        eventColor.innerText = "Event Color: " + EventColor[event.color];
        modalBody.appendChild(eventColor);
        modalBody.appendChild(document.createElement("br"));
        modalBody.appendChild(document.createElement("br"));

        //Recurrence
        if (event.doesRecur) {
            //Label
            let recurLabel = document.createElement("label");
            recurLabel.className = "text-discord-full-white form-label"
            recurLabel.innerText = "Recurrence Settings";
            recurLabel.appendChild(document.createElement("br"));
            modalBody.appendChild(recurLabel);

            //Group
            let recurGroup = document.createElement("div");
            recurGroup.className = "input-group";
            modalBody.appendChild(recurGroup);

            //Frequency
            let freqLabel = document.createElement("span");
            freqLabel.className = "input-group-text";
            freqLabel.id = "event-freq-label-" + event.eventId;
            freqLabel.innerText = "Frequency";
            recurGroup.appendChild(freqLabel);
            let freq = document.createElement("input");
            freq.className = "form-control";
            freq.type = "text";
            freq.setAttribute("aria-describedby", "event-freq-label-" + event.eventId);
            freq.readOnly = true;
            freq.innerText = EventFrequency[event.recurrence.frequency];
            recurGroup.appendChild(freq);

            //Count
            let countLabel = document.createElement("span");
            countLabel.className = "input-group-text";
            countLabel.id = "event-count-label-" + event.eventId;
            countLabel.innerText = "Count";
            recurGroup.appendChild(countLabel);
            let count = document.createElement("input");
            count.className = "form-control";
            count.type = "number";
            count.setAttribute("aria-describedby", "event-count-label-" + event.eventId);
            count.readOnly = true;
            count.innerText = event.recurrence.count.toString();
            recurGroup.appendChild(count);

            //Interval
            let intervalLabel = document.createElement("span");
            intervalLabel.className = "input-group-text";
            intervalLabel.id = "event-interval-label-" + event.eventId;
            intervalLabel.innerText = "Interval";
            recurGroup.appendChild(intervalLabel);
            let interval = document.createElement("input");
            interval.className = "form-control";
            interval.type = "number";
            interval.setAttribute("aria-describedby", "event-interval-label-" + event.eventId);
            interval.readOnly = true;
            interval.innerText = event.recurrence.interval.toString();
            recurGroup.appendChild(interval);

            modalBody.appendChild(document.createElement("br"));
            modalBody.appendChild(document.createElement("br"));
        }

        //Image
        if (event.image.trim().length > 0) {
            let img = document.createElement("img");
            img.className = "img-fluid round";
            img.id = "event-img-" + event.eventId;
            img.alt = "Event Image";
            img.src = event.image;
            modalBody.appendChild(img);
            modalBody.appendChild(document.createElement("br"));
            modalBody.appendChild(document.createElement("br"));
        }

        //ID
        let idLabel = document.createElement("label");
        idLabel.className = "text-discord-full-white form-label";
        idLabel.htmlFor = "event-id-" + event.eventId;
        idLabel.innerText = "Event ID";
        idLabel.appendChild(document.createElement("br"));
        modalBody.appendChild(idLabel);
        let id = document.createElement("input");
        id.className = "form-control";
        id.id = "event-id-" + event.eventId;
        id.type = "text";
        id.name = "id";
        id.readOnly = true;
        id.value = event.eventId;
        modalBody.appendChild(id);
        modalBody.appendChild(document.createElement("br"));
        modalBody.appendChild(document.createElement("br"));


        //Create modal footer
        let modalFooter = document.createElement("div");
        modalFooter.className = "modal-footer";
        modalCon.appendChild(modalFooter);

        let closeButton = document.createElement("button");
        closeButton.type = "button";
        closeButton.className = "btn bg-discord-blurple btn-discord btn-block text-discord-full-white";
        closeButton.setAttribute("data-dismiss", "modal");
        closeButton.innerText = "Close";
        modalFooter.appendChild(closeButton);

        return modalContainer;
    }
}
