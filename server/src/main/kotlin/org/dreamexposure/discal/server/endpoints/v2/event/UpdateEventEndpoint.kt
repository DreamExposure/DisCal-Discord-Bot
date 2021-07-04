package org.dreamexposure.discal.server.endpoints.v2.event

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.`object`.event.Recurrence
import org.dreamexposure.discal.core.entities.spec.update.UpdateEventSpec
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
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
import java.time.Instant

@RestController
@RequestMapping("/v2/event")
class UpdateEventEndpoint(val client: DiscordClient) {
    @PostMapping("/update", produces = ["application/json"])
    fun update(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
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
            val calendarNumber = body.getInt("calendar_number")
            val eventId = body.getString("event_id")

            return@flatMap client.getGuildById(guildId).getCalendar(calendarNumber).flatMap { it.getEvent(eventId) }
                    .flatMap { event ->
                        var spec = UpdateEventSpec()

                        if (body.has("epoch_start")) {
                            spec = spec.copy(start = Instant.ofEpochMilli(body.getString("epoch_start").toLong()))
                        }
                        if (body.has("epoch_end")) {
                            spec = spec.copy(end = Instant.ofEpochMilli(body.getString("epoch_end").toLong()))
                        }
                        if (body.has("name")) {
                            spec = spec.copy(name = body.getString("name"))
                        }
                        if (body.has("description")) {
                            spec = spec.copy(description = body.getString("description"))
                        }
                        if (body.has("color")) {
                            spec = spec.copy(color = EventColor.fromNameOrHexOrId(body.getString("color")))
                        }
                        if (body.has("location")) {
                            spec = spec.copy(location = body.getString("location"))
                        }
                        if (body.has("recur") && body.getBoolean("recur")) {
                            spec = spec.copy(
                                    recur = true,
                                    recurrence = GlobalVal.JSON_FORMAT.decodeFromString(
                                            Recurrence.serializer(), body.getJSONObject("recurrence").toString())
                            )
                        }
                        if (body.has("image")) {
                            spec = spec.copy(image = body.getString("image"))
                        }

                        event.update(spec)
                                .filter { it.success }
                                .map { it.new?.toJson() }
                                .map { JSONObject().put("event", it).put("message", "Success").toString() }
                                .doOnNext { response.rawStatusCode = GlobalVal.STATUS_SUCCESS }
                                .switchIfEmpty(responseMessage("Event update failed")
                                        .doOnNext { response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR }
                                )
                    }.switchIfEmpty(responseMessage("Event not Found")
                            .doOnNext { response.rawStatusCode = GlobalVal.STATUS_NOT_FOUND }
                    )
        }.onErrorResume(JSONException::class.java) {
            it.printStackTrace()

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LogFeed.log(LogObject.forException("[API-v2] update event err", it, this.javaClass))

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
