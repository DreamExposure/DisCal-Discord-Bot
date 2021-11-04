package org.dreamexposure.discal.cam.endpoints.v1

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.cam.google.GoogleAuth
import org.dreamexposure.discal.core.`object`.network.discal.CredentialData
import org.dreamexposure.discal.core.annotations.Authentication
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/")
class GetEndpoint {

    @Authentication(access = Authentication.AccessLevel.ADMIN)
    @GetMapping("token", produces = ["application/json"])
    fun get(@RequestParam host: CalendarHost, @RequestParam id: Int, @RequestParam guild: Snowflake?): Mono<CredentialData> {
        return Mono.defer {
            when (host) {
                CalendarHost.GOOGLE -> {
                    if (guild == null) {
                        // Internal (owned by DisCal, should never go bad)
                        GoogleAuth.requestNewAccessToken(id)
                    } else {
                        // External (owned by user)
                        DatabaseManager.getCalendar(guild, id).flatMap(GoogleAuth::requestNewAccessToken)
                    }
                }
            }
        }.doOnError {
            LOGGER.error(DEFAULT, "Access token grant failure", it)
        }
    }
}
