package org.dreamexposure.discal.server.endpoints.v2.event

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.logger.LOGGER
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

@RestController
@RequestMapping("/v2/event")
class GetEventEndpoint(val client: DiscordClient) {
    @PostMapping("/get", produces = ["application/json"])
    fun get(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
        return Authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(GlobalVal.JSON_FORMAT.encodeToString(authState))
            }

            //Handle request
            val body = JSONObject(rBody)
            val guildId = Snowflake.of(body.getString("guild_id"))
            val calendarNumber = body.getInt("calendar_number")
            val eventId = body.getString("event_id")

            return@flatMap client.getGuildById(guildId).getCalendar(calendarNumber)
                    .flatMap { it.getEvent(eventId) }
                    .map(Event::toJson)
                    .map(JSONObject::toString)
                    .doOnNext { response.rawStatusCode = GlobalVal.STATUS_SUCCESS }
                    .switchIfEmpty(responseMessage("Event not Found")
                            .doOnNext { response.rawStatusCode = GlobalVal.STATUS_NOT_FOUND }
                    )
        }.onErrorResume(JSONException::class.java) {
            it.printStackTrace()

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LOGGER.error(GlobalVal.DEFAULT, "[API-v2] get event error", it)

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
