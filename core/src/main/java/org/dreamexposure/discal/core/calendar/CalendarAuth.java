package org.dreamexposure.discal.core.calendar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import org.dreamexposure.discal.core.crypto.AESEncryption;
import org.dreamexposure.discal.core.google.MySQLDataStoreFactory;
import org.dreamexposure.discal.core.network.google.Authorization;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: https://www.dreamexposure.org
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"ConstantConditions", "ReturnOfNull"})
public class CalendarAuth {
    /**
     * Application name.
     */
    private final static String APPLICATION_NAME = "DisCal";

    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-java-quickstart
     */
    private final static List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR, CalendarScopes.CALENDAR_EVENTS);

    private final static List<DisCalCredential> CREDENTIALS;

    static {
        try {
            List<DisCalCredential> credentials = new ArrayList<>();

            int credCount = Integer.parseInt(BotSettings.CREDENTIALS_COUNT.get());
            for (int i = 0; i < credCount; i++) {
                credentials.add(new DisCalCredential(i,
                    new MySQLDataStoreFactory(i), //Using custom data store impl.
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance()));
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
            // Load client secrets.
            InputStream in = new FileInputStream("client_secret.json");
            GoogleClientSecrets clientSecrets = GoogleClientSecrets
                .load(JacksonFactory.getDefaultInstance(), new InputStreamReader(in));

            // Build flow and trigger user authorization request.
            DisCalCredential cred = getCredential(credentialId);

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(cred.getTransport(), cred.getJsonFactory(), clientSecrets, SCOPES)
                .setDataStoreFactory(cred.getStoreFactory())
                .setAccessType("offline")
                .build();

            Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())
                .authorize("user");

            //Try to close input stream since I don't think it was ever closed?
            in.close();

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
        DisCalCredential disCalCredential = getCredential(credentialId);

        return authorize(credentialId).map(cred -> new Calendar
            .Builder(disCalCredential.getTransport(), disCalCredential.getJsonFactory(), cred)
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

    private static @Nullable DisCalCredential getCredential(int id) {
        for (DisCalCredential c : CREDENTIALS) {
            if (c.getCredentialId() == id) {
                return c;
            }
        }

        return null;
    }

    public static int credentialsCount() {
        return CREDENTIALS.size();
    }


    private static class DisCalCredential {
        private final int credentialId;

        private final DataStoreFactory storeFactory;

        private final HttpTransport transport;

        private final JsonFactory jsonFactory;

        DisCalCredential(int id, DataStoreFactory store, HttpTransport transport, JsonFactory jsonFactory) {
            this.credentialId = id;
            this.storeFactory = store;
            this.transport = transport;
            this.jsonFactory = jsonFactory;
        }

        int getCredentialId() {
            return this.credentialId;
        }

        DataStoreFactory getStoreFactory() {
            return this.storeFactory;
        }

        HttpTransport getTransport() {
            return this.transport;
        }

        JsonFactory getJsonFactory() {
            return this.jsonFactory;
        }
    }
}
