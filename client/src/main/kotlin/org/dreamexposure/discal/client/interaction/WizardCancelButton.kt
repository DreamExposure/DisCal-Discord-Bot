package org.dreamexposure.discal.client.interaction

import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.core.business.AnnouncementService
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.PermissionService
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCmdMessage
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component

@Component
class WizardCancelButton(
    private val permissionService: PermissionService,
    private val calendarService: CalendarService,
    private val announcementService: AnnouncementService,
): InteractionHandler<ButtonInteractionEvent> {
    override val ids = arrayOf("wizard-cancel-")
    override val ephemeral = true

    override suspend fun handle(event: ButtonInteractionEvent, settings: GuildSettings) {
        when (event.customId.split("-")[2]) {
            "calendar" -> {
                // Validate permissions
                val hasElevatedPerms = permissionService.hasElevatedPermissions(settings.guildId, event.interaction.user.id)
                if (!hasElevatedPerms) {
                    event.createFollowup(getCommonMsg("error.perms.elevated", settings.locale))
                        .withEphemeral(ephemeral)
                        .awaitSingle()
                    return
                }

                calendarService.cancelCalendarWizard(settings.guildId, event.interaction.user.id)

                event.createFollowup(getCmdMessage("calendar", "cancel.success", settings.locale))
                    .withEphemeral(ephemeral)
                    .awaitSingle()
            }
            "event" -> {
                // Validate permissions
                val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
                if (!hasControlRole) {
                    event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
                        .withEphemeral(ephemeral)
                        .awaitSingle()
                    return
                }

                calendarService.cancelEventWizard(settings.guildId, event.interaction.user.id)

                event.createFollowup(getCmdMessage("event", "cancel.success", settings.locale))
                    .withEphemeral(ephemeral)
                    .awaitSingle()
            }
            "announcement" -> {
                // Validate permissions
                val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
                if (!hasControlRole) {
                    event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
                        .withEphemeral(ephemeral)
                        .awaitSingle()
                    return
                }

                announcementService.cancelWizard(settings.guildId, event.interaction.user.id)

                event.createFollowup(getCmdMessage("announcement", "cancel.success", settings.locale))
                    .withEphemeral(ephemeral)
                    .awaitSingle()

            }
            else -> throw UnsupportedOperationException("Unrecognized wizard type ${event.customId}")
        }
    }
}