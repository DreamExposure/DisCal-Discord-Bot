# noinspection SqlResolveForFile

# Add to events table
ALTER TABLE ${prefix}events
    ADD COLUMN calendar_number TINYINT DEFAULT 1
        after event_id;

# Add to rsvp table
ALTER TABLE ${prefix}rsvp
    ADD COLUMN calendar_number TINYINT DEFAULT 1
        after event_id;

# Add to announcements table
ALTER TABLE ${prefix}announcements
    ADD COLUMN calendar_number TINYINT DEFAULT 1
        after announcement_id;
