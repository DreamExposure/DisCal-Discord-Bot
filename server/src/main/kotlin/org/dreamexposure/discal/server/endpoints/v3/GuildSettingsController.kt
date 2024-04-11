package org.dreamexposure.discal.server.endpoints.v3

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.business.GuildSettingsService
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.`object`.new.security.Scope
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v3/guilds/{guildId}/settings")
class GuildSettingsController(
    private val settingsService: GuildSettingsService,
) {
    // TODO: Need way to check if authenticated user has access to the guild

    @SecurityRequirement(scopes = [Scope.GUILD_SETTINGS_READ])
    @GetMapping(produces = ["application/json"])
    suspend fun getSettings(@PathVariable guildId: Snowflake): GuildSettings {
        return settingsService.getSettings(guildId)
    }

    @SecurityRequirement(scopes = [Scope.GUILD_SETTINGS_WRITE])
    @PatchMapping(produces = ["application/json"])
    suspend fun updateSettings(@PathVariable guildId: Snowflake, @RequestBody settings: GuildSettings): GuildSettings {
        TODO("Not yet implemented")
    }

}
