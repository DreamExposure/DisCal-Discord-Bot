function getMonthName(index) {
	return ['January', 'February',
		'March', 'April',
		'May', 'June',
		'July', 'August',
		'September', 'October',
		'November', 'December'][index];
}

function init() {
	var today = new Date();

	setMonth({date: today});
}

function setMonth(parameters) {
	var date = parameters.date;

	document.getElementById("month-display").innerHTML = getMonthName(date.getMonth()) + ' ' + date.getFullYear();
}