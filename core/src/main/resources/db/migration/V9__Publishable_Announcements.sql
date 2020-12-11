# noinspection SqlResolveForFile

ALTER TABLE `${prefix}announcements`
    ADD COLUMN PUBLISH BIT(1) NOT NULL DEFAULT 0
        AFTER INFO_ONLY;