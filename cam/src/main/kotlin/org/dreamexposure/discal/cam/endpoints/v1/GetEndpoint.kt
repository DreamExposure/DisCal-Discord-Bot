package org.dreamexposure.discal.cam.endpoints.v1

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.cam.google.GoogleAuth
import org.dreamexposure.discal.core.annotations.Authentication
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.`object`.network.discal.CredentialData
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/")
class GetEndpoint(
    private val calendarService: CalendarService,
    private val googleAuth: GoogleAuth,
) {
    @Authentication(access = Authentication.AccessLevel.ADMIN)
    @GetMapping("token", produces = ["application/json"])
    suspend fun get(@RequestParam host: CalendarHost, @RequestParam id: Int, @RequestParam guild: Snowflake?): CredentialData? {
        return when (host) {
            CalendarHost.GOOGLE -> {
                if (guild == null) {
                    // Internal (owned by DisCal, should never go bad)
                    googleAuth.requestNewAccessToken(id)
                } else {
                    // External (owned by user)
                    val calendar = calendarService.getCalendar(guild, id) ?: return null
                    googleAuth.requestNewAccessToken(calendar)
                }
            }
        }
    }
}
