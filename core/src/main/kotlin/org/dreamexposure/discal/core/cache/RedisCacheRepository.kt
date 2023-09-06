package org.dreamexposure.discal.core.cache

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.cache.RedisCacheManager

class RedisCacheRepository<K : Any, V>(
    private val valueType: Class<V>,
    redisCacheManager: RedisCacheManager,
    private val mapper: ObjectMapper,
    cacheName: String,
) : CacheRepository<K, V> {
    private val cache = redisCacheManager.getCache(cacheName)!!

    override suspend fun put(key: K, value: V) {
        mapper.writer()
        cache.put(key, mapper.writeValueAsString(value))

    }

    override suspend fun get(key: K): V? {
        val raw = cache.get(key, String::class.java)
        return if (raw != null) mapper.readValue(raw, valueType) else null
    }

    override suspend fun getAndRemove(key: K): V? {
        val raw = cache.get(key, String::class.java)
        val parsed = if (raw != null) mapper.readValue(raw, valueType) else null

        evict(key)
        return parsed
    }

    override suspend fun evict(key: K) {
        cache.evictIfPresent(key)
    }

    companion object {
        inline operator fun <K : Any, reified V> invoke(
            redisCacheManager: RedisCacheManager,
            mapper: ObjectMapper,
            cacheName: String,
        ) = RedisCacheRepository<K, V>(V::class.java, redisCacheManager, mapper, cacheName)
    }
}
