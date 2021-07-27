package org.dreamexposure.discal.core.wrapper.google

import com.google.api.services.calendar.model.AclRule
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

object AclRuleWrapper {
    fun insertRule(rule: AclRule, calData: CalendarData): Mono<AclRule> {
        return GoogleAuthWrapper.getCalendarService(calData).flatMap { service ->
            Mono.fromCallable {
                service.acl()
                        .insert(calData.calendarId, rule)
                        .setQuotaUser(calData.guildId.asString())
                        .execute()
            }.subscribeOn(Schedulers.boundedElastic())
        }.doOnError {
            LogFeed.log(LogObject.forException("G.Calendar ACL insert failure", it, this.javaClass))
        }.onErrorResume { Mono.empty() }
    }
}
