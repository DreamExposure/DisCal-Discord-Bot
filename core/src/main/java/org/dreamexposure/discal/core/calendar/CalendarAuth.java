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
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: https://www.dreamexposure.org
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"ReturnOfNull"})
public class CalendarAuth {
    private final static String APPLICATION_NAME = "DisCal";

    private final static List<DisCalGoogleCredential> CREDENTIALS;

    static {
        try {
            List<DisCalGoogleCredential> credentials = new ArrayList<>();

            int credCount = Integer.parseInt(BotSettings.CREDENTIALS_COUNT.get());
            for (int i = 0; i < credCount; i++) {
                DatabaseManager.INSTANCE.getCredentialData(i)
                    .map(DisCalGoogleCredential::new)
                    .doOnNext(credentials::add)
                    .block();
            }

            CREDENTIALS = Collections.unmodifiableList(credentials);

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
            throw new RuntimeException(t); //Never reached, makes compiler happy :)
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     */
    private static Mono<Credential> authorize(int credentialId) {
        return Mono.fromCallable(() -> {
            DisCalGoogleCredential cred = getCredential(credentialId);

            //Handle refreshing... TODO: Make this more optimized!!..
            String accessToken = Authorization.getAuth().requestNewAccessToken(cred);

            //Create google's cred and pass it along
            Credential credential = new GoogleCredential();
            credential.setAccessToken(accessToken);
            return credential;
        }).subscribeOn(Schedulers.boundedElastic());
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
        }).subscribeOn(Schedulers.boundedElastic());
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
        }).flatMap(Function.identity());
    }

    public static Mono<Calendar> getCalendarService(int credentialId) {
        return authorize(credentialId).map(cred -> new Calendar
            .Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), cred)
            .setApplicationName(APPLICATION_NAME)
            .build());
    }

    public static Mono<Calendar> getExternalCalendarService(CalendarData calendarData) {
        return authorize(calendarData).map(cred ->
            new Calendar.
                Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), cred)
                .setApplicationName(APPLICATION_NAME)
                .build());
    }

    private static @Nullable DisCalGoogleCredential getCredential(int id) {
        for (DisCalGoogleCredential c : CREDENTIALS) {
            if (c.getCredentialData().getCredentialNumber() == id) {
                return c;
            }
        }

        return null;
    }

    public static int credentialsCount() {
        return CREDENTIALS.size();
    }
}
