package org.dreamexposure.discal.core.extensions

import org.dreamexposure.discal.core.enums.time.BadTimezone
import org.dreamexposure.discal.core.utils.GlobalVal
import org.jsoup.Jsoup
import java.time.ZoneId
import java.util.*

fun String.embedTitleSafe(additionalTrim: Int  = 0): String = this.substring(0, (256 - additionalTrim).coerceAtMost(this.length))

fun String.embedDescriptionSafe(additionalTrim: Int  = 0): String = this.substring(0, (4096 - additionalTrim).coerceAtMost(this.length))

fun String.embedFieldSafe(additionalTrim: Int  = 0): String = this.substring(0, (1024 - additionalTrim).coerceAtMost(this.length))

fun String.messageContentSafe(additionalTrim: Int  = 0): String = this.substring(0, (2000 - additionalTrim).coerceAtMost(this.length))

fun String.autocompleteSafe(additionalTrim: Int  = 0): String = this.substring(0, (100 - additionalTrim).coerceAtMost(this.length))

fun String.sanitize(): String = Jsoup.clean(this, GlobalVal.HTML_WHITELIST)

fun String.toMarkdown(): String = GlobalVal.MARKDOWN_CONVERTER.convert(this.sanitize()).unescapeNewLines()

fun String.unescapeNewLines(): String = this.replace(Regex("([\\\\\\n]+)(n)"), "\n").replace("=0D=0A", "\n")

fun String.toZoneId(): ZoneId? {
    return try {
        if (!BadTimezone.isBad(this)) ZoneId.of(this) else null
    } catch (_: Exception) {
        null
    }
}

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

// TODO: Do db migration so this can be removed
fun String.asLocale(): Locale {
    return when (this) {
        "ENGLISH" -> Locale.ENGLISH
        "JAPANESE" -> Locale.JAPANESE
        "PORTUGUESE" -> Locale.forLanguageTag("pt")
        "SPANISH" -> Locale.forLanguageTag("es")
        else -> Locale.ENGLISH
    }
}

fun String.asStringListFromDatabase(): List<String> = this.split(",").filter(String::isNotBlank)
