package org.dreamexposure.discal.core.utils;

import org.dreamexposure.discal.core.database.DatabaseManager;

import java.util.UUID;

import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class AnnouncementUtils {
    /**
     * Checks if the announcement exists.
     *
     * @param value The announcement ID.
     * @return {@code true} if the announcement exists, else {@code false}.
     */
    public static Mono<Boolean> announcementExists(final String value, final Snowflake guildId) {
        return Mono.just(UUID.fromString(value))
            .flatMap(id -> DatabaseManager.getAnnouncement(id, guildId)
                .hasElement())
            .onErrorReturn(false); //If there's an error because of a bad value
    }
}