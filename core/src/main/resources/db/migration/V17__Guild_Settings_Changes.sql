
# Delete simple announcement
ALTER TABLE ${prefix}guild_settings
    DROP COLUMN SIMPLE_ANNOUNCEMENT,
    DROP COLUMN 12_HOUR;

# Add announcement_format
ALTER TABLE ${prefix}guild_settings
    ADD COLUMN announcement_style TINYINT default 1,
    ADD COLUMN time_format TINYINT default 1
        after DISCAL_CHANNEL;
