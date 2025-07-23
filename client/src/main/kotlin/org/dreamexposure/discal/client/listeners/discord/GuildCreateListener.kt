package org.dreamexposure.discal.client.listeners.discord

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.business.GuildSettingsService
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component

@Component
class GuildCreateListener(
    private val settingsService: GuildSettingsService,
    objectMapper: ObjectMapper
) : EventListener<GuildCreateEvent> {
    private final val premiumCommands: List<ApplicationCommandRequest>
    private final val devCommands: List<ApplicationCommandRequest>

    init {
        val matcher = PathMatchingResourcePatternResolver()

        // Get premium commands
        val premiumCommands = mutableListOf<ApplicationCommandRequest>()
        for (res in matcher.getResources("commands/premium/*.json")) {
            val request = objectMapper.readValue<ApplicationCommandRequest>(res.inputStream)
            premiumCommands.add(request)
        }
        this.premiumCommands = premiumCommands

        // Get dev commands
        val devCommands = mutableListOf<ApplicationCommandRequest>()
        for (res in matcher.getResources("commands/dev/*.json")) {
            val request = objectMapper.readValue<ApplicationCommandRequest>(res.inputStream)
            devCommands.add(request)
        }
        this.devCommands = devCommands

    }

    override suspend fun handle(event: GuildCreateEvent) {
        val guildId = event.guild.id
        val settings = settingsService.getSettings(guildId)
        val appService = event.client.restClient.applicationService
        val appId = event.client.selfId.asLong()

        val commands = mutableListOf<ApplicationCommandRequest>()
        if (settings.patronGuild) commands.addAll(premiumCommands)
        if (settings.devGuild) commands.addAll(devCommands)

        if (commands.isNotEmpty()) {
            appService.bulkOverwriteGuildApplicationCommand(appId, guildId.asLong(), commands)
                .doOnNext { LOGGER.debug("Bulk guild overwrite read: {} | {}", it.name(), guildId) }
                .doOnError { LOGGER.error(DEFAULT, "Bulk guild overwrite failed | ${guildId.asLong()}", it) }
                .then()
                .awaitSingleOrNull()
        }
    }
}
