package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.discordjson.json.RoleData
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.stereotype.Component
import java.util.function.Predicate

@Component
class PermissionService(
    private val settingsService: GuildSettingsService,
    private val beanFactory: BeanFactory
) {
    private val discordClient
        get() = beanFactory.getBean<DiscordClient>()

    suspend fun hasControlRole(guildId: Snowflake, memberId: Snowflake): Boolean {
        val settings = settingsService.getSettings(guildId)

        if (settings.controlRole == null || settings.controlRole == guildId) return true

        val memberData = discordClient.getMemberById(guildId, memberId).data.awaitSingle()

        return memberData.roles().map(Snowflake::of).contains(settings.controlRole)
    }

    suspend fun hasPermissions(guildId: Snowflake, memberId: Snowflake, pred: Predicate<PermissionSet>): Boolean {
        val guildData = discordClient.getGuildById(guildId).data.awaitSingle()

        // Owner has full permissions, always
        if (guildData.ownerId().asLong() == memberId.asLong()) return true

        val memberData = discordClient.getMemberById(guildId, memberId).data.awaitSingle()

        val computedPermissions = PermissionSet.of(
            guildData.roles()
                .filter { memberData.roles().contains(it.id()) }
                .map(RoleData::permissions)
                .reduceOrNull { acc, lng -> acc or lng } ?: 0L
        )

        return pred.test(computedPermissions)
    }

    suspend fun hasElevatedPermissions(guildId: Snowflake, memberId: Snowflake): Boolean {
        return hasPermissions(guildId, memberId) {
            it.contains(Permission.MANAGE_GUILD) || it.contains(Permission.ADMINISTRATOR)
        }
    }
}
