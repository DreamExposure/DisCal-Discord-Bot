package org.dreamexposure.discal

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.cache.CacheRepository
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.Credential

// Cache
//typealias GuildSettingsCache = CacheRepository<Long, GuildSettings>
typealias CredentialsCache = CacheRepository<Int, Credential>
typealias OauthStateCache = CacheRepository<String, String>
typealias CalendarCache = CacheRepository<Snowflake, Array<Calendar>>
