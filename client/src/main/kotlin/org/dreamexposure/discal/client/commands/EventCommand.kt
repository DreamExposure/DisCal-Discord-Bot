package org.dreamexposure.discal.client.commands

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.dreamexposure.discal.client.message.embed.EventEmbed
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.Wizard
import org.dreamexposure.discal.core.`object`.event.PreEvent
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.enums.event.EventFrequency
import org.dreamexposure.discal.core.extensions.discord4j.followupEphemeral
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.discord4j.hasControlRole
import org.dreamexposure.discal.core.extensions.discord4j.hasElevatedPermissions
import org.dreamexposure.discal.core.utils.ImageValidator
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class EventCommand(private val wizard: Wizard<PreEvent>) : SlashCommand {
    override val name = "event"
    override val ephemeral = true

    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return when (event.options[0].name) {
            "create" -> create(event, settings)
            "name" -> name(event, settings)
            "description" -> description(event, settings)
            "start" -> start(event, settings)
            "end" -> end(event, settings)
            "color" -> color(event, settings)
            "location" -> location(event, settings)
            "image" -> image(event, settings)
            "recur" -> recur(event, settings)
            "review" -> review(event, settings)
            "confirm" -> confirm(event, settings)
            "cancel" -> cancel(event, settings)
            "edit" -> edit(event, settings)
            "copy" -> copy(event, settings)
            "view" -> view(event, settings)
            "delete" -> delete(event, settings)
            else -> Mono.empty() // Never can reach this, makes compiler happy.
        }
    }

    private fun create(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val name = event.options[0].getOption("name")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("")

        val calendarNumber = event.options[0].getOption("calendar")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .orElse(1)

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            if (wizard.get(settings.guildID) == null) {
                event.interaction.guild.flatMap { guild ->
                    guild.getCalendar(calendarNumber).flatMap { cal ->
                        val pre = PreEvent.new(cal)
                        pre.name = name
                        wizard.start(pre)

                        event.followupEphemeral(getMessage("create.success", settings), EventEmbed.pre(guild, settings, pre))
                    }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
                }
            } else {
                event.interaction.guild
                        .map { EventEmbed.pre(it, settings, wizard.get(settings.guildID)!!) }
                        .flatMap { event.followupEphemeral(getMessage("error.wizard.started", settings)) }
            }

        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun name(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val name = event.options[0].getOption("name")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                pre.name = name
                event.interaction.guild
                        .map { EventEmbed.pre(it, settings, pre) }
                        .flatMap { event.followupEphemeral(getMessage("name.success", settings), it) }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun description(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val description = event.options[0].getOption("description")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                pre.description = description
                event.interaction.guild
                        .map { EventEmbed.pre(it, settings, pre) }
                        .flatMap { event.followupEphemeral(getMessage("description.success", settings), it) }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    @Suppress("DuplicatedCode")
    private fun start(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val year = event.options[0].getOption("year")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .get()
        val month = event.options[0].getOption("month")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .get()
        val day = event.options[0].getOption("day")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .get()
        val hour = event.options[0].getOption("hour")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .orElse(0)
        val minute = event.options[0].getOption("minute")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .orElse(0)

        TODO("Not yet implemented")
    }

    @Suppress("DuplicatedCode")
    private fun end(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val year = event.options[0].getOption("year")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .get()
        val month = event.options[0].getOption("month")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .get()
        val day = event.options[0].getOption("day")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .get()
        val hour = event.options[0].getOption("hour")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .orElse(0)
        val minute = event.options[0].getOption("minute")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .orElse(0)
        TODO("Not yet implemented")
    }

    private fun color(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val color = event.options[0].getOption("color")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .map(EventColor.Companion::fromId)
                .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                pre.color = color
                event.interaction.guild
                        .map { EventEmbed.pre(it, settings, pre) }
                        .flatMap { event.followupEphemeral(getMessage("color.success", settings), it) }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun location(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val location = event.options[0].getOption("location")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                pre.location = location
                event.interaction.guild
                        .map { EventEmbed.pre(it, settings, pre) }
                        .flatMap { event.followupEphemeral(getMessage("location.success", settings), it) }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun image(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val image = event.options[0].getOption("image")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                ImageValidator.validate(image, settings.patronGuild || settings.devGuild).flatMap { valid ->
                    if (valid) {
                        pre.image = image
                        event.interaction.guild
                                .map { EventEmbed.pre(it, settings, pre) }
                                .flatMap { event.followupEphemeral(getMessage("image.success", settings), it) }
                    } else {
                        event.interaction.guild
                                .map { EventEmbed.pre(it, settings, pre) }
                                .flatMap { event.followupEphemeral(getMessage("image.failure", settings), it) }
                    }
                }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun recur(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val shouldRecur = event.options[0].getOption("recur")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asBoolean)
                .orElse(true)
        val frequency = event.options[0].getOption("frequency")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .map(EventFrequency.Companion::fromValue)
                .orElse(EventFrequency.WEEKLY)
        val interval = event.options[0].getOption("interval")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .orElse(1)
        val count = event.options[0].getOption("count")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .orElse(-1)

        TODO("Not yet implemented")
    }

    private fun review(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasElevatedPermissions).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                event.interaction.guild.flatMap {
                    event.followupEphemeral(EventEmbed.pre(it, settings, pre))
                }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun confirm(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }

    private fun cancel(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasElevatedPermissions).flatMap {
            wizard.remove(settings.guildID)

            event.followupEphemeral(getMessage("cancel.success", settings))
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings)))
    }

    private fun edit(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val eventId = event.options[0].getOption("event")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .get()
        val calendarNumber = event.options[0].getOption("calendar")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .orElse(1)

        TODO("Not yet implemented")
    }

    private fun copy(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val eventId = event.options[0].getOption("event")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .get()
        val calendarNumber = event.options[0].getOption("calendar")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .orElse(1)
        val targetCalendarNumber = event.options[0].getOption("target")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .orElse(calendarNumber)


        TODO("Not yet implemented")
    }

    private fun view(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val eventId = event.options[0].getOption("event")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .get()
        val calendarNumber = event.options[0].getOption("calendar")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .orElse(1)

        TODO("Not yet implemented")
    }

    private fun delete(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val eventId = event.options[0].getOption("event")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .get()
        val calendarNumber = event.options[0].getOption("calendar")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .orElse(1)

        TODO("Not yet implemented")
    }
}
