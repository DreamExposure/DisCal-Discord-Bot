package org.dreamexposure.discal.client.interaction

import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.ComponentService
import org.dreamexposure.discal.core.business.PermissionService
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component

@Component
class EventRecurrenceMonthlyDropdown(
    private val calendarService: CalendarService,
    private val componentService: ComponentService,
    private val permissionService: PermissionService,
): InteractionHandler<SelectMenuInteractionEvent> {
    override val ids = arrayOf("select.event.recurrence.month-option")
    override val ephemeral = true

    override suspend fun handle(event: SelectMenuInteractionEvent, settings: GuildSettings) {
        val selected = event.values[0]

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) {
            event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
            return
        }
        // Check if wizard not started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) {
            event.createFollowup(getCommonMsg("error.event.wizard.notStarted", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
            return
        }

        when (selected) {
            "monthly_date" -> {
                event.presentModal()
                    .withCustomId("event-wizard.recurrence.monthly_date")
                    .withTitle(getCommonMsg("modal.event.recurrence.monthly.title", settings.locale))
                    .withComponents(*componentService.getEventRecurrenceMonthlyDateModalComponents(settings, existingWizard.entity))
                    .awaitSingleOrNull()
            }
            "monthly_variable" -> {
                event.presentModal()
                    .withCustomId("event-wizard.recurrence.monthly_variable")
                    .withTitle(getCommonMsg("modal.event.recurrence.monthly.title", settings.locale))
                    .withComponents(*componentService.getEventRecurrenceMonthlyVariableModalComponents(settings, existingWizard.entity))
                    .awaitSingleOrNull()

            }
            else -> throw IllegalStateException("Unknown selected monthly recurrence option: $selected")
        }
    }
}