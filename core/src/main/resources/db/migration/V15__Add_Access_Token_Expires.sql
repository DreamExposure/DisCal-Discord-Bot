# noinspection SqlResolveForFile

# Add to credential table
ALTER TABLE ${prefix}credentials
    ADD COLUMN expires_at BIGINT DEFAULT 0
        after access_token;

# Add to calendars table
ALTER TABLE ${prefix}calendars
    ADD COLUMN expires_at BIGINT DEFAULT 0
        after refresh_token;
