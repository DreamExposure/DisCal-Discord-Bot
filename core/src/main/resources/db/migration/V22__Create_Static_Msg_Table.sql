CREATE TABLE IF NOT EXISTS static_messages
(
    guild_id BIGINT NOT NULL,
    message_id BIGINT NOT NULL,
    type SMALLINT NOT NULL,
    last_update DATETIME NOT NULL,
    PRIMARY KEY (guild_id, message_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
