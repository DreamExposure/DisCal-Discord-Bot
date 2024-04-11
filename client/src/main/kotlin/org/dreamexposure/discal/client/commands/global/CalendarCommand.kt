package org.dreamexposure.discal.client.commands.global

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.client.message.embed.CalendarEmbed
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.StaticMessageService
import org.dreamexposure.discal.core.entities.response.UpdateCalendarResponse
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.extensions.discord4j.createCalendar
import org.dreamexposure.discal.core.extensions.discord4j.followupEphemeral
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.discord4j.hasElevatedPermissions
import org.dreamexposure.discal.core.extensions.isValidTimezone
import org.dreamexposure.discal.core.extensions.toZoneId
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.Wizard
import org.dreamexposure.discal.core.`object`.calendar.PreCalendar
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.ZoneId

@Component
class CalendarCommand(
    private val wizard: Wizard<PreCalendar>,
    private val calendarService: CalendarService,
    private val staticMessageService: StaticMessageService
) : SlashCommand {
    override val name = "calendar"
    override val hasSubcommands = true
    override val ephemeral = true

    @Deprecated("Use new handleSuspend for K-coroutines")
    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return when (event.options[0].name) {
            "create" -> create(event, settings)
            "name" -> name(event, settings)
            "description" -> description(event, settings)
            "timezone" -> timezone(event, settings)
            "review" -> review(event, settings)
            "confirm" -> confirm(event, settings)
            "cancel" -> cancel(event, settings)
            "delete" -> delete(event, settings)
            "edit" -> edit(event, settings)
            else -> Mono.empty() //Never can reach this, makes compiler happy.
        }
    }

    private fun create(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val name = event.options[0].getOption("name")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val description = event.options[0].getOption("description")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse("")

        val timezone = event.options[0].getOption("timezone")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse("")

        val host = event.options[0].getOption("host")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .map(CalendarHost::valueOf)
            .orElse(CalendarHost.GOOGLE)

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasElevatedPermissions).flatMap {
            if (wizard.get(settings.guildId) == null) {
                //Start calendar wizard
                val pre = PreCalendar.new(settings.guildId, host, name)
                pre.description = description
                pre.timezone = timezone.toZoneId() // Extension method auto-checks if the timezone is valid

                // Message content for wizard start so that if a bad optional timezone is given, we can provide better feedback.
                val msg = if (timezone.isNotEmpty() && !timezone.isValidTimezone()) {
                    // Invalid timezone specified
                    getMessage("create.success.badTimezone", settings)
                } else {
                    getMessage("create.success", settings)
                }

                event.interaction.guild
                    .filterWhen { mono { calendarService.canAddNewCalendar(it.id) } }
                    .doOnNext { wizard.start(pre) } //only start wizard if another calendar can be added
                    .map { CalendarEmbed.pre(it, settings, pre) }
                    .flatMap { event.followupEphemeral(msg, it) }
                    .switchIfEmpty(event.followupEphemeral(getCommonMsg("error.calendar.max", settings.locale)))
            } else {
                event.interaction.guild
                    .map { CalendarEmbed.pre(it, settings, wizard.get(settings.guildId)!!) }
                    .flatMap { event.followupEphemeral(getMessage("error.wizard.started", settings), it) }
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings.locale)))
    }

    private fun name(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val name = event.options[0].getOption("name")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasElevatedPermissions).flatMap {
            val pre = wizard.get(settings.guildId)
            if (pre != null) {
                pre.name = name
                event.interaction.guild
                    .map { CalendarEmbed.pre(it, settings, pre) }
                    .flatMap { event.followupEphemeral(getMessage("name.success", settings), it) }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings.locale)))
    }

    private fun description(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val desc = event.options[0].getOption("description")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasElevatedPermissions).flatMap {
            val pre = wizard.get(settings.guildId)
            if (pre != null) {
                pre.description = desc
                event.interaction.guild
                    .map { CalendarEmbed.pre(it, settings, pre) }
                    .flatMap { event.followupEphemeral(getMessage("description.success", settings), it) }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings.locale)))
    }

    private fun timezone(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val timezone = event.options[0].getOption("timezone")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasElevatedPermissions).flatMap {
            val pre = wizard.get(settings.guildId)
            if (pre != null) {
                if (timezone.isValidTimezone()) {
                    pre.timezone = ZoneId.of(timezone)

                    event.interaction.guild
                        .map { CalendarEmbed.pre(it, settings, pre) }
                        .flatMap { event.followupEphemeral(getMessage("timezone.success", settings), it) }
                } else {
                    event.followupEphemeral(getMessage("timezone.failure.invalid", settings))
                }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings.locale)))
    }

    private fun review(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasElevatedPermissions).flatMap {
            val pre = wizard.get(settings.guildId)
            if (pre != null) {
                event.interaction.guild.flatMap {
                    event.followupEphemeral(CalendarEmbed.pre(it, settings, pre))
                }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings.locale)))
    }

    private fun confirm(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasElevatedPermissions).flatMap {
            val pre = wizard.get(settings.guildId)
            if (pre != null) {
                if (!pre.hasRequiredValues()) {
                    return@flatMap event.followupEphemeral(getMessage("confirm.failure.missing", settings))
                }

                event.interaction.guild.flatMap { guild ->
                    if (!pre.editing) {
                        // New calendar
                        pre.createSpec(guild)
                            .flatMap(guild::createCalendar)
                            .doOnNext { wizard.remove(settings.guildId) }
                            .flatMap {
                                event.followupEphemeral(
                                    getMessage("confirm.success.create", settings),
                                    CalendarEmbed.link(guild, settings, it)
                                )
                            }.doOnError {
                                LOGGER.error("Create calendar with command failure", it)
                            }.onErrorResume {
                                event.followupEphemeral(getMessage("confirm.failure.create", settings))
                            }
                    } else {
                        // Editing
                        pre.calendar!!.update(pre.updateSpec())
                            .filter(UpdateCalendarResponse::success)
                            .doOnNext { wizard.remove(settings.guildId) }
                            .flatMap { ucr ->
                                val updateMessages = mono {
                                    staticMessageService.updateStaticMessages(settings.guildId, ucr.new!!.calendarNumber)
                                }
                                 event.followupEphemeral(
                                        getMessage("confirm.success.edit", settings),
                                        CalendarEmbed.link(guild, settings, ucr.new!!)
                                ).flatMap { updateMessages.thenReturn(it) }
                            }.switchIfEmpty(event.followupEphemeral(getMessage("confirm.failure.edit", settings)))
                    }
                }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings.locale)))
    }

    private fun cancel(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasElevatedPermissions).flatMap {
            wizard.remove(settings.guildId)

            event.followupEphemeral(getMessage("cancel.success", settings))
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings.locale)))
    }

    private fun delete(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasElevatedPermissions).flatMap {
            // Before we delete the calendar, if the wizard is editing that calendar we need to cancel the wizard
            val pre = wizard.get(settings.guildId)
            if (pre != null && pre.calendar?.calendarNumber == calendarNumber) wizard.remove(settings.guildId)

            event.interaction.guild
                .flatMap { it.getCalendar(calendarNumber) }
                .flatMap { it.delete() }
                .flatMap { event.followupEphemeral(getMessage("delete.success", settings)) }
                .switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings.locale)))
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings.locale)))
    }

    private fun edit(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasElevatedPermissions).flatMap {
            if (wizard.get(settings.guildId) == null) {
                event.interaction.guild.flatMap { guild ->
                    guild.getCalendar(calendarNumber)
                        .map { PreCalendar.edit(it) }
                        .doOnNext { wizard.start(it) }
                        .map { CalendarEmbed.pre(guild, settings, it) }
                        .flatMap { event.followupEphemeral(getMessage("edit.success", settings), it) }
                        .switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings.locale)))
                }
            } else {
                event.interaction.guild
                    .map { CalendarEmbed.pre(it, settings, wizard.get(settings.guildId)!!) }
                    .flatMap { event.followupEphemeral(getMessage("error.wizard.started", settings), it) }
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings.locale)))
    }
}
