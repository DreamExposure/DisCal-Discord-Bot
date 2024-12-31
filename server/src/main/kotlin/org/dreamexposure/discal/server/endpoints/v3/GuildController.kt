package org.dreamexposure.discal.server.endpoints.v3

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.`object`.new.model.discal.WebGuildV3Model
import org.dreamexposure.discal.core.`object`.new.security.Scope
import org.dreamexposure.discal.server.business.WebGuildService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v3/guilds/{guildId}")
class GuildController(
    private val webGuildService: WebGuildService,
) {
    // TODO: Need way to check if authenticated user has access to the guild...

    @SecurityRequirement(scopes = [Scope.GUILD_WEB_READ, Scope.CALENDAR_READ])
    @GetMapping(produces = ["application/json"])
    suspend fun getGuild(@PathVariable guildId: Snowflake): WebGuildV3Model {
        // TODO: Need to extract user from token and make that accessible here. how do???
        return webGuildService.getWebGuildForUser(guildId, TODO())
    }
}