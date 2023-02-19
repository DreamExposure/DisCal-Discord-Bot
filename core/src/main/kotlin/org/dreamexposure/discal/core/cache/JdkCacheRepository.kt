package org.dreamexposure.discal.core.cache

import reactor.core.publisher.Flux
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class JdkCacheRepository<K : Any, V>(override val ttl: Duration) : CacheRepository<K, V> {
    private val cache = ConcurrentHashMap<K, Pair<Instant, V>>()

    init {
        Flux.interval(Duration.ofMinutes(5))
            .map { evictOld() }
            .subscribe()
    }

    override suspend fun put(key: K, value: V) {
        cache[key] = Pair(Instant.now(), value)
    }

    override suspend fun get(key: K): V? {
        return cache[key]?.second
    }

    override suspend fun getAndRemove(key: K): V? {
        val cached = cache[key]?.second

        evict(key)
        return cached
    }

    override suspend fun evict(key: K) {
        cache.remove(key)
    }

    private fun evictOld() {
        cache.forEach { (key, pair) -> if (Duration.between(pair.first, Instant.now()) >= ttl) cache.remove(key) }
    }

}
