package org.dreamexposure.discal.client.interaction

import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.ComponentService
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.business.RsvpService
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCmdMessage
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component

@Component
class RsvpDropdown(
    private val calendarService: CalendarService,
    private val rsvpService: RsvpService,
    private val embedService: EmbedService,
    private val componentService: ComponentService,
): InteractionHandler<SelectMenuInteractionEvent> {
    override val ids = arrayOf("rsvp")
    override val ephemeral = true

    override suspend fun handle(event: SelectMenuInteractionEvent, settings: GuildSettings) {
        val calendarNumber = event.customId.split("|")[1].toInt()
        val eventId = event.customId.split("|")[2]
        val selected = event.values[0]

        val userId = event.interaction.user.id
        val calendar = calendarService.getCalendar(settings.guildId, calendarNumber)
        val calendarEvent = calendarService.getEvent(settings.guildId, calendarNumber, eventId)

        // Validate required conditions
        if (calendar == null) {
            event.createFollowup(getCommonMsg("error.notFound.calendar", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
            return
        }
        if (calendarEvent == null) {
            event.createFollowup(getCommonMsg("error.notFound.event", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
            return
        }
        if (calendarEvent.isOver()) {
            event.createFollowup(getCommonMsg("error.event.ended", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
            return
        }

        var rsvp = rsvpService.getRsvp(settings.guildId, eventId)
        val message: String


        when (selected) {
            "rsvp_on_time" -> {
                if (rsvp.hasRoom()) {
                    rsvp = rsvpService.upsertRsvp(rsvp.copyWithUserStatus(userId, goingOnTime = rsvp.goingOnTime + userId))
                    message = getCmdMessage("rsvp", "onTime.success", settings.locale)
                } else {
                    rsvp = rsvpService.upsertRsvp(rsvp.copyWithUserStatus(userId, waitlist = rsvp.waitlist + userId))
                    message = getCmdMessage("rsvp", "onTime.failure.limit", settings.locale)
                }
            }
            "rsvp_late" -> {
                if (rsvp.hasRoom()) {
                    rsvp = rsvpService.upsertRsvp(rsvp.copyWithUserStatus(userId, goingLate = rsvp.goingLate + userId))
                    message = getCmdMessage("rsvp", "late.success", settings.locale)
                } else {
                    rsvp = rsvpService.upsertRsvp(rsvp.copyWithUserStatus(userId, waitlist = rsvp.waitlist + userId))
                    message = getCmdMessage("rsvp", "late.failure.limit", settings.locale)
                }
            }
            "rsvp_not_going" -> {
                rsvp = rsvpService.upsertRsvp(rsvp.copyWithUserStatus(userId, notGoing = rsvp.notGoing + userId))
                message = getCmdMessage("rsvp", "notGoing.success", settings.locale)
            }
            "rsvp_undecided" -> {
                rsvp = rsvpService.upsertRsvp(rsvp.copyWithUserStatus(userId, undecided = rsvp.undecided + userId))
                message = getCmdMessage("rsvp", "unsure.success", settings.locale)
            } else -> {
                message = getCommonMsg("dropdown.rsvp.response.unexpected-option", settings.locale)
            }
        }

        event.createFollowup(message)
            .withEmbeds(embedService.rsvpListEmbed(calendarEvent, rsvp, settings))
            .withComponents(*componentService.getEventRsvpComponents(calendarEvent, settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }
}