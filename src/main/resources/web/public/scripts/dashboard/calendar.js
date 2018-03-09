var calendar = {
	todaysDate: new Date(),
	selectedDate: new Date(),
	displays: []
};


function getMonthName(index) {
	return ["January", "February",
		"March", "April",
		"May", "June",
		"July", "August",
		"September", "October",
		"November", "December"][index];
}

function getDayName(index) {
	return ["Sunday", "Monday",
		"Tuesday", "Wednesday",
		"Thursday", "Friday",
		"Saturday"][index];
}

function dateDisplays() {
	return ["r1c1", "r1c2", "r1c3", "r1c4", "r1c5", "r1c6", "r1c7",
		"r2c1", "r2c2", "r2c3", "r2c4", "r2c5", "r2c6", "r2c7",
		"r3c1", "r3c2", "r3c3", "r3c4", "r3c5", "r3c6", "r3c7",
		"r4c1", "r4c2", "r4c3", "r4c4", "r4c5", "r4c6", "r4c7",
		"r5c1", "r5c2", "r5c3", "r5c4", "r5c5", "r5c6", "r5c7",
		"r6c1", "r6c2", "r6c3", "r6c4", "r6c5", "r6c6", "r6c7"];
}

function dateDisplaysToChange(str) {
	return dateDisplays().slice(dateDisplays().indexOf(str), dateDisplays().length - 1);
}

function findFirstDayOfMonthPosition() {
	var firstDay = new Date(calendar.selectedDate.getFullYear(), calendar.selectedDate.getMonth(), 1);

	var firstDayName = getDayName(firstDay.getDay());

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
}

function daysInMonth() {
	return new Date(calendar.selectedDate.getFullYear(), calendar.selectedDate.getMonth() + 1, 0).getDate();
}

function setMonth(parameters) {
	var date = parameters.date;

	document.getElementById("month-display").innerHTML = getMonthName(date.getMonth()) + " " + date.getFullYear();

	calendar.displays = [];

	var tcc = dateDisplays();
	for (var ii = 0; ii < tcc.length; ii++) {
		var e = document.getElementById(tcc[ii]);
		e.innerHTML = "";
		e.className = "";
	}

	var tc = dateDisplaysToChange(findFirstDayOfMonthPosition());
	var count = daysInMonth();
	for (var i = 0; i < tc.length; i++) {
		var d = i + 1;
		if (d <= count) {
			var el = document.getElementById(tc[i]);
			el.innerHTML = d + "";
			calendar.displays[d] = tc[i];

			var thisDate = new Date(calendar.selectedDate.getFullYear(), calendar.selectedDate.getMonth(), d);
			if (d === calendar.selectedDate.getDate()) {
				el.className = "selected";
			}
			if (thisDate.getMonth() === calendar.todaysDate.getMonth()
				&& thisDate.getFullYear() === calendar.todaysDate.getFullYear()
				&& thisDate.getDate() === calendar.todaysDate.getDate()) {
				el.className = "today";
			}
		}
	}
}

function previousMonth() {
	calendar.selectedDate.setMonth(calendar.selectedDate.getMonth() - 1);
	calendar.selectedDate.setDate(1);

	setMonth({date: calendar.selectedDate});

	getEventsForMonth();
}

function nextMonth() {
	calendar.selectedDate.setMonth(calendar.selectedDate.getMonth() + 1);
	calendar.selectedDate.setDate(1);

	setMonth({date: calendar.selectedDate});

	getEventsForMonth();
}

function getEventsForMonth() {
	var ds = new Date(calendar.selectedDate.getFullYear(), calendar.selectedDate.getMonth(), 1);
	ds.setHours(0, 0, 0, 0);


	var bodyRaw = {
		"StartDate": "1",
		"SelectedDate": calendar.selectedDate.getDate().toString(),
		"Month": ds.getMonth().toString(),
		"Year": ds.getFullYear().toString(),
		"DaysInMonth": daysInMonth().toString(),
		"StartEpoch": ds.getTime().toString()
	};

	var q = $.post("/api/v1/events/list/month", JSON.stringify(bodyRaw), function (response) {
		var obj = JSON.parse(response);

		//Display the event counts on the calendar...
		for (var i = 0; i < obj.events.length; i++) {
			var d = new Date(obj.events[i].epochStart);

			document.getElementById(calendar.displays[d.getDate()]).innerHTML = d.getDate() + "[1]";
		}


		//Display the selected day's event details for editing and such.


	})
		.fail(function () {
			alert("Internal error! Failed to get events from google! >.<");
		}, "json");


}

function selectDate(clickedId) {
	var e = document.getElementById(clickedId);
	var dateString = e.innerHTML.split("[")[0];
	if (dateString !== "") {
		var dateNum = parseInt(dateString);

		calendar.selectedDate.setDate(dateNum);

		setMonth({date: calendar.selectedDate});

		getEventsForMonth();
	}
}

function init() {
	setMonth({date: calendar.todaysDate});

	getEventsForMonth();
}