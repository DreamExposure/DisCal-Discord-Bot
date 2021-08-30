package org.dreamexposure.discal.client.commands

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.event.domain.interaction.SlashCommandEvent
import org.dreamexposure.discal.client.message.Responder
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.extensions.discord4j.canAddCalendar
import org.dreamexposure.discal.core.extensions.discord4j.hasElevatedPermissions
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AddCalCommand : SlashCommand {
    override val name = "addcal"
    override val ephemeral = true

    override fun handle(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        //TODO: Remove dev-only and switch to patron-only once this is completed
        return if (settings.devGuild) {
            Mono.justOrEmpty(event.interaction.member)
                  .filterWhen(Member::hasElevatedPermissions).flatMap {
                      //Check if a calendar can be added since non-premium only allows 1 calendar.
                      event.interaction.guild.filterWhen(Guild::canAddCalendar).flatMap {
                          Responder.followupEphemeral(event, getMessage("response.start", settings, getLink(settings)))
                      }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.calendar.max", settings)))
                  }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.perms.elevated", settings)))
                  .then()
        } else {
            Responder.followupEphemeral(event, getCommonMsg("error.disabled", settings)).then()
        }
    }

    private fun getLink(settings: GuildSettings): String {
        return "${BotSettings.BASE_URL.get()}/dashboard/${settings.guildID.asString()}/calendar/new?type=1&step=0"
    }
}
