package org.dreamexposure.discal.server.endpoints.v3

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.business.RsvpService
import org.dreamexposure.discal.core.`object`.new.Rsvp
import org.dreamexposure.discal.core.`object`.new.security.Scope
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v3/guilds/{guildId}/events/{eventId}/rsvp")
class RsvpController(
    private val rsvpService: RsvpService,
) {

    @SecurityRequirement(scopes = [Scope.EVENT_RSVP_READ])
    @GetMapping(produces = ["application/json"])
    suspend fun getRsvp(@PathVariable guildId: Snowflake, @PathVariable eventId: String): Rsvp {
        // TODO: Need way to check if authenticated user has access to this guild
        return rsvpService.getRsvp(guildId, eventId)
    }


    @SecurityRequirement(scopes = [Scope.EVENT_RSVP_WRITE])
    @PatchMapping(produces = ["application/json"], consumes = ["application/json"])
    suspend fun patchRsvp(@PathVariable guildId: Snowflake, @PathVariable eventId: String, @RequestBody rsvp: Rsvp): Rsvp {
        // TODO: Need a way to check if authenticated user has access to this guild
        return rsvpService.updateRsvp(rsvp)
    }
}
