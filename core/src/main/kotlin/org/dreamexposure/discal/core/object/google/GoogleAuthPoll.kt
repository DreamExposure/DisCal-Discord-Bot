package org.dreamexposure.discal.core.`object`.google

import reactor.core.publisher.Mono

open class GoogleAuthPoll(
        open var interval: Int,
        open val expiresIn: Int,
        open var remainingSeconds: Int,
        open val deviceCode: String,
        open val callback: ((poll: GoogleAuthPoll) -> Mono<Void>),
)
