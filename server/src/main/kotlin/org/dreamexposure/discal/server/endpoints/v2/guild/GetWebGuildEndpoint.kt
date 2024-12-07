package org.dreamexposure.discal.server.endpoints.v2.guild

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.mono
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.enums.announcement.AnnouncementStyle
import org.dreamexposure.discal.core.exceptions.BotNotInGuildException
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.web.WebCalendar
import org.dreamexposure.discal.core.`object`.web.WebGuild
import org.dreamexposure.discal.core.`object`.web.WebRole
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.server.business.WebGuildService
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
@RequestMapping("/v2/guild")
@Deprecated("Prefer using v3 implementation, this needs to go at some point")
class GetWebGuildEndpoint(
    private val webGuildService: WebGuildService,
) {
    @PostMapping(value = ["/get"], produces = ["application/json"])
    @SecurityRequirement(disableSecurity = true, scopes = [])
    fun getSettings(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
        return Authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(GlobalVal.JSON_FORMAT.encodeToString(authState))
            }

            //Handle request
            val body = JSONObject(rBody)
            val guildId = Snowflake.of(body.getString("guild_id"))
            val userId = Snowflake.of(body.getString("user_id"))

            // This is so stupid, but I need to map these objects to maintain
            // existing compatibility because I can't be assed to update a several year old website that is deprecated

            mono { webGuildService.getWebGuildForUser(guildId, userId) }.map { newModel ->
                val oldModel = WebGuild(
                    id = newModel.id.asLong(),
                    name = newModel.name,
                    iconUrl = newModel.iconUrl,
                    settings = GuildSettings(
                        guildID = newModel.settings.guildId,
                        controlRole = newModel.settings.controlRole?.asString() ?: "everyone",
                        announcementStyle = AnnouncementStyle.fromValue(newModel.settings.interfaceStyle.announcementStyle.value),
                        timeFormat = newModel.settings.interfaceStyle.timeFormat,
                        lang = newModel.settings.locale.toLanguageTag(),
                        prefix = "!",
                        patronGuild = newModel.settings.patronGuild,
                        devGuild = newModel.settings.devGuild,
                        maxCalendars = newModel.settings.maxCalendars,
                        branded = newModel.settings.interfaceStyle.branded,
                        eventKeepDuration = newModel.settings.eventKeepDuration,
                    ),
                    botNick = newModel.botNickname,
                    elevatedAccess = newModel.userHasElevatedAccess,
                    discalRole = newModel.userHasDisCalControlRole,
                    calendar = if (newModel.calendars.isNotEmpty()) WebCalendar(newModel.calendars[0]) else WebCalendar.empty(),
                )
                oldModel.roles.addAll(newModel.roles.map(::WebRole))

                oldModel
            }.map {
                GlobalVal.JSON_FORMAT.encodeToString(it)
            }.doOnNext {
                response.rawStatusCode = GlobalVal.STATUS_SUCCESS
            }
        }.onErrorResume(BotNotInGuildException::class.java) {
            response.rawStatusCode = GlobalVal.STATUS_NOT_FOUND
            return@onErrorResume responseMessage("Guild not connected to DisCal")
        }.onErrorResume(JSONException::class.java) {
            LOGGER.trace("[API-v2] JSON error. Bad request?", it)

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LOGGER.error(GlobalVal.DEFAULT, "[API-v2] get web guild error", it)

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
