package org.dreamexposure.discal.core.wrapper.google

import com.google.api.services.calendar.model.AclRule
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
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
            LOGGER.error(GlobalVal.DEFAULT, "[G.Cal] ACLRule insert failure", it)

        }.onErrorResume { Mono.empty() }
    }
}
