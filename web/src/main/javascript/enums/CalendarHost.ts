export enum CalendarHost {
    GOOGLE,
}

export function humanFriendlyHostName(host: CalendarHost) {
    switch (host) {
        case CalendarHost.GOOGLE: {
            return 'Google';
        }
        default: {
            return "Unknown Host";
        }
    }
}
