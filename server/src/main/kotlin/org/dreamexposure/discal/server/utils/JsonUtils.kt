package org.dreamexposure.discal.server.utils

import reactor.core.publisher.Mono

internal fun responseMessage(str: String) = Mono.just("""{"message": "$str"}""")
