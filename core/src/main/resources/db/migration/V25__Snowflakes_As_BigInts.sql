# commented out sections are due to legacy issues >.<

ALTER TABLE announcements MODIFY guild_id BIGINT NOT NULL;
#ALTER TABLE announcements MODIFY channel_id BIGINT NOT NULL;

#ALTER TABLE api MODIFY user_id BIGINT NOT NULL;

ALTER TABLE calendars MODIFY guild_id BIGINT NOT NULL;

ALTER TABLE events MODIFY guild_id BIGINT NOT NULL;

ALTER TABLE guild_settings MODIFY guild_id BIGINT NOT NULL;
#ALTER TABLE guild_settings MODIFY control_role BIGINT NOT NULL;

ALTER TABLE rsvp MODIFY guild_id BIGINT NOT NULL;

