package org.dreamexposure.discal.server.endpoints.v3

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.model.discal.cam.CalendarV3Model
import org.dreamexposure.discal.core.`object`.new.security.Scope
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v3/guilds/{guildId}/calendars")
class CalendarController(
    private val calendarService: CalendarService,
) {
    // TODO: Need a way to check if authenticated user has access to the guild...

    // TODO: Create calendar endpoint???

    @SecurityRequirement(scopes = [Scope.CALENDAR_READ])
    @GetMapping(produces = ["application/json"])
    suspend fun getAllCalendars(@PathVariable("guildId") guildId: Snowflake): List<CalendarV3Model> {
        return calendarService.getAllCalendars(guildId).map(::CalendarV3Model)
    }

    @SecurityRequirement(scopes = [Scope.CALENDAR_READ])
    @GetMapping("/{calendarNumber}")
    suspend fun getCalendar(@PathVariable guildId: Snowflake, @PathVariable calendarNumber: Int): CalendarV3Model? {
        val calendar = calendarService.getCalendar(guildId, calendarNumber) ?: return null
        return CalendarV3Model(calendar)
    }

    @SecurityRequirement(scopes = [Scope.CALENDAR_WRITE])
    @PatchMapping("/{calendarNumber}", produces = ["application/json"], consumes = ["application/json"])
    suspend fun updateCalendar(@PathVariable guildId: Snowflake, @PathVariable calendarNumber: Int, @RequestBody spec: Calendar.UpdateSpec): CalendarV3Model {
        val calendar = calendarService.updateCalendar(guildId, calendarNumber, spec)
        return CalendarV3Model(calendar)
    }

    @SecurityRequirement(scopes = [Scope.CALENDAR_WRITE])
    @DeleteMapping("/{calendarNumber}")
    suspend fun deleteCalendar(@PathVariable guildId: Snowflake, @PathVariable calendarNumber: Int) {
        calendarService.deleteCalendar(guildId, calendarNumber)
    }
}