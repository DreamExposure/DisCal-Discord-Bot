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
    public static Mono<Boolean> deleteEvent(final GuildSettings settings, final int calNumber, final String eventId) {
        return DatabaseManager.INSTANCE.getCalendar(settings.getGuildID(), calNumber)
            .flatMap(data ->
                EventWrapper.INSTANCE.deleteEvent(data, eventId)
                    .flatMap(success -> {
                        if (success) {
                            return Mono.when(
                                DatabaseManager.INSTANCE.deleteAnnouncementsForEvent(settings.getGuildID(), eventId),
                                DatabaseManager.INSTANCE.deleteEventData(eventId)
                            ).thenReturn(true);
                        } else {
                            return Mono.just(false);
                        }
                    })
            ).defaultIfEmpty(false);
    }

    @Deprecated
    public static Mono<Boolean> eventExists(final GuildSettings settings, final String eventId) {
        return DatabaseManager.INSTANCE.getMainCalendar(settings.getGuildID())
            .flatMap(data ->
                EventWrapper.INSTANCE.getEvent(data, eventId)
                    .hasElement()
            ).switchIfEmpty(Mono.just(false));
    }

    public static Mono<Boolean> eventExists(final GuildSettings settings, final int calNumber, final String eventId) {
        return DatabaseManager.INSTANCE.getCalendar(settings.getGuildID(), calNumber)
            .flatMap(data ->
                EventWrapper.INSTANCE.getEvent(data, eventId)
                    .hasElement()
            ).switchIfEmpty(Mono.just(false));
    }
}
