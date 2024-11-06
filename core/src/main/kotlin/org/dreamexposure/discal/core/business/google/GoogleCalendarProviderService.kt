package org.dreamexposure.discal.core.business.google

import com.google.api.services.calendar.model.AclRule
import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.business.CalendarProvider
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.dreamexposure.discal.core.exceptions.ApiException
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random

@Component
class GoogleCalendarProviderService(
    val googleCalendarApiWrapper: GoogleCalendarApiWrapper,
) : CalendarProvider {
    override val host = CalendarMetadata.Host.GOOGLE

    private fun randomCredentialId() = Random.nextInt(Config.SECRET_GOOGLE_CREDENTIAL_COUNT.getInt())

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
        if (aclRuleResponse.error != null) throw ApiException(aclRuleResponse.error.error, aclRuleResponse.error.exception)

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
        if (aclRuleResponse.error != null) throw ApiException(aclRuleResponse.error.error, aclRuleResponse.error.exception)

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
}
