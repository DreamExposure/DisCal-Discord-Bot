package org.dreamexposure.discal.server.endpoints.v2.guild.settings

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.mono
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.business.GuildSettingsService
import org.dreamexposure.discal.core.enums.time.TimeFormat
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.GuildSettings
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
import java.util.*

@RestController
@RequestMapping("/v2/guild/settings")
class UpdateGuildSettingsEndpoint(
    private val settingsService: GuildSettingsService,
) {
    @PostMapping(value = ["/update"], produces = ["application/json"])
    @SecurityRequirement(disableSecurity = true, scopes = [])
    fun updateSettings(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
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

            mono { settingsService.getSettings(guildId) }.flatMap { settings ->
                //Handle various things that are allowed to change
                val conRole = body.optString("control_role", settings.controlRole)
                val aStyle = body.optInt("announcement_style", settings.interfaceStyle.announcementStyle.value)
                val lang = body.optString("lang", settings.locale.toLanguageTag())
                val prefix = body.optString("prefix", settings.prefix)
                val timeFormat = body.optInt("time_format", settings.interfaceStyle.timeFormat.value)
                var patronGuild = settings.patronGuild
                var devGuild = settings.devGuild
                var branded = settings.interfaceStyle.branded
                var maxCals = settings.maxCalendars

                //Allow official DisCal shards to change some extra stuff
                if (authState.fromDiscalNetwork) {
                    patronGuild = body.optBoolean("patron_guild", patronGuild)
                    devGuild = body.optBoolean("dev_guild", devGuild)
                    branded = body.optBoolean("branded", branded)
                    maxCals = body.optInt("max_calendars", maxCals)
                }

                val newSettings = settings.copy(
                    controlRole = conRole,
                    interfaceStyle = settings.interfaceStyle.copy(
                        announcementStyle = GuildSettings.AnnouncementStyle.entries.first { it.value == aStyle },
                        timeFormat = TimeFormat.fromValue(timeFormat),
                        branded = branded,
                    ),
                    locale = Locale.forLanguageTag(lang),
                    prefix = prefix,
                    patronGuild = patronGuild,
                    devGuild = devGuild,
                    maxCalendars = maxCals,
                )

                mono { settingsService.upsertSettings(newSettings) }
                    .then(responseMessage("Success"))
                    .doOnNext {
                        response.rawStatusCode = GlobalVal.STATUS_SUCCESS
                    }
            }
        }.onErrorResume(JSONException::class.java) {
            LOGGER.trace("[API-v2] JSON error. Bad request?", it)

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LOGGER.error(GlobalVal.DEFAULT, "[API-v2] Update settings error", it)

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
