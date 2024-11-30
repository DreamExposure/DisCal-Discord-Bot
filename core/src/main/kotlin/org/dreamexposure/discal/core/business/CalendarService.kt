package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.*
import org.dreamexposure.discal.core.crypto.AESEncryption
import org.dreamexposure.discal.core.database.CalendarMetadataData
import org.dreamexposure.discal.core.database.CalendarMetadataRepository
import org.dreamexposure.discal.core.exceptions.NotFoundException
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import org.dreamexposure.discal.core.`object`.new.Event
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class   CalendarService(
    private val calendarMetadataRepository: CalendarMetadataRepository,
    private val calendarMetadataCache: CalendarMetadataCache,
    private val eventCache: EventCache,
    private val calendarProviders: List<CalendarProvider>,
    private val calendarCache: CalendarCache,
    private val calendarWizardStateCache: CalendarWizardStateCache,
    private val eventWizardStateCache: EventWizardStateCache,
    private val settingsService: GuildSettingsService,
    private val eventMetadataService: EventMetadataService,
    private val beanFactory: BeanFactory,
) {
    // We're going to fetch beans here for a lot of services that depend on CalendarService due to architecture (plus these are used in only one place so...)
    private val staticMessageService
        get() = beanFactory.getBean<StaticMessageService>()
    private val rsvpService
        get() = beanFactory.getBean<RsvpService>()
    private val announcementService
    get() = beanFactory.getBean<AnnouncementService>()


    /////////
    /// Calendar count
    /////////
    suspend fun getCalendarCount(): Long = calendarMetadataRepository.countAll().awaitSingle()

    suspend fun getCalendarCount(guildId: Snowflake) = calendarMetadataRepository.countAllByGuildId(guildId.asLong()).awaitSingle()

    /////////
    /// Calendar metadata - Prefer using full Calendar implementation
    /////////
    suspend fun getAllCalendarMetadata(guildId: Snowflake): List<CalendarMetadata> {
        var calendars = calendarMetadataCache.get(key = guildId)?.toList()
        if (calendars != null) return calendars

        calendars = calendarMetadataRepository.findAllByGuildId(guildId.asLong())
            .flatMap { mono { CalendarMetadata(it) } }
            .collectList()
            .awaitSingle()

        calendarMetadataCache.put(key = guildId, value = calendars.toTypedArray())
        return calendars
    }

    suspend fun getCalendarMetadata(guildId: Snowflake, number: Int): CalendarMetadata? {
        return getAllCalendarMetadata(guildId).firstOrNull { it.number == number }
    }

    suspend fun createCalendarMetadata(calendar: CalendarMetadata): CalendarMetadata {
        val aes = AESEncryption(calendar.secrets.privateKey)
        val encryptedRefreshToken = aes.encrypt(calendar.secrets.refreshToken).awaitSingle()
        val encryptedAccessToken = aes.encrypt(calendar.secrets.accessToken).awaitSingle()

        calendarMetadataRepository.save(CalendarMetadataData(
            guildId = calendar.guildId.asLong(),
            calendarNumber = calendar.number,
            host = calendar.host.name,
            calendarId = calendar.id,
            calendarAddress = calendar.address,
            external = calendar.external,
            credentialId = calendar.secrets.credentialId,
            privateKey = calendar.secrets.privateKey,
            accessToken = encryptedAccessToken,
            refreshToken = encryptedRefreshToken,
            expiresAt = calendar.secrets.expiresAt.toEpochMilli(),
        )).flatMap { mono { CalendarMetadata(it) } }.awaitSingle()

        val cached = calendarMetadataCache.get(key = calendar.guildId)
        if (cached != null) calendarMetadataCache.put(key = calendar.guildId, value = cached + calendar)

        return calendar
    }

    suspend fun updateCalendarMetadata(calendar: CalendarMetadata) {
        val aes = AESEncryption(calendar.secrets.privateKey)
        val encryptedRefreshToken = aes.encrypt(calendar.secrets.refreshToken).awaitSingle()
        val encryptedAccessToken = aes.encrypt(calendar.secrets.accessToken).awaitSingle()

        calendarMetadataRepository.updateCalendarByGuildIdAndCalendarNumber(
            guildId = calendar.guildId.asLong(),
            calendarNumber = calendar.number,
            host = calendar.host.name,
            calendarId = calendar.id,
            calendarAddress = calendar.address,
            external = calendar.external,
            credentialId = calendar.secrets.credentialId,
            privateKey = calendar.secrets.privateKey,
            accessToken = encryptedAccessToken,
            refreshToken = encryptedRefreshToken,
            expiresAt = calendar.secrets.expiresAt.toEpochMilli(),
        ).awaitSingleOrNull()

        val cached = calendarMetadataCache.get(key = calendar.guildId)
        if (cached != null) {
            val newList = cached.toMutableList()
            newList.removeIf { it.number == calendar.number }
            calendarMetadataCache.put(key = calendar.guildId,value = (newList + calendar).toTypedArray())
        }

        val cachedFullCalendar = calendarCache.get(calendar.guildId, calendar.number)
        if (cachedFullCalendar != null) calendarCache.put(calendar.guildId, calendar.number, cachedFullCalendar.copy(metadata = calendar))
    }

    suspend fun getNextCalendarNumber(guildId: Snowflake): Int = getAllCalendarMetadata(guildId).size + 1

    /////////
    /// Calendar
    /////////
    suspend fun getCalendar(guildId: Snowflake, number: Int): Calendar? {
        var calendar = calendarCache.get(guildId, number)
        if (calendar != null) return calendar

        val metadata = getCalendarMetadata(guildId, number) ?: return null

        calendar = calendarProviders
            .first { it.host == metadata.host }
            .getCalendar(metadata)
        if (calendar != null) calendarCache.put(guildId, number, calendar)

        return calendar
    }

    suspend fun createCalendar(guildId: Snowflake, spec: Calendar.CreateSpec): Calendar {
        val calendar = calendarProviders
            .first { it.host == spec.host }
            .createCalendar(guildId, spec)

        createCalendarMetadata(calendar.metadata)

        calendarCache.put(guildId, calendar.metadata.number, calendar)
        return calendar
    }

    suspend fun updateCalendar(guildId: Snowflake, number: Int, spec: Calendar.UpdateSpec): Calendar {
        val metadata = getCalendarMetadata(guildId, number) ?: throw NotFoundException("Cannot update a calendar that does not exist")

        val calendar = calendarProviders
            .first { it.host == metadata.host }
            .updateCalendar(guildId, metadata, spec)

        calendarCache.put(guildId, calendar.metadata.number, calendar)

        // Cancel wizards
        cancelCalendarWizard(guildId, number)

        // Make sure static messages get updated
        staticMessageService.updateStaticMessages(guildId, number)

        return calendar
    }

    suspend fun deleteCalendar(guildId: Snowflake, number: Int) {
        val metadata = getCalendarMetadata(guildId, number) ?: return

        // Delete from 3rd party locations
        calendarProviders.first { it.host == metadata.host }.deleteCalendar(guildId, metadata)

        // Cancel any wizards
        cancelCalendarWizard(guildId, number)
        cancelEventWizard(guildId, number)

        // Delete from db and handle "re-indexing" all calendar resources (because calendars are sequential for user-convenience)
        calendarMetadataRepository.deleteAllByGuildIdAndCalendarNumber(guildId.asLong(), number).awaitSingleOrNull()
        calendarMetadataRepository.decrementCalendarsByGuildIdAndCalendarNumber(guildId.asLong(), number).awaitSingleOrNull()

        // Remove from caches
        calendarCache.evict(guildId, metadata.number)
        calendarMetadataCache.evict(key = guildId)

        /*
        This is a set of calls to replicate the behavior of the old monolith db call
        that would go through all tables to handle deleting (as cascade delete constraints have not yet been added),
        and to update the calendar number references down-stream. This is again for user-convenience, or so I tell myself
        as a cope for how badly designed this project originally was and I just, can't let go of it,
         so I keep trying to fix it bit by bit <3
         */
        eventMetadataService.deleteEventMetadataForCalendarDeletion(guildId, number)
        rsvpService.deleteRsvpForCalendarDeletion(guildId, number)
        announcementService.deleteAnnouncementsForCalendarDeletion(guildId, number)
        staticMessageService.deleteStaticMessagesForCalendarDeletion(guildId, number)
    }

    /////////
    /// Event
    /// TODO: Need to figure out if I can fetch event sets from cache one day (eg, ongoing events)
    /////////
    suspend fun getEvent(guildId: Snowflake, calendarNumber: Int, id: String): Event? {
        var event = eventCache.get(guildId, id)
        if (event != null) return event

        val calendar = getCalendar(guildId, calendarNumber) ?: return null

        event = calendarProviders
            .first { it.host == calendar.metadata.host }
            .getEvent(calendar, id)
        if (event != null) eventCache.put(guildId, id, event)

        return event
    }

    suspend fun getUpcomingEvents(guildId: Snowflake, calendarNumber: Int, amount: Int): List<Event> {
        val calendar = getCalendar(guildId, calendarNumber) ?: return emptyList()

        val events = calendarProviders
            .first { it.host == calendar.metadata.host }
            .getUpcomingEvents(calendar, amount)
        events.forEach { event -> eventCache.put(guildId, event.id, event) }

        return events
    }

    suspend fun getOngoingEvents(guildId: Snowflake, calendarNumber: Int): List<Event> {
        val calendar = getCalendar(guildId, calendarNumber) ?: return emptyList()

        val events = calendarProviders
            .first { it.host == calendar.metadata.host }
            .getOngoingEvents(calendar)
        events.forEach { event -> eventCache.put(guildId, event.id, event) }

        return events
    }

    suspend fun getEventsInTimeRange(guildId: Snowflake, calendarNumber: Int, start: Instant, end: Instant): List<Event> {
        val calendar = getCalendar(guildId, calendarNumber) ?: return emptyList()

        val events = calendarProviders
            .first { it.host == calendar.metadata.host }
            .getEventsInTimeRange(calendar, start, end)
        events.forEach { event -> eventCache.put(guildId, event.id, event) }

        return events
    }

    suspend fun getEventsInNext24HourPeriod(guildId: Snowflake, calendarNumber: Int, start: Instant): List<Event> {
        return getEventsInTimeRange(guildId, calendarNumber, start, start.plus(1, ChronoUnit.DAYS))
    }

    suspend fun getEventsInMonth(guildId: Snowflake, calendarNumber: Int, start: Instant, daysInMonth: Int): List<Event> {
        return getEventsInTimeRange(guildId, calendarNumber, start, start.plus(daysInMonth.toLong(), ChronoUnit.DAYS))
    }

    suspend fun getEventsInNextNDays(guildId: Snowflake, calendarNumber: Int, days: Int): List<Event> {
        return getEventsInTimeRange(guildId, calendarNumber, Instant.now(), Instant.now().plus(days.toLong(), ChronoUnit.DAYS))
    }

    suspend fun createEvent(guildId: Snowflake, calendarNumber: Int, spec: Event.CreateSpec): Event {
        val calendar = getCalendar(guildId, calendarNumber) ?: throw NotFoundException("Cannot create a new event without a calendar")

        val event = calendarProviders
            .first { it.host == calendar.metadata.host }
            .createEvent(calendar, spec)

        eventCache.put(guildId, event.id, event)
        return event
    }

    suspend fun updateEvent(guildId: Snowflake, calendarNumber: Int, spec: Event.UpdateSpec): Event {
        val calendar = getCalendar(guildId, calendarNumber) ?: throw NotFoundException("Cannot update event without a calendar")

        val event = calendarProviders
            .first { it.host == calendar.metadata.host }
            .updateEvent(calendar, spec)

        eventCache.put(guildId, event.id, event)

        cancelEventWizard(guildId, event.id)

        return event
    }

    suspend fun deleteEvent(guildId: Snowflake, calendarNumber: Int, id: String) {
        val calendar = getCalendar(guildId, calendarNumber) ?: return

        calendarProviders
            .first { it.host == calendar.metadata.host }
            .deleteEvent(calendar, id)
        eventCache.evict(guildId, id)

        eventMetadataService.deleteEventMetadata(guildId, id)
        announcementService.deleteAnnouncements(guildId, id)

        cancelEventWizard(guildId, id)

    }


    /////////
    /// Wizards
    /////////
    suspend fun getCalendarWizard(guildId: Snowflake, userId: Snowflake): CalendarWizardState? {
        return calendarWizardStateCache.get(guildId, userId)
    }

    suspend fun putCalendarWizard(state: CalendarWizardState) {
        calendarWizardStateCache.put(state.guildId, state.userId, state)
    }

    suspend fun cancelCalendarWizard(guildId: Snowflake, userId: Snowflake) {
        calendarWizardStateCache.evict(guildId, userId)
    }

    suspend fun cancelCalendarWizard(guildId: Snowflake, calendarNumber: Int) {
        calendarWizardStateCache.getAll(guildId)
            .filter { it.entity.metadata.number == calendarNumber }
            .forEach { calendarWizardStateCache.evict(guildId, it.userId) }
    }

    suspend fun getEventWizard(guildId: Snowflake, userId: Snowflake): EventWizardState? {
        return eventWizardStateCache.get(guildId, userId)
    }

    suspend fun putEventWizard(state: EventWizardState) {
        eventWizardStateCache.put(state.guildId, state.userId, state)
    }

    suspend fun cancelEventWizard(guildId: Snowflake, userId: Snowflake) {
        eventWizardStateCache.evict(guildId, userId)
    }

    suspend fun cancelEventWizard(guildId: Snowflake, calendarNumber: Int) {
        eventWizardStateCache.getAll(guildId)
            .filter { it.entity.calendarNumber == calendarNumber }
            .forEach { eventWizardStateCache.evict(guildId, it.userId) }
    }

    suspend fun cancelEventWizard(guildId: Snowflake, eventId: String) {
        eventWizardStateCache.getAll(guildId)
            .filter { it.entity.id == eventId }
            .forEach { eventWizardStateCache.evict(guildId, it.userId) }
    }


    /////////
    /// Extra functions
    /////////
    suspend fun canAddNewCalendar(guildId: Snowflake): Boolean {
        val calCount = getCalendarCount(guildId)
        if (calCount == 0L) return true

        val settings = settingsService.getSettings(guildId)
        return calCount < settings.maxCalendars
    }
}
