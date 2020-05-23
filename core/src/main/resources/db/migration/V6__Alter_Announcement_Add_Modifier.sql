# noinspection SqlResolveForFile

ALTER TABLE `${prefix}announcements`
    ADD COLUMN MODIFIER VARCHAR(255) NOT NULL DEFAULT 'BEFORE'
        AFTER ANNOUNCEMENT_TYPE;