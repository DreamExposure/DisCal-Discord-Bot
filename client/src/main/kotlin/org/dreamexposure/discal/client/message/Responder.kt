package org.dreamexposure.discal.client.message

import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.discordjson.json.MessageData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest
import reactor.core.publisher.Mono

object Responder {
    fun followup(event: InteractionCreateEvent, embed: EmbedCreateSpec): Mono<MessageData> {
        val spec = WebhookExecuteRequest.builder()
                .addEmbed(embed.asRequest())
                .build()

        return sendFollowup(event, spec)
    }

    fun followup(event: InteractionCreateEvent, message: String): Mono<MessageData> {
        val spec = WebhookExecuteRequest.builder()
                .content(message)
                .build()

        return sendFollowup(event, spec)
    }

    fun followup(event: InteractionCreateEvent, message: String, embed: EmbedCreateSpec): Mono<MessageData> {
        val spec = WebhookExecuteRequest.builder()
              .content(message)
              .addEmbed(embed.asRequest())
              .build()

        return sendFollowup(event, spec)
    }

    fun followupEphemeral(event: InteractionCreateEvent, embed: EmbedCreateSpec): Mono<MessageData> {
        val spec = WebhookExecuteRequest.builder()
              .addEmbed(embed.asRequest())
              .build()

        return sendFollowupEphemeral(event, spec)
    }

    fun followupEphemeral(event: InteractionCreateEvent, message: String): Mono<MessageData> {
        val spec = WebhookExecuteRequest.builder()
              .content(message)
              .build()

        return sendFollowupEphemeral(event, spec)
    }

    fun followupEphemeral(event: InteractionCreateEvent, message: String, embed: EmbedCreateSpec): Mono<MessageData> {
        val spec = WebhookExecuteRequest.builder()
              .content(message)
              .addEmbed(embed.asRequest())
              .build()

        return sendFollowupEphemeral(event, spec)
    }

    private fun sendFollowup(event: InteractionCreateEvent, request: WebhookExecuteRequest) =
          event.interactionResponse.createFollowupMessage(MultipartRequest.ofRequest(request))

    private fun sendFollowupEphemeral(event: InteractionCreateEvent, request: WebhookExecuteRequest) =
          event.interactionResponse.createFollowupMessageEphemeral(MultipartRequest.ofRequest(request))
}
