package org.dreamexposure.discal.client.commands

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.spec.InteractionFollowupCreateSpec
import discord4j.rest.util.AllowedMentions
import org.dreamexposure.discal.client.message.embed.AnnouncementEmbed
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.Wizard
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.extensions.discord4j.*
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.function.TupleUtils

@Component
class AnnouncementCommand(val wizard: Wizard<Announcement>) : SlashCommand {
    override val name = "announcement"
    override val ephemeral = true

    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return when (event.options[0].name) {
            "create" -> create(event, settings)
            "type" -> type(event, settings)
            "event" -> event(event, settings)
            "color" -> color(event, settings)
            "channel" -> channel(event, settings)
            "minutes" -> minutes(event, settings)
            "hours" -> hours(event, settings)
            "info" -> info(event, settings)
            "calendar" -> calendar(event, settings)
            "publish" -> publish(event, settings)
            "review" -> review(event, settings)
            "confirm" -> confirm(event, settings)
            "cancel" -> cancel(event, settings)
            "edit" -> edit(event, settings)
            "copy" -> copy(event, settings)
            "delete" -> delete(event, settings)
            "enable" -> enable(event, settings)
            "view" -> view(event, settings)
            "list" -> list(event, settings)
            "subscribe" -> subscribe(event, settings)
            "unsubscribe" -> unsubscribe(event, settings)
            else -> Mono.empty() // Never can reach this, makes compiler happy.
        }
    }

    private fun create(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val type = event.options[0].getOption("type")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .map(AnnouncementType.Companion::fromValue)
            .orElse(AnnouncementType.UNIVERSAL)

        val channelMono = event.options[0].getOption("channel")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asChannel)
            .map { it.ofType(MessageChannel::class.java) }
            .orElse(event.interaction.channel)

        val minutes = event.options[0].getOption("minutes")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(0)

        val hours = event.options[0].getOption("hours")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(0)

        val calendar = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            if (wizard.get(settings.guildID) == null) {
                channelMono.flatMap { channel ->
                    val pre = Announcement(settings.guildID)
                    pre.type = type
                    pre.announcementChannelId = channel.id.asString()
                    pre.minutesBefore = minutes
                    pre.hoursBefore = hours
                    pre.calendarNumber = calendar
                    wizard.start(pre)

                    event.interaction.guild
                        .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                        .flatMap { event.followupEphemeral(getMessage("create.success", settings), it) }
                }
            } else {
                event.interaction.guild
                    .flatMap { AnnouncementEmbed.pre(it, wizard.get(settings.guildID)!!, settings) }
                    .flatMap { event.followup(getMessage("error.wizard.started", settings), it) }
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun type(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val type = event.options[0].getOption("type")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .map(AnnouncementType.Companion::fromValue)
            .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                pre.type = type
                event.interaction.guild
                    .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                    .flatMap { event.followupEphemeral(getMessage("type.success", settings), it) }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun event(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                if (pre.type == AnnouncementType.RECUR || pre.type == AnnouncementType.SPECIFIC) {
                    event.interaction.guild
                        .flatMap { it.getCalendar(pre.calendarNumber) }
                        .flatMap { it.getEvent(eventId) }
                        .flatMap { calEvent ->
                            if (pre.type == AnnouncementType.RECUR) pre.eventId = calEvent.eventId.split("_")[0]
                            else pre.eventId = eventId

                            event.interaction.guild
                                .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                                .flatMap { event.followupEphemeral(getMessage("event.success", settings), it) }
                        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.event", settings)))
                } else {
                    event.interaction.guild
                        .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                        .flatMap { event.followupEphemeral(getMessage("event.failure.type", settings), it) }
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
                if (pre.type == AnnouncementType.COLOR) {
                    pre.eventColor = color

                    event.interaction.guild
                        .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                        .flatMap { event.followupEphemeral(getMessage("color.success", settings), it) }
                } else {
                    event.interaction.guild
                        .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                        .flatMap { event.followupEphemeral(getMessage("color.failure.type", settings), it) }
                }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun channel(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val channelMono = event.options[0].getOption("channel")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asChannel)
            .map { it.ofType(MessageChannel::class.java) }
            .orElse(event.interaction.channel)

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                channelMono.flatMap { channel ->
                    pre.announcementChannelId = channel.id.asString()

                    event.interaction.guild
                        .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                        .flatMap { event.followupEphemeral(getMessage("channel.success", settings), it) }
                }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun minutes(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val minutes = event.options[0].getOption("minutes")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                pre.minutesBefore = minutes
                event.interaction.guild
                    .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                    .flatMap { event.followupEphemeral(getMessage("minutes.success", settings), it) }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun hours(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val hours = event.options[0].getOption("hours")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(0)

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                pre.hoursBefore = hours
                event.interaction.guild
                    .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                    .flatMap { event.followupEphemeral(getMessage("hours.success", settings), it) }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun info(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val info = event.options[0].getOption("info")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse("None")

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                pre.info = info
                event.interaction.guild
                    .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                    .flatMap { event.followupEphemeral(getMessage("info.success.set", settings), it) }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun calendar(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val calendar = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                pre.calendarNumber = calendar
                event.interaction.guild
                    .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                    .flatMap { event.followupEphemeral(getMessage("calendar.success", settings), it) }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun publish(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val publish = event.options[0].getOption("publish")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                if (settings.patronGuild) {
                    pre.publish = publish
                    event.interaction.guild
                        .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                        .flatMap { event.followupEphemeral(getMessage("publish.success", settings), it) }
                } else {
                    event.interaction.guild
                        .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                        .flatMap { event.followupEphemeral(getMessage("error.patronOnly", settings), it) }
                }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun review(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                event.interaction.guild
                    .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                    .flatMap { event.followupEphemeral(it) }
            } else {
                event.followupEphemeral(getMessage("error.wizard.notStarted", settings))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun confirm(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            val pre = wizard.get(settings.guildID)
            if (pre != null) {
                if (pre.hasRequiredValues()) {
                    DatabaseManager.updateAnnouncement(pre).flatMap { success ->
                        if (success) {
                            val msg = if (pre.editing) getMessage("confirm.success.edit", settings)
                            else getMessage("confirm.success.create", settings)

                            event.interaction.guild.flatMap { AnnouncementEmbed.view(pre, it) }.flatMap { embed ->
                                event.interaction.channel.flatMap {
                                    it.createMessage(msg).withEmbeds(embed)
                                        .then(event.followupEphemeral(getCommonMsg("success.generic", settings)))
                                }
                            }
                        } else {
                            val msg = if (pre.editing) getMessage("confirm.failure.edit", settings)
                            else getMessage("confirm.failure.create", settings)

                            event.interaction.guild
                                .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                                .flatMap { event.followupEphemeral(msg, it) }
                        }
                    }
                } else {
                    event.interaction.guild
                        .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                        .flatMap { event.followupEphemeral(getMessage("confirm.failure.missing", settings), it) }
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
        val announcementId = event.options[0].getOption("announcement")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            if (wizard.get(settings.guildID) == null) {
                DatabaseManager.getAnnouncement(announcementId, settings.guildID).flatMap { ann ->
                    val pre = ann.copy(editing = true)
                    wizard.start(pre)

                    event.interaction.guild
                        .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                        .flatMap { event.followupEphemeral(getMessage("edit.success", settings), it) }
                }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.announcement", settings)))
            } else {
                event.interaction.guild
                    .flatMap { AnnouncementEmbed.pre(it, wizard.get(settings.guildID)!!, settings) }
                    .flatMap { event.followup(getMessage("error.wizard.started", settings), it) }
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun copy(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val announcementId = event.options[0].getOption("announcement")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            if (wizard.get(settings.guildID) == null) {
                DatabaseManager.getAnnouncement(announcementId, settings.guildID).flatMap { ann ->
                    val pre = ann.copy(id = KeyGenerator.generateAnnouncementId())
                    wizard.start(pre)

                    event.interaction.guild
                        .flatMap { AnnouncementEmbed.pre(it, pre, settings) }
                        .flatMap { event.followupEphemeral(getMessage("copy.success", settings), it) }
                }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.announcement", settings)))
            } else {
                event.interaction.guild
                    .flatMap { AnnouncementEmbed.pre(it, wizard.get(settings.guildID)!!, settings) }
                    .flatMap { event.followup(getMessage("error.wizard.started", settings), it) }
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun delete(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val announcementId = event.options[0].getOption("announcement")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            // Before we delete the announcement, if the wizard is editing it, we need to cancel the wizard
            val pre = wizard.get(settings.guildID)
            if (pre != null && pre.id == announcementId) wizard.remove(settings.guildID)

            DatabaseManager.getAnnouncement(announcementId, settings.guildID).flatMap { announcement ->
                DatabaseManager.deleteAnnouncement(announcement.id)
                    .then(event.followupEphemeral(getMessage("delete.success", settings)))
            }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.announcement", settings)))
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun enable(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val announcementId = event.options[0].getOption("announcement")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val enabled = event.options[0].getOption("enabled")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .get()

        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasControlRole).flatMap {
            DatabaseManager.getAnnouncement(announcementId, settings.guildID).flatMap { announcement ->
                announcement.enabled = enabled

                DatabaseManager.updateAnnouncement(announcement).flatMap {
                    if (enabled) {
                        event.interaction.guild
                            .flatMap { AnnouncementEmbed.view(announcement, it) }
                            .flatMap { event.followupEphemeral(getMessage("enable.success", settings), it) }
                    } else {
                        event.interaction.guild
                            .flatMap { AnnouncementEmbed.view(announcement, it) }
                            .flatMap { event.followupEphemeral(getMessage("disable.success", settings), it) }
                    }
                }
            }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.announcement", settings)))
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun view(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val announcementId = event.options[0].getOption("announcement")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return DatabaseManager.getAnnouncement(announcementId, settings.guildID).flatMap { announcement ->
            val guildMono = event.interaction.guild.cache()
            val embedMono = guildMono.flatMap { AnnouncementEmbed.view(announcement, it) }
            val subscribersMono = guildMono.flatMap { it.buildMentions(announcement) }

            Mono.zip(embedMono, subscribersMono).flatMap(TupleUtils.function { embed, subs ->
                event.createFollowup(InteractionFollowupCreateSpec.builder()
                    .content(subs)
                    .addEmbed(embed)
                    .allowedMentions(AllowedMentions.suppressAll())
                    .build()
                )
            })
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.announcement", settings)))
    }

    private fun list(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val amount = event.options[0].getOption("amount")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .get()

        val calendar = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val showDisabled = event.options[0].getOption("show-disabled")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .orElse(false)

        val type = event.options[0].getOption("type")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .map(AnnouncementType.Companion::fromValue)

        // Determine which db query to use.
        val announcementsMono = if (!showDisabled) {
            if (type.isPresent) {
                DatabaseManager.getEnabledAnnouncements(settings.guildID, type.get())
            } else {
                DatabaseManager.getEnabledAnnouncements(settings.guildID)
            }
        } else {
            if (type.isPresent) {
                DatabaseManager.getAnnouncements(settings.guildID, type.get())
            } else {
                DatabaseManager.getAnnouncements(settings.guildID)
            }
        }

        return announcementsMono.map { it.filter { a -> a.calendarNumber == calendar } }.flatMap { announcements ->
            if (announcements.isEmpty()) {
                event.followupEphemeral(getMessage("list.success.none", settings))
            } else if (announcements.size == 1) {
                event.interaction.guild.flatMap { AnnouncementEmbed.view(announcements[0], it) }.flatMap {
                    event.createFollowup(InteractionFollowupCreateSpec.builder()
                        .content(getMessage("list.success.one", settings))
                        .addEmbed(it)
                        .allowedMentions(AllowedMentions.suppressAll())
                        .build()
                    )
                }
            } else {
                val limit = if (amount > 0) amount.coerceAtMost(announcements.size) else announcements.size
                val guildMono = event.interaction.guild.cache()

                val successMessage = event.followupEphemeral(getMessage("list.success.many", settings, "$limit"))
                val condAns = guildMono.flatMapMany { guild ->
                    Flux.fromIterable(announcements.subList(0, limit)).flatMap { a ->
                        AnnouncementEmbed.condensed(a, guild).flatMap(event::followupEphemeral)
                    }
                }

                successMessage.then(condAns.last())
            }
        }
    }

    private fun subscribe(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val announcementId = event.options[0].getOption("announcement")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val subId = event.options[0].getOption("sub")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asSnowflake)

        return DatabaseManager.getAnnouncement(announcementId, settings.guildID).flatMap { announcement ->
            event.interaction.guild.flatMap { guild ->
                if (subId.isPresent) {
                    val memberMono = guild.getMemberById(subId.get()).onErrorResume { Mono.empty() }
                    val roleMono = guild.getRoleById(subId.get()).onErrorResume { Mono.empty() }

                    memberMono.flatMap { member ->
                        announcement.subscriberUserIds.remove(member.id.asString())
                        announcement.subscriberUserIds.add(member.id.asString())

                        DatabaseManager.updateAnnouncement(announcement).flatMap {
                            AnnouncementEmbed.view(announcement, guild).flatMap { embed ->
                                event.createFollowup(InteractionFollowupCreateSpec.builder()
                                    .content(getMessage("subscribe.success.other", settings, member.nicknameMention))
                                    .addEmbed(embed)
                                    .allowedMentions(AllowedMentions.suppressAll())
                                    .build()
                                )
                            }
                        }
                    }.switchIfEmpty(roleMono.flatMap { role ->
                        announcement.subscriberRoleIds.remove(role.id.asString())
                        announcement.subscriberRoleIds.add(role.id.asString())

                        DatabaseManager.updateAnnouncement(announcement).flatMap {
                            AnnouncementEmbed.view(announcement, guild).flatMap { embed ->
                                event.createFollowup(InteractionFollowupCreateSpec.builder()
                                    .content(getMessage("subscribe.success.other", settings, role.mention))
                                    .addEmbed(embed)
                                    .allowedMentions(AllowedMentions.suppressAll())
                                    .build()
                                )
                            }
                        }
                    })
                } else {
                    announcement.subscriberUserIds.remove(event.interaction.user.id.asString())
                    announcement.subscriberUserIds.add(event.interaction.user.id.asString())

                    DatabaseManager.updateAnnouncement(announcement).flatMap {
                        AnnouncementEmbed.view(announcement, guild).flatMap { embed ->
                            event.followupEphemeral(getMessage("subscribe.success.self", settings), embed)
                        }
                    }
                }
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.announcement", settings)))
    }

    private fun unsubscribe(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val announcementId = event.options[0].getOption("announcement")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val subId = event.options[0].getOption("sub")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asSnowflake)

        return DatabaseManager.getAnnouncement(announcementId, settings.guildID).flatMap { announcement ->
            event.interaction.guild.flatMap { guild ->
                if (subId.isPresent) {
                    val memberMono = guild.getMemberById(subId.get()).onErrorResume { Mono.empty() }
                    val roleMono = guild.getRoleById(subId.get()).onErrorResume { Mono.empty() }

                    memberMono.flatMap { member ->
                        announcement.subscriberUserIds.remove(member.id.asString())

                        DatabaseManager.updateAnnouncement(announcement).flatMap {
                            AnnouncementEmbed.view(announcement, guild).flatMap { embed ->
                                event.createFollowup(InteractionFollowupCreateSpec.builder()
                                    .content(getMessage("unsubscribe.success.other", settings, member.nicknameMention))
                                    .addEmbed(embed)
                                    .allowedMentions(AllowedMentions.suppressAll())
                                    .build()
                                )
                            }
                        }
                    }.switchIfEmpty(roleMono.flatMap { role ->
                        announcement.subscriberRoleIds.remove(role.id.asString())

                        DatabaseManager.updateAnnouncement(announcement).flatMap {
                            AnnouncementEmbed.view(announcement, guild).flatMap { embed ->
                                event.createFollowup(InteractionFollowupCreateSpec.builder()
                                    .content(getMessage("unsubscribe.success.other", settings, role.mention))
                                    .addEmbed(embed)
                                    .allowedMentions(AllowedMentions.suppressAll())
                                    .build()
                                )
                            }
                        }
                    })
                } else {
                    announcement.subscriberUserIds.remove(event.interaction.user.id.asString())

                    DatabaseManager.updateAnnouncement(announcement).flatMap {
                        AnnouncementEmbed.view(announcement, guild).flatMap { embed ->
                            event.followupEphemeral(getMessage("unsubscribe.success.self", settings), embed)
                        }
                    }
                }
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.announcement", settings)))
    }
}
