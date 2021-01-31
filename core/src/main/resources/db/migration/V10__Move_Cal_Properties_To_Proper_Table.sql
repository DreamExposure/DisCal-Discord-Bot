# noinspection SqlResolveForFile

# Add columns for calendar data that will be moved over
ALTER TABLE ${prefix}calendars
    ADD COLUMN private_key   VARCHAR(16) NOT NULL DEFAULT 'N/a',
    ADD COLUMN access_token  LONGTEXT    NOT NULL,
    ADD COLUMN refresh_token LONGTEXT    NOT NULL;

# Copy and move data, setting defaults if not present
UPDATE ${prefix}calendars AS c
    LEFT JOIN ${prefix}guild_settings AS g
    on c.guild_id = g.guild_id
SET c.private_key   = IFNULL(g.private_key, 'N/a'),
    c.access_token  = IFNULL(g.access_token, 'N/a'),
    c.refresh_token = IFNULL(g.refresh_token, 'N/a');

# Delete columns from guild settings as they have now been moved over
ALTER TABLE ${prefix}guild_settings
    DROP COLUMN external_calendar,
    DROP COLUMN private_key,
    DROP COLUMN access_token,
    DROP COLUMN refresh_token;
