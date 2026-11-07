ALTER TABLE events
    ADD COLUMN image_alt TEXT NOT NULL DEFAULT ''
        AFTER image_link;