package org.dreamexposure.discal.core.extensions

import java.time.Instant

fun Instant.asDiscordTimestamp() = "<t:${this.toEpochMilli() / 1000}:F>"
