# noinspection SqlResolveForFile

RENAME TABLE '${prefix}announcements' TO 'announcements',
    '${prefix}api' TO 'api',
    '${prefix}calendars' TO 'calendars',
    '${prefix}credentials' TO 'credentials',
    '${prefix}events' TO 'events',
    '${prefix}guild_settings' TO 'guild_settings',
    '${prefix}rsvp' TO 'rsvp';

