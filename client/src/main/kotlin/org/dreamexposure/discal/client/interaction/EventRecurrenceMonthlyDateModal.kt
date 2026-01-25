package org.dreamexposure.discal.client.interaction

import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent
import discord4j.core.`object`.component.TextInput
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.ComponentService
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.business.PermissionService
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component

@Component
class EventRecurrenceMonthlyDateModal(
    private val permissionService: PermissionService,
    private val calendarService: CalendarService,
    private val embedService: EmbedService,
    private val componentService: ComponentService,
): InteractionHandler<ModalSubmitInteractionEvent> {
    override val ids = arrayOf("event-wizard.recurrence.monthly_date")
    override val ephemeral = true

    override suspend fun handle(event: ModalSubmitInteractionEvent, settings: GuildSettings) {
        val inputs = event.getComponents(TextInput::class.java)

        val selectedDate = inputs.first { it.customId == "event-recurrence-monthly-date" }
            .value
            .get()
            .toIntOrNull()
            ?.coerceAtLeast(1)
            ?.coerceAtMost(31)

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

        // Make sure input was valid
        if (selectedDate == null) {
            event.createFollowup(getCommonMsg("modal.event.recurrence.monthly.error.date-not-number", settings.locale))
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.eventWizardEmbed(existingWizard, settings))
                .withComponents(*componentService.getWizardComponents(existingWizard, settings))
            return
        }

        // Apply change and send response
        val modifiedWizard = existingWizard.copy(entity = existingWizard.entity.copy(recurrence = existingWizard.entity.recurrence?.copy(byMonthDay = selectedDate)))
        calendarService.putEventWizard(modifiedWizard)

        event.createFollowup(getCommonMsg("modal.event.recurrence.monthly.success.date", settings.locale, selectedDate.toString()))
            .withEmbeds(embedService.eventWizardEmbed(modifiedWizard, settings))
            .withComponents(*componentService.getWizardComponents(modifiedWizard, settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }
}