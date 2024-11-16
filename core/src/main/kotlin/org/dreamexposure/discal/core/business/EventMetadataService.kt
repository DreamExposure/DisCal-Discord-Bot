package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.EventCache
import org.dreamexposure.discal.core.database.EventMetadataData
import org.dreamexposure.discal.core.database.EventMetadataRepository
import org.dreamexposure.discal.core.`object`.new.EventMetadata
import org.springframework.stereotype.Component

@Component
class EventMetadataService(
    private val eventMetadataRepository: EventMetadataRepository,
    private val eventCache: EventCache,
) {

    /////////
    /// Event metadata - Prefer using full Event implementation in CalendarService
    /////////
    suspend fun getEventMetadata(guildId: Snowflake, eventId: String): EventMetadata? {
        val computedId = eventId.split("_")[0]

        return eventMetadataRepository.findByGuildIdAndEventId(guildId.asLong(), computedId)
            .map(::EventMetadata)
            .awaitSingleOrNull()
    }

    suspend fun getMultipleEventsMetadata(guildId: Snowflake, eventIds: List<String>): List<EventMetadata> {
        val computedIds = eventIds.map { eventId -> eventId.split("_")[0] }

        return eventMetadataRepository.findAllByGuildIdAndEventIdIn(guildId.asLong(), computedIds)
            .map(::EventMetadata)
            .collectList()
            .awaitSingle()
    }

    suspend fun createEventMetadata(event: EventMetadata): EventMetadata {
        val computedId = event.id.split("_")[0]

        return eventMetadataRepository.save(EventMetadataData(
            guildId = event.guildId.asLong(),
            eventId = computedId,
            calendarNumber = event.calendarNumber,
            eventEnd = event.eventEnd.toEpochMilli(),
            imageLink = event.imageLink,
        )).map(::EventMetadata).awaitSingle()
    }

    suspend fun updateEventMetadata(event: EventMetadata) {
        val computedId = event.id.split("_")[0]

        eventMetadataRepository.updateByGuildIdAndEventId(
            guildId = event.guildId.asLong(),
            eventId = computedId,
            calendarNumber = event.calendarNumber,
            eventEnd = event.eventEnd.toEpochMilli(),
            imageLink = event.imageLink
        ).awaitSingleOrNull()
    }

    suspend fun deleteEventMetadataForCalendarDeletion(guildId: Snowflake, calendarNumber: Int) {
        eventMetadataRepository.deleteAllByGuildIdAndCalendarNumber(guildId.asLong(), calendarNumber).awaitSingleOrNull()
        eventMetadataRepository.decrementCalendarsByGuildIdAndCalendarNumber(guildId.asLong(), calendarNumber).awaitSingleOrNull()
        eventCache.evictAll(guildId)
    }
}