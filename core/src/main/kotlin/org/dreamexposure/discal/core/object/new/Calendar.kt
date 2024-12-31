package org.dreamexposure.discal.core.`object`.new

import org.dreamexposure.discal.core.config.Config
import java.time.ZoneId

data class Calendar(
    val metadata: CalendarMetadata,
    val name: String,
    val description: String,
    val timezone: ZoneId,
    val hostLink: String,
) {
    // TODO: Consider moving this link formatting somewhere else?
    val link: String
        get() = "${Config.URL_BASE.getString()}/embed/${metadata.guildId.asString()}/calendar/${metadata.number}"

    ////////////////////////////
    ////// Nested classes //////
    ////////////////////////////
    data class CreateSpec(
        val host: CalendarMetadata.Host,
        val number: Int,
        val name: String,
        val description: String?,
        val timezone: ZoneId,
    )

    data class UpdateSpec(
        val name: String?,
        val description: String?,
        val timezone: ZoneId?,
    )
}
