package org.dreamexposure.discal.server.endpoints.v3.guild

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import org.dreamexposure.discal.core.annotations.Authentication
import org.dreamexposure.discal.core.exceptions.NotFoundException
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.discord4j.getSettings
import org.dreamexposure.discal.core.`object`.web.WebCalendar
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.function.TupleUtils


@RestController
@RequestMapping("/v3/guilds/{guildId}/calendars/")
class CalendarEndpoint(private val client: DiscordClient) {

    @Authentication(access = Authentication.AccessLevel.PUBLIC, tokenType = Authentication.TokenType.BEARER)
    @GetMapping("{id}/view", produces = ["application/json"])
    fun get(@PathVariable guildId: Snowflake, @PathVariable id: Int, swe: ServerWebExchange): Mono<WebCalendar> {
        val restGuild = client.getGuildById(guildId)
        val settingsMono = restGuild.getSettings()
        val calendarMono = restGuild.getCalendar(id)

        return Mono.zip(settingsMono, calendarMono).map(TupleUtils.function { settings, calendar ->
            // TODO: handle calendar privacy

            calendar.toWebCalendar(settings)
        }).switchIfEmpty(Mono.error(NotFoundException()))
    }
}
