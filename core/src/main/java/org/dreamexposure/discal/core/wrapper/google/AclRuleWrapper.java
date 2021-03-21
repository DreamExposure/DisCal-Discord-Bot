package org.dreamexposure.discal.core.wrapper.google;

import com.google.api.services.calendar.model.AclRule;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class AclRuleWrapper {
    public static Mono<AclRule> insertRule(AclRule rule, CalendarData data) {
        return CalendarAuth.getCalendarService(data)
            .flatMap(service -> Mono.fromCallable(() ->
                service.acl()
                    .insert(data.getCalendarId(), rule)
                    .setQuotaUser(data.getGuildId().asString())
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic()))
            .onErrorResume(e -> Mono.empty());
    }
}
