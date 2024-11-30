package org.dreamexposure.discal.client.commands.premium

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.PermissionService
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component

@Component
class AddCalCommand(
    private val calendarService: CalendarService,
    private val permissionService: PermissionService,
) : SlashCommand {
    override val name = "addcal"
    override val hasSubcommands = false
    override val ephemeral = true

    override suspend fun suspendHandle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        //TODO: Remove dev-only and switch to patron-only once this is completed
        if (!settings.devGuild) return event.createFollowup(getCommonMsg("error.disabled", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Validate permissions
        val hasElevatedPerms = permissionService.hasElevatedPermissions(settings.guildId, event.interaction.user.id)
        if (!hasElevatedPerms)
            return event.createFollowup(getCommonMsg("error.perms.elevated", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()

        val canAddCalendar = calendarService.canAddNewCalendar(settings.guildId)
        if (!canAddCalendar) return event.createFollowup(getCommonMsg("error.calendar.max", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        return event.createFollowup(getMessage("response.start", settings, getLink(settings)))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private fun getLink(settings: GuildSettings): String {
        return "${Config.URL_BASE.getString()}/dashboard/${settings.guildId.asLong()}/calendar/new?type=1&step=0"
    }
}
