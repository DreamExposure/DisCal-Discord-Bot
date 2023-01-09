package org.dreamexposure.discal.client.message.embed

import discord4j.core.`object`.entity.Guild
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Image
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.dreamexposure.discal.core.utils.getEmbedMessage

interface EmbedMaker {
    fun defaultBuilder(guild: Guild, settings: GuildSettings): EmbedCreateSpec.Builder {
        val builder = EmbedCreateSpec.builder()

        if (settings.branded)
            builder.author(guild.name, Config.URL_BASE.getString(), guild.getIconUrl(Image.Format.PNG).orElse(GlobalVal.iconUrl))
        else
            builder.author(getCommonMsg("bot.name", settings), Config.URL_BASE.getString(), GlobalVal.iconUrl)

        return builder
    }

    fun getMessage(embed: String, key: String, settings: GuildSettings, vararg args: String) =
            getEmbedMessage(embed, key, settings, *args)
}
