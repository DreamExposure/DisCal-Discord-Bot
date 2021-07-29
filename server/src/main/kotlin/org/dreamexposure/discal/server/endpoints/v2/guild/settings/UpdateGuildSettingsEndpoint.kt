package org.dreamexposure.discal.server.endpoints.v2.guild.settings

import discord4j.common.util.Snowflake
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.database.DatabaseManager
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
@RequestMapping("/v2/guild/settings")
class UpdateGuildSettingsEndpoint {
    @PostMapping(value = ["/update"], produces = ["application/json"])
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

            DatabaseManager.getSettings(guildId).flatMap { settings ->
                //Handle various things that are allowed to change
                val conRole = body.optString("control_role", settings.controlRole)
                var disChannel = settings.discalChannel
                if (body.has("discal_channel")) {
                    val id = body.getString("discal_channel")
                    disChannel = if (id.equals("0") || id.equals("all", true)) "all" else id
                }
                val simpleAnn = body.optBoolean("simple_announcements", settings.simpleAnnouncements)
                val lang = body.optString("lang", settings.lang)
                val prefix = body.optString("prefix", settings.prefix)
                val twelveHour = body.optBoolean("twelve_hour", settings.twelveHour)
                var patronGuild = settings.patronGuild
                var devGuild = settings.devGuild
                var branded = settings.branded
                var maxCals = settings.maxCalendars

                //Allow official DisCal shards to change some extra stuff
                if (authState.fromDiscalNetwork) {
                    patronGuild = body.optBoolean("patron_guild", patronGuild)
                    devGuild = body.optBoolean("dev_guild", devGuild)
                    branded = body.optBoolean("branded", branded)
                    maxCals = body.optInt("max_calendars", maxCals)
                }

                val newSettings = settings.copy(controlRole = conRole, discalChannel = disChannel,
                        simpleAnnouncements = simpleAnn, lang = lang,
                        prefix = prefix, patronGuild = patronGuild,
                        devGuild = devGuild, maxCalendars = maxCals,
                        twelveHour = twelveHour, branded = branded
                )

                DatabaseManager.updateSettings(newSettings)
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
