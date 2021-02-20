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
