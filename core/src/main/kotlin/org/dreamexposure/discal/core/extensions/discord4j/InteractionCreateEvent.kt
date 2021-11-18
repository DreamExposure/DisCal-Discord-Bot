package org.dreamexposure.discal.core.extensions.discord4j

import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionFollowupCreateSpec
import reactor.core.publisher.Mono

fun DeferrableInteractionEvent.followup(embed: EmbedCreateSpec): Mono<Message> {
    val spec = InteractionFollowupCreateSpec.builder()
        .addEmbed(embed)
        .build()

    return this.createFollowup(spec)
}

fun DeferrableInteractionEvent.followup(message: String): Mono<Message> {
    val spec = InteractionFollowupCreateSpec.builder()
        .content(message)
        .build()

    return this.createFollowup(spec)
}

fun DeferrableInteractionEvent.followup(message: String, embed: EmbedCreateSpec): Mono<Message> {
    val spec = InteractionFollowupCreateSpec.builder()
        .content(message)
        .addEmbed(embed)
        .build()

    return this.createFollowup(spec)
}

fun DeferrableInteractionEvent.followupEphemeral(embed: EmbedCreateSpec): Mono<Message> {
    val spec = InteractionFollowupCreateSpec.builder()
        .addEmbed(embed)
        .ephemeral(true)
        .build()

    return this.createFollowup(spec)
}

fun DeferrableInteractionEvent.followupEphemeral(message: String): Mono<Message> {
    val spec = InteractionFollowupCreateSpec.builder()
        .content(message)
        .ephemeral(true)
        .build()

    return this.createFollowup(spec)
}

fun DeferrableInteractionEvent.followupEphemeral(message: String, embed: EmbedCreateSpec): Mono<Message> {
    val spec = InteractionFollowupCreateSpec.builder()
        .content(message)
        .addEmbed(embed)
        .ephemeral(true)
        .build()

    return this.createFollowup(spec)
}
