package org.dreamexposure.discal.client.message.embed

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Role
import discord4j.core.spec.EmbedCreateSpec
import org.dreamexposure.discal.core.`object`.GuildSettings
import reactor.core.publisher.Mono

object SettingsEmbed : EmbedMaker {

    fun getView(guild: Guild, settings: GuildSettings): Mono<EmbedCreateSpec> {
        val roleMono = guild.getRoleById(Snowflake.of(settings.controlRole))
              .map(Role::getName)
              .onErrorReturn("everyone") // This should be more efficient than looping through all roles

        return roleMono.map { roleName ->
            defaultBuilder(guild, settings)
                  .title(getMessage("settings", "view.title", settings))
                  .addField(getMessage("settings", "view.field.role", settings), roleName, false)
                  .addField(getMessage("settings", "view.field.style", settings), settings.announcementStyle.name, true)
                  .addField(getMessage("settings", "view.field.format", settings), settings.timeFormat.name, true)
                  .addField(getMessage("settings", "view.field.lang", settings), settings.getLocale().displayName, false)
                  .addField(getMessage("settings", "view.field.patron", settings), "${settings.patronGuild}", true)
                  .addField(getMessage("settings", "view.field.dev", settings), "${settings.devGuild}", true)
                  .addField(getMessage("settings", "view.field.cal", settings), "${settings.maxCalendars}", true)
                  .addField(getMessage("settings", "view.field.brand", settings), "${settings.branded}", false)
                  .footer(getMessage("settings", "view.footer", settings), null)
                  .build()
        }
    }
}
