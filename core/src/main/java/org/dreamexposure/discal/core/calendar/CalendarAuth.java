package org.dreamexposure.discal.core.calendar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import org.dreamexposure.discal.core.crypto.AESEncryption;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.entities.google.DisCalGoogleCredential;
import org.dreamexposure.discal.core.network.google.Authorization;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Function;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: https://www.dreamexposure.org
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"ReturnOfNull"})
public class CalendarAuth {
    private final static String APPLICATION_NAME = "DisCal";

    private final static Flux<DisCalGoogleCredential> CREDENTIALS;

    static {
        int credCount = Integer.parseInt(BotSettings.CREDENTIALS_COUNT.get());
        CREDENTIALS = Flux.range(0, credCount)
            .flatMap(DatabaseManager.INSTANCE::getCredentialData)
            .map(DisCalGoogleCredential::new)
            .doOnError(ignored -> System.exit(1))
            .cache();
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     */
    private static Mono<Credential> authorize(int credentialId) {
        return CREDENTIALS.filter(c -> c.getCredentialData().getCredentialNumber() == credentialId)
            .next().map(cred -> {
                //Handle refreshing... TODO: Make this more optimized!!..
                String accessToken = Authorization.getAuth().requestNewAccessToken(cred);

                //Create google's cred and pass it along
                Credential credential = new GoogleCredential();
                credential.setAccessToken(accessToken);
                return credential;
            })
            .subscribeOn(Schedulers.boundedElastic())
            .switchIfEmpty(Mono.error(new IllegalStateException("Empty not allowed")));
    }

    private static Mono<Credential> authorize(CalendarData calData) {
        return Mono.fromCallable(() -> {
            if ("N/a".equalsIgnoreCase(calData.getEncryptedAccessToken()))
                return null;

            AESEncryption encryption = new AESEncryption(calData.getPrivateKey());
            String accessToken = Authorization.getAuth().requestNewAccessToken(calData, encryption);

            Credential credential = new GoogleCredential();
            credential.setAccessToken(accessToken);
            return credential;
        })
            .subscribeOn(Schedulers.boundedElastic())
            .switchIfEmpty(Mono.error(new IllegalStateException("Empty not allowed")));
    }

    public static Mono<Calendar> getCalendarService(@NotNull CalendarData calData) {
        return Mono.fromCallable(() -> {
            if (calData.getExternal()) {
                return authorize(calData).map(cred ->
                    new Calendar.
                        Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), cred)
                        .setApplicationName(APPLICATION_NAME)
                        .build());
            } else {
                return getCalendarService(calData.getCredentialId());
            }
        })
            .flatMap(Function.identity())
            .switchIfEmpty(Mono.error(new IllegalStateException("Empty not allowed")));
    }

    public static Mono<Calendar> getCalendarService(int credentialId) {
        return authorize(credentialId).map(cred -> new Calendar
            .Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), cred)
            .setApplicationName(APPLICATION_NAME)
            .build())
            .switchIfEmpty(Mono.error(new IllegalStateException("Empty not allowed")));
    }

    public static Mono<Calendar> getExternalCalendarService(CalendarData calendarData) {
        return authorize(calendarData).map(cred -> new Calendar
            .Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), cred)
            .setApplicationName(APPLICATION_NAME)
            .build())
            .switchIfEmpty(Mono.error(new IllegalStateException("Empty not allowed")));
    }

    public static Flux<Calendar> getAllDisCalServices() {
        return credentialsCount()
            .flatMapMany(count -> Flux.range(0, count))
            .flatMap(CalendarAuth::getCalendarService)
            .switchIfEmpty(Mono.error(new IllegalStateException("Empty not allowed")));
    }

    public static Mono<Integer> credentialsCount() {
        return CREDENTIALS.count()
            .map(Long::intValue);
    }
}
