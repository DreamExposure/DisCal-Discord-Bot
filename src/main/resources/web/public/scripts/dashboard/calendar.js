var calendar = {
	todaysDate: new Date(),
	selectedDate: new Date()
};


function getMonthName(index) {
	return ['January', 'February',
		'March', 'April',
		'May', 'June',
		'July', 'August',
		'September', 'October',
		'November', 'December'][index];
}

function init() {
	setMonth({date: calendar.todaysDate});
}

function setMonth(parameters) {
	var date = parameters.date;

	document.getElementById("month-display").innerHTML = getMonthName(date.getMonth()) + ' ' + date.getFullYear();
}

function previousMonth() {
	calendar.selectedDate.setMonth(calendar.selectedDate.getMonth() - 1);

	setMonth({date: calendar.selectedDate});
}

function nextMonth() {
	calendar.selectedDate.setMonth(calendar.selectedDate.getMonth() + 1);
	setMonth({date: calendar.selectedDate});
}