package org.dreamexposure.discal.client.listeners.discord;

import org.dreamexposure.discal.core.database.DatabaseManager;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.role.RoleDeleteEvent;
import reactor.core.publisher.Mono;

public class RoleDeleteListener {
    public static Mono<Void> handle(final RoleDeleteEvent event) {
        Mono<Boolean> updateRsvps = DatabaseManager.removeRSVPRole(event.getGuildId(), event.getRoleId());

        Mono<Boolean> updateControlRole = DatabaseManager.getSettings(event.getGuildId())
            .filter(settings -> !"everyone".equalsIgnoreCase(settings.getControlRole()))
            .filter(settings -> event.getRoleId().equals(Snowflake.of(settings.getControlRole())))
            .doOnNext(settings -> settings.setControlRole("everyone"))
            .flatMap(DatabaseManager::updateSettings);

        return Mono.when(updateRsvps, updateControlRole);
    }
}
