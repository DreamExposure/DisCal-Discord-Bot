package org.dreamexposure.discal.core.extensions

import java.time.Duration

fun Long.asSeconds(): Duration = Duration.ofSeconds(this)

fun Long.asMinutes(): Duration = Duration.ofMinutes(this)
