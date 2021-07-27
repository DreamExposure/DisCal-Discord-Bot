package org.dreamexposure.discal.core.`object`.google

import discord4j.core.`object`.entity.User
import org.dreamexposure.discal.core.`object`.GuildSettings
import reactor.core.publisher.Mono

data class ExternalGoogleAuthPoll(
        val user: User,
        val settings: GuildSettings,
        override var interval: Int,
        override val expiresIn: Int,
        override var remainingSeconds: Int,
        override val deviceCode: String,
        override val callback: ((poll: GoogleAuthPoll) -> Mono<Void>)
): GoogleAuthPoll(interval, expiresIn, remainingSeconds, deviceCode, callback)
