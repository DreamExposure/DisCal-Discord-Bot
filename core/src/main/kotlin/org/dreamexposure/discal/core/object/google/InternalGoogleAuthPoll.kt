package org.dreamexposure.discal.core.`object`.google

import reactor.core.publisher.Mono

data class InternalGoogleAuthPoll(
        val credNumber: Int,
        override var interval: Int,
        override val expiresIn: Int,
        override var remainingSeconds: Int,
        override val deviceCode: String,
        override val callback: ((poll: GoogleAuthPoll) -> Mono<Void>)
): GoogleAuthPoll(interval, expiresIn, remainingSeconds, deviceCode, callback)
