package org.dreamexposure.discal.server.endpoints.v2.event.list

import com.fasterxml.jackson.databind.ObjectMapper
import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.mono
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.model.discal.v2.EventV2Model
import org.dreamexposure.discal.core.utils.GlobalVal
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
class ListEventRangeEndpoint(
    private val authentication: Authentication,
    private val calendarService: CalendarService,
    private val objectMapper: ObjectMapper,
) {
    @PostMapping("/range", produces = ["application/json"])
    @SecurityRequirement(disableSecurity = true, scopes = [])
    fun listByRange(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
        return authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(GlobalVal.JSON_FORMAT.encodeToString(authState))
            }

            //Handle request
            val body = JSONObject(rBody)
            val guildId = Snowflake.of(body.getString("guild_id"))
            val calendarNumber = body.getInt("calendar_number")
            val start = Instant.ofEpochMilli(body.getLong("epoch_start"))
            val end = Instant.ofEpochMilli(body.getLong("epoch_end"))

            mono { calendarService.getCalendar(guildId, calendarNumber) }.flatMap { calendar ->
                mono { calendarService.getEventsInTimeRange(guildId, calendarNumber, start, end) }.map { events ->
                    events.map { objectMapper.writeValueAsString(EventV2Model(it, calendar)) }
                }
            }.map(::JSONArray)
                .map { JSONObject().put("events", it).put("message", "Success").toString() }
                .doOnNext { response.rawStatusCode = GlobalVal.STATUS_SUCCESS }
                .switchIfEmpty(responseMessage("Calendar not found")
                    .doOnNext { response.rawStatusCode = GlobalVal.STATUS_NOT_FOUND }
                )
        }.onErrorResume(JSONException::class.java) {
            LOGGER.trace("[API-v2] JSON error. Bad request?", it)

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LOGGER.error(GlobalVal.DEFAULT, "[API-v2] list events by range error", it)

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}