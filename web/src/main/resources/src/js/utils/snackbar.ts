export class Snackbar {
    public static showSnackbar(textToDisplay: string) {
        // Get the snackbar DIV
        let x = document.getElementById("snackbar")!;

        if (x == null) {
            return;
        }

        x.innerHTML = textToDisplay;

        // Add the "show" class to DIV
        x.className = "show";

        // After 3 seconds, remove the show class from DIV
        setTimeout(function () {
            x.className = x.className.replace("show", "");
        }, 3000);
    }
}