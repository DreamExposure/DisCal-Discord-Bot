package org.dreamexposure.discal.client.commands.premium

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.extensions.discord4j.canAddCalendar
import org.dreamexposure.discal.core.extensions.discord4j.followupEphemeral
import org.dreamexposure.discal.core.extensions.discord4j.hasElevatedPermissions
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AddCalCommand : SlashCommand {
    override val name = "addcal"
    override val hasSubcommands = false
    override val ephemeral = true

    @Deprecated("Use new handleSuspend for K-coroutines")
    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        //TODO: Remove dev-only and switch to patron-only once this is completed
        return if (settings.devGuild) {
            Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasElevatedPermissions).flatMap {
                //Check if a calendar can be added since non-premium only allows 1 calendar.
                event.interaction.guild.filterWhen(Guild::canAddCalendar).flatMap {
                    event.followupEphemeral(getMessage("response.start", settings, getLink(settings)))
                }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.calendar.max", settings)))
            }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings)))
        } else {
            event.followupEphemeral(getCommonMsg("error.disabled", settings))
        }
    }

    private fun getLink(settings: GuildSettings): String {
        return "${Config.URL_BASE.getString()}/dashboard/${settings.guildID.asString()}/calendar/new?type=1&step=0"
    }
}
