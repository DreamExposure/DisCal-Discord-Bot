package org.dreamexposure.discal.client.commands

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.dreamexposure.discal.client.message.embed.CalendarEmbed
import org.dreamexposure.discal.client.wizards.CalendarWizard
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.calendar.PreCalendar
import org.dreamexposure.discal.core.entities.response.UpdateCalendarResponse
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.extensions.discord4j.*
import org.dreamexposure.discal.core.extensions.isValidTimezone
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.ZoneId

@Component
class CalendarCommand(val wizard: CalendarWizard) : SlashCommand {
    override val name = "calendar"
    override val ephemeral = true

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

        val host = event.options[0].getOption("host")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .map(CalendarHost::valueOf)
            .orElse(CalendarHost.GOOGLE)

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasElevatedPermissions).flatMap {
            if (wizard.get(settings.guildID) == null) {
                //Start calendar wizard
                val pre = PreCalendar.new(settings.guildID, host, name)

                event.interaction.guild
                    .filterWhen(Guild::canAddCalendar)
                    .doOnNext { wizard.start(pre) } //only start wizard if another calendar can be added
                    .map { CalendarEmbed.pre(it, settings, pre) }
                    .flatMap { event.followupEphemeral(getMessage("create.success", settings), it) }
                    .switchIfEmpty(event.followupEphemeral(getCommonMsg("error.calendar.max", settings)))
            } else {
                event.interaction.guild
                    .map { CalendarEmbed.pre(it, settings, wizard.get(settings.guildID)!!) }
                    .flatMap { event.followupEphemeral(getMessage("error.wizard.started", settings), it) }
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings)))
    }

    private fun name(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val name = event.options[0].getOption("name")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return event.interaction.member.get().hasElevatedPermissions().filter { it }.flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                pre.name = name
                event.interaction.guild
                    .map { CalendarEmbed.pre(it, settings, pre) }
                    .flatMap { event.followupEphemeral(getMessage("name.success", settings), it) }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings)))
    }

    private fun description(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val desc = event.options[0].getOption("description")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return event.interaction.member.get().hasElevatedPermissions().filter { it }.flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                pre.description = desc
                event.interaction.guild
                    .map { CalendarEmbed.pre(it, settings, pre) }
                    .flatMap { event.followupEphemeral(getMessage("description.success", settings), it) }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings)))
    }

    private fun timezone(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val timezone = event.options[0].getOption("timezone")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return event.interaction.member.get().hasElevatedPermissions().filter { it }.flatMap {

            val pre = wizard.get(settings.guildID)
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
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings)))
    }

    private fun review(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return event.interaction.member.get().hasElevatedPermissions().filter { it }.flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                event.interaction.guild.flatMap {
                    event.followupEphemeral(CalendarEmbed.pre(it, settings, pre))
                }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings)))
    }

    private fun confirm(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return event.interaction.member.get().hasElevatedPermissions().filter { it }.flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                if (!pre.hasRequiredValues()) {
                    event.followupEphemeral(getMessage("confirm.failure.missing", settings))
                }

                event.interaction.guild.flatMap { guild ->
                    if (!pre.editing) {
                        // New calendar
                        pre.createSpec(guild)
                            .flatMap(guild::createCalendar)
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
                            .map { CalendarEmbed.link(guild, settings, it.new!!) }
                            .flatMap { event.followupEphemeral(getMessage("confirm.success.edit", settings), it) }
                            .switchIfEmpty(event.followupEphemeral(getMessage("confirm.failure.edit", settings)))
                    }
                }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings)))
    }

    private fun cancel(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return event.interaction.member.get().hasElevatedPermissions().filter { it }.flatMap {
            wizard.remove(settings.guildID)

            event.followupEphemeral(getMessage("cancel.success", settings))
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings)))
    }

    private fun delete(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        return event.interaction.member.get().hasElevatedPermissions().filter { it }.flatMap {
            event.interaction.guild
                .flatMap { it.getCalendar(calendarNumber) }
                .flatMap { it.delete() }
                .flatMap { event.followupEphemeral(getMessage("delete.success", settings)) }
                .switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings)))
    }

    private fun edit(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        return event.interaction.member.get().hasElevatedPermissions().filter { it }.flatMap {
            if (wizard.get(settings.guildID) == null) {
                event.interaction.guild.flatMap { guild ->
                    guild.getCalendar(calendarNumber)
                        .map { PreCalendar.edit(it) }
                        .doOnNext { wizard.start(it) }
                        .map { CalendarEmbed.pre(guild, settings, it) }
                        .flatMap { event.followupEphemeral(getMessage("edit.success", settings), it) }
                        .switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
                }
            } else {
                event.interaction.guild
                    .map { CalendarEmbed.pre(it, settings, wizard.get(settings.guildID)!!) }
                    .flatMap { event.followupEphemeral(getMessage("error.wizard.started", settings), it) }
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings)))
    }
}
