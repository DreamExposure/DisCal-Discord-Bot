package org.dreamexposure.discal.core.utils

import org.dreamexposure.discal.core.`object`.GuildSettings
import org.springframework.context.support.ResourceBundleMessageSource
import java.nio.charset.StandardCharsets
import java.util.*

object MessageSourceLoader {
    private val sources: MutableMap<String, ResourceBundleMessageSource> = mutableMapOf()

    fun getSourceByPath(path: String): ResourceBundleMessageSource {
        val fullPath = "i18n/$path"

        return if (sources.containsKey(fullPath))
            sources.getValue(fullPath)
        else {
            val src = ResourceBundleMessageSource()
            src.setBasename(fullPath)

            src.setFallbackToSystemLocale(false)
            src.setAlwaysUseMessageFormat(true)
            src.setDefaultEncoding(StandardCharsets.UTF_8.name())
            sources[fullPath] = src

            src
        }
    }
}

//FIXME: I think the varargs is bugging out with 3 or more provided. Will need to debug this later, but not today.

fun getCommonMsg(key: String, settings: GuildSettings, vararg args: String) = getCommonMsg(key, settings.getLocale(), *args)

fun getCommonMsg(key: String, locale: Locale, vararg args: String): String {
    val src= MessageSourceLoader.getSourceByPath("common")

    return src.getMessage(key, args, locale)
}

fun getEmbedMessage(embed: String, key: String, settings: GuildSettings, vararg args: String): String {
    val src = MessageSourceLoader.getSourceByPath("embed/$embed")

    return src.getMessage(key, args, settings.getLocale())
}

fun getCmdMessage(cmd: String, key: String, settings: GuildSettings, vararg args: String): String {
    val src = MessageSourceLoader.getSourceByPath("command/$cmd/$cmd")

    return src.getMessage(key, args, settings.getLocale())
}
