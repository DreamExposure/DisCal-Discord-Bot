package org.dreamexposure.discal.client.network.google;

import org.dreamexposure.discal.core.exceptions.GoogleAuthCancelException;
import org.dreamexposure.discal.core.object.network.google.Poll;

import java.time.Duration;

import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class PollManager {
    static {
        instance = new PollManager();
    }

    private final static PollManager instance;

    //Prevent initialization.
    private PollManager() {
    }

    public static PollManager getManager() {
        return instance;
    }

    //Timer methods.
    public void scheduleNextPoll(Poll poll) {
        Mono.defer(() -> {
            poll.setRemainingSeconds(poll.getRemainingSeconds() - poll.getInterval());
            return GoogleExternalAuth.getAuth().pollForAuth(poll);
        }).then(Mono.defer(() -> Mono.delay(Duration.ofSeconds(poll.getInterval()))))
            .repeat()
            .then()
            .onErrorResume(GoogleAuthCancelException.class, e -> Mono.empty())
            .subscribe();
    }
}