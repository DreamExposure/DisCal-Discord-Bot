package org.dreamexposure.discal.server.business

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.discordjson.json.MemberData
import discord4j.discordjson.possible.Possible
import discord4j.rest.http.client.ClientException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.GuildSettingsService
import org.dreamexposure.discal.core.business.PermissionService
import org.dreamexposure.discal.core.exceptions.BotNotInGuildException
import org.dreamexposure.discal.core.`object`.new.model.discal.CalendarV3Model
import org.dreamexposure.discal.core.`object`.new.model.discal.WebGuildV3Model
import org.dreamexposure.discal.core.`object`.new.model.discal.WebRoleV3Model
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.justOrEmpty
import kotlin.jvm.optionals.getOrNull

@Component
class WebGuildService(
    private val settingsService: GuildSettingsService,
    private val permissionService: PermissionService,
    private val calendarService: CalendarService,
    private val discordClient: DiscordClient,
) {
    suspend fun getWebGuildForUser(guildId: Snowflake, userId: Snowflake): WebGuildV3Model {
        val settings = settingsService.getSettings(guildId)
        val restGuild = discordClient.getGuildById(settings.guildId)
        val guildData = restGuild.data
            .onErrorResume(ClientException::class.java) {
                Mono.error(BotNotInGuildException())
            }.switchIfEmpty(Mono.error(BotNotInGuildException()))
            .awaitSingleOrNull() ?: throw BotNotInGuildException() // Just in case lol

        val botNickname = discordClient.getSelfMember(guildId)
            .map(MemberData::nick)
            .map { Possible.flatOpt(it) }
            .flatMap { justOrEmpty(it) }
            .defaultIfEmpty("DisCal")
            .awaitSingle()

        val roles = restGuild.roles.map {
                val id = Snowflake.of(it.id())
                val isEveryone = id == guildId
                WebRoleV3Model(
                id = id,
                name = it.name(),
                managed = it.managed(),
                controlRole =
                    if (isEveryone && settings.controlRole == null) true
                    else settings.controlRole == id,
                everyone = isEveryone,
            )
        }.collectList().awaitSingle()

        val calendars = calendarService.getAllCalendars(guildId)
            .map(::CalendarV3Model)

        return WebGuildV3Model(
            id = guildId,
            name = guildData.name(),
            iconUrl = guildData.icon().getOrNull(),
            botNickname = botNickname,
            userHasElevatedAccess = permissionService.hasElevatedPermissions(guildId, userId),
            userHasDisCalControlRole = permissionService.hasControlRole(guildId, userId),
            roles = roles,
            settings = settings,
            calendars = calendars,
        )
    }
}