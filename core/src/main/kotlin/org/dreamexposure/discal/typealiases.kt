package org.dreamexposure.discal

import org.dreamexposure.discal.core.cache.CacheRepository
import org.dreamexposure.discal.core.`object`.new.Credential

// Cache
//typealias GuildSettingsCache = CacheRepository<Long, GuildSettings>
typealias CredentialsCache = CacheRepository<Int, Credential>
typealias OauthStateCache = CacheRepository<String, String>
