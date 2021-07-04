package org.dreamexposure.discal.server.endpoints.v2.guild

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.discordjson.json.ImmutableNicknameModifyData
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
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
import java.util.*

@RestController
@RequestMapping("/v2/guild")
class UpdateWebGuildEndpoint(val client: DiscordClient) {
    @PostMapping(value = ["/update"], produces = ["application/json"])
    fun updateGuild(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
        return Authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(GlobalVal.JSON_FORMAT.encodeToString(authState))
            } else if (authState.readOnly) {
                response.rawStatusCode = GlobalVal.STATUS_AUTHORIZATION_DENIED
                return@flatMap responseMessage("Read-Only key not allowed")
            }

            //Handle request
            val body = JSONObject(rBody)
            val guildId = Snowflake.of(body.getString("guild_id"))

            val guild = client.getGuildById(guildId)

            //Handle nickname change
            val nicknameMono = Mono.defer {
                if (body.has("bot_nick")) {
                    return@defer guild.modifyOwnNickname(ImmutableNicknameModifyData.of(Optional.ofNullable(body.getString
                    ("bot_nick"))))
                }
                return@defer Mono.empty()
            }

            return@flatMap Mono.`when`(nicknameMono)
                    .then(responseMessage("Success"))
                    .doOnNext { response.rawStatusCode = GlobalVal.STATUS_SUCCESS }
        }.onErrorResume(JSONException::class.java) {
            it.printStackTrace()

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LogFeed.log(LogObject.forException("[API-v2] update web guild err", it, this.javaClass))

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
