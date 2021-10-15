package org.dreamexposure.discal.core.extensions

import org.dreamexposure.discal.core.enums.time.BadTimezone
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.ImageValidator
import org.jsoup.Jsoup
import java.time.ZoneId

fun String.sanitize(): String {
    return Jsoup.clean(this, GlobalVal.HTML_WHITELIST)
}

fun String.toMarkdown(): String {
    return GlobalVal.MARKDOWN_CONVERTER.convert(this.sanitize())
}

fun String.isValidTimezone(): Boolean {
    return try {
        ZoneId.getAvailableZoneIds().contains(this) && !BadTimezone.isBad(this)
    } catch (ignore: Exception) {
        false
    }
}

fun String.isValidImage(allowGif: Boolean) = ImageValidator.validate(this, allowGif)
