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

function colors() {
	return ["MELROSE", "RIPTIDE", "MAUVE", "TANGERINE",
		"DANDELION", "MAC_AND_CHEESE", "TURQUOISE",
		"MERCURY", "BLUE", "GREED", "RED", "NONE"];
}

function frequencies() {
	return ["DAILY", "WEEKLY", "MONTHLY", "YEARLY"];
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

function changeRecurrenceEditDisplays(checkbox) {
	var eventId = checkbox.id.split("-")[1];
	if (checkbox.checked) {
		//Enable recur input
		document.getElementById("editFrequency-" + eventId).disabled = false;
		document.getElementById("editCount-" + eventId).disabled = false;
		document.getElementById("editInterval-" + eventId).disabled = false;

	} else {
		//Disable recur input
		document.getElementById("editFrequency-" + eventId).disabled = true;
		document.getElementById("editCount-" + eventId).disabled = true;
		document.getElementById("editInterval-" + eventId).disabled = true;
	}
}

function changeRecurrenceCreateDisplays(checkbox) {
	if (checkbox.checked) {
		//Enable recur input
		document.getElementById("create-frequency").disabled = false;
		document.getElementById("create-count").disabled = false;
		document.getElementById("create-interval").disabled = false;

	} else {
		//Disable recur input
		document.getElementById("create-frequency").disabled = true;
		document.getElementById("create-count").disabled = true;
		document.getElementById("create-interval").disabled = true;
	}
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
		"DaysInMonth": daysInMonth().toString(),
		"StartEpoch": ds.getTime().toString()
	};

	var q = $.post("/api/v1/events/list/month", JSON.stringify(bodyRaw), function (response) {
		var obj = JSON.parse(response);

		//Display the event counts on the calendar...
		for (var i = 0; i < obj.events.length; i++) {
			var d = new Date(obj.events[i].epochStart);

			var e = document.getElementById(calendar.displays[d.getDate()]);

			if (e.innerHTML.indexOf("[") === -1) {
				e.innerHTML = d.getDate() + "[1]";
			} else {
				e.innerHTML = d.getDate().toString() + "[" + (parseInt(e.innerHTML.split("[")[1][0]) + 1).toString() + "]";
			}
		}

	})
		.fail(function () {
			showSnackbar("Our hippos failed to find your events!");
		}, "json");
}

function getEventsForSelectedDate() {
	var ds = new Date(calendar.selectedDate.getFullYear(), calendar.selectedDate.getMonth(), calendar.selectedDate.getDate());
	ds.setHours(0, 0, 0, 0);


	var bodyRaw = {
		"DaysInMonth": daysInMonth().toString(),
		"StartEpoch": ds.getTime().toString()
	};

	var q = $.post("/api/v1/events/list/date", JSON.stringify(bodyRaw), function (response) {
		var obj = JSON.parse(response);

		//Display the selected day's event details for editing and such.
		var container = document.getElementById("event-container");

		while (container.firstChild) {
			container.removeChild(container.firstChild);
		}

		for (var i = 0; i < obj.count; i++) {
			var event = obj.events[i];

			//Create Edit Button
			var editButton = document.createElement("button");
			editButton.type = "button";
			editButton.setAttribute("data-toggle", "modal");
			editButton.setAttribute("data-target", "#modal-" + event.id);
			editButton.innerHTML = "Edit";
			container.appendChild(editButton);

			//Create Delete button
			var deleteButton = document.createElement("button");
			deleteButton.type = "button";
			deleteButton.innerHTML = "Delete";
			deleteButton.className = "danger";
			deleteButton.id = "delete-" + event.id;
			deleteButton.onclick = function (ev) {
				deleteEvent(this.id)
			};
			container.appendChild(deleteButton);

			container.appendChild(document.createElement("br"));
			container.appendChild(document.createElement("br"));

			//Create modal container
			var modalContainer = document.createElement("div");
			modalContainer.className = "modal fade";
			modalContainer.id = "modal-" + event.id;
			modalContainer.role = "dialog";
			container.appendChild(modalContainer);

			//Create modal-dialog
			var modalDia = document.createElement("div");
			modalDia.className = "modal-dialog";
			modalContainer.appendChild(modalDia);

			//Create Modal Content
			var modalCon = document.createElement("div");
			modalCon.className = "modal-content";
			modalDia.appendChild(modalCon);

			//Create modal header and title
			var modalHeader = document.createElement("div");
			modalHeader.className = "modal-header";
			modalCon.appendChild(modalHeader);
			var modalTitle = document.createElement("h4");
			modalTitle.className = "modal-title";
			modalTitle.innerHTML = "Editing Event";
			modalHeader.appendChild(modalTitle);

			//Create Modal Body
			var modalBody = document.createElement("div");
			modalBody.className = "modal-body";
			modalCon.appendChild(modalBody);

			var form = document.createElement("form");
			modalBody.appendChild(form);

			//Summary
			var summaryLabel = document.createElement("label");
			summaryLabel.innerHTML = "Summary";
			summaryLabel.appendChild(document.createElement("br"));
			form.appendChild(summaryLabel);
			var summary = document.createElement("input");
			summary.name = "summary";
			summary.type = "text";
			summary.value = event.summary;
			summary.id = "editSummary-" + event.id;
			summaryLabel.appendChild(summary);
			form.appendChild(document.createElement("br"));
			form.appendChild(document.createElement("br"));

			//Description
			var descriptionLabel = document.createElement("label");
			descriptionLabel.innerHTML = "Description";
			descriptionLabel.appendChild(document.createElement("br"));
			form.appendChild(descriptionLabel);
			var description = document.createElement("input");
			description.name = "edit-description";
			description.type = "text";
			description.value = event.description;
			description.id = "editDescription-" + event.id;
			descriptionLabel.appendChild(description);
			form.appendChild(document.createElement("br"));
			form.appendChild(document.createElement("br"));

			//Start date and time
			var sd = new Date(event.epochStart);
			var startLabel = document.createElement("label");
			startLabel.innerHTML = "Start Date and Time";
			startLabel.appendChild(document.createElement("br"));
			form.appendChild(startLabel);
			var startDate = document.createElement("input");
			startDate.name = "start-date";
			startDate.type = "date";
			startDate.valueAsDate = sd;
			startDate.id = "editStartDate-" + event.id;
			startLabel.appendChild(startDate);
			var startTime = document.createElement("input");
			startTime.name = "start-time";
			startTime.type = "time";
			startTime.value = (sd.getHours() < 10 ? "0" : "") + sd.getHours() + ":" + (sd.getMinutes() < 10 ? "0" : "") + sd.getMinutes();
			startTime.id = "editStartTime-" + event.id;
			startLabel.appendChild(startTime);
			form.appendChild(document.createElement("br"));
			form.appendChild(document.createElement("br"));

			//End date and time
			var ed = new Date(event.epochEnd);
			var endLabel = document.createElement("label");
			endLabel.innerHTML = "End Date and Time";
			endLabel.appendChild(document.createElement("br"));
			form.appendChild(endLabel);
			var endDate = document.createElement("input");
			endDate.name = "end-date";
			endDate.type = "date";
			endDate.valueAsDate = ed;
			endDate.id = "editEndDate-" + event.id;
			endLabel.appendChild(endDate);
			var endTime = document.createElement("input");
			endTime.name = "end-time";
			endTime.type = "time";
			endTime.value = (ed.getHours() < 10 ? "0" : "") + ed.getHours() + ":" + (ed.getMinutes() < 10 ? "0" : "") + ed.getMinutes();
			endTime.id = "editEndTime-" + event.id;
			endLabel.appendChild(endTime);
			form.appendChild(document.createElement("br"));
			form.appendChild(document.createElement("br"));


			//Timezone (read only)
			var timezoneLabel = document.createElement("label");
			timezoneLabel.innerHTML = "Timezone";
			timezoneLabel.appendChild(document.createElement("br"));
			form.appendChild(timezoneLabel);
			var timezone = document.createElement("input");
			timezone.name = "timezone";
			timezone.type = "text";
			timezone.value = event.timezone;
			timezone.disabled = true;
			timezoneLabel.appendChild(timezone);
			form.appendChild(document.createElement("br"));
			form.appendChild(document.createElement("br"));

			//Location
			var locationLabel = document.createElement("label");
			locationLabel.innerHTML = "Location";
			locationLabel.appendChild(document.createElement("br"));
			form.appendChild(locationLabel);
			var location = document.createElement("input");
			location.name = "location";
			location.type = "text";
			location.value = event.location;
			location.id = "editLocation-" + event.id;
			locationLabel.appendChild(location);
			form.appendChild(document.createElement("br"));
			form.appendChild(document.createElement("br"));

			//Color
			var colorLabel = document.createElement("label");
			colorLabel.innerHTML = "Color";
			colorLabel.appendChild(document.createElement("br"));
			form.appendChild(colorLabel);
			var colorSelect = document.createElement("select");
			colorSelect.name = "color";
			colorSelect.id = "editColor-" + event.id;
			colorLabel.appendChild(colorSelect);

			for (var c = 0; c < colors().length; c++) {
				var option = document.createElement("option");
				option.value = colors()[c];
				option.text = colors()[c];
				option.selected = (event.color === colors()[c]);
				colorSelect.appendChild(option);
			}
			form.appendChild(document.createElement("br"));
			form.appendChild(document.createElement("br"));

			//Recurrence
			var recurrenceLabel = document.createElement("label");
			recurrenceLabel.innerHTML = "Recurrence";
			recurrenceLabel.appendChild(document.createElement("br"));
			form.appendChild(recurrenceLabel);

			if (event.isParent) {
				var enableRecurrence = document.createElement("input");
				enableRecurrence.name = "enable-recurrence";
				enableRecurrence.type = "checkbox";
				enableRecurrence.checked = false;
				enableRecurrence.id = "editEnableRecur-" + event.id;
				enableRecurrence.onclick = function (ev) {
					changeRecurrenceEditDisplays(this);
				};
				recurrenceLabel.appendChild(enableRecurrence);
				form.appendChild(document.createElement("br"));
				form.appendChild(document.createElement("br"));

				//Frequency
				var frequencyLabel = document.createElement("label");
				frequencyLabel.innerHTML = "Recurrence - Frequency";
				frequencyLabel.appendChild(document.createElement("br"));
				form.appendChild(frequencyLabel);
				var freqSelect = document.createElement("select");
				freqSelect.name = "frequency";
				freqSelect.id = "editFrequency-" + event.id;
				frequencyLabel.appendChild(freqSelect);

				for (var f = 0; f < frequencies().length; f++) {
					var op = document.createElement("option");
					op.value = frequencies()[f];
					op.text = frequencies()[f];
					op.selected = (event.recurrence.frequency === frequencies()[f]);
					freqSelect.appendChild(op);
				}
				freqSelect.disabled = true;
				frequencyLabel.appendChild(freqSelect);
				form.appendChild(document.createElement("br"));
				form.appendChild(document.createElement("br"));

				//Count
				var countLabel = document.createElement("label");
				countLabel.innerHTML = "Recurrence - Count";
				countLabel.appendChild(document.createElement("br"));
				form.appendChild(countLabel);
				var count = document.createElement("input");
				count.name = "count";
				count.type = "number";
				count.valueAsNumber = parseInt(event.recurrence.count);
				count.min = "-1";
				count.id = "editCount-" + event.id;
				count.disabled = true;
				countLabel.appendChild(count);
				form.appendChild(document.createElement("br"));
				form.appendChild(document.createElement("br"));

				//Interval
				var intervalLabel = document.createElement("label");
				intervalLabel.innerHTML = "Recurrence - Interval";
				intervalLabel.appendChild(document.createElement("br"));
				form.appendChild(intervalLabel);
				var interval = document.createElement("input");
				interval.name = "interval";
				interval.type = "number";
				interval.valueAsNumber = parseInt(event.recurrence.interval);
				interval.min = "1";
				interval.id = "editInterval-" + event.id;
				interval.disabled = true;
				intervalLabel.appendChild(interval);
				form.appendChild(document.createElement("br"));
				form.appendChild(document.createElement("br"));

			} else {
				//Cannot edit recurrence
				var cannotEditRecur = document.createElement("input");
				cannotEditRecur.name = "ignore-cer";
				cannotEditRecur.type = "text";
				cannotEditRecur.disabled = true;
				cannotEditRecur.value = "Cannot edit child";
				recurrenceLabel.appendChild(cannotEditRecur);
			}
			form.appendChild(document.createElement("br"));
			form.appendChild(document.createElement("br"));

			//Image
			var imageLabel = document.createElement("label");
			imageLabel.innerHTML = "Image";
			imageLabel.appendChild(document.createElement("br"));
			form.appendChild(imageLabel);
			var image = document.createElement("input");
			image.name = "image";
			image.type = "text";
			image.value = event.image;
			image.id = "editImage-" + event.id;
			imageLabel.appendChild(image);
			form.appendChild(document.createElement("br"));
			form.appendChild(document.createElement("br"));

			//ID (readonly) for API
			var idLabel = document.createElement("label");
			idLabel.innerHTML = "Event ID";
			idLabel.appendChild(document.createElement("br"));
			form.appendChild(idLabel);
			var hiddenId = document.createElement("input");
			hiddenId.type = "text";
			hiddenId.name = "id";
			hiddenId.value = event.id;
			hiddenId.id = "editId-" + event.id;
			hiddenId.readOnly = true;
			idLabel.appendChild(hiddenId);
			form.appendChild(document.createElement("br"));
			form.appendChild(document.createElement("br"));

			//Submit button
			var submit = document.createElement("button");
			submit.className = "submit";
			submit.type = "button";
			submit.id = "editsubmit-" + event.id;
			submit.innerHTML = "Update Event!";
			submit.onclick = function (ignore) {
				updateEvent(this.id);
			};
			form.appendChild(submit);
			//TODO: permission handling for submit button!!!!

			//TODO: Reset button

			//Create modal footer
			var modalFooter = document.createElement("div");
			modalFooter.className = "modal-footer";
			modalCon.appendChild(modalFooter);

			var closeButton = document.createElement("button");
			closeButton.type = "button";
			closeButton.setAttribute("data-dismiss", "modal");
			closeButton.innerHTML = "Close";
			modalFooter.appendChild(closeButton);
			//Oh my god finally done!!!
		}
	})
		.fail(function () {
			showSnackbar("Our hippos failed to find your events for the day!");
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

		getEventsForSelectedDate();
	}
}

function updateEvent(editSubmitId) {
	var eventId = editSubmitId.split("-")[1];

	var startTimeString = document.getElementById("editStartTime-" + eventId).value.split(":");
	var startDate = document.getElementById("editStartDate-" + eventId).valueAsDate;

	var endTimeString = document.getElementById("editEndTime-" + eventId).value.split(":");
	var endDate = document.getElementById("editEndDate-" + eventId).valueAsDate;

	startDate.setHours(parseInt(startTimeString[0]), parseInt(startTimeString[1]), 0, 0);

	endDate.setHours(parseInt(endTimeString[0]), parseInt(endTimeString[1]), 0, 0);

	var colorElement = document.getElementById("editColor-" + eventId);
	var timeOffset = 0;
	if (is.firefox() || is.edge()) {
		timeOffset = 86400000;
	}
	if (document.getElementById("editEnableRecur-" + eventId) !== null) {
		var freqElement = document.getElementById("editFrequency-" + eventId);
		var bodyRaw = {
			"id": eventId,
			"summary": document.getElementById("editSummary-" + eventId).value,
			"description": document.getElementById("editDescription-" + eventId).value,
			"location": document.getElementById("editLocation-" + eventId).value,
			"image": document.getElementById("editImage-" + eventId).value,
			"color": colorElement.options[colorElement.selectedIndex].value,
			recurrence: {
				"recur": document.getElementById("editEnableRecur-" + eventId).checked,
				"frequency": freqElement.options[freqElement.selectedIndex].value,
				"count": parseInt(document.getElementById("editCount-" + eventId).value),
				"interval": parseInt(document.getElementById("editInterval-" + eventId).value)
			},
			"epochStart": startDate.getTime() + timeOffset,
			"epochEnd": endDate.getTime() + timeOffset
		};
	} else {
		bodyRaw = {
			"id": eventId,
			"summary": document.getElementById("editSummary-" + eventId).value,
			"description": document.getElementById("editDescription-" + eventId).value,
			"location": document.getElementById("editLocation-" + eventId).value,
			"image": document.getElementById("editImage-" + eventId).value,
			"color": colorElement.options[colorElement.selectedIndex].value,
			"recurrence": {
				"recur": false,
				"frequency": "DAILY",
				"count": -1,
				"interval": 1
			},
			"epochStart": startDate.getTime() + timeOffset,
			"epochEnd": endDate.getTime() + timeOffset
		};
	}

	var q = $.post("/api/v1/events/update", JSON.stringify(bodyRaw), function (response) {

		$('#modal-' + bodyRaw.id).modal('hide');

		showSnackbar("Event successfully updated!");

		setMonth({date: calendar.selectedDate});
		getEventsForMonth();

		getEventsForSelectedDate();
	})
		.fail(function () {
			showSnackbar("Our hippos failed to update your event!");
		}, "json");

}

function createNewEvent() {
	var startTimeString = document.getElementById("create-start-time").value.split(":");
	var startDate = document.getElementById("create-start-date").valueAsDate;

	var endTimeString = document.getElementById("create-end-time").value.split(":");
	var endDate = document.getElementById("create-end-date").valueAsDate;

	startDate.setHours(parseInt(startTimeString[0]), parseInt(startTimeString[1]), 0, 0);

	endDate.setHours(parseInt(endTimeString[0]), parseInt(endTimeString[1]), 0, 0);

	var colorElement = document.getElementById("create-color");
	var freqElement = document.getElementById("create-frequency");

	var timeOffset = 0;
	if (is.firefox() || is.edge()) {
		timeOffset = 86400000;
	}

	var bodyRaw = {
		"summary": document.getElementById("create-summary").value,
		"description": document.getElementById("create-description").value,
		"location": document.getElementById("create-location").value,
		"image": document.getElementById("create-image").value,
		"color": colorElement.options[colorElement.selectedIndex].value,
		"recurrence": {
			"recur": document.getElementById("create-enableRecur").checked,
			"frequency": freqElement.options[freqElement.selectedIndex].value,
			"count": parseInt(document.getElementById("create-count").value),
			"interval": parseInt(document.getElementById("create-interval").value)
		},
		"epochStart": startDate.getTime() + timeOffset,
		"epochEnd": endDate.getTime() + timeOffset
	};

	var q = $.post("/api/v1/events/create", JSON.stringify(bodyRaw), function (response) {
		showSnackbar("Event successfully created!");

		$('html:not(:animated), body:not(:animated)').animate({
			scrollTop: $("#calendar").offset().top
		}, 2000);

		document.getElementById("create-form").reset();

		setMonth({date: calendar.selectedDate});
		getEventsForMonth();

		getEventsForSelectedDate();
	})
		.fail(function () {
			showSnackbar("Our hippos failed to create your event!");
		}, "json");
}

function deleteEvent(clickedId) {
	var eventId = clickedId.replace("delete-", "");
	var bodyRaw = {"id": eventId};

	var q = $.post("/api/v1/events/delete", JSON.stringify(bodyRaw), function (ignore) {

		showSnackbar("Successfully deleted event!");

		setMonth({date: calendar.selectedDate});
		getEventsForMonth();

		getEventsForSelectedDate();
	})
		.fail(function () {
			showSnackbar("Our hippos failed to delete your event!");
		}, "json");
}

function init() {
	setMonth({date: calendar.todaysDate});

	getEventsForMonth();
}