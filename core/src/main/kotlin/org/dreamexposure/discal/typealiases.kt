package org.dreamexposure.discal

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.cache.CacheRepository
import org.dreamexposure.discal.core.`object`.new.*
import org.dreamexposure.discal.core.`object`.new.model.discal.cam.TokenV1Model

// Cache
typealias GuildSettingsCache = CacheRepository<Snowflake, GuildSettings>
typealias CredentialsCache = CacheRepository<Int, Credential>
typealias OauthStateCache = CacheRepository<String, String>
typealias CalendarMetadataCache = CacheRepository<Snowflake, Array<CalendarMetadata>>
typealias RsvpCache = CacheRepository<String, Rsvp>
typealias StaticMessageCache = CacheRepository<Snowflake, StaticMessage>
typealias AnnouncementCache = CacheRepository<Snowflake, Array<Announcement>>
typealias AnnouncementWizardStateCache = CacheRepository<Snowflake, AnnouncementWizardState>
typealias CalendarTokenCache = CacheRepository<Int, TokenV1Model>
