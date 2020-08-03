package org.dreamexposure.discal.core.wrapper.google;

import com.google.api.services.calendar.model.AclRule;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.object.GuildSettings;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class AclRuleWrapper {
    public static Mono<AclRule> insertRule(final AclRule rule, final String calendarId, final GuildSettings settings) {
        return CalendarAuth.getCalendarService(settings)
            .flatMap(service -> Mono.fromCallable(() ->
                service.acl()
                    .insert(calendarId, rule)
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic()))
            .onErrorResume(e -> Mono.empty());
    }
}
