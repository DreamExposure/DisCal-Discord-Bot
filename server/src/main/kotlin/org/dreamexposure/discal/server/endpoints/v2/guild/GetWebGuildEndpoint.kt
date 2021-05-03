package org.dreamexposure.discal.server.endpoints.v2.guild

import discord4j.common.util.Snowflake
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dreamexposure.discal.core.`object`.web.WebGuild
import org.dreamexposure.discal.core.exceptions.BotNotInGuildException
import org.dreamexposure.discal.core.file.ReadFile
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.utils.GlobalConst
import org.dreamexposure.discal.core.utils.PermissionChecker
import org.dreamexposure.discal.server.DisCalServer
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
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.function.TupleUtils

@RestController
@RequestMapping("/v2/guild")
class GetWebGuildEndpoint {
    @PostMapping(value = ["/get"], produces = ["application/json"])
    fun getSettings(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
        return Authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(Json.encodeToString(authState))
            }

            //Handle request
            val body = JSONObject(rBody)
            val guildId = Snowflake.of(body.getString("guild_id"))
            val userId = Snowflake.of(body.getString("user_id"))

            val g = DisCalServer.client.getGuildById(guildId)

            WebGuild.fromGuild(g).onErrorResume(BotNotInGuildException::class.java) { Mono.empty() }.flatMap { wg ->
                val member = g.member(userId)

                val manageServerMono = PermissionChecker.hasManageServerRole(member, g)
                val discalRoleMono = PermissionChecker.hasSufficientRole(member, wg.settings)
                val langsMono = ReadFile.readAllLangFiles()
                        .map { it.keySet() }
                        .flatMapMany { Flux.fromIterable(it) }
                        .map { it as String }
                        .collectList()

                Mono.zip(manageServerMono, discalRoleMono, langsMono)
                        .map(TupleUtils.function { manageServer, discalRole, langs ->
                            wg.manageServer = manageServer
                            wg.discalRole = discalRole
                            wg.availableLangs.addAll(langs)

                            wg
                        }).map(Json.Default::encodeToString)
                        .doOnNext { response.rawStatusCode = GlobalConst.STATUS_SUCCESS }
            }
        }.onErrorResume(BotNotInGuildException::class.java) {
            response.rawStatusCode = GlobalConst.STATUS_NOT_FOUND
            return@onErrorResume responseMessage("Guild not connected to DisCal")
        }.onErrorResume(JSONException::class.java) {
            it.printStackTrace()

            response.rawStatusCode = GlobalConst.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LogFeed.log(LogObject.forException("[API-v2] get web guild err", it, this.javaClass))

            response.rawStatusCode = GlobalConst.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
