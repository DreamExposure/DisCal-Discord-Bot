# noinspection SqlResolveForFile

ALTER TABLE `${prefix}guild_settings`
    ADD COLUMN CREDENTIALS_ID INTEGER NOT NULL DEFAULT 0
        AFTER PRIVATE_KEY;