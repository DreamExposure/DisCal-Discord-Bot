package org.dreamexposure.discal.server.endpoints.v2.event

import discord4j.common.util.Snowflake
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dreamexposure.discal.core.`object`.event.Recurrence
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.entities.spec.create.CreateEventSpec
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.utils.GlobalConst
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
import reactor.core.publisher.Mono
import java.time.Instant

@RestController
@RequestMapping("/v2/event")
class CreateEventEndpoint {
    @PostMapping("/create", produces = ["application/json"])
    fun create(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
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
            val calendarNumber = body.getInt("calendar_number")

            return@flatMap DisCalServer.client.getGuildById(guildId).getCalendar(calendarNumber)
                    .flatMap { calendar ->
                        var spec = CreateEventSpec(
                                start = Instant.ofEpochMilli(body.getString("epoch_start").toLong()),
                                end = Instant.ofEpochMilli(body.getString("epoch_end").toLong())
                        )

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
                                    recurrence = Json.Default.decodeFromString(
                                            Recurrence.serializer(), body.getJSONObject("recurrence").toString())
                            )
                        }
                        if (body.has("image")) {
                            spec = spec.copy(image = body.getString("image"))
                        }

                        calendar.createEvent(spec)
                                .map(Event::toJson)
                                .map { JSONObject().put("event", it).put("message", "Success").toString() }
                                .doOnNext { response.rawStatusCode = GlobalConst.STATUS_SUCCESS }
                                .switchIfEmpty(responseMessage("Event creation failed")
                                        .doOnNext { response.rawStatusCode = GlobalConst.STATUS_INTERNAL_ERROR })
                    }.switchIfEmpty(responseMessage("Calendar not found")
                            .doOnNext { response.rawStatusCode = GlobalConst.STATUS_NOT_FOUND }
                    )
        }.onErrorResume(JSONException::class.java) {
            it.printStackTrace()

            response.rawStatusCode = GlobalConst.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LogFeed.log(LogObject.forException("[API-v2] create event err", it, this.javaClass))

            response.rawStatusCode = GlobalConst.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
