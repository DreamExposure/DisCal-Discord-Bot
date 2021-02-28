export enum EventColor {
    MELROSE,
    RIPTIDE,
    MAUVE,
    TANGERINE,
    DANDELION,
    MAC_AND_CHEESE,
    TURQUOISE,
    MERCURY,
    BLUE,
    GREEN,
    RED,
    NONE
}

export function eventColorRGB(color: EventColor) {
    switch (color) {
        case EventColor.MELROSE: {
            return '#A4BDFC';
        }
        case EventColor.RIPTIDE: {
            return '#7AE7BF';
        }
        case EventColor.MAUVE: {
            return '#DBADFF';
        }
        case EventColor.TANGERINE: {
            return '#FF887C';
        }
        case EventColor.DANDELION: {
            return '#FBD75B';
        }
        case EventColor.MAC_AND_CHEESE: {
            return '#FFB878';
        }
        case EventColor.TURQUOISE: {
            return '#46D6DB';
        }
        case EventColor.MERCURY: {
            return '#E1E1E1';
        }
        case EventColor.BLUE: {
            return '#5484ED';
        }
        case EventColor.GREEN: {
            return '#51B749';
        }
        case EventColor.RED: {
            return '#DC2127';
        }
    }
}

export function eventColorClass(color: EventColor) {
    switch (color) {
        case EventColor.MELROSE: {
            return 'google-melrose';
        }
        case EventColor.RIPTIDE: {
            return 'google-riptide';
        }
        case EventColor.MAUVE: {
            return 'google-mauve';
        }
        case EventColor.TANGERINE: {
            return 'google-tangerine';
        }
        case EventColor.DANDELION: {
            return 'google-dandelion';
        }
        case EventColor.MAC_AND_CHEESE: {
            return 'google-mac_and_cheese';
        }
        case EventColor.TURQUOISE: {
            return 'google-turquoise';
        }
        case EventColor.MERCURY: {
            return 'google-mercury';
        }
        case EventColor.BLUE: {
            return 'google-blue';
        }
        case EventColor.GREEN: {
            return 'google-green';
        }
        case EventColor.RED: {
            return 'google-red';
        }
        default: {
            return "discord-blurple";
        }
    }
}

