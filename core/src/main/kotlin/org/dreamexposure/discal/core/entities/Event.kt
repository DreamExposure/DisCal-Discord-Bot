package org.dreamexposure.discal.core.entities

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.response.UpdateEventResponse
import org.dreamexposure.discal.core.entities.spec.update.UpdateEventSpec
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.`object`.event.EventData
import org.dreamexposure.discal.core.`object`.event.Recurrence
import org.dreamexposure.discal.core.`object`.event.RsvpData
import org.dreamexposure.discal.core.utils.GlobalVal.JSON_FORMAT
import org.json.JSONObject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

interface Event {
    /**
     * The ID of the event.
     * In the format eXXXXXXXXXX if generated by DisCal, otherwise it was generated by the 3rd party calendar service
     */
    val eventId: String

    /**
     * The ID of the [Guild] this event belongs to.
     */
    val guildId: Snowflake
        get() = calendar.guildId

    /**
     * The [Calendar] this Event belongs to
     */
    val calendar: Calendar

    /**
     * The of the event saved to the DisCal database
     */
    val eventData: EventData

    /**
     * The name of the event, renamed from "summary" to make it more user-friendly and clear.
     */
    val name: String

    /**
     * A description of what the event is about.
     */
    val description: String

    /**
     * The location at which the event occurs, usually a map location.
     */
    val location: String

    /**
     * The link to view the event at
     */
    val link: String

    /**
     * The color of the event. Used for visually identifying it in Discord embeds.
     * If no event color is assigned, it returns [EventColor.NONE] which is DisCal blue.
     */
    val color: EventColor

    /**
     * The start of the event, as an [Instant] representing the time starting from January 1st 1970.
     */
    val start: Instant

    /**
     * The end of the event, as an [Instant] representing the time starting from January 1st 1970.
     */
    val end: Instant

    /**
     * Whether the event is a recurring event.
     */
    val recur: Boolean

    /**
     * The rules of the recurring event. Contains the RRule an human-readable information on how the event will recur
     */
    val recurrence: Recurrence

    /**
     * A link to the image, if none is present, returns empty
     */
    val image: String
        get() = eventData.imageLink

    /**
     * The timezone that the event takes place in. This is always the same as the [Calendar]'s timezone
     */
    val timezone: ZoneId
        get() = calendar.timezone

    //Reactive

    /**
     * Attempts to request the announcements linked to the event, such as a [SPECIFIC][AnnouncementType.SPECIFIC]
     * type announcement.
     * If an error occurs, it is emitted through the Flux.
     *
     * @return A [Flux] of all announcements that are linked to the event.
     */
    fun getLinkedAnnouncements(): Flux<Announcement> {
        return DatabaseManager.getAnnouncements(this.guildId)
            .flatMapMany { Flux.fromIterable(it) }
            .filter { ann ->
                when (ann.type) {
                    AnnouncementType.UNIVERSAL -> return@filter true
                    AnnouncementType.COLOR -> return@filter ann.eventColor == this.color
                    AnnouncementType.SPECIFIC -> return@filter ann.eventId == this.eventId
                    AnnouncementType.RECUR -> return@filter this.eventId.contains(ann.eventId)
                }
            }
    }

    /**
     * Attempts to request the [RsvpData] of the event.
     * If an error occurs, it is emitted through the Mono.
     *
     * @return A [Mono] containing the [RsvpData] of the event
     */
    fun getRsvp(): Mono<RsvpData> = DatabaseManager.getRsvpData(guildId, eventId)

    fun updateRsvp(rsvp: RsvpData) = DatabaseManager.updateRsvpData(rsvp)

    /**
     * Attempts to update the event and returns the result.
     * If an error occurs, it is emitted through the [Mono].
     *
     * @param spec The information to update the event with
     * @return A [Mono] that contains the [UpdateEventResponse] containing information on success and the changes.
     */
    fun update(spec: UpdateEventSpec): Mono<UpdateEventResponse>

    /**
     * Attempts to delete the event and returns the result.
     * If an error occurs, it is emitted through the [Mono].
     *
     * @return A [Mono] containing whether delete succeeded.
     */
    fun delete(): Mono<Boolean>

    fun isOngoing(): Boolean = start.isBefore(Instant.now()) && end.isAfter(Instant.now())

    fun isOver(): Boolean = end.isBefore(Instant.now())

    fun isStarted() = start.isBefore(Instant.now())

    /**
     * Whether the event is 24 hours long.
     *
     * @return Whether the event is 24 hours long
     */
    fun is24Hours() = Duration.between(start, end).toHours() == 24L

    /**
     * Whether the event lasts for a full calendar day (midnight to midnight) or longer.
     *
     * @return Whether the event is all day
     */
    fun isAllDay(): Boolean {
        val start = this.start.atZone(timezone)

        return start.hour == 0 && is24Hours()
    }

    /**
     * Whether the event spans across multiple calendar days (ex Monday 8pm to Tuesday 3am)
     *
     * @return Whether the event is multi-day
     */
    fun isMultiDay(): Boolean {
        if (isAllDay()) return false // All day events should not count as multi-day events

        val start = this.start.atZone(timezone).truncatedTo(ChronoUnit.DAYS)
        val end = this.end.atZone(timezone).truncatedTo(ChronoUnit.DAYS)

        return when {
            start.year != end.year -> true
            start.month != end.month -> true
            start.dayOfYear != end.dayOfYear -> true
            else -> false
        }
    }

    //Json bullshit
    fun toJson(): JSONObject {
        return JSONObject()
            .put("guild_id", guildId)
            .put("calendar", calendar.toJson())
            .put("event_id", eventId)
            .put("epoch_start", start.toEpochMilli())
            .put("epoch_end", end.toEpochMilli())
            .put("name", name)
            .put("description", description)
            .put("location", location)
            .put("is_parent", !eventId.contains("_"))
            .put("color", color.name)
            .put("recur", recur)
            .put("recurrence", JSONObject(JSON_FORMAT.encodeToString(recurrence)))
            .put("rrule", recurrence.toRRule())
            .put("image", eventData.imageLink)
    }
}
