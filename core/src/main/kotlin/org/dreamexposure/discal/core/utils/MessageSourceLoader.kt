package org.dreamexposure.discal.core.utils

import org.dreamexposure.discal.core.`object`.GuildSettings
import org.springframework.context.support.ResourceBundleMessageSource
import java.nio.charset.StandardCharsets

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

fun getCommonMsg(key: String, settings: GuildSettings, vararg args: String): String {
    val src = MessageSourceLoader.getSourceByPath("common")

    return src.getMessage(key, args, settings.getLocale())
}
