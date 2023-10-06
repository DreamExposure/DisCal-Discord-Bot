package org.dreamexposure.discal.core.cache

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.extensions.isExpiredTtl
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class JdkCacheRepository<K : Any, V>(override val ttl: Duration) : CacheRepository<K, V> {
    private val cache = ConcurrentHashMap<Snowflake?, ConcurrentHashMap<K, Pair<Instant, V>>>()

    init {
        Flux.interval(Duration.ofMinutes(30))
            .map { evictOld() }
            .subscribe()
    }

    override suspend fun put(guildId: Snowflake?, key: K, value: V) {
        val guildedMap = getGuildedCache(guildId)

        guildedMap[key] = Pair(Instant.now().plus(ttl), value)
        cache[guildId] = guildedMap
    }

    override suspend fun putMany(guildId: Snowflake?, values: Map<K, V>) {
        val guildedMap = getGuildedCache(guildId)

        guildedMap.putAll(values.mapValues { Pair(Instant.now().plus(ttl), it.value) })
        cache[guildId] = guildedMap
    }


    override suspend fun get(guildId: Snowflake?, key: K): V? {
        // guild id lets us narrow search
        if (guildId != null) {
            val cached = cache[guildId]?.get(key) ?: return null

            if (cached.first.isExpiredTtl()) {
                evict(guildId, key)
                return null
            }
            return cached.second
        }

        return cache.firstNotNullOfOrNull { (owningGuild, guildedCache) ->
            val cached = guildedCache[key] ?: return@firstNotNullOfOrNull null

            if (cached.first.isExpiredTtl()) {
                evict(owningGuild, key)
                null
            } else cached.second
        }
    }

    override suspend fun getAll(guildId: Snowflake?): List<V> {
        // Guild id lets us narrow search
        if (guildId != null) {
            return getGuildedCache(guildId)
                .map(Map.Entry<K, Pair<Instant, V>>::value)
                .filterNot { it.first.isExpiredTtl() }
                .map(Pair<Instant, V>::second)
        }

        return cache.values
            .flatMap(ConcurrentHashMap<K, Pair<Instant, V>>::values)
            .filterNot { it.first.isExpiredTtl() }
            .map(Pair<Instant, V>::second)
    }


    override suspend fun getAndRemove(guildId: Snowflake?, key: K): V? {
        // guild id lets us narrow search
        if (guildId != null) {
            val cached = getGuildedCache(guildId)[key] ?: return null
            evict(guildId, key)
            return if (cached.first.isExpiredTtl()) null else cached.second
        }

        return cache.firstNotNullOfOrNull { (owningGuild, guildedCache) ->
            val cached = guildedCache[key] ?: return@firstNotNullOfOrNull null
            evict(owningGuild, key)
            return if (cached.first.isExpiredTtl()) null else cached.second
        }
    }

    override suspend fun getAndRemoveAll(guildId: Snowflake?): List<V> {
        val allCached = getAll(guildId)
        evictAll(guildId)

        return allCached
    }


    override suspend fun evict(guildId: Snowflake?, key: K) {
        if (guildId != null) cache[guildId]?.remove(key)
        else cache.values.forEach { it.remove(key) }
    }

    override suspend fun evictAll(guildId: Snowflake?) {
        if (guildId != null) cache.remove(guildId)
        else cache.clear()
    }


    private fun evictOld() {
        cache.toMap().forEach { (owningGuildId, guildedCache) ->
            guildedCache.toMap().forEach { (key, cachedPair) ->
                if (cachedPair.first.isExpiredTtl()) guildedCache.remove(key)
            }
            if (guildedCache.isEmpty()) cache.remove(owningGuildId) // Remove any empty maps
        }
    }

    private fun getGuildedCache(guildId: Snowflake?) = cache.getOrDefault(guildId, ConcurrentHashMap<K, Pair<Instant, V>>())
}
