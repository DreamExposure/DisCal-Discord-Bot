package org.dreamexposure.discal.core.`object`.new.model.discal

import discord4j.common.util.Snowflake

data class PartialGuildV3Model(
    val id: Snowflake,
    val name: String,
    val iconUrl: String?,
    val isOwner: Boolean,
    val permissions: String?, // This is a bit map, would love to use d4j's impl if I actually end up using this value
)
