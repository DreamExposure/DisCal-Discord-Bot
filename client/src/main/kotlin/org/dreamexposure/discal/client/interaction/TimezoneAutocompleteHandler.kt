package org.dreamexposure.discal.client.interaction

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.enums.time.GoodTimezone
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.springframework.stereotype.Component

@Component
class TimezoneAutocompleteHandler: InteractionHandler<ChatInputAutoCompleteEvent> {
    override val ids: Array<String> = arrayOf("calendar.timezone")
    override val ephemeral = true

    private val allTimezonesFormatted =  GoodTimezone.entries
        .map(GoodTimezone::name)
        .map { it.replace("___", "/") }

    override suspend fun handle(event: ChatInputAutoCompleteEvent, settings: GuildSettings) {
        val input = event.focusedOption.value
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val toSendMatching = allTimezonesFormatted
            .filter { it.contains(input, ignoreCase = true) }
            .ifEmpty { allTimezonesFormatted }

        val toSendActual = toSendMatching
            .subList(0, 25.coerceAtMost(toSendMatching.size))
            .map {
                ApplicationCommandOptionChoiceData.builder()
                    .name(it)
                    .value(it)
                    .build()
            }

        event.respondWithSuggestions(toSendActual).awaitSingleOrNull()

    }
}
