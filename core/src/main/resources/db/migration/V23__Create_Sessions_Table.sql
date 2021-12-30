CREATE TABLE IF NOT EXISTS sessions
(
    token CHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at DATETIME NOT NULL,
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL,
    PRIMARY KEY (token)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
