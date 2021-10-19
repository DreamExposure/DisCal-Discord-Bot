package org.dreamexposure.discal.cam.endpoints.v1

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.`object`.network.discal.CredentialData
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class GetEndpoint {
    @GetMapping("get", produces = ["application/json"])
    fun get(@RequestParam host: CalendarHost, @RequestParam id: Int, @RequestParam guildId: Snowflake?): Mono<CredentialData> {
        if (guildId != null) {
            // External
        } else {
            // Internal
        }

        TODO()
    }
}
