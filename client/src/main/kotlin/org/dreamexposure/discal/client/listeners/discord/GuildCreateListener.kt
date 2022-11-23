package org.dreamexposure.discal.client.listeners.discord

import com.fasterxml.jackson.module.kotlin.readValue
import discord4j.common.JacksonResources
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component

@Component
class GuildCreateListener : EventListener<GuildCreateEvent> {
    private val premiumCommands: List<ApplicationCommandRequest>
    private val devCommands: List<ApplicationCommandRequest>

    init {
        val d4jMapper = JacksonResources.create()
        val matcher = PathMatchingResourcePatternResolver()

        // Get premium commands
        val premiumCommands = mutableListOf<ApplicationCommandRequest>()
        for (res in matcher.getResources("commands/premium/*.json")) {
            val request = d4jMapper.objectMapper.readValue<ApplicationCommandRequest>(res.inputStream)
            premiumCommands.add(request)
        }
        this.premiumCommands = premiumCommands

        // Get dev commands
        val devCommands = mutableListOf<ApplicationCommandRequest>()
        for (res in matcher.getResources("commands/dev/*.json")) {
            val request = d4jMapper.objectMapper.readValue<ApplicationCommandRequest>(res.inputStream)
            premiumCommands.add(request)
        }
        this.devCommands = devCommands

    }

    override suspend fun handle(event: GuildCreateEvent) {
        val settings = DatabaseManager.getSettings(event.guild.id).awaitSingle()
        val appService = event.client.restClient.applicationService
        val guildId = settings.guildID.asLong()
        val appId = event.client.selfId.asLong()

        val commands = mutableListOf<ApplicationCommandRequest>()
        if (settings.patronGuild) commands.addAll(premiumCommands)
        if (settings.devGuild) commands.addAll(devCommands)

        if (commands.isNotEmpty()) {
            appService.bulkOverwriteGuildApplicationCommand(appId, guildId, commands)
                .doOnNext { LOGGER.debug("Bulk guild overwrite read: ${it.name()} | $guildId") }
                .doOnError { LOGGER.error(DEFAULT, "Bulk guild overwrite failed | $guildId", it) }
                .then()
                .awaitSingleOrNull()
        }
    }
}
