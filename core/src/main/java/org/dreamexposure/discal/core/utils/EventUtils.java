package org.dreamexposure.discal.core.utils;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.wrapper.google.EventWrapper;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@Deprecated
public class EventUtils {
    @Deprecated
    public static Mono<Boolean> eventExists(final GuildSettings settings, final String eventId) {
        return DatabaseManager.INSTANCE.getMainCalendar(settings.getGuildID())
            .flatMap(data ->
                EventWrapper.INSTANCE.getEvent(data, eventId)
                    .hasElement()
            ).switchIfEmpty(Mono.just(false));
    }
}
