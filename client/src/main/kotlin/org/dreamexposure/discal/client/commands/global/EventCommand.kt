package org.dreamexposure.discal.client.commands.global

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.spec.MessageCreateSpec
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.client.message.embed.EventEmbed
import org.dreamexposure.discal.client.service.StaticMessageService
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.entities.response.UpdateEventResponse
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.enums.event.EventFrequency
import org.dreamexposure.discal.core.extensions.discord4j.followupEphemeral
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.discord4j.hasControlRole
import org.dreamexposure.discal.core.extensions.isValidImage
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.Wizard
import org.dreamexposure.discal.core.`object`.event.PreEvent
import org.dreamexposure.discal.core.`object`.event.Recurrence
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.*
import java.time.temporal.ChronoUnit

@Suppress("DuplicatedCode")
@Component
class EventCommand(val wizard: Wizard<PreEvent>, val staticMessageSrv: StaticMessageService) : SlashCommand {
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

        val description = event.options[0].getOption("description")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse("")

        val location = event.options[0].getOption("location")
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
                        pre.description = description
                        pre.location = location
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
                .filter { !it.equals("N/a") || !it.equals("None") }
                .orElse("")


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
                .filter { !it.equals("N/a") || !it.equals("None") }
                .orElse("")


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

    private fun start(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val year = event.options[0].getOption("year")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .map { it.coerceAtLeast(Year.MIN_VALUE).coerceAtMost(Year.MAX_VALUE) }
                .get()
        val month = event.options[0].getOption("month")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .map(Month::of)
                .get()
        val day = event.options[0].getOption("day")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .map { it.coerceAtLeast(1).coerceAtMost(month.maxLength()) }
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
                .map { it.coerceAtLeast(0).coerceAtMost(59) }
                .orElse(0)

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                //Build date time object
                val start = ZonedDateTime.of(
                        LocalDateTime.of(year, month, day, hour, minute),
                        pre.timezone
                ).toInstant()

                if (pre.end == null) {
                    pre.start = start
                    pre.end = start.plus(1, ChronoUnit.HOURS) // Add default end time to 1 hour after start.
                    if (pre.start!!.isAfter(Instant.now())) {
                        event.interaction.guild
                                .map { EventEmbed.pre(it, settings, pre) }
                                .flatMap { event.followupEphemeral(getMessage("start.success", settings), it) }
                    } else {
                        // scheduled for the past, allow but add a warning.
                        event.interaction.guild
                                .map { EventEmbed.pre(it, settings, pre) }
                                .flatMap { event.followupEphemeral(getMessage("start.success.past", settings), it) }
                    }
                } else {
                    // Event end already set, make sure everything is in order
                    if (pre.end!!.isAfter(start)) {
                        pre.start = start
                        if (pre.start!!.isAfter(Instant.now())) {
                            event.interaction.guild
                                    .map { EventEmbed.pre(it, settings, pre) }
                                    .flatMap { event.followupEphemeral(getMessage("start.success", settings), it) }
                        } else {
                            // scheduled for the past, allow but add a warning.
                            event.interaction.guild
                                    .map { EventEmbed.pre(it, settings, pre) }
                                    .flatMap { event.followupEphemeral(getMessage("start.success.past", settings), it) }
                        }
                    } else {
                        // Event end cannot be before event start
                        event.interaction.guild
                                .map { EventEmbed.pre(it, settings, pre) }
                                .flatMap { event.followupEphemeral(getMessage("start.failure.afterEnd", settings), it) }
                    }
                }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun end(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val year = event.options[0].getOption("year")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .map { it.coerceAtLeast(Year.MIN_VALUE).coerceAtMost(Year.MAX_VALUE) }
                .get()
        val month = event.options[0].getOption("month")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .map(Month::of)
                .get()
        val day = event.options[0].getOption("day")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .map { it.coerceAtLeast(1).coerceAtMost(month.maxLength()) }
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
                .map { it.coerceAtLeast(0).coerceAtMost(59) }
                .orElse(0)

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                //Build date time object
                val end = ZonedDateTime.of(
                        LocalDateTime.of(year, month, day, hour, minute),
                        pre.timezone
                ).toInstant()

                if (pre.start == null) {
                    pre.end = end
                    pre.start = end.minus(1, ChronoUnit.HOURS) // Add default start time to 1 hour before end.
                    if (pre.end!!.isAfter(Instant.now())) {
                        event.interaction.guild
                                .map { EventEmbed.pre(it, settings, pre) }
                                .flatMap { event.followupEphemeral(getMessage("end.success", settings), it) }
                    } else {
                        // scheduled for the past, allow but add a warning.
                        event.interaction.guild
                                .map { EventEmbed.pre(it, settings, pre) }
                                .flatMap { event.followupEphemeral(getMessage("end.success.past", settings), it) }
                    }
                } else {
                    // Event start already set, make sure everything is in order
                    if (pre.start!!.isBefore(end)) {
                        pre.end = end
                        if (pre.end!!.isAfter(Instant.now())) {
                            event.interaction.guild
                                    .map { EventEmbed.pre(it, settings, pre) }
                                    .flatMap { event.followupEphemeral(getMessage("end.success", settings), it) }
                        } else {
                            // scheduled for the past, allow but add a warning.
                            event.interaction.guild
                                    .map { EventEmbed.pre(it, settings, pre) }
                                    .flatMap { event.followupEphemeral(getMessage("end.success.past", settings), it) }
                        }
                    } else {
                        // Event start cannot be after event end
                        event.interaction.guild
                                .map { EventEmbed.pre(it, settings, pre) }
                                .flatMap { event.followupEphemeral(getMessage("end.failure.beforeStart", settings), it) }
                    }
                }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
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
                .filter { !it.equals("N/a") || !it.equals("None") }
                .orElse("")

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
                Mono.just(image).filterWhen { it.isValidImage(settings.patronGuild || settings.devGuild) }.flatMap {
                    pre.image = image
                    event.interaction.guild
                            .map { EventEmbed.pre(it, settings, pre) }
                            .flatMap { event.followupEphemeral(getMessage("image.success", settings), it) }
                }.switchIfEmpty(event.interaction.guild
                        .map { EventEmbed.pre(it, settings, pre) }
                        .flatMap { event.followupEphemeral(getMessage("image.failure", settings), it) }
                )
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

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                if (shouldRecur) {
                    pre.recurrence = Recurrence(frequency, interval, count)
                    event.interaction.guild
                            .map { EventEmbed.pre(it, settings, pre) }
                            .flatMap { event.followupEphemeral(getMessage("recur.success.enable", settings), it) }
                } else {
                    pre.recurrence = null
                    event.interaction.guild
                            .map { EventEmbed.pre(it, settings, pre) }
                            .flatMap { event.followupEphemeral(getMessage("recur.success.disable", settings), it) }
                }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStart", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun review(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
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
        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                if (!pre.hasRequiredValues()) {
                    return@flatMap event.followupEphemeral(getMessage("confirm.failure.missing", settings))
                }

                event.interaction.guild.flatMap { guild ->
                    if (!pre.editing) {
                        // New event
                        guild.getCalendar(pre.calNumber)
                                .flatMap { it.createEvent(pre.createSpec()) }
                                .doOnNext { wizard.remove(settings.guildID) }
                                .flatMap { calEvent ->
                                    val updateMessages = staticMessageSrv.updateStaticMessages(
                                            guild,
                                            calEvent.calendar,
                                            settings
                                    )
                                    val embedMono = event.interaction.channel.flatMap {
                                        val spec = MessageCreateSpec.builder()
                                                .content(getMessage("confirm.success.create", settings))
                                                .addEmbed(EventEmbed.getFull(guild, settings, calEvent))
                                                .build()

                                        it.createMessage(spec)
                                    }
                                    val followupMono = event.followupEphemeral(getCommonMsg("success.generic", settings))

                                    embedMono.then(followupMono).flatMap { updateMessages.thenReturn(it) }
                                }.doOnError {
                                    LOGGER.error("Create event with command failure", it)
                                }.onErrorResume {
                                    event.followupEphemeral(getMessage("confirm.failure.create", settings))
                                }.switchIfEmpty(event.followupEphemeral(getMessage("confirm.failure.create", settings)))
                    } else {
                        // Editing
                        pre.event!!.update(pre.updateSpec())
                                .filter(UpdateEventResponse::success)
                                .doOnNext { wizard.remove(settings.guildID) }
                                .flatMap { uer ->
                                    val updateMessages = staticMessageSrv.updateStaticMessages(
                                            guild,
                                            uer.new!!.calendar,
                                            settings
                                    )
                                    val embedMono = event.interaction.channel.flatMap {
                                        val spec = MessageCreateSpec.builder()
                                                .content(getMessage("confirm.success.edit", settings))
                                                .addEmbed(EventEmbed.getFull(guild, settings, uer.new!!))
                                                .build()

                                        it.createMessage(spec)
                                    }
                                    val followupMono = event.followupEphemeral(getCommonMsg("success.generic", settings))

                                    embedMono.then(followupMono).flatMap { updateMessages.thenReturn(it) }
                                }
                                .switchIfEmpty(event.followupEphemeral(getMessage("confirm.failure.edit", settings)))
                    }
                }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun cancel(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            wizard.remove(settings.guildID)

            event.followupEphemeral(getMessage("cancel.success", settings))
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
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

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            if (wizard.get(settings.guildID) == null) {
                event.interaction.guild.flatMap { guild ->
                    guild.getCalendar(calendarNumber).flatMap { calendar ->
                        calendar.getEvent(eventId)
                                .map { PreEvent.edit(it) }
                                .doOnNext { wizard.start(it) }
                                .map { EventEmbed.pre(guild, settings, it) }
                                .flatMap { event.followupEphemeral(getMessage("edit.success", settings), it) }
                                .switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.event", settings)))
                    }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
                }
            } else {
                event.interaction.guild
                        .map { EventEmbed.pre(it, settings, wizard.get(settings.guildID)!!) }
                        .flatMap { event.followupEphemeral(getMessage("error.wizard.started", settings), it) }
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
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

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            if (wizard.get(settings.guildID) == null) {
                event.interaction.guild.flatMap { guild ->
                    guild.getCalendar(calendarNumber).flatMap { calendar ->
                        calendar.getEvent(eventId)
                                .flatMap { PreEvent.copy(guild, it, targetCalendarNumber) }
                                .doOnNext { wizard.start(it) }
                                .map { EventEmbed.pre(guild, settings, it) }
                                .flatMap { event.followupEphemeral(getMessage("copy.success", settings), it) }
                                .switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.event", settings)))
                    }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
                }
            } else {
                event.interaction.guild
                        .map { EventEmbed.pre(it, settings, wizard.get(settings.guildID)!!) }
                        .flatMap { event.followupEphemeral(getMessage("error.wizard.started", settings), it) }
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
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

        return event.interaction.guild.flatMap { guild ->
            guild.getCalendar(calendarNumber).flatMap { calendar ->
                calendar.getEvent(eventId).flatMap { calEvent ->
                    event.interaction.channel.flatMap {
                        // Create message so others can see
                        event.followupEphemeral(getMessage("view.success", settings)).then(
                                it.createMessage(EventEmbed.getFull(guild, settings, calEvent))
                        )
                    }
                }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
            }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
        }
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

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            // Before we delete the event, if the wizard is editing that event we need to cancel the wizard
            val pre = wizard.get(settings.guildID)
            if (pre != null && pre.event?.eventId == eventId) wizard.remove(settings.guildID)

            event.interaction.guild.flatMap { it.getCalendar(calendarNumber) }.flatMap { calendar ->
                calendar.getEvent(eventId)
                        .flatMap(Event::delete)
                        .flatMap { event.followupEphemeral(getMessage("delete.success", settings)) }
                        .flatMap { staticMessageSrv.updateStaticMessage(calendar, settings).thenReturn(it) }
                        .switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.event", settings)))
            }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }
}
