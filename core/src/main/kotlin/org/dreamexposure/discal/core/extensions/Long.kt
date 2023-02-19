package org.dreamexposure.discal.core.extensions

import discord4j.common.util.Snowflake
import java.time.Duration
import java.time.Instant

fun Long.asSnowflake(): Snowflake = Snowflake.of(this)

fun Long.asInstantMilli(): Instant = Instant.ofEpochMilli(this)

fun Long.asSeconds(): Duration = Duration.ofSeconds(this)

fun Long.asMinutes(): Duration = Duration.ofMinutes(this)
