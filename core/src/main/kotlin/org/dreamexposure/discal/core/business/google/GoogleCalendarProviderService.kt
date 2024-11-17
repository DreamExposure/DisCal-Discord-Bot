package org.dreamexposure.discal.core.business.google

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.AclRule
import com.google.api.services.calendar.model.EventDateTime
import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.business.CalendarProvider
import org.dreamexposure.discal.core.business.EventMetadataService
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.exceptions.ApiException
import org.dreamexposure.discal.core.extensions.google.asInstant
import org.dreamexposure.discal.core.`object`.event.Recurrence
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import org.dreamexposure.discal.core.`object`.new.Event
import org.dreamexposure.discal.core.`object`.new.EventMetadata
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import com.google.api.services.calendar.model.Event as GoogleEvent

@Component
class GoogleCalendarProviderService(
    val googleCalendarApiWrapper: GoogleCalendarApiWrapper,
    val eventMetadataService: EventMetadataService,
) : CalendarProvider {
    override val host = CalendarMetadata.Host.GOOGLE

    /////////
    /// Calendar
    /////////
    override suspend fun getCalendar(metadata: CalendarMetadata): Calendar? {
        val response = googleCalendarApiWrapper.getCalendar(metadata)
        if (response.entity == null) return null

        return Calendar(
            metadata = metadata,
            name = response.entity.summary.orEmpty(),
            description = response.entity.description,
            timezone = ZoneId.of(response.entity.timeZone),
            hostLink = "https://calendar.google.com/calendar/embed?src=${metadata.id}"
        )
    }

    override suspend fun createCalendar(guildId: Snowflake, spec: Calendar.CreateSpec): Calendar {
        val credentialId = randomCredentialId()
        val googleCalendar = com.google.api.services.calendar.model.Calendar()

        googleCalendar.summary = spec.name
        googleCalendar.description = spec.description
        googleCalendar.timeZone = spec.timezone.id


        val response = googleCalendarApiWrapper.createCalendar(googleCalendar, credentialId, guildId)
        if (response.entity == null) throw ApiException(response.error?.error, response.error?.exception)

        val metadata = CalendarMetadata(
            guildId = guildId,
            number = spec.number,
            host = CalendarMetadata.Host.GOOGLE,
            id = response.entity.id,
            address = response.entity.id,
            external = false,
            secrets = CalendarMetadata.Secrets(
                credentialId = credentialId,
                privateKey = KeyGenerator.csRandomAlphaNumericString(16),
                expiresAt = Instant.now(),
                refreshToken = "",
                accessToken = "",
            )
        )

        // Add required ACL rule
        val aclRuleResponse = googleCalendarApiWrapper.insertAclRule(
            AclRule().setScope(AclRule.Scope().setType("default")).setRole("reader"),
            metadata
        )
        if (aclRuleResponse.error != null) throw ApiException(
            aclRuleResponse.error.error,
            aclRuleResponse.error.exception
        )

        return Calendar(
            metadata = metadata,
            name = response.entity.summary.orEmpty(),
            description = response.entity.description,
            timezone = ZoneId.of(response.entity.timeZone),
            hostLink = "https://calendar.google.com/calendar/embed?src=${response.entity.id}",
        )
    }

    override suspend fun updateCalendar(guildId: Snowflake, metadata: CalendarMetadata, spec: Calendar.UpdateSpec): Calendar {
        val content = com.google.api.services.calendar.model.Calendar()

        spec.name?.let { content.summary = it }
        spec.description?.let { content.description = it }
        spec.timezone?.let { content.timeZone = it.id }

        val response = googleCalendarApiWrapper.patchCalendar(content, metadata)
        if (response.entity == null) throw ApiException(response.error?.error, response.error?.exception)

        // Add required ACL rule
        val aclRuleResponse = googleCalendarApiWrapper.insertAclRule(
            AclRule().setScope(AclRule.Scope().setType("default")).setRole("reader"),
            metadata
        )
        if (aclRuleResponse.error != null) throw ApiException(
            aclRuleResponse.error.error,
            aclRuleResponse.error.exception
        )

        return Calendar(
            metadata = metadata,
            name = response.entity.summary.orEmpty(),
            description = response.entity.description,
            timezone = ZoneId.of(response.entity.timeZone),
            hostLink = "https://calendar.google.com/calendar/embed?src=${response.entity.id}",
        )
    }

    override suspend fun deleteCalendar(guildId: Snowflake, metadata: CalendarMetadata) {
        val response = googleCalendarApiWrapper.deleteCalendar(metadata)
        if (response.error != null) throw ApiException(response.error.error, response.error.exception)
    }

    /////////
    /// Events
    /////////
    override suspend fun getEvent(calendar: Calendar, id: String): Event? {
        val response = googleCalendarApiWrapper.getEvent(calendar.metadata, id)
        if (response.entity == null) return null
        val baseEvent = response.entity

        val metadata = eventMetadataService.getEventMetadata(calendar.metadata.guildId, id)
            ?: EventMetadata(id, calendar.metadata.guildId, calendar.metadata.number)

        return mapGoogleEventToDisCalEvent(calendar, baseEvent, metadata)
    }

    override suspend fun getUpcomingEvents(calendar: Calendar, amount: Int): List<Event> {
        val response = googleCalendarApiWrapper.getEvents(calendar.metadata, amount, Instant.now())
        if (response.entity == null) return emptyList()

        return loadEvents(calendar, response.entity)
    }

    override suspend fun getOngoingEvents(calendar: Calendar): List<Event> {
        val now = Instant.now()
        val start = now.minus(14, ChronoUnit.DAYS) // 2 weeks ago
        val end = now.plus(1, ChronoUnit.DAYS) // One day from now


        val response = googleCalendarApiWrapper.getEvents(calendar.metadata, start, end)
        if (response.entity == null) return emptyList()

        // Filter for only the ongoing events
        val filtered = response.entity
            .filter { it.start.asInstant(calendar.timezone).isBefore(now) }
            .filter { it.end.asInstant(calendar.timezone).isAfter(now) }

        return loadEvents(calendar, filtered)
    }

    override suspend fun getEventsInTimeRange(calendar: Calendar, start: Instant, end: Instant): List<Event> {
        val response = googleCalendarApiWrapper.getEvents(calendar.metadata, start, end)
        if (response.entity == null) return emptyList()

        return loadEvents(calendar, response.entity)
    }

    override suspend fun createEvent(calendar: Calendar, spec: Event.CreateSpec): Event {
        val event = GoogleEvent()
        event.id = KeyGenerator.generateEventId()
        event.visibility = "public"

        event.summary = spec.name
        event.description = spec.description
        event.location = spec.location

        event.start = EventDateTime()
            .setDateTime(DateTime(spec.start.toEpochMilli()))
            .setTimeZone(calendar.timezone.id)
        event.end = EventDateTime()
            .setDateTime(DateTime(spec.end.toEpochMilli()))
            .setTimeZone(calendar.timezone.id)

        if (spec.color != EventColor.NONE)
            event.colorId = spec.color.id.toString()

        if (spec.recur && spec.recurrence != null)
            event.recurrence = listOf(spec.recurrence.toRRule())

        // Create event in google
        val response = googleCalendarApiWrapper.createEvent(calendar.metadata, event)
        if (response.error != null || response.entity == null) throw ApiException(response.error?.error, response.error?.exception)

        // Create and save metadata
        val metadata = eventMetadataService.createEventMetadata(EventMetadata(
            id = event.id,
            guildId = calendar.metadata.guildId,
            calendarNumber = calendar.metadata.number,
            eventEnd = spec.end,
            imageLink = spec.image.orEmpty(),
        ))

        return mapGoogleEventToDisCalEvent(calendar, response.entity, metadata)
    }

    override suspend fun updateEvent(calendar: Calendar, spec: Event.UpdateSpec): Event {
        val event = GoogleEvent()
        event.id = spec.id

        event.summary = spec.name
        event.description = spec.description
        event.location = spec.location

        // Always update start/end so that we can safely handle all day events without DateTime by overwriting it
        event.start = EventDateTime()
            .setDateTime(DateTime(spec.start.toEpochMilli()))
            .setTimeZone(calendar.timezone.id)
        event.end = EventDateTime()
            .setDateTime(DateTime(spec.end.toEpochMilli()))
            .setTimeZone(calendar.timezone.id)

        if (spec.color == EventColor.NONE)
            event.colorId = null
        else
            event.colorId = spec.color.id.toString()

        // Special recurrence handling
        if (spec.recur != null) {
            if (spec.recur) {
                //event now recurs, add the RRUle.
                spec.recurrence?.let { event.recurrence = listOf(it.toRRule()) }
            }
        } else {
            //Recur equals null, so it's not changing whether its recurring, so handle if RRule changes only
            spec.recurrence?.let { event.recurrence = listOf(it.toRRule()) }
        }

        // Okay, all values are set, let's patch this event now
        val response = googleCalendarApiWrapper.patchEvent(calendar.metadata, event)
        if (response.error != null || response.entity == null) throw ApiException(response.error?.error, response.error?.exception)

        // Upsert metadata
        val metadata = eventMetadataService.getEventMetadata(calendar.metadata.guildId, event.id)
            ?: EventMetadata(
            id = event.id,
            guildId = calendar.metadata.guildId,
            calendarNumber = calendar.metadata.number,
            eventEnd = spec.end,
            imageLink = spec.image.orEmpty(),
        )
        eventMetadataService.upsertEventMetadata(metadata)

        return mapGoogleEventToDisCalEvent(calendar, response.entity, metadata)
    }

    override suspend fun deleteEvent(calendar: Calendar, id: String) {
        val response = googleCalendarApiWrapper.deleteEvent(calendar.metadata, id)
        if (response.error != null) throw ApiException(response.error.error, response.error.exception)
    }

    /////////
    /// Private util functions
    /////////
    private fun randomCredentialId() = Random.nextInt(Config.SECRET_GOOGLE_CREDENTIAL_COUNT.getInt())

    private suspend fun loadEvents(calendar: Calendar, events: List<GoogleEvent>): List<Event> {
        val metadataList = eventMetadataService.getMultipleEventsMetadata(calendar.metadata.guildId, events.map { it.id})

        return events.map { googleEvent ->
            val computedId = googleEvent.id.split("_")[0]
            val metadata = metadataList.firstOrNull { it.id == computedId }
                ?: EventMetadata(googleEvent.id, calendar.metadata.guildId, calendar.metadata.number)

            mapGoogleEventToDisCalEvent(calendar, googleEvent, metadata)
        }
    }

    private fun mapGoogleEventToDisCalEvent(calendar: Calendar, baseEvent: GoogleEvent, metadata: EventMetadata): Event {
        return Event(
            id = baseEvent.id,
            guildId = calendar.metadata.guildId,
            calendarNumber = calendar.metadata.number,
            name = baseEvent.summary.orEmpty(),
            description = baseEvent.description.orEmpty(),
            location = baseEvent.location.orEmpty(),
            link = baseEvent.htmlLink.orEmpty(),
            color = if (baseEvent.colorId.isNullOrBlank()) {
                EventColor.fromNameOrHexOrId(baseEvent.colorId)
            } else EventColor.NONE,
            start = if (baseEvent.start.dateTime != null) {
                Instant.ofEpochMilli(baseEvent.start.dateTime.value)
            } else Instant.ofEpochMilli(baseEvent.start.date.value)
                .plus(1, ChronoUnit.DAYS)
                .atZone(calendar.timezone)
                .truncatedTo(ChronoUnit.DAYS)
                .toLocalDate()
                .atStartOfDay()
                .atZone(calendar.timezone)
                .toInstant(),
            end = if (baseEvent.end.dateTime != null) {
                Instant.ofEpochMilli(baseEvent.end.dateTime.value)
            } else Instant.ofEpochMilli(baseEvent.end.date.value)
                .plus(1, ChronoUnit.DAYS)
                .atZone(calendar.timezone)
                .truncatedTo(ChronoUnit.DAYS)
                .toLocalDate()
                .atStartOfDay()
                .atZone(calendar.timezone)
                .toInstant(),
            recur = !baseEvent.recurrence.isNullOrEmpty(),
            recurrence = if (baseEvent.recurrence.isNullOrEmpty()) Recurrence() else Recurrence.fromRRule(baseEvent.recurrence[0]),
            image = metadata.imageLink,
            timezone = calendar.timezone,
        )
    }
}
