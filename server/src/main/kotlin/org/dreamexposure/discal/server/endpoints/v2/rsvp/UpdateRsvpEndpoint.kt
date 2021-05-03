package org.dreamexposure.discal.server.endpoints.v2.rsvp

import discord4j.common.util.Snowflake
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.utils.GlobalConst
import org.dreamexposure.discal.core.utils.RoleUtils
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
@RequestMapping("/v2/rsvp")
class UpdateRsvpEndpoint {
    @PostMapping(value = ["/update"], produces = ["application/json"])
    fun updateRsvp(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
        return Authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(Json.encodeToString(authState))
            } else if (authState.readOnly) {
                response.rawStatusCode = GlobalConst.STATUS_AUTHORIZATION_DENIED
                return@flatMap responseMessage("Read-Only key not allowed")
            }

            //Handle request
            val body = JSONObject(rBody)
            val guildId = Snowflake.of(body.getString("guild_id"))
            val eventId = body.getString("event_id")

            val rsvpMono = DatabaseManager.getRsvpData(guildId, eventId)
            val settingsMono = DatabaseManager.getSettings(guildId)
            Mono.zip(rsvpMono, settingsMono).flatMap(TupleUtils.function { rsvp, settings ->
                //Handle limit change
                rsvp.limit = body.optInt("limit", rsvp.limit)

                //Handle role change
                val roleChangeMono: Mono<Void> = Mono.just(body).filter {
                    it.has("role_id") && (settings.patronGuild || settings.devGuild)
                }.flatMap { jsonBody ->
                    if (jsonBody.isNull("role_id") || jsonBody.getString("role_id").equals("none", true)) {
                        rsvp.clearRole(DisCalServer.client)
                    } else {
                        val roleId = Snowflake.of(jsonBody.getString("role_id"))

                        RoleUtils.roleExists(DisCalServer.client, guildId, roleId)
                                .filter { it }
                                .flatMap { rsvp.setRole(roleId, DisCalServer.client) }
                    }
                }

                //Handle removals (we do this first just in case they are using the limit)
                val removalMono: Mono<Void> = Mono.just(body).filter {
                    it.has("to_remove")
                }.map {
                    it.getJSONObject("to_remove")
                }.flatMap { toRemoveJson ->
                    val toRemove: MutableList<String> = mutableListOf()

                    if (toRemoveJson.has("on_time")) {
                        val ar = toRemoveJson.getJSONArray("on_time")
                        (0 until ar.length()).forEach { toRemove.add(ar.getString(it)) }
                    }
                    if (toRemoveJson.has("late")) {
                        val ar = toRemoveJson.getJSONArray("late")
                        (0 until ar.length()).forEach { toRemove.add(ar.getString(it)) }
                    }
                    if (toRemoveJson.has("not_going")) {
                        val ar = toRemoveJson.getJSONArray("not_going")
                        (0 until ar.length()).forEach { toRemove.add(ar.getString(it)) }
                    }
                    if (toRemoveJson.has("undecided")) {
                        val ar = toRemoveJson.getJSONArray("undecided")
                        (0 until ar.length()).forEach { toRemove.add(ar.getString(it)) }
                    }

                    Flux.fromIterable(toRemove).flatMap { rsvp.removeCompletely(it, DisCalServer.client) }.then()
                }

                //Handle additions
                val addMono: Mono<Void> = Mono.just(body).filter {
                    it.has("to_add")
                }.map {
                    it.getJSONObject("to_add")
                }.flatMap { toAddJson ->
                    val allTheMonos = mutableListOf<Mono<Void>>()

                    if (toAddJson.has("on_time")) {
                        val ar = toAddJson.getJSONArray("on_time")
                        for (i in 0 until ar.length()) {
                            if (rsvp.hasRoom(ar.getString(i))) {
                                allTheMonos.add(rsvp.removeCompletely(ar.getString(i), DisCalServer.client)
                                        .then(rsvp.addGoingOnTime(ar.getString(i), DisCalServer.client)))
                            }
                        }
                    }

                    if (toAddJson.has("late")) {
                        val ar = toAddJson.getJSONArray("late")
                        for (i in 0 until ar.length()) {
                            if (rsvp.hasRoom(ar.getString(i))) {
                                allTheMonos.add(rsvp.removeCompletely(ar.getString(i), DisCalServer.client)
                                        .then(rsvp.addGoingLate(ar.getString(i), DisCalServer.client)))
                            }
                        }
                    }

                    if (toAddJson.has("not_going")) {
                        val ar = toAddJson.getJSONArray("on_time")
                        for (i in 0 until ar.length()) {
                            if (rsvp.hasRoom(ar.getString(i))) {
                                allTheMonos.add(rsvp.removeCompletely(ar.getString(i), DisCalServer.client)
                                        .then(Mono.from { rsvp.notGoing.add(ar.getString(i)) }))
                            }
                        }
                    }

                    if (toAddJson.has("undecided")) {
                        val ar = toAddJson.getJSONArray("undecided")
                        for (i in 0 until ar.length()) {
                            if (rsvp.hasRoom(ar.getString(i))) {
                                allTheMonos.add(rsvp.removeCompletely(ar.getString(i), DisCalServer.client)
                                        .then(Mono.from { rsvp.undecided.add(ar.getString(i)) }))
                            }
                        }
                    }

                    Flux.fromIterable(allTheMonos).then()
                }


                //Honestly no fucking idea if this will work, like at all.
                roleChangeMono.then(removalMono).then(addMono).then(DatabaseManager.updateRsvpData(rsvp))
            })
                    .then(responseMessage("Success!"))
                    .doOnNext { response.rawStatusCode = GlobalConst.STATUS_SUCCESS }
        }.onErrorResume(JSONException::class.java) {
            it.printStackTrace()

            response.rawStatusCode = GlobalConst.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LogFeed.log(LogObject.forException("[API-v2] keep alive err", it, this.javaClass))

            response.rawStatusCode = GlobalConst.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }

}
