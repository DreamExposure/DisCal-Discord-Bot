package org.dreamexposure.discal.server.endpoints.v2.calendar

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.annotations.Authentication.AccessLevel
import org.dreamexposure.discal.core.entities.spec.update.UpdateCalendarSpec
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.isValidTimezone
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
import java.time.ZoneId

@RestController
@RequestMapping("/v2/calendar")
class UpdateCalendarEndpoint(val client: DiscordClient) {
    @PostMapping("/update", produces = ["application/json"])
    @org.dreamexposure.discal.core.annotations.Authentication(access = AccessLevel.PUBLIC)
    fun updateCalendar(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
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

            client.getGuildById(guildId).getCalendar(calendarNumber).flatMap { calendar ->
                var spec = UpdateCalendarSpec()

                if (body.has("name")) {
                    spec = spec.copy(name = body.getString("name"))
                }
                if (body.has("description")) {
                    spec = spec.copy(description = body.getString("description"))
                }
                if (body.has("timezone")) {
                    val tzRaw = body.getString("timezone")
                    if (tzRaw.isValidTimezone())
                        spec = spec.copy(timezone = ZoneId.of(tzRaw))
                }

                calendar.update(spec)
                        .filter { it.success }
                        .map { JSONObject().put("calendar", it.new?.toJson()).put("message", "Success") }
                        .doOnNext { response.rawStatusCode = GlobalVal.STATUS_SUCCESS }
                        .map(JSONObject::toString)
                        .switchIfEmpty(responseMessage("Update failed")
                                .doOnNext { response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR }
                        )
            }.switchIfEmpty(responseMessage("Calendar not found")
                    .doOnNext { response.rawStatusCode = GlobalVal.STATUS_NOT_FOUND }
            )
        }.onErrorResume(JSONException::class.java) {
            LOGGER.trace("[API-v2] JSON error. Bad request?", it)

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LOGGER.error(GlobalVal.DEFAULT, "[API-v2] update calendar error", it)

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
