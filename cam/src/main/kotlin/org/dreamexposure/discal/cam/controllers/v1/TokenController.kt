package org.dreamexposure.discal.cam.controllers.v1

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.cam.managers.CalendarAuthManager
import org.dreamexposure.discal.core.annotations.Authentication
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.`object`.network.discal.CredentialData
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/token")
class TokenController(
    private val calendarAuthManager: CalendarAuthManager,
) {
    @Authentication(access = Authentication.AccessLevel.ADMIN)
    @GetMapping(produces = ["application/json"])
    suspend fun getToken(@RequestParam host: CalendarHost, @RequestParam id: Int, @RequestParam guild: Snowflake?): CredentialData? {
        return calendarAuthManager.getCredentialData(host, id, guild)
    }
}
