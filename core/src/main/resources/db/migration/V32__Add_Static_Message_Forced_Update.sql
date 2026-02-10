ALTER TABLE static_messages
    ADD COLUMN forced_update DATETIME NULL DEFAULT NULL
        AFTER scheduled_update;