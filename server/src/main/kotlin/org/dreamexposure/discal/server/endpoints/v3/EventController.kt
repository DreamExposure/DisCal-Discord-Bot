package org.dreamexposure.discal.server.endpoints.v3

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.`object`.new.Event
import org.dreamexposure.discal.core.`object`.new.security.Scope
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/v3/guilds{guildId}/calendars/{calendarNumber}/events")
class EventController(
    private val calendarService: CalendarService,
) {
    // TODO: Need a way to check if authenticated user has access to the guild...

    @SecurityRequirement(scopes = [Scope.CALENDAR_EVENT_WRITE])
    @PostMapping(produces = ["application/json"], consumes = ["application/json"])
    suspend fun createEvent(@PathVariable guildId: Snowflake, @PathVariable calendarNumber: Int, @RequestBody spec: Event.CreateSpec): Event {
        return calendarService.createEvent(guildId, calendarNumber, spec)
    }

    @SecurityRequirement(scopes = [Scope.CALENDAR_EVENT_READ])
    @GetMapping("/{eventId}", produces = ["application/json"])
    suspend fun getEvent(@PathVariable guildId: Snowflake, @PathVariable calendarNumber: Int, @PathVariable eventId: String): Event? {
        return calendarService.getEvent(guildId, calendarNumber, eventId)
    }


    @SecurityRequirement(scopes = [Scope.CALENDAR_EVENT_READ])
    @GetMapping("/range")
    suspend fun getEventsInRange(@PathVariable guildId: Snowflake, @PathVariable calendarNumber: Int, @RequestParam start: Instant, @RequestParam end: Instant): List<Event> {
        return calendarService.getEventsInTimeRange(guildId, calendarNumber, start, end)
    }

    @SecurityRequirement(scopes = [Scope.CALENDAR_EVENT_READ])
    @GetMapping("/ongoing", produces = ["application/json"])
    suspend fun getOngoingEvents(@PathVariable guildId: Snowflake, @PathVariable calendarNumber: Int): List<Event> {
        return calendarService.getOngoingEvents(guildId, calendarNumber)
    }

    @SecurityRequirement(scopes = [Scope.CALENDAR_EVENT_WRITE])
    @PatchMapping("/{eventId}", consumes = ["application/json"], produces = ["application/json"])
    suspend fun updateEvent(@PathVariable guildId: Snowflake, @PathVariable calendarNumber: Int, @PathVariable eventId: String, @RequestBody spec: Event.UpdateSpec): Event {
        return calendarService.updateEvent(guildId, calendarNumber, spec.copy(id = eventId)) // Makes sure event id is in spec
    }

    @SecurityRequirement(scopes = [Scope.CALENDAR_EVENT_WRITE])
    @DeleteMapping("/{eventId}")
    suspend fun deleteEvent(@PathVariable guildId: Snowflake, @PathVariable calendarNumber: Int, @PathVariable eventId: String) {
        calendarService.deleteEvent(guildId, calendarNumber, eventId)
    }

}