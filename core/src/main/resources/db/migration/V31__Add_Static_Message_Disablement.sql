ALTER TABLE static_messages
    ADD COLUMN enabled BIT NOT NULL DEFAULT 1
        AFTER scheduled_update;