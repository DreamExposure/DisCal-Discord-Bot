ALTER TABLE guild_settings
    ADD COLUMN show_rsvp_dropdown BIT NOT NULL DEFAULT 1
        AFTER event_keep_duration;