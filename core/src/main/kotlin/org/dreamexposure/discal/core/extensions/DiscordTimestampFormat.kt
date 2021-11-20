package org.dreamexposure.discal.core.extensions

enum class DiscordTimestampFormat(val value: String) {
    SHORT_TIME("t"),
    LONG_TIME("T"),
    SHORT_DATE("d"),
    LONG_DATE("D"),
    SHORT_DATETIME("f"),
    LONG_DATETIME("F"),
    RELATIVE_TIME("R"),
}
