package org.dreamexposure.discal.core.wrapper.google

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.function.Predicate

object EventWrapper {
    fun createEvent(calData: CalendarData, event: Event): Mono<Event> {
        return GoogleAuthWrapper.getCalendarService(calData).flatMap { service ->
            Mono.fromCallable {
                service.events()
                        .insert(calData.calendarId, event)
                        .setQuotaUser(calData.guildId.asString())
                        .execute()
            }.subscribeOn(Schedulers.boundedElastic())
        }.doOnError {
            LOGGER.error(GlobalVal.DEFAULT, "[G.Cal] Event create failure", it)
        }.onErrorResume { Mono.empty() }
    }

    fun patchEvent(calData: CalendarData, event: Event): Mono<Event> {
        return GoogleAuthWrapper.getCalendarService(calData).flatMap { service ->
            Mono.fromCallable {
                service.events()
                        .patch(calData.calendarId, event.id, event)
                        .setQuotaUser(calData.guildId.asString())
                        .execute()
            }.subscribeOn(Schedulers.boundedElastic())
        }.doOnError {
            LOGGER.error(GlobalVal.DEFAULT, "[G.Cal] Event patch failure", it)
        }.onErrorResume { Mono.empty() }
    }

    fun updateEvent(calData: CalendarData, event: Event): Mono<Event> {
        return GoogleAuthWrapper.getCalendarService(calData).flatMap { service ->
            Mono.fromCallable {
                service.events()
                        .update(calData.calendarId, event.id, event)
                        .setQuotaUser(calData.guildId.asString())
                        .execute()
            }.subscribeOn(Schedulers.boundedElastic())
        }.doOnError {
            LOGGER.error(GlobalVal.DEFAULT, "[G.Cal] Event update failure", it)
        }.onErrorResume { Mono.empty() }
    }

    fun getEvent(calData: CalendarData, id: String): Mono<Event> {
        return GoogleAuthWrapper.getCalendarService(calData).flatMap { service ->
            Mono.fromCallable {
                service.events()
                        .get(calData.calendarId, id)
                        .setQuotaUser(calData.guildId.asString())
                        .execute()
            }.filter {
                //Don't show "deleted" events
                /*
                See "status" flag: https://developers.google.com/calendar/api/v3/reference/events#resource
                 */
                !it.status.equals("cancelled", true)
            }.subscribeOn(Schedulers.boundedElastic())
        }.onErrorResume { Mono.empty() } //Can safely ignore this, the event just doesn't exist.
    }

    fun getEvents(calData: CalendarData, amount: Int, start: Long): Mono<List<Event>> {
        return GoogleAuthWrapper.getCalendarService(calData).flatMap { service ->
            Mono.fromCallable {
                service.events()
                        .list(calData.calendarId)
                        .setMaxResults(amount)
                        .setTimeMin(DateTime(start))
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .setShowDeleted(false)
                        .setQuotaUser(calData.guildId.asString())
                        .execute()
                        .items
            }.subscribeOn(Schedulers.boundedElastic())
        }.doOnError {
            LOGGER.error(GlobalVal.DEFAULT, "[G.Cal] Event list(1) failure", it)
        }.onErrorResume { Mono.empty() }
    }

    @Deprecated(message = "Deprecated, do not use service directly")
    fun getEvents(calData: CalendarData, service: Calendar, amount: Int, start: Long): Mono<List<Event>> {
        return Mono.fromCallable {
            service.events()
                    .list(calData.calendarId)
                    .setMaxResults(amount)
                    .setTimeMin(DateTime(start))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setShowDeleted(false)
                    .setQuotaUser(calData.guildId.asString())
                    .execute().items
        }.subscribeOn(Schedulers.boundedElastic())
                .doOnError {
                    LOGGER.error(GlobalVal.DEFAULT, "[G.Cal] Event list(2) failure", it)
                }.onErrorResume { Mono.empty() }
    }

    fun getEvents(calData: CalendarData, amount: Int, start: Long, end: Long): Mono<List<Event>> {
        return GoogleAuthWrapper.getCalendarService(calData).flatMap { service: Calendar ->
            Mono.fromCallable {
                service.events()
                        .list(calData.calendarId)
                        .setMaxResults(amount)
                        .setTimeMin(DateTime(start))
                        .setTimeMax(DateTime(end))
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .setShowDeleted(false)
                        .setQuotaUser(calData.guildId.asString())
                        .execute().items
            }.subscribeOn(Schedulers.boundedElastic())
        }.doOnError {
            LOGGER.error(GlobalVal.DEFAULT, "[G.Cal] Event list(3) failure", it)
        }.onErrorResume { Mono.empty() }
    }

    fun getEvents(calData: CalendarData, start: Long, end: Long): Mono<List<Event>> {
        return GoogleAuthWrapper.getCalendarService(calData).flatMap { service: Calendar ->
            Mono.fromCallable {
                service.events()
                        .list(calData.calendarId)
                        .setTimeMin(DateTime(start))
                        .setTimeMax(DateTime(end))
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .setShowDeleted(false)
                        .setQuotaUser(calData.guildId.asString())
                        .execute().items
            }.subscribeOn(Schedulers.boundedElastic())
        }.doOnError {
            LOGGER.error(GlobalVal.DEFAULT, "[G.Cal] Event list(4) failure", it)
        }.onErrorResume { Mono.empty() }
    }

    fun deleteEvent(calData: CalendarData, id: String?): Mono<Boolean> {
        return GoogleAuthWrapper.getCalendarService(calData).flatMap { service: Calendar ->
            Mono.fromCallable {
                val response = service.events()
                        .delete(calData.calendarAddress, id)
                        .setQuotaUser(calData.guildId.asString())
                        .executeUnparsed()

                //Google sends 4 possible status codes, 200, 204, 404, 410.
                // First 2 should be treated as successful, and the other 2 as not found.
                when (response.statusCode) {
                    200, 204 -> {
                        return@fromCallable true
                    }
                    404, 410 -> {
                        return@fromCallable false
                    }
                    else -> {
                        //Log response data and return false as google sent an unexpected response code.
                        LOGGER.debug(GlobalVal.DEFAULT, "Event delete error | ${response.statusCode} | ${response.statusMessage}")
                        return@fromCallable false
                    }
                }
            }.subscribeOn(Schedulers.boundedElastic())
        }.doOnError(GoogleJsonResponseException::class.java) {
            if (it.statusCode != 410 || it.statusCode != 404) {
                LOGGER.error(GlobalVal.DEFAULT, "[G.Cal] Event delete failure", it)
            }
        }.doOnError(Predicate.not(GoogleJsonResponseException::class.java::isInstance)) {
            LOGGER.error(GlobalVal.DEFAULT, "[G.Cal] Event delete failure", it)
        }.onErrorReturn(false)
    }
}
