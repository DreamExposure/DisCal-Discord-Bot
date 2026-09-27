package org.dreamexposure.discal.client.interaction

import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent
import discord4j.core.`object`.component.StringSelectMenu
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.ComponentService
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.business.PermissionService
import org.dreamexposure.discal.core.`object`.new.EventRecurrence
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import java.time.DayOfWeek

@Component
class EventRecurrenceMonthlyVariableModal(
    private val permissionService: PermissionService,
    private val calendarService: CalendarService,
    private val embedService: EmbedService,
    private val componentService: ComponentService,
): InteractionHandler<ModalSubmitInteractionEvent> {
    override val ids = arrayOf("event-wizard.recurrence.monthly_variable")
    override val ephemeral = true

    override suspend fun handle(event: ModalSubmitInteractionEvent, settings: GuildSettings) {
        val inputs = event.getComponents(StringSelectMenu::class.java)

        val selectedPosition = inputs.first { it.customId == "select.event.recurrence.position" }
            .values.get().map { EventRecurrence.SetPos.valueOf(it) }.first()
        val selectedDay = inputs.first { it.customId == "select.event.recurrence.day" }
            .values.get().map { DayOfWeek.valueOf(it) }
            .map { dow -> EventRecurrence.Day.entries.first { it.dayOfWeek == dow } }

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

        // Apply change and send response
        val modifiedWizard = existingWizard.copy(entity = existingWizard.entity.copy(recurrence = existingWizard.entity.recurrence?.copy(
            bySetPos = selectedPosition,
            byDay = selectedDay,
        )))
        calendarService.putEventWizard(modifiedWizard)

        event.createFollowup(getCommonMsg("modal.event.recurrence.monthly.success.variable", settings.locale, selectedPosition.name, selectedDay.first().dayOfWeek.name))
            .withEmbeds(embedService.eventWizardEmbed(modifiedWizard, settings))
            .withComponents(*componentService.getWizardComponents(modifiedWizard, settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }
}