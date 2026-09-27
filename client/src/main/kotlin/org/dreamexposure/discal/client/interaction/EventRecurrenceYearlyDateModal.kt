package org.dreamexposure.discal.client.interaction

import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent
import discord4j.core.`object`.component.StringSelectMenu
import discord4j.core.`object`.component.TextInput
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.ComponentService
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.business.PermissionService
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import java.time.Month

@Component
class EventRecurrenceYearlyDateModal(
    private val permissionService: PermissionService,
    private val calendarService: CalendarService,
    private val embedService: EmbedService,
    private val componentService: ComponentService,
): InteractionHandler<ModalSubmitInteractionEvent> {
    override val ids = arrayOf("event-wizard.recurrence.yearly_date")
    override val ephemeral = true

    override suspend fun handle(event: ModalSubmitInteractionEvent, settings: GuildSettings) {
        val selectInputs = event.getComponents(StringSelectMenu::class.java)
        val textInputs = event.getComponents(TextInput::class.java)

        val selectedMonth = selectInputs.first { it.customId == "select.event.recurrence.month" }
            .values.get().map { Month.valueOf(it) }.first()
        val selectedDate = textInputs.first { it.customId == "event-recurrence-yearly-date" }
            .value
            .get()
            .toIntOrNull()
            ?.coerceAtLeast(1)
            ?.coerceAtMost(selectedMonth.maxLength())

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
            event.createFollowup(getCommonMsg("modal.event.recurrence.yearly.error.date-not-number", settings.locale))
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.eventWizardEmbed(existingWizard, settings))
                .withComponents(*componentService.getWizardComponents(existingWizard, settings))
            return
        }

        // Apply changes and send message
        val modifiedWizard = existingWizard.copy(entity = existingWizard.entity.copy(recurrence = existingWizard.entity.recurrence?.copy(
            byMonth = selectedMonth,
            byMonthDay = selectedDate
        )))
        calendarService.putEventWizard(modifiedWizard)

        event.createFollowup(getCommonMsg("modal.event.recurrence.yearly.success.date", settings.locale, selectedMonth.name, selectedDate.toString()))
            .withEmbeds(embedService.eventWizardEmbed(modifiedWizard, settings))
            .withComponents(*componentService.getWizardComponents(modifiedWizard, settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }
}