package org.dreamexposure.discal

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.cache.CacheRepository
import org.dreamexposure.discal.core.`object`.new.*

// Cache
//typealias GuildSettingsCache = CacheRepository<Long, GuildSettings>
typealias CredentialsCache = CacheRepository<Int, Credential>
typealias OauthStateCache = CacheRepository<String, String>
typealias CalendarCache = CacheRepository<Snowflake, Array<Calendar>>
typealias RsvpCache = CacheRepository<String, Rsvp>
typealias StaticMessageCache = CacheRepository<Snowflake, StaticMessage>
typealias AnnouncementCache = CacheRepository<Snowflake, Array<Announcement>>
typealias AnnouncementWizardStateCache = CacheRepository<Snowflake, WizardState<Announcement>>
