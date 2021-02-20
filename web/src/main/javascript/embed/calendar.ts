import {Calendar} from "@fullcalendar/core";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import boostrapPlugin from "@fullcalendar/bootstrap";
import interactionPlugin from "@fullcalendar/interaction";
import rrulePlugin from "@fullcalendar/rrule";
import momentTimezonePlugin from "@fullcalendar/moment-timezone";
import tippy from "tippy.js";
import {ElementUtil} from "@/utils/ElementUtil";
import {CalendarGetRequest} from "@/network/calendar/CalendarGetRequest";
import {TaskCallback} from "@/objects/task/TaskCallback";
import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";
import {Snackbar} from "@/utils/snackbar";
import {TaskType} from "@/enums/TaskType";
import {WebCalendar} from "@/objects/calendar/WebCalendar";
import {Event} from "@/objects/event/Event";

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
            views: {
                timeGrid: {
                    eventDidMount: (info) => {
                        //TODO: Okay, so can't actually add new elements and make it look decent...
                        //I guess what I could do is the old event modals and open them when clicking on the
                        // time grid view... That seems to be the best position solution here..
                        // So here we will call the method to build the event modal and have that method add to dom
                    },
                    eventWillUnmount: (info) => {
                        //TODO: Remove the event modal from the dom
                    },
                    eventClick: (info) => {
                        //TODO: Open modal to show all the event details instead of just the short info...
                    }
                },
                dayGrid: {
                    eventDidMount: (info) => {
                        let desc: String = info.event.extendedProps.description;
                        if (desc && desc.trim().length > 0) {
                            tippy(info.el, {
                                content: info.event.extendedProps.description,
                                allowHTML: false,
                                hideOnClick: false,
                                theme: 'discal',
                                popperOptions: {
                                    strategy: 'fixed',
                                },
                            });
                        }
                    },
                    eventClick: (arg) => {
                        if (!navigator.clipboard) {
                            return; //Clipboard API not available
                        }
                        const text = arg.event.id;
                        try {
                            navigator.clipboard.writeText(text).then(
                                () => {
                                    Snackbar.showSnackbar("Event ID copied to clipboard!");
                                }
                            );
                        } catch (err) {
                            console.error('Failed to copy event ID!', err);
                        }
                    }
                }
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
}
