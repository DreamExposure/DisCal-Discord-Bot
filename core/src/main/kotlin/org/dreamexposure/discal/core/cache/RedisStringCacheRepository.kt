package org.dreamexposure.discal.core.cache

import com.fasterxml.jackson.databind.ObjectMapper
import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.logger.LOGGER
import org.springframework.data.redis.connection.DataType
import org.springframework.data.redis.core.*
import java.time.Duration

class RedisStringCacheRepository<K, V>(
    private val valueType: Class<V>,
    private val objectMapper: ObjectMapper,
    private val redisTemplate: ReactiveStringRedisTemplate,
    override val ttl: Duration,
    cacheName: String,
) : CacheRepository<K, V> {
    private val valueOps = redisTemplate.opsForValue()
    private val keyPrefix = "${Config.CACHE_PREFIX.getString()}:$cacheName"

    init {
        objectMapper.writer()
    }

    override suspend fun put(guildId: Snowflake?, key: K, value: V) {
        valueOps.setAndAwait(formatKey(guildId, key), objectMapper.writeValueAsString(value), ttl)
    }

    override suspend fun putMany(guildId: Snowflake?, values: Map<K, V>) {
        values.forEach { (key, value) ->
            valueOps.setAndAwait(formatKey(guildId, key), objectMapper.writeValueAsString(value), ttl)
        }
    }


    override suspend fun get(guildId: Snowflake?, key: K): V? {
        val raw = valueOps.getAndAwait(formatKey(guildId, key))
        return if (raw != null) {
            try {
                objectMapper.readValue(raw, valueType)
            } catch (ex: Exception) {
                LOGGER.error("Failed to read value from redis... evicting | guildId:$guildId | key:$key | data:$raw", ex)

                evict(guildId, key)

                null
            }
        } else null
    }

    override suspend fun getAll(guildId: Snowflake?): List<V> {
        val keys = redisTemplate.scan(ScanOptions.scanOptions()
            .type(DataType.STRING)
            .match(formatKeySearch(guildId))
            .build()
        ).collectList().awaitSingle()

        val rawValues = valueOps.multiGetAndAwait(keys)

        return try {
            rawValues.map { objectMapper.readValue(it, valueType) }
        } catch (ex: Exception) {
            LOGGER.error("Failed to read value from redis... evicting all | guildId:$guildId | keys:${keys.joinToString(",")} | data:${rawValues.joinToString(",")}", ex)
            evictAll(guildId)

            emptyList()
        }
    }


    override suspend fun getAndRemove(guildId: Snowflake?, key: K): V? {
        val raw = valueOps.getAndDelete(formatKey(guildId, key)).awaitSingleOrNull()
        return if (raw != null) {
            try {
                objectMapper.readValue(raw, valueType)
            } catch (ex: Exception) {
                LOGGER.error("Failed to read value from redis | guildId:$guildId | key: $key | data:$raw", ex)

                null
            }
        } else null
    }

    override suspend fun getAndRemoveAll(guildId: Snowflake?): List<V> {
        return redisTemplate.scan(ScanOptions.scanOptions().type(DataType.STRING).match(formatKeySearch(guildId)).build())
            .flatMap(valueOps::getAndDelete)
            .mapNotNull {
                try {
                    objectMapper.readValue(it, valueType)
                } catch (ex: Exception) {
                    LOGGER.error("Failed to read value from redis | guildId:$guildId | keySearch:${formatKeySearch(guildId)} | data:$it", ex)
                    null
                }
            }.map { it!! } // Yeah, this shouldn't be required with mapNotNull, but it generates an error otherwise, probably a language bug?
            .collectList()
            .awaitSingle()
    }


    override suspend fun evict(guildId: Snowflake?, key: K) {
        valueOps.deleteAndAwait(formatKey(guildId, key))
    }

    override suspend fun evictAll(guildId: Snowflake?) {
        val keys = redisTemplate.scan(ScanOptions.scanOptions()
            .type(DataType.STRING)
            .match(formatKeySearch(guildId))
            .build()
        ).collectList().awaitSingle()

        redisTemplate.deleteAndAwait(*keys.toTypedArray())
    }


    private fun formatKey(guildId: Snowflake?, key: K): String {
        val normalizedGuildId = guildId?.asString() ?: "_"
        val normalizedKey = objectMapper.writeValueAsString(key)

        return "$keyPrefix:$normalizedGuildId:$normalizedKey"
    }

    private fun formatKeySearch(guildId: Snowflake?): String {
        val normalizedGuildId = guildId?.asString() ?: "*"
        return "$keyPrefix:$normalizedGuildId:*"
    }


    companion object {
        inline operator fun <K : Any, reified V> invoke(
            objectMapper: ObjectMapper,
            redisTemplate: ReactiveStringRedisTemplate,
            cacheName: String,
            ttl: Duration,
        ) = RedisStringCacheRepository<K, V>(V::class.java, objectMapper, redisTemplate, ttl, cacheName)
    }
}
