package org.dreamexposure.discal.core.`object`.web

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.enums.calendar.CalendarHost

@Serializable
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
    companion object {
        fun empty() = WebCalendar("primary", "primary", 1, CalendarHost.GOOGLE,
                "N/a", "N/a", "N/a", "N/a", "N/a", false)
    }
}
