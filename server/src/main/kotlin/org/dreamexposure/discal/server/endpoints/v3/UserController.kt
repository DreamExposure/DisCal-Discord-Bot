package org.dreamexposure.discal.server.endpoints.v3

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.`object`.new.model.discal.PartialGuildV3Model
import org.dreamexposure.discal.core.`object`.new.security.AccessLevel
import org.dreamexposure.discal.core.`object`.new.security.Scope
import org.dreamexposure.discal.server.business.GuildService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Optional
import kotlin.jvm.optionals.getOrDefault

@RestController
@RequestMapping("/v3/users/{userId}")
class UserController(
    val guildService: GuildService,
) {
    @SecurityRequirement(scopes = [Scope.GUILD_LIST_READ], accessLevel = AccessLevel.USER)
    @GetMapping("/guilds", produces = ["application/json"])
    suspend fun getGuilds(@PathVariable userId: Snowflake, @RequestParam skipCache: Optional<Boolean>): List<PartialGuildV3Model> {
        return guildService.getGuildsForUser(userId, skipCache.getOrDefault(false))
    }
}