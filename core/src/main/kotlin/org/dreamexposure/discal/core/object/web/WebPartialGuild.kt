package org.dreamexposure.discal.core.`object`.web

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer

@Serializable
data class WebPartialGuild(
        @Serializable(with = LongAsStringSerializer::class)
        val id: Long,
        val name: String,
        @SerialName("icon_url")
        val iconUrl: String,
)
