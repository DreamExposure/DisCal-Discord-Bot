package org.dreamexposure.discal.client.listeners.discord

import discord4j.core.event.domain.Event
import org.springframework.core.GenericTypeResolver

interface EventListener<T: Event> {
    @Suppress("UNCHECKED_CAST")
    val genericType: Class<T>
        get() = GenericTypeResolver.resolveTypeArgument(javaClass, EventListener::class.java) as Class<T>

    suspend fun handle(event: T)
}
