package org.dreamexposure.discal.core.extensions

import org.dreamexposure.discal.core.enums.time.BadTimezone
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.ImageValidator
import org.jsoup.Jsoup
import java.time.ZoneId

fun String.embedTitleSafe(): String = this.substring(0, (256).coerceAtMost(this.length))

fun String.embedDescriptionSafe(): String = this.substring(0, (4096).coerceAtMost(this.length))

fun String.embedFieldSafe(): String = this.substring(0, (1024).coerceAtMost(this.length))

fun String.messageContentSafe(): String = this.substring(0, (2000).coerceAtMost(this.length))

fun String.sanitize(): String = Jsoup.clean(this, GlobalVal.HTML_WHITELIST)

fun String.toMarkdown(): String = GlobalVal.MARKDOWN_CONVERTER.convert(this.sanitize()).unescapeNewLines()

fun String.unescapeNewLines(): String = this.replace(Regex("([\\\\n]+)([n])"), "\n").replace("=0D=0A", "\n")

fun String.isValidTimezone(): Boolean {
    return try {
        ZoneId.getAvailableZoneIds().contains(this) && !BadTimezone.isBad(this)
    } catch (ignore: Exception) {
        false
    }
}

fun String.isValidImage(allowGif: Boolean) = ImageValidator.validate(this, allowGif)

fun String.padCenter(length: Int, padChar: Char = ' '): String {
    if (this.length >= length) return this

    val chars: CharArray = this.toCharArray()
    val delta = length - chars.size
    val a = if (delta % 2 == 0) delta / 2 else delta / 2 + 1
    val b = a + chars.size

    val output = CharArray(length)
    for (i in 0 until length) {
        if (i < a) {
            output[i] = padChar
        } else if (i < b) {
            output[i] = chars[i - a]
        } else {
            output[i] = padChar
        }
    }
    return String(output)
}
