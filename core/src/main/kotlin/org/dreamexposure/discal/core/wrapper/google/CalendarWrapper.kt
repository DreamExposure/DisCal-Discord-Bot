package org.dreamexposure.discal.core.wrapper.google

import com.google.api.services.calendar.model.Calendar
import com.google.api.services.calendar.model.CalendarListEntry
import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

object CalendarWrapper {
    fun createCalendar(calendar: Calendar, credId: Int, guildId: Snowflake): Mono<Calendar> {
        return GoogleAuthWrapper.getCalendarService(credId).flatMap { service ->
            Mono.fromCallable {
                service.calendars()
                        .insert(calendar)
                        .setQuotaUser(guildId.asString())
                        .execute()
            }.subscribeOn(Schedulers.boundedElastic())
        }.doOnError {
            LogFeed.log(LogObject.forException("G.Calendar create failure", it, this.javaClass))
        }.onErrorResume { Mono.empty() }
    }

    fun patchCalendar(calendar: Calendar, calData: CalendarData): Mono<Calendar> {
        return GoogleAuthWrapper.getCalendarService(calData).flatMap { service ->
            Mono.fromCallable {
                service.calendars()
                        .patch(calendar.id, calendar)
                        .setQuotaUser(calData.guildId.asString())
                        .execute()
            }.subscribeOn(Schedulers.boundedElastic())
        }.doOnError {
            LogFeed.log(LogObject.forException("G.Calendar patch failure", it, this.javaClass))
        }.onErrorResume { Mono.empty() }
    }

    fun updateCalendar(calendar: Calendar, calData: CalendarData): Mono<Calendar> {
        return GoogleAuthWrapper.getCalendarService(calData).flatMap { service ->
            Mono.fromCallable {
                service.calendars()
                        .update(calendar.id, calendar)
                        .setQuotaUser(calData.guildId.asString())
                        .execute()
            }.subscribeOn(Schedulers.boundedElastic())
        }.doOnError {
            LogFeed.log(LogObject.forException("G.Calendar update failure", it, this.javaClass))
        }.onErrorResume { Mono.empty() }
    }

    fun getCalendar(calData: CalendarData): Mono<Calendar> {
        return GoogleAuthWrapper.getCalendarService(calData).flatMap { service ->
            Mono.fromCallable {
                service.calendars()
                        .get(calData.calendarAddress)
                        .setQuotaUser(calData.guildId.asString())
                        .execute()
            }.subscribeOn(Schedulers.boundedElastic())
        }.doOnError {
            LogFeed.log(LogObject.forException("G.Calendar get failure", it, this.javaClass))
        }.onErrorResume { Mono.empty() }
    }

    fun deleteCalendar(calData: CalendarData): Mono<Boolean> {
        return Mono.just(calData)
                .filter { !it.external }
                .filter { !it.calendarAddress.equals("primary", true) }
                .flatMap { GoogleAuthWrapper.getCalendarService(calData) }
                .flatMap { service ->
                    Mono.fromCallable {
                        service.calendars()
                                .delete(calData.calendarAddress)
                                .setQuotaUser(calData.guildId.asString())
                                .execute()
                    }.subscribeOn(Schedulers.boundedElastic())
                }.thenReturn(true)
                .doOnError {
                    LogFeed.log(LogObject.forException("G.Calendar delete failure", it, this.javaClass))
                }.onErrorReturn(false)
    }

    fun getUsersExternalCalendars(calData: CalendarData): Mono<List<CalendarListEntry>> {
        return GoogleAuthWrapper.getCalendarService(calData).flatMap { service ->
            Mono.fromCallable {
                service.calendarList()
                        .list()
                        .setMinAccessRole("writer")
                        .setQuotaUser(calData.guildId.asString())
                        .execute()
                        .items
            }.subscribeOn(Schedulers.boundedElastic())
        }.doOnError {
            LogFeed.log(LogObject.forException("G.Calendar external list failure", it, this.javaClass))
        }.onErrorResume { Mono.empty() }
    }
}
