function handleVisibility() {
	var e = document.getElementById("create-ann-type");
	var value = e.options[e.selectedIndex].value;

	if (value === "UNIVERSAL") {
		//HIde color and event ID
		document.getElementById("create-ann-event").style.visibility = "hidden";
		document.getElementById("create-ann-color").style.visibility = "hidden";
	} else if (value === "SPECIFIC" || value === "RECUR") {
		//Hide color, show event ID.
		document.getElementById("create-ann-event").style.visibility = "visible";
		document.getElementById("create-ann-color").style.visibility = "hidden";
	} else if (value === "COLOR") {
		//Hide event ID, show color.
		document.getElementById("create-ann-event").style.visibility = "hidden";
		document.getElementById("create-ann-color").style.visibility = "visible";
	}
}