ALTER TABLE guild_settings
    ADD COLUMN event_keep_duration BIT NOT NULL DEFAULT 0
        AFTER announcement_style;
