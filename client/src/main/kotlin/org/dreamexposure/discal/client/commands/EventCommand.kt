package org.dreamexposure.discal.client.commands

import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.dreamexposure.discal.core.`object`.GuildSettings
import reactor.core.publisher.Mono

class EventCommand: SlashCommand {
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
        TODO("Not yet implemented")
    }

    private fun name(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }

    private fun description(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }

    private fun start(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }

    private fun end(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }

    private fun color(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }

    private fun location(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }

    private fun image(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }

    private fun recur(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }

    private fun review(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }

    private fun confirm(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }

    private fun cancel(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }

    private fun edit(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }

    private fun copy(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }

    private fun view(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }

    private fun delete(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        TODO("Not yet implemented")
    }
}
