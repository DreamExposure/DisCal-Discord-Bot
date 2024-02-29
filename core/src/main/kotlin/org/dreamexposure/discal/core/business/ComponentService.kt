package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.Button
import discord4j.core.`object`.component.LayoutComponent
import discord4j.core.`object`.reaction.ReactionEmoji
import org.springframework.stereotype.Component

@Component
class ComponentService {

    fun getStaticMessageComponents(): Array<LayoutComponent> {
        val refreshButton = Button.secondary(
            "refresh-static-message",
            ReactionEmoji.custom(Snowflake.of(1175580426585247815), "refresh_ts", false)
        )

        return arrayOf(ActionRow.of(refreshButton))
    }
}
