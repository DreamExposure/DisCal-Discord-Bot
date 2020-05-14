package org.dreamexposure.discal.client.module.announcement;

import java.time.Duration;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AnnouncementThreader {
    static {
        instance = new AnnouncementThreader();
    }

    private final static AnnouncementThreader instance;

    private AnnouncementThreader() {
    }

    public static AnnouncementThreader getThreader() {
        return instance;
    }

    public Mono<Void> init() {
        return Flux.interval(Duration.ofMinutes(5))
            .onBackpressureBuffer()
            .flatMap(i -> new AnnouncementThread().run())
            .then();
    }
}