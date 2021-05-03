package org.dreamexposure.discal.server.endpoints.v2.event.list

import discord4j.common.util.Snowflake
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.utils.GlobalConst
import org.dreamexposure.discal.server.DisCalServer
import org.dreamexposure.discal.server.utils.Authentication
import org.dreamexposure.discal.server.utils.responseMessage
import org.json.JSONArray
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
@RequestMapping("/v2/events/list")
class ListEventRangeEndpoint {
    @PostMapping("/range", produces = ["application/json"])
    fun listByRange(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
        return Authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(Json.encodeToString(authState))
            }

            //Handle request
            val body = JSONObject(rBody)
            val guildId = Snowflake.of(body.getString("guild_id"))
            val calendarNumber = body.getInt("calendar_number")
            val start = Instant.ofEpochMilli(body.getLong("epoch_start"))
            val end = Instant.ofEpochMilli(body.getLong("epoch_end"))

            return@flatMap DisCalServer.client.getGuildById(guildId).getCalendar(calendarNumber)
                    .flatMapMany { it.getEventsInTimeRange(start, end) }
                    .map(Event::toJson)
                    .collectList()
                    .map(::JSONArray)
                    .map { JSONObject().put("events", it).put("message", "Success").toString() }
                    .doOnNext { response.rawStatusCode = GlobalConst.STATUS_SUCCESS }
                    .switchIfEmpty(responseMessage("Calendar not found")
                            .doOnNext { response.rawStatusCode = GlobalConst.STATUS_NOT_FOUND }
                    )
        }.onErrorResume(JSONException::class.java) {
            it.printStackTrace()

            response.rawStatusCode = GlobalConst.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LogFeed.log(LogObject.forException("[API-v2] list events by range err", it, this.javaClass))

            response.rawStatusCode = GlobalConst.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
