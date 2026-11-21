package org.dreamexposure.discal.server.endpoints.v3

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.`object`.new.model.discal.WebGuildV3Model
import org.dreamexposure.discal.core.`object`.new.security.AccessLevel
import org.dreamexposure.discal.core.`object`.new.security.Scope
import org.dreamexposure.discal.server.business.WebGuildService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v3/guilds/{guildId}")
class GuildController(
    private val webGuildService: WebGuildService,
) {
    @SecurityRequirement(scopes = [Scope.GUILD_WEB_READ, Scope.CALENDAR_READ, Scope.GUILD_SETTINGS_READ], accessLevel = AccessLevel.GUILD_MEMBER_ONLY)
    @GetMapping(produces = ["application/json"])
    suspend fun getGuild(@PathVariable guildId: Snowflake, @RequestParam userId: Snowflake): WebGuildV3Model {
        return webGuildService.getWebGuildForUser(guildId, userId)
    }
}