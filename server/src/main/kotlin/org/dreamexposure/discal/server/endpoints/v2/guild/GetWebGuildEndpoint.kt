package org.dreamexposure.discal.server.endpoints.v2.guild

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.exceptions.BotNotInGuildException
import org.dreamexposure.discal.core.extensions.discord4j.hasControlRole
import org.dreamexposure.discal.core.extensions.discord4j.hasElevatedPermissions
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.web.WebGuild
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.server.utils.Authentication
import org.dreamexposure.discal.server.utils.responseMessage
import org.json.JSONException
import org.json.JSONObject
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.function.TupleUtils

@RestController
@RequestMapping("/v2/guild")
class GetWebGuildEndpoint(val client: DiscordClient) {
    @PostMapping(value = ["/get"], produces = ["application/json"])
    @SecurityRequirement(disableSecurity = true, scopes = [])
    fun getSettings(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
        return Authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(GlobalVal.JSON_FORMAT.encodeToString(authState))
            }

            //Handle request
            val body = JSONObject(rBody)
            val guildId = Snowflake.of(body.getString("guild_id"))
            val userId = Snowflake.of(body.getString("user_id"))

            val g = client.getGuildById(guildId)

            WebGuild.fromGuild(g).flatMap { wg ->
                val member = g.member(userId)

                val elevatedMono = member.hasElevatedPermissions()
                val discalRoleMono = member.hasControlRole()

                Mono.zip(elevatedMono, discalRoleMono)
                        .map(TupleUtils.function { elevated, discalRole ->
                            wg.elevatedAccess = elevated
                            wg.discalRole = discalRole

                            wg
                        }).map { GlobalVal.JSON_FORMAT.encodeToString(it) }
                        .doOnNext { response.rawStatusCode = GlobalVal.STATUS_SUCCESS }
            }
        }.onErrorResume(BotNotInGuildException::class.java) {
            response.rawStatusCode = GlobalVal.STATUS_NOT_FOUND
            return@onErrorResume responseMessage("Guild not connected to DisCal")
        }.onErrorResume(JSONException::class.java) {
            LOGGER.trace("[API-v2] JSON error. Bad request?", it)

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LOGGER.error(GlobalVal.DEFAULT, "[API-v2] get web guild error", it)

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
