package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.GuildSettingsCache
import org.dreamexposure.discal.core.database.GuildSettingsData
import org.dreamexposure.discal.core.database.GuildSettingsRepository
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.springframework.stereotype.Component

@Component
class GuildSettingsService(
    private val repository: GuildSettingsRepository,
    private val cache: GuildSettingsCache,
) {

    suspend fun hasSettings(guildId: Snowflake): Boolean {
        return repository.existsByGuildId(guildId.asLong()).awaitSingle()
    }

    suspend fun getSettings(guildId: Snowflake): GuildSettings {
        var settings = cache.get(key = guildId)
        if (settings != null) return settings

        settings = repository.findByGuildId(guildId.asLong())
            .map(::GuildSettings)
            .defaultIfEmpty(GuildSettings(guildId = guildId))
            .awaitSingle()

        cache.put(key = guildId, value = settings)
        return settings
    }

    suspend fun createSettings(settings: GuildSettings): GuildSettings {
        LOGGER.debug("Creating new settings for guild: {}", settings.guildId)

        val saved = repository.save(GuildSettingsData(
            guildId = settings.guildId.asLong(),

            controlRole = settings.controlRole?.asString() ?: "everyone",
            timeFormat = settings.interfaceStyle.timeFormat.value,
            patronGuild = settings.patronGuild,
            devGuild = settings.devGuild,
            maxCalendars = settings.maxCalendars,
            lang = settings.locale.toLanguageTag(),
            branded = settings.interfaceStyle.branded,
            announcementStyle = settings.interfaceStyle.announcementStyle.value,
            eventKeepDuration = settings.eventKeepDuration,
        )).map(::GuildSettings).awaitSingle()

        cache.put(key = saved.guildId, value = saved)
        return saved
    }

    suspend fun updateSettings(settings: GuildSettings) {
        LOGGER.debug("Updating guild settings for {}", settings.guildId)

        repository.updateByGuildId(
            guildId = settings.guildId.asLong(),

            controlRole = settings.controlRole?.asString() ?: "everyone",
            timeFormat = settings.interfaceStyle.timeFormat.value,
            patronGuild = settings.patronGuild,
            devGuild = settings.devGuild,
            maxCalendars = settings.maxCalendars,
            lang = settings.locale.toLanguageTag(),
            branded = settings.interfaceStyle.branded,
            announcementStyle = settings.interfaceStyle.announcementStyle.value,
            eventKeepDuration = settings.eventKeepDuration,
        ).awaitSingleOrNull()

        cache.put(key = settings.guildId, value = settings)
    }

    suspend fun upsertSettings(settings: GuildSettings): GuildSettings {
        if (hasSettings(settings.guildId)) updateSettings(settings)
        else return createSettings(settings)
        return settings
    }
}
