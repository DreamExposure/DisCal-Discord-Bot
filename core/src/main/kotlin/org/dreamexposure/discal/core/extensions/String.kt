package org.dreamexposure.discal.core.extensions

import org.dreamexposure.discal.core.utils.GlobalVal
import org.jsoup.Jsoup

fun String.sanitize(): String {
    return Jsoup.clean(this, GlobalVal.HTML_WHITELIST)
}

fun String.toMarkdown(): String {
    return GlobalVal.MARKDOWN_CONVERTER.convert(this.sanitize())
}
