package org.dreamexposure.discal.core.`object`.web

import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper
import reactor.core.publisher.Mono

@Serializable
data class WebCalendar internal constructor(
        val id: String,
        val address: String,
        val number: Int,
        val host: CalendarHost,
        val link: String,
        val name: String,
        val description: String,
        val timezone: String,
        val external: Boolean,
) {
    companion object {
        fun empty() = WebCalendar("primary", "primary", 1, CalendarHost.GOOGLE,
                "N/a", "N/a", "N/a", "N/a", false)

        /**
         * @see [Calendar.toWebCalendar]
         */
        @Deprecated("Does not support calendar hosts other than Google.")
        fun fromCalendar(cd: CalendarData, gs: GuildSettings): Mono<WebCalendar> {
            return if (cd.calendarAddress.equals("primary", true))
                Mono.just(empty())
            else {
                val link = "https://www.discalbot.com/embed/calendar/${gs.guildID.asString()}"
                CalendarWrapper.getCalendar(cd).map { cal ->
                    WebCalendar(
                            cd.calendarId,
                            cd.calendarAddress,
                            1,
                            CalendarHost.GOOGLE,
                            link,
                            cal.summary,
                            cal.description,
                            cal.timeZone.replace("/", "___"),
                            cd.external)
                }.onErrorReturn(empty())
            }
        }
    }
}
