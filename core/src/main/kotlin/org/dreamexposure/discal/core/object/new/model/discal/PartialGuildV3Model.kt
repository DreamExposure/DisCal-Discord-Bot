package org.dreamexposure.discal.core.`object`.new.model.discal

import discord4j.common.util.Snowflake
import discord4j.rest.util.PermissionSet

data class PartialGuildV3Model(
    val id: Snowflake,
    val name: String,
    val iconUrl: String?,
    val isOwner: Boolean,
    val permissions: PermissionSet?,
)
