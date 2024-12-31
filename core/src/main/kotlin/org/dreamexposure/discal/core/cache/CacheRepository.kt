package org.dreamexposure.discal.core.cache

import discord4j.common.util.Snowflake
import java.time.Duration
import kotlin.random.Random

interface CacheRepository<K, V> {
    val ttl: Duration
        get() = Duration.ofMinutes(60)
    val ttlWithJitter: Duration
        get() = ttl.plusSeconds(Random.Default.nextLong(-333,333))

    // Write
    suspend fun put(guildId: Snowflake? = null, key: K, value: V)
    suspend fun putMany(guildId: Snowflake? = null, values: Map<K, V>)

    // Read
    suspend fun get(guildId: Snowflake? = null, key: K): V?
    suspend fun getAll(guildId: Snowflake? = null): List<V>

    // Read & Remove
    suspend fun getAndRemove(guildId: Snowflake? = null, key: K): V?
    suspend fun getAndRemoveAll(guildId: Snowflake? = null): List<V>

    // Remove
    suspend fun evict(guildId: Snowflake? = null, key: K)
    suspend fun evictAll(guildId: Snowflake? = null)
}
