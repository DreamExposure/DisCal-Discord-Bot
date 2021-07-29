package org.dreamexposure.discal.server.endpoints.v2.announcement

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.enums.announcement.AnnouncementModifier
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.extensions.discord4j.getAnnouncement
import org.dreamexposure.discal.core.extensions.discord4j.updateAnnouncement
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
import java.util.*

@RestController
@RequestMapping("/v2/announcement")
class UpdateAnnouncementEndpoint(val client: DiscordClient) {
    @PostMapping("/update", produces = ["application/json"])
    fun update(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
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
            val announcementId = UUID.fromString(body.getString("announcement_id"))

            val guild = client.getGuildById(guildId)

            return@flatMap guild.getAnnouncement(announcementId).flatMap { ann ->
                ann.announcementChannelId = body.optString("channel", ann.announcementChannelId)
                ann.eventId = body.optString("event_id", ann.eventId)
                ann.hoursBefore = body.optInt("hours", ann.hoursBefore)
                ann.minutesBefore = body.optInt("hours", ann.minutesBefore)
                ann.info = body.optString("info", ann.info)
                ann.infoOnly = body.optBoolean("info_only", ann.infoOnly)
                ann.enabled = body.optBoolean("enabled", ann.enabled)
                ann.publish = body.optBoolean("publish", ann.publish)

                if (body.has("event_color"))
                    ann.eventColor = EventColor.fromNameOrHexOrId(body.getString("event_color"))
                if (body.has("type"))
                    ann.type = AnnouncementType.fromValue(body.getString("type"))
                if (body.has("modifier"))
                    ann.modifier = AnnouncementModifier.fromValue(body.getString("modifier"))

                //Handle subscribers
                if (body.has("remove_subscriber_roles")) {
                    val jList = body.getJSONArray("remove_subscriber_roles")
                    for (i in 0 until jList.length())
                        ann.subscriberRoleIds.remove(jList.getString(i))
                }
                if (body.has("remove_subscriber_users")) {
                    val jList = body.getJSONArray("remove_subscriber_users")
                    for (i in 0 until jList.length())
                        ann.subscriberUserIds.remove(jList.getString(i))
                }

                if (body.has("add_subscriber_roles")) {
                    val jList = body.getJSONArray("add_subscriber_roles")
                    for (i in 0 until jList.length())
                        ann.subscriberRoleIds.add(jList.getString(i))
                }
                if (body.has("add_subscriber_users")) {
                    val jList = body.getJSONArray("add_subscriber_users")
                    for (i in 0 until jList.length())
                        ann.subscriberUserIds.add(jList.getString(i))
                }


                guild.updateAnnouncement(ann).flatMap { success ->
                    if (success) {
                        response.rawStatusCode = GlobalVal.STATUS_SUCCESS

                        val json = JSONObject()
                        json.put("message", "Success").put("announcement", GlobalVal.JSON_FORMAT.encodeToString(ann))

                        Mono.just(json.toString())
                    } else {
                        response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
                        responseMessage("Internal Server Error")
                    }
                }
            }.switchIfEmpty(responseMessage("Announcement not found")
                    .doOnNext { response.rawStatusCode = GlobalVal.STATUS_NOT_FOUND }
            )
        }.onErrorResume(JSONException::class.java) {
            it.printStackTrace()

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LOGGER.error(GlobalVal.DEFAULT, "[API-v2] update announcement error", it)

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
