package org.dreamexposure.discal.core.`object`.new.model.discal

import discord4j.common.util.Snowflake

data class WebRoleV3Model(
    val id: Snowflake,
    val name: String,

    val managed: Boolean,
    val controlRole: Boolean,
    val everyone: Boolean,
)