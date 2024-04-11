package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.discordjson.Id
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.stereotype.Component

@Component
class PermissionService(
    private val settingsService: GuildSettingsService,
    private val beanFactory: BeanFactory
) {
    private val discordClient
        get() = beanFactory.getBean<DiscordClient>()

    suspend fun hasControlRole(guildId: Snowflake, memberId: Snowflake): Boolean {
        val settings = settingsService.getSettings(guildId)

        if (settings.controlRole.equals("everyone", true)) return true
        if (settings.controlRole == guildId.asString()) return true

        val memberData = discordClient.getMemberById(guildId, memberId).data.awaitSingle()

        return memberData.roles().contains(Id.of(settings.controlRole))
    }
}
