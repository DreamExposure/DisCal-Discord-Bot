# noinspection SqlResolveForFile

ALTER TABLE `${prefix}guild_settings`
    DROP COLUMN CREDENTIALS_ID;

ALTER TABLE `${prefix}calendars`
    ADD COLUMN CREDENTIAL_ID INTEGER NOT NULL DEFAULT 0
        AFTER EXTERNAL;