ALTER TABLE sessions
    ADD COLUMN scopes LONGTEXT NOT NULL
        AFTER refresh_token;

ALTER TABLE api
    ADD COLUMN scopes LONGTEXT NOT NULL
        AFTER time_issued;
