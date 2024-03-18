package org.dreamexposure.discal.client.commands.global

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.business.RsvpService
import org.dreamexposure.discal.core.extensions.discord4j.followupEphemeral
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.discord4j.hasControlRole
import org.dreamexposure.discal.core.extensions.discord4j.hasElevatedPermissions
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RsvpCommand(
    private val rsvpService: RsvpService,
    private val embedService: EmbedService,
) : SlashCommand {
    override val name = "rsvp"
    override val hasSubcommands = true
    override val ephemeral = true


    override suspend fun suspendHandle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        return when (event.options[0].name) {
            "ontime" -> onTime(event, settings)
            "late" -> late(event, settings)
            "not-going" -> notGoing(event, settings)
            "unsure" -> unsure(event, settings)
            "remove" -> remove(event, settings)
            "list" -> list(event, settings)
            "limit" -> limit(event, settings)
            "role" -> role(event, settings)
            else -> throw IllegalStateException("Invalid subcommand specified")
        }
    }

    private suspend fun onTime(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)
        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val userId = event.interaction.user.id
        val guild = event.interaction.guild.awaitSingle()
        val calendar = guild.getCalendar(calendarNumber).awaitSingleOrNull()
        val calendarEvent = calendar?.getEvent(eventId)?.awaitSingleOrNull()

        // Validate required conditions
        if (calendar == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)).awaitSingle()
        if (calendarEvent == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.event", settings)).awaitSingle()
        if (calendarEvent.isOver())
            return event.followupEphemeral(getCommonMsg("error.event.ended", settings)).awaitSingle()

        var rsvp = rsvpService.getRsvp(guild.id, eventId)

        return if (rsvp.hasRoom(userId)) {
            rsvp = rsvpService.upsertRsvp(rsvp.copyWithUserStatus(userId, goingOnTime = rsvp.goingOnTime + userId))

            event.followupEphemeral(
                getMessage("onTime.success", settings),
                embedService.rsvpListEmbed(calendarEvent, rsvp, settings)
            ).awaitSingle()
        } else {
            rsvp = rsvpService.upsertRsvp(rsvp.copyWithUserStatus(userId, waitlist = rsvp.waitlist + userId))

            event.followupEphemeral(
                getMessage("onTime.failure.limit", settings),
                embedService.rsvpListEmbed(calendarEvent, rsvp, settings)
            ).awaitSingle()
        }
    }

    private suspend fun late(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)
        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val userId = event.interaction.user.id
        val guild = event.interaction.guild.awaitSingle()
        val calendar = guild.getCalendar(calendarNumber).awaitSingleOrNull()
        val calendarEvent = calendar?.getEvent(eventId)?.awaitSingleOrNull()

        // Validate required conditions
        if (calendar == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)).awaitSingle()
        if (calendarEvent == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.event", settings)).awaitSingle()
        if (calendarEvent.isOver())
            return event.followupEphemeral(getCommonMsg("error.event.ended", settings)).awaitSingle()

        var rsvp = rsvpService.getRsvp(guild.id, eventId)

        return if (rsvp.hasRoom(userId)) {
            rsvp = rsvpService.upsertRsvp(rsvp.copyWithUserStatus(userId, goingLate = rsvp.goingLate + userId))

            event.followupEphemeral(
                getMessage("late.success", settings),
                embedService.rsvpListEmbed(calendarEvent, rsvp, settings)
            ).awaitSingle()
        } else {
            rsvp = rsvpService.upsertRsvp(rsvp.copy(waitlist = rsvp.waitlist + userId))

            event.followupEphemeral(
                getMessage("late.failure.limit", settings),
                embedService.rsvpListEmbed(calendarEvent, rsvp, settings)
            ).awaitSingle()
        }
    }

    private suspend fun unsure(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)
        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val userId = event.interaction.user.id
        val guild = event.interaction.guild.awaitSingle()
        val calendar = guild.getCalendar(calendarNumber).awaitSingleOrNull()
        val calendarEvent = calendar?.getEvent(eventId)?.awaitSingleOrNull()

        // Validate required conditions
        if (calendar == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)).awaitSingle()
        if (calendarEvent == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.event", settings)).awaitSingle()
        if (calendarEvent.isOver())
            return event.followupEphemeral(getCommonMsg("error.event.ended", settings)).awaitSingle()

        var rsvp = rsvpService.getRsvp(guild.id, eventId)

        rsvp = rsvpService.upsertRsvp(rsvp.copyWithUserStatus(userId, undecided = rsvp.undecided + userId))

        return event.followupEphemeral(
            getMessage("unsure.success", settings),
            embedService.rsvpListEmbed(calendarEvent, rsvp, settings)
        ).awaitSingle()
    }

    private suspend fun notGoing(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)
        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val userId = event.interaction.user.id
        val guild = event.interaction.guild.awaitSingle()
        val calendar = guild.getCalendar(calendarNumber).awaitSingleOrNull()
        val calendarEvent = calendar?.getEvent(eventId)?.awaitSingleOrNull()

        // Validate required conditions
        if (calendar == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)).awaitSingle()
        if (calendarEvent == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.event", settings)).awaitSingle()
        if (calendarEvent.isOver())
            return event.followupEphemeral(getCommonMsg("error.event.ended", settings)).awaitSingle()

        var rsvp = rsvpService.getRsvp(guild.id, eventId)

        rsvp = rsvpService.upsertRsvp(rsvp.copyWithUserStatus(userId, notGoing = rsvp.notGoing + userId))

        return event.followupEphemeral(
            getMessage("notGoing.success", settings),
            embedService.rsvpListEmbed(calendarEvent, rsvp, settings)
        ).awaitSingle()
    }

    private suspend fun remove(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)
        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val userId = event.interaction.user.id
        val guild = event.interaction.guild.awaitSingle()
        val calendar = guild.getCalendar(calendarNumber).awaitSingleOrNull()
        val calendarEvent = calendar?.getEvent(eventId)?.awaitSingleOrNull()

        // Validate required conditions
        if (calendar == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)).awaitSingle()
        if (calendarEvent == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.event", settings)).awaitSingle()
        if (calendarEvent.isOver())
            return event.followupEphemeral(getCommonMsg("error.event.ended", settings)).awaitSingle()

        var rsvp = rsvpService.getRsvp(guild.id, eventId)

        rsvp = rsvpService.upsertRsvp(rsvp.copyWithUserStatus(userId))

        return event.followupEphemeral(
            getMessage("remove.success", settings),
            embedService.rsvpListEmbed(calendarEvent, rsvp, settings)
        ).awaitSingle()
    }

    private suspend fun list(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)
        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val guild = event.interaction.guild.awaitSingle()
        val calendar = guild.getCalendar(calendarNumber).awaitSingleOrNull()
        val calendarEvent = calendar?.getEvent(eventId)?.awaitSingleOrNull()

        // Validate required conditions
        if (calendar == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)).awaitSingle()
        if (calendarEvent == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.event", settings)).awaitSingle()

        val rsvp = rsvpService.getRsvp(guild.id, eventId)

        return event.followupEphemeral(embedService.rsvpListEmbed(calendarEvent, rsvp, settings)).awaitSingle()
    }

    private suspend fun limit(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val limit = event.options[0].getOption("limit")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .get()

        // Validate control role first to reduce work
        val hasControlRole = event.interaction.member.get().hasControlRole().awaitSingle()
        if (!hasControlRole)
            return event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)).awaitSingle()


        val guild = event.interaction.guild.awaitSingle()
        val calendar = guild.getCalendar(calendarNumber).awaitSingleOrNull()
        val calendarEvent = calendar?.getEvent(eventId)?.awaitSingleOrNull()

        // Validate required conditions
        if (calendar == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)).awaitSingle()
        if (calendarEvent == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.event", settings)).awaitSingle()
        if (calendarEvent.isOver())
            return event.followupEphemeral(getCommonMsg("error.event.ended", settings)).awaitSingle()

        var rsvp = rsvpService.getRsvp(guild.id, eventId)
        rsvp = rsvpService.upsertRsvp(rsvp.copy(limit = limit))


        return event.followupEphemeral(
            getMessage("limit.success", settings, limit.toString()),
            embedService.rsvpListEmbed(calendarEvent, rsvp, settings)
        ).awaitSingle()
    }

    private suspend fun role(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        if (!settings.patronGuild)
            return event.followupEphemeral(getCommonMsg("error.patronOnly", settings)).awaitSingle()

        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)
        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()
        val role = Mono.justOrEmpty(
            event.options[0].getOption("role").flatMap(ApplicationCommandInteractionOption::getValue)
        ).flatMap(ApplicationCommandInteractionOptionValue::asRole).awaitSingle()


        // Validate control role first to reduce work
        val hasElevatedPerms = event.interaction.member.get().hasElevatedPermissions().awaitSingle()
        if (!hasElevatedPerms)
            return event.followupEphemeral(getCommonMsg("error.perms.elevated", settings)).awaitSingle()

        val guild = event.interaction.guild.awaitSingle()
        val calendar = guild.getCalendar(calendarNumber).awaitSingleOrNull()
        val calendarEvent = calendar?.getEvent(eventId)?.awaitSingleOrNull()

        // Validate required conditions
        if (calendar == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)).awaitSingle()
        if (calendarEvent == null)
            return event.followupEphemeral(getCommonMsg("error.notFound.event", settings)).awaitSingle()
        if (calendarEvent.isOver())
            return event.followupEphemeral(getCommonMsg("error.event.ended", settings)).awaitSingle()


        var rsvp = rsvpService.getRsvp(guild.id, eventId)
        rsvp = rsvpService.upsertRsvp(rsvp.copy(role = if (role.isEveryone) null else role.id))

        val embed = embedService.rsvpListEmbed(calendarEvent, rsvp, settings)

        return if (role.isEveryone) event.followupEphemeral(getMessage("role.success.remove", settings), embed).awaitSingle()
        else event.followupEphemeral(getMessage("role.success.set", settings, role.name), embed).awaitSingle()
    }
}
