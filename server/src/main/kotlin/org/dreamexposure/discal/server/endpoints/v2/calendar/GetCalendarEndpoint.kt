package org.dreamexposure.discal.server.endpoints.v2.calendar

import com.fasterxml.jackson.databind.ObjectMapper
import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.mono
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.model.discal.v2.CalendarV2Model
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
@RequestMapping("/v2/calendar")
class GetCalendarEndpoint(
    private val authentication: Authentication,
    private val calendarService: CalendarService,
    private val objectMapper: ObjectMapper,
) {
    @PostMapping("/get", produces = ["application/json"])
    @SecurityRequirement(disableSecurity = true, scopes = [])
    fun getCalendar(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
        return authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(GlobalVal.JSON_FORMAT.encodeToString(authState))
            }

            //Handle request
            val body = JSONObject(rBody)
            val guildId = Snowflake.of(body.getString("guild_id"))
            val calNumber = body.getInt("calendar_number")

            return@flatMap mono {calendarService.getCalendar(guildId, calNumber) }
                .map(::CalendarV2Model)
                .map { objectMapper.writeValueAsString(it) }
                .switchIfEmpty(responseMessage("Calendar not found")
                    .doOnNext { response.rawStatusCode = GlobalVal.STATUS_NOT_FOUND }
                )
        }.onErrorResume(JSONException::class.java) {
            LOGGER.trace("[API-v2] JSON error. Bad request?", it)

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LOGGER.error(GlobalVal.DEFAULT, "[API-v2] get calendar error", it)

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}