package org.dreamexposure.discal.client.interaction

import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent
import discord4j.core.`object`.component.StringSelectMenu
import discord4j.core.`object`.component.TextInput
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.ComponentService
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.business.PermissionService
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.`object`.new.Event
import org.dreamexposure.discal.core.`object`.new.EventWizardState
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component

@Component
class EventCreateModal(
    private val permissionService: PermissionService,
    private val calendarService: CalendarService,
    private val embedService: EmbedService,
    private val componentService: ComponentService,
): InteractionHandler<ModalSubmitInteractionEvent> {
    override val ids = arrayOf("event-wizard.create-event")
    override val ephemeral = true

    override suspend fun handle(event: ModalSubmitInteractionEvent, settings: GuildSettings) {
        val textInputs = event.getComponents(TextInput::class.java)
        val selectInputs = event.getComponents(StringSelectMenu::class.java)

        val name = textInputs.first { it.customId == "event.create.name" }
            .value
            .orElse("")
        val description = textInputs.first { it.customId == "event.create.description" }
            .value
            .orElse("")
        val location = textInputs.first { it.customId == "event.create.location" }
            .value
            .orElse("")
        val calendarNumber = selectInputs.first { it.customId == "event.create.calendar" }
            .values
            .get()
            .first()
            .toInt()


        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) {
            event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
            return
        }

        // Check if wizard already started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard != null) {
            event.createFollowup(getCommonMsg("error.event.wizard.started", settings.locale))
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.eventWizardEmbed(existingWizard, settings))
                .withComponents(*componentService.getWizardComponents(existingWizard, settings))
                .awaitSingle()
            return
        }

        // Make sure calendar exists
        val calendar = calendarService.getCalendar(settings.guildId, calendarNumber)
        if (calendar == null) {
            event.createFollowup(getCommonMsg("error.notFound.calendar", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
            return
        }

        val newWizard = EventWizardState(
            guildId = settings.guildId,
            userId = event.interaction.user.id,
            editing = false,
            entity = Event.PartialEvent(
                id = null,
                guildId = settings.guildId,
                calendarNumber = calendarNumber,
                name = name,
                description = description,
                location = location,
                color = EventColor.NONE,
                start = null,
                end = null,
                recur = false,
                recurrence = null,
                image = null,
                timezone = calendar.timezone,
            )
        )
        calendarService.putEventWizard(newWizard)

        event.createFollowup(getCommonMsg("modal.event.create.success", settings.locale))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.eventWizardEmbed(newWizard, settings))
            .withComponents(*componentService.getWizardComponents(newWizard, settings))
            .awaitSingle()
    }
}