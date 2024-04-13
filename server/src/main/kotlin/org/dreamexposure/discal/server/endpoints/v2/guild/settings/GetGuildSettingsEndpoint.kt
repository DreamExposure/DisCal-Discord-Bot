package org.dreamexposure.discal.server.endpoints.v2.guild.settings

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.mono
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.business.GuildSettingsService
import org.dreamexposure.discal.core.enums.announcement.AnnouncementStyle
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.GuildSettings
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
@RequestMapping("/v2/guild/settings")
class GetGuildSettingsEndpoint(
    private val settingsService: GuildSettingsService,
) {

    @PostMapping(value = ["/get"], produces = ["application/json"])
    @SecurityRequirement(disableSecurity = true, scopes = [])
    fun getSettings(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
        return Authentication.authenticate(swe).flatMap<String?> { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(GlobalVal.JSON_FORMAT.encodeToString(authState))
            }

            //Handle request
            val body = JSONObject(rBody)
            val guildId = Snowflake.of(body.getString("guild_id"))



            return@flatMap mono { settingsService.getSettings(guildId) }
                .map {
                    // Convert to deprecated settings for compatibility with legacy website
                    GuildSettings(
                        guildID = it.guildId,
                        controlRole = it.controlRole?.asString() ?: "everyone",
                        announcementStyle = AnnouncementStyle.fromValue(it.interfaceStyle.announcementStyle.value),
                        timeFormat = it.interfaceStyle.timeFormat,
                        lang = it.locale.toLanguageTag(),
                        prefix = "!",
                        patronGuild = it.patronGuild,
                        devGuild = it.devGuild,
                        maxCalendars = it.maxCalendars,
                        branded = it.interfaceStyle.branded,
                        eventKeepDuration = it.eventKeepDuration,
                    )
                }.map { GlobalVal.JSON_FORMAT.encodeToString(it) }
                .doOnNext { response.rawStatusCode = GlobalVal.STATUS_SUCCESS }
        }.onErrorResume(JSONException::class.java) {
            LOGGER.trace("[API-v2] JSON error. Bad request?", it)

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LOGGER.error(GlobalVal.DEFAULT, "[API-v2] get settings error", it)

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
