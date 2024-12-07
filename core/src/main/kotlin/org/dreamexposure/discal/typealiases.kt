package org.dreamexposure.discal

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.cache.CacheRepository
import org.dreamexposure.discal.core.`object`.new.*
import org.dreamexposure.discal.core.`object`.new.model.discal.cam.TokenV1Model

// Wizards
typealias AnnouncementWizardState = WizardState<Announcement>
typealias EventWizardState = WizardState<Event.PartialEvent>
typealias CalendarWizardState = WizardState<Calendar>

// Cache
typealias GuildSettingsCache = CacheRepository<Snowflake, GuildSettings>
typealias CredentialsCache = CacheRepository<Int, Credential>
typealias OauthStateCache = CacheRepository<String, String>
typealias CalendarMetadataCache = CacheRepository<Snowflake, Array<CalendarMetadata>>
typealias CalendarCache = CacheRepository<Int, Calendar>
typealias RsvpCache = CacheRepository<String, Rsvp>
typealias StaticMessageCache = CacheRepository<Snowflake, StaticMessage>
typealias AnnouncementCache = CacheRepository<Snowflake, Array<Announcement>>
typealias AnnouncementWizardStateCache = CacheRepository<Snowflake, AnnouncementWizardState>
typealias EventWizardStateCache = CacheRepository<Snowflake, EventWizardState>
typealias CalendarWizardStateCache = CacheRepository<Snowflake, CalendarWizardState>
typealias CalendarTokenCache = CacheRepository<Int, TokenV1Model>
typealias EventCache = CacheRepository<String, Event>
