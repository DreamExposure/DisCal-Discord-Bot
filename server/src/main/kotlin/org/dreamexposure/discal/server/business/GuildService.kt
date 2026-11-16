package org.dreamexposure.discal.server.business

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.awaitSingle
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dreamexposure.discal.PartialGuildModelCache
import org.dreamexposure.discal.core.business.api.CamApiWrapper
import org.dreamexposure.discal.core.`object`.new.model.discal.PartialGuildV3Model
import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.dreamexposure.discal.core.`object`.new.model.discord.PartialGuildData
import kotlin.collections.map
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

@Component
class GuildService(
    val camApiWrapper: CamApiWrapper,
    val partialGuildModelCache: PartialGuildModelCache,
    val httpClient: OkHttpClient,
    val objectMapper: ObjectMapper,
) {
    suspend fun getGuildsForUser(userId: Snowflake): List<PartialGuildV3Model> {
        val cached = partialGuildModelCache.get(key = userId)?.toList()
        if (cached != null) return cached

        // Get user's oauth token, since we cannot do this via the bot api
        val discordTokenResponse = camApiWrapper.getDiscordToken(userId)

        // Can't fetch without an oauth token, not technically a state we need to error on
        if (discordTokenResponse.entity == null) return emptyList()

        // Fetch raw data from discord
        val discordGuildsRequest = Request.Builder()
            .get()
            .url("${GlobalVal.discordApiUrl}/users/@me/guilds")
            .header("Authorization", "Bearer ${discordTokenResponse.entity?.accessToken}")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build()
        val discordGuildsResponse = Mono.fromCallable(httpClient.newCall(discordGuildsRequest)::execute)
            .subscribeOn(Schedulers.boundedElastic())
            .awaitSingle()

        if (discordGuildsResponse.isSuccessful) {
            val discordGuilds = objectMapper.readValue<List<PartialGuildData>>(discordGuildsResponse.body.string())
            discordGuildsResponse.close()

            // Map to our partial guild object
            val partialGuilds = discordGuilds.map {
                PartialGuildV3Model(
                    id = it.id,
                    name = it.name,
                    iconUrl = if (it.icon.isPresent) "${GlobalVal.discordCdnUrl}/icons/${it.id.asString()}/${it.icon.get()}.png" else null,
                    isOwner = it.owner.toOptional().getOrDefault(false),
                    permissions = it.permissions.toOptional().getOrNull()?.toString()
                )
            }

            partialGuildModelCache.put(key = userId, value = partialGuilds.toTypedArray())
            return partialGuilds
        } else {
            throw UnknownError("Failed to get guilds for user $userId | Code: ${discordGuildsResponse.code}")
        }
    }
}