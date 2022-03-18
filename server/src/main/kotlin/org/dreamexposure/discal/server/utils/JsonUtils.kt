package org.dreamexposure.discal.server.utils

import reactor.core.publisher.Mono

@Deprecated(message = "Use GenericResponse data class")
internal fun responseMessage(str: String) = Mono.just("""{"message": "$str"}""")
