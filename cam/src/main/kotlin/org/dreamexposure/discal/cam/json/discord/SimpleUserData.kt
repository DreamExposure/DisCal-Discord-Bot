package org.dreamexposure.discal.cam.json.discord

import discord4j.common.util.Snowflake

data class SimpleUserData(
    val id: Snowflake,
    val username: String,
    val discriminator: String,
    val avatar: String?,
)
