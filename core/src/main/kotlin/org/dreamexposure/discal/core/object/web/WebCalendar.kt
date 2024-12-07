package org.dreamexposure.discal.core.`object`.web

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.`object`.new.model.discal.CalendarV3Model

@Serializable
@Deprecated("Prefer to use the CalendarV3Model impl")
data class WebCalendar internal constructor(
        val id: String,
        val address: String,
        val number: Int,
        val host: CalendarHost,
        val link: String,
        @SerialName("host_link")
        val hostLink: String,
        val name: String,
        val description: String,
        val timezone: String,
        val external: Boolean,
) {
    constructor(newCalendar: CalendarV3Model): this(
        id = "calendar_id_here_as_not_mapped",
        address = "calendar_id_here_as_not_mapped",
        number = newCalendar.number,
        host = CalendarHost.valueOf(newCalendar.host.name),
        link = newCalendar.link,
        hostLink = newCalendar.hostLink,
        name = newCalendar.name,
        description = newCalendar.description,
        timezone = newCalendar.timezone.id,
        external = newCalendar.external,
    )

    companion object {
        fun empty() = WebCalendar("primary", "primary", 1, CalendarHost.GOOGLE,
                "N/a", "N/a", "N/a", "N/a", "N/a", false)
    }
}
