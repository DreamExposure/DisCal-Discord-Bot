# noinspection SqlResolveForFile

ALTER TABLE ${prefix}calendars
    ADD COLUMN host VARCHAR(255) NOT NULL DEFAULT 'GOOGLE' AFTER calendar_number;
