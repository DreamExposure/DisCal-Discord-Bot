package org.dreamexposure.discal.server.endpoints.v2.guild.settings

import discord4j.common.util.Snowflake
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.utils.GlobalConst
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
                return@flatMap Mono.just(Json.encodeToString(authState))
            } else if (authState.readOnly) {
                response.rawStatusCode = GlobalConst.STATUS_AUTHORIZATION_DENIED
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
                            response.rawStatusCode = GlobalConst.STATUS_SUCCESS
                        }
            }
        }.onErrorResume(JSONException::class.java) {
            it.printStackTrace()

            response.rawStatusCode = GlobalConst.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LogFeed.log(LogObject.forException("[API-v2] update settings err", it, this.javaClass))

            response.rawStatusCode = GlobalConst.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
