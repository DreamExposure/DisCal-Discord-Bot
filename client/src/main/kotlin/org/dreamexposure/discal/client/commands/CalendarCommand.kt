package org.dreamexposure.discal.client.commands

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.spec.InteractionReplyEditSpec
import org.dreamexposure.discal.client.message.embed.CalendarEmbed
import org.dreamexposure.discal.client.wizards.CalendarWizard
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.calendar.PreCalendar
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.extensions.discord4j.createCalendar
import org.dreamexposure.discal.core.extensions.discord4j.followup
import org.dreamexposure.discal.core.extensions.discord4j.followupEphemeral
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.TimeZoneUtils
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.function.TupleUtils
import java.time.ZoneId

//TODO: Add permissions checking for commands. forgot to do that so far
@Component
class CalendarCommand(val wizard: CalendarWizard) : SlashCommand {
    override val name = "calendar"
    override val ephemeral = true

    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
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

    //TODO: Check if guild can create a new calendar
    private fun create(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        val guildMono = event.interaction.guild

        val nameMono = Mono.justOrEmpty(event.options[0].getOption("name").flatMap { it.value })
                .map { it.asString() }

        val hostMono = Mono.justOrEmpty(event.options[0].getOption("host").flatMap { it.value })
                .map { CalendarHost.valueOf(it.asString()) }
                .defaultIfEmpty(CalendarHost.GOOGLE)

        return if (wizard.get(settings.guildID) == null) {
            //Start calendar wizard
            Mono.zip(guildMono, nameMono, hostMono)
                    .flatMap(TupleUtils.function { guild, name, host ->
                        val pre = PreCalendar.new(settings.guildID, host, name)
                        wizard.start(pre)

                        event.followup(getMessage("create.success", settings), CalendarEmbed.pre(guild, settings, pre))
                    }).then()
        } else {
            guildMono.flatMap {
                event.followupEphemeral(
                        getMessage("error.wizard.started", settings),
                        CalendarEmbed.pre(it, settings, wizard.get(settings.guildID)!!)
                )
            }.then()
        }
    }

    private fun name(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        val guildMono = event.interaction.guild

        val nameMono = Mono.justOrEmpty(event.options[0].getOption("name").flatMap { it.value })
                .map { it.asString() }

        val pre = wizard.get(settings.guildID)
        return if (pre != null) {
            Mono.zip(guildMono, nameMono).flatMap(TupleUtils.function { guild, name ->
                pre.name = name

                event.followupEphemeral(getMessage("name.success", settings), CalendarEmbed.pre(guild, settings, pre))
            }).then()
        } else {
            event.followupEphemeral(getMessage("error.wizard.notStarted", settings)).then();
        }
    }

    private fun description(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        val guildMono = event.interaction.guild

        val descMono = Mono.justOrEmpty(event.options[0].getOption("description").flatMap { it.value })
                .map { it.asString() }

        val pre = wizard.get(settings.guildID)
        return if (pre != null) {
            Mono.zip(guildMono, descMono).flatMap<Any>(TupleUtils.function { guild, desc ->
                pre.description = desc

                event.followupEphemeral(
                        getMessage("description.success", settings),
                        CalendarEmbed.pre(guild, settings, pre)
                )
            }).then()
        } else {
            event.followupEphemeral(getMessage("error.wizard.notStarted", settings)).then()
        }
    }

    private fun timezone(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        val tzMono = Mono.justOrEmpty(event.options[0].getOption("timezone").flatMap { it.value })
                .map { it.asString() }

        val pre = wizard.get(settings.guildID)
        return if (pre != null) {
            Mono.zip(event.interaction.guild, tzMono).flatMap(TupleUtils.function { guild, timezone ->
                if (TimeZoneUtils.isValid(timezone)) {
                    pre.timezone = ZoneId.of(timezone)

                    event.followupEphemeral(
                            getMessage("timezone.success", settings),
                            CalendarEmbed.pre(guild, settings, pre)
                    )
                } else {
                    event.followupEphemeral(getMessage("timezone.failure.invalid", settings))
                }
            }).then()
        } else {
            event.followupEphemeral(getMessage("error.wizard.notStarted", settings)).then()
        }
    }

    private fun review(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        val pre = wizard.get(settings.guildID)
        return if (pre != null) {
            event.interaction.guild.flatMap {
                event.followupEphemeral(CalendarEmbed.pre(it, settings, pre))
            }.then()
        } else {
            event.followupEphemeral(getMessage("error.wizard.notStarted", settings)).then()
        }
    }

    private fun confirm(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        val pre = wizard.get(settings.guildID)
        return if (pre != null) {
            if (!pre.hasRequiredValues()) {
                event.followupEphemeral(getMessage("confirm.failure.missing", settings))
            }

            event.editReply(InteractionReplyEditSpec.builder()
                    .contentOrNull(getMessage("confirm.pending", settings))
                    .build()
            ).then(event.interaction.guild.flatMap { guild ->
                if (!pre.editing) {
                    // New calendar
                    guild.createCalendar(pre.createSpec()).flatMap {
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
                    pre.calendar!!.update(pre.updateSpec()).flatMap { response ->
                        if (response.success) {
                            event.followupEphemeral(
                                    getMessage("confirm.success.edit", settings),
                                    CalendarEmbed.link(guild, settings, response.new!!)
                            )
                        } else {
                            event.followupEphemeral(getMessage("confirm.failure.edit", settings))
                        }
                    }
                }
            }).then()
        } else {
            event.followupEphemeral(getMessage("error.wizard.notStarted", settings)).then()
        }
    }

    private fun cancel(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        wizard.remove(settings.guildID)

        return event.followupEphemeral(getMessage("cancel.success", settings)).then();
    }

    private fun delete(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        val calNumMono = Mono.justOrEmpty(event.options[0].getOption("calendar").flatMap { it.value })
                .map { it.asLong().toInt() }
                .defaultIfEmpty(1)


        TODO("Not yet implemented")
    }

    private fun edit(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        //Determine which calendar they want to use...
        val calNumMono = Mono.justOrEmpty(event.options[0].getOption("calendar").flatMap { it.value })
                .map { it.asLong().toInt() }
                .defaultIfEmpty(1)

        TODO("Not yet implemented")
    }
}
