package org.dreamexposure.discal.core.`object`.web

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer

@Serializable
@Deprecated("I dunno why this even exists, I don't think the list in WebGuild ever populated due to the bugged type filtering")
data class WebChannel(
        @Serializable(with = LongAsStringSerializer::class)
        val id: Long,
        val name: String,
)
