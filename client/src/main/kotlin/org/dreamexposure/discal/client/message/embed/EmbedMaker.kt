package org.dreamexposure.discal.client.message.embed

import discord4j.core.`object`.entity.Guild
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Image
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.MessageSourceLoader
import org.dreamexposure.discal.core.utils.getCommonMsg

interface EmbedMaker {
    fun defaultBuilder(guild: Guild, settings: GuildSettings): EmbedCreateSpec.Builder {
        val builder = EmbedCreateSpec.builder()

        if (settings.branded)
            builder.author(guild.name, BotSettings.BASE_URL.get(), guild.getIconUrl(Image.Format.PNG).orElse(GlobalVal.iconUrl))
        else
            builder.author(getCommonMsg("bot.name", settings), BotSettings.BASE_URL.get(), GlobalVal.iconUrl)

        return builder
    }

    fun getMessage(embed: String, key: String, settings: GuildSettings, vararg args: String): String {
        val src = MessageSourceLoader.getSourceByPath("embed/$embed")

        return src.getMessage(key, args, settings.getLocale())
    }
}
