package org.dreamexposure.discal.core.cache

import java.time.Duration

interface CacheRepository<K, V> {

    val ttl: Duration
        get() = Duration.ofMinutes(60)

    suspend fun put(key: K, value: V)

    suspend fun get(key: K): V?

    suspend fun getAndRemove(key: K): V?

    suspend fun evict(key: K)
}
