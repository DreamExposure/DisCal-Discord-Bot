package org.dreamexposure.discal.server.endpoints.v2.announcement

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.enums.announcement.AnnouncementModifier
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.extensions.discord4j.createAnnouncement
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
@RequestMapping("/v2/announcement")
class CreateAnnouncementEndpoint(val client: DiscordClient) {
    @PostMapping("/create", produces = ["application/json"])
    fun create(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
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

            val announcement = Announcement(guildId)

            announcement.announcementChannelId = body.getString("channel")
            announcement.type = AnnouncementType.fromValue(body.getString("type"))

            announcement.modifier = AnnouncementModifier.fromValue(body.optString("modifier", "BEFORE"))

            if (announcement.type == AnnouncementType.COLOR) {
                announcement.eventColor = EventColor.fromNameOrHexOrId(body.getString("color"))
            }

            if (announcement.type == AnnouncementType.RECUR || announcement.type == AnnouncementType.SPECIFIC) {
                announcement.eventId = body.getString("event_id")
            }

            announcement.hoursBefore = body.optInt("hours", 0)
            announcement.minutesBefore = body.optInt("minutes", 0)

            announcement.info = body.optString("info", "N/a")
            announcement.infoOnly = body.optBoolean("info_only", false)

            announcement.publish = body.optBoolean("publish", false)

            return@flatMap client.getGuildById(guildId).createAnnouncement(announcement).flatMap { success ->
                if (success) {
                    response.rawStatusCode = GlobalConst.STATUS_SUCCESS

                    val json = JSONObject()
                    json.put("message", "Success").put("announcement", Json.Default.encodeToString(announcement))

                    Mono.just(json.toString())
                } else {
                    response.rawStatusCode = GlobalConst.STATUS_INTERNAL_ERROR
                    responseMessage("Internal Server Error")
                }
            }
        }.onErrorResume(JSONException::class.java) {
            it.printStackTrace()

            response.rawStatusCode = GlobalConst.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LogFeed.log(LogObject.forException("[API-v2] create announcement err", it, this.javaClass))

            response.rawStatusCode = GlobalConst.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
