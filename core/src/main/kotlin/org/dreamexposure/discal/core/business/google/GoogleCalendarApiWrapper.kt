package org.dreamexposure.discal.core.business.google

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.AclRule
import com.google.api.services.calendar.model.Calendar
import com.google.api.services.calendar.model.CalendarListEntry
import com.google.api.services.calendar.model.Event
import discord4j.common.util.Snowflake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dreamexposure.discal.CalendarTokenCache
import org.dreamexposure.discal.core.business.api.CamApiWrapper
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.extensions.asSnowflake
import org.dreamexposure.discal.core.extensions.isExpiredTtl
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import org.dreamexposure.discal.core.`object`.new.model.ResponseModel
import org.dreamexposure.discal.core.`object`.rest.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.time.Instant
import com.google.api.services.calendar.Calendar as GoogleCalendarService


@Component
class GoogleCalendarApiWrapper(
    private val camApiWrapper: CamApiWrapper,
    private val calendarTokenCache: CalendarTokenCache, // Technically an antipattern, but it is domain-specific so...
) {
    // Using this as guildId for caching discal owned credentials with guild owned ones cuz its efficient
    private val discalId = Config.DISCORD_APP_ID.getLong().asSnowflake()

    /////////
    /// Auth
    /////////

    /* Get access token to calendar from cache or CAM */
    private suspend fun getAccessToken(credentialId: Int): String {
        val token = calendarTokenCache.get(guildId = discalId, credentialId)
        if (token != null && !token.validUntil.isExpiredTtl())  return token.accessToken

        LOGGER.debug("Fetching new local-copy of global google calendar token via CAM | credentialId:$credentialId")

        val tokenResponse = camApiWrapper.getCalendarToken(credentialId)
        return if (tokenResponse.entity != null) {
            calendarTokenCache.put(guildId = discalId, credentialId, tokenResponse.entity)
            tokenResponse.entity.accessToken
        } else {
            throw RuntimeException("Error requesting local google calendar token from CAM for credentialId: $credentialId | response:  error: ${tokenResponse.error?.error}")
        }
    }

    private suspend fun getAccessToken(guildId: Snowflake, calendarNumber: Int): String {
        val token = calendarTokenCache.get(guildId, calendarNumber)
        if (token != null && !token.validUntil.isExpiredTtl())  return token.accessToken

        LOGGER.debug("Fetching new local-copy of external google calendar token via CAM | guild:${guildId.asLong()} calendarNumber:$calendarNumber")

        val tokenResponse = camApiWrapper.getCalendarToken(guildId, calendarNumber, CalendarHost.GOOGLE)
        return if (tokenResponse.entity != null) {
            calendarTokenCache.put(guildId, calendarNumber, tokenResponse.entity)
            tokenResponse.entity.accessToken
        } else if (tokenResponse.code == HttpStatus.FORBIDDEN.value() && tokenResponse.error?.error.equals("Access to resource revoked")) {
            // User MUST reauthorize DisCal in Google if we are seeing this error as the refresh token is invalid
            // TODO: Call to delete calendar here to mimic old behavior. Consider marking instead so they can re-auth?
            throw UnsupportedOperationException("Call to delete calendar on refresh token revoked (or alternative) not yet implemented")
        } else {
            throw RuntimeException("Error requesting local google calendar token from CAM for guild:${guildId.asString()} calendarNumber: $calendarNumber | response: error: ${tokenResponse.error?.error}")
        }
    }

    private suspend fun buildGoogleCalendarService(accessToken: String): GoogleCalendarService {
        val credential = GoogleCredential().setAccessToken(accessToken)

        return GoogleCalendarService.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
            .setApplicationName("DisCal")
            .build()
    }

    private suspend fun getGoogleCalendarService(credentialId: Int) = buildGoogleCalendarService(getAccessToken(credentialId))

    private suspend fun getGoogleCalendarService(guildId: Snowflake, calendarNumber: Int) = buildGoogleCalendarService(getAccessToken(guildId, calendarNumber))

    private suspend fun getGoogleCalendarService(calendarMetadata: CalendarMetadata): GoogleCalendarService {
        return if (calendarMetadata.external) getGoogleCalendarService(calendarMetadata.guildId, calendarMetadata.number)
        else getGoogleCalendarService(calendarMetadata.secrets.credentialId)
    }

    /////////
    /// ACL Rule
    /////////
    suspend fun insertAclRule(rule: AclRule, calMetadata: CalendarMetadata): ResponseModel<AclRule> = withContext(Dispatchers.IO) {
        val service = getGoogleCalendarService(calMetadata)

        try {
            val aclRule = service.acl()
                .insert(calMetadata.id, rule)
                .setQuotaUser(calMetadata.guildId.asString())
                .execute()
            ResponseModel(aclRule)
        } catch (e: Exception) {
            LOGGER.error("Failed to insert ACL rule for Google Calendar", e)
            ResponseModel(ErrorResponse("Failed to insert ACL rule for Google Calendar", e), 600)
        }
    }

    /////////
    /// Calendars
    /////////
    suspend fun createCalendar(calendar: Calendar, credentialId: Int, guildId: Snowflake): ResponseModel<Calendar> = withContext(Dispatchers.IO) {
        val service = getGoogleCalendarService(credentialId)

        try {
            val calendar = service.calendars()
                .insert(calendar)
                .setQuotaUser(guildId.asString())
                .execute()
            ResponseModel(calendar)
        } catch (e: Exception) {
            LOGGER.error("Failed to create calendar for Google Calendar", e)
            ResponseModel(ErrorResponse("Failed to create calendar for Google Calendar", e), 600)
        }
    }

    suspend fun patchCalendar(calendar: Calendar, metadata: CalendarMetadata): ResponseModel<Calendar> = withContext(Dispatchers.IO) {
        val service = getGoogleCalendarService(metadata)

        try {
            val calendar = service.calendars()
                .patch(calendar.id, calendar)
                .setQuotaUser(metadata.guildId.asString())
                .execute()
            ResponseModel(calendar)
        } catch (e: Exception) {
            LOGGER.error("Failed to patch calendar for Google Calendar", e)
            ResponseModel(ErrorResponse("Failed to patch calendar for Google Calendar", e), 600)
        }
    }

    suspend fun updateCalendar(calendar: Calendar, metadata: CalendarMetadata): ResponseModel<Calendar> = withContext(Dispatchers.IO) {
        val service = getGoogleCalendarService(metadata)

        try {
            val calendar = service.calendars()
                .patch(calendar.id, calendar)
                .setQuotaUser(metadata.guildId.asString())
                .execute()
            ResponseModel(calendar)
        } catch (e: Exception){
            LOGGER.error("Failed to update calendar for Google Calendar", e)
            ResponseModel(ErrorResponse("Failed to update calendar for Google Calendar", e), 600)
        }
    }

    suspend fun getCalendar(metadata: CalendarMetadata): ResponseModel<Calendar> = withContext(Dispatchers.IO) {
        val service = getGoogleCalendarService(metadata)

        try {
            val calendar= service.calendars()
                .get(metadata.address)
                .setQuotaUser(metadata.guildId.asString())
                .execute()
            ResponseModel(calendar)
        } catch (e: Exception) {
            LOGGER.error("Failed to get calendar from Google Calendar", e)
            ResponseModel(ErrorResponse("Failed to get calendar from Google Calendar", e), 600)
        }
    }

    suspend fun deleteCalendar(metadata: CalendarMetadata): ResponseModel<Boolean> = withContext(Dispatchers.IO) {
        // Sanity check if calendar can be deleted
        if (metadata.external || metadata.address.equals("primary", true)) return@withContext ResponseModel(false)

        val service = getGoogleCalendarService(metadata)

        try {
            service.calendars()
                .delete(metadata.address)
                .setQuotaUser(metadata.guildId.asString())
                .execute()
            ResponseModel(true)
        } catch (e: GoogleJsonResponseException) {
            // Treat 404 errors on this endpoint as a soft success
            if (e.statusCode == 404) ResponseModel(true)
            else {
                LOGGER.error("Failed to delete calendar from Google Calendar", e)
                ResponseModel(600, false, ErrorResponse("Failed to delete calendar from Google Calendar", e))
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to delete calendar from Google Calendar", e)
            ResponseModel(600, false, ErrorResponse("Failed to delete calendar from Google Calendar", e))
        }
    }

    suspend fun getUsersExternalCalendars(metadata: CalendarMetadata): ResponseModel<List<CalendarListEntry>> = withContext(Dispatchers.IO) {
        val service = getGoogleCalendarService(metadata)

        try {
            val calendarList = service.calendarList()
                .list()
                .setMinAccessRole("writer")
                .setQuotaUser(metadata.guildId.asString())
                .execute()
            ResponseModel(calendarList.items)
        } catch (e: Exception) {
            LOGGER.error("Failed to list external calendars", e)
            ResponseModel(600, emptyList(), ErrorResponse("Failed to list external calendars", e))
        }
    }

    /////////
    /// Events
    /////////
    suspend fun createEvent(metadata: CalendarMetadata, event: Event): ResponseModel<Event> = withContext(Dispatchers.IO) {
        val service = getGoogleCalendarService(metadata)

        try {
            val event = service.events()
                .insert(metadata.id, event)
                .setQuotaUser(metadata.guildId.asString())
                .execute()
            ResponseModel(event)
        } catch (e: Exception) {
            LOGGER.error("Failed to create event on Google Calendar", e)
            ResponseModel(ErrorResponse("Failed to create event on Google Calendar", e), 600)
        }
    }

    suspend fun patchEvent(metadata: CalendarMetadata, event: Event): ResponseModel<Event> = withContext(Dispatchers.IO) {
        val service = getGoogleCalendarService(metadata)

        try {
            val event = service.events()
                .patch(metadata.id, event.id, event)
                .setQuotaUser(metadata.guildId.asString())
                .execute()
            ResponseModel(event)
        } catch (e: Exception) {
            LOGGER.error("Failed to patch event on Google Calendar", e)
            ResponseModel(ErrorResponse("Failed to patch event on Google Calendar", e), 600)
        }
    }

    suspend fun updateEvent(metadata: CalendarMetadata, event: Event): ResponseModel<Event> = withContext(Dispatchers.IO) {
        val service = getGoogleCalendarService(metadata)

        try {
            val event = service.events()
                .update(metadata.id, event.id, event)
                .setQuotaUser(metadata.guildId.asString())
                .execute()
            ResponseModel(event)
        } catch (e: Exception) {
            LOGGER.error("Failed to update event on Google Calendar", e)
            ResponseModel(ErrorResponse("Failed to update event on Google Calendar", e), 600)
        }
    }

    suspend fun getEvent(metadata: CalendarMetadata, id: String): ResponseModel<Event> = withContext(Dispatchers.IO) {
        val service = getGoogleCalendarService(metadata)

        // This whole block can probably be shortened once old impl behavior is moved to a higher abstraction layer
        try {
            val event = service.events()
                .get(metadata.id, id)
                .setQuotaUser(metadata.guildId.asString())
                .execute()

            if (event.status.equals("cancelled", true)) ResponseModel(404, null, null)
            else ResponseModel(event)
        } catch (e: GoogleJsonResponseException) {
            when (HttpStatus.valueOf(e.statusCode)) {
                HttpStatus.GONE -> {
                    // Event is gone. Sometimes Google will return this if the event is deleted
                    ResponseModel(404, null, ErrorResponse("Event Deleted"))
                }
                HttpStatus.NOT_FOUND -> {
                    // Event not found. Was this ever an event?
                    ResponseModel(404, null, ErrorResponse("Event Not Found"))
                } else -> {
                    LOGGER.error("Failed to get event on Google Calendar w/ GoogleResponseException", e)
                ResponseModel(ErrorResponse("Failed to get event on Google Calendar w/ GoogleResponseException", e), e.statusCode)
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to get event from Google Calendar", e)
            ResponseModel(ErrorResponse("Failed to get event from Google Calendar", e), 600)
        }
    }

    suspend fun getEvents(metadata: CalendarMetadata, amount: Int, start: Instant): ResponseModel<List<Event>> = withContext(Dispatchers.IO) {
        val service = getGoogleCalendarService(metadata)

        try {
            val events = service.events()
                .list(metadata.id)
                .setMaxResults(amount)
                .setTimeMin(DateTime(start.toEpochMilli()))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setShowDeleted(false)
                .setQuotaUser(metadata.guildId.asString())
                .execute()
            ResponseModel(events.items)
        } catch (e: Exception) {
            LOGGER.error("Failed to get events from Google Calendar by start date (variant 1)", e)
            ResponseModel(600, emptyList(), ErrorResponse("Failed to get events from Google Calendar", e))
        }
    }

    suspend fun getEvents(metadata: CalendarMetadata, amount: Int, start: Instant, end: Instant): ResponseModel<List<Event>> = withContext(Dispatchers.IO) {
        val service = getGoogleCalendarService(metadata)

        try {
            val events = service.events()
                .list(metadata.id)
                .setMaxResults(amount)
                .setTimeMin(DateTime(start.toEpochMilli()))
                .setTimeMax(DateTime(end.toEpochMilli()))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setShowDeleted(false)
                .setQuotaUser(metadata.guildId.asString())
                .execute()
            ResponseModel(events.items)
        } catch (e: Exception) {
            LOGGER.error("Failed to get events from Google Calendar by start and end date (variant 2)", e)
            ResponseModel(600, emptyList(), ErrorResponse("Failed to get events from Google Calendar", e))
        }
    }

    suspend fun getEvents(metadata: CalendarMetadata, start: Instant, end: Instant): ResponseModel<List<Event>> = withContext(Dispatchers.IO) {
        val service = getGoogleCalendarService(metadata)

        try {
            val events = service.events()
                .list(metadata.id)
                .setTimeMin(DateTime(start.toEpochMilli()))
                .setTimeMax(DateTime(end.toEpochMilli()))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setShowDeleted(false)
                .setQuotaUser(metadata.guildId.asString())
                .execute()
            ResponseModel(events.items)
        } catch (e: Exception) {
            LOGGER.error("Failed to get events from Google Calendar by start and end date without amount (variant 3)", e)
            ResponseModel(600, emptyList(), ErrorResponse("Failed to get events from Google Calendar", e))
        }
    }

    suspend fun deleteEvent(metadata: CalendarMetadata, id: String): ResponseModel<Boolean> = withContext(Dispatchers.IO) {
        val service = getGoogleCalendarService(metadata)

        try {
            val response = service.events()
                .delete(metadata.id, id)
                .setQuotaUser(metadata.guildId.asString())
                .executeUnparsed()

            //Google sends 4 possible status codes, 200, 204, 404, 410.
            // First 2 should be treated as successful, and the other 2 as not found.
            when (response.statusCode) {
                200, 204 -> ResponseModel(true)
                404, 410 -> ResponseModel(false)
                else -> {
                    //Log response data and return false as google sent an unexpected response code.
                    LOGGER.error("Failed to delete event from Google Calendar w/ unknown response | ${response.statusCode} | ${response.statusMessage}")
                    ResponseModel(response.statusCode, false, ErrorResponse(response.statusMessage))
                }
            }
        } catch (e: GoogleJsonResponseException) {
           if (e.statusCode != 410 || e.statusCode != 404) {
               LOGGER.error("Failed to delete event from Google Calendar", e)
               ResponseModel(e.statusCode, false, ErrorResponse(e.statusMessage, e))
           } else ResponseModel(true)
        } catch (e: Exception) {
            LOGGER.error("Failed to delete event from Google Calendar w/ unknown error", e)
            ResponseModel(600, false, ErrorResponse("Failed to delete event from Google Calendar", e))
        }
    }
}
