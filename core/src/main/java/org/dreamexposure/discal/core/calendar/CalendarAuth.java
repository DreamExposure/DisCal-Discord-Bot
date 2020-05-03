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
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import org.dreamexposure.discal.core.crypto.AESEncryption;
import org.dreamexposure.discal.core.network.google.Authorization;
import org.dreamexposure.discal.core.object.GuildSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class CalendarAuth {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "DisCal";

    /**
     * Directory to store user credentials for this application.
     */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/DisCal");

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     */
    private static Mono<Credential> authorize() {
        return Mono.fromCallable(() -> {
            // Load client secrets.
            //InputStream in = CalendarAuth.class.getResourceAsStream("/client_secret.json"); <- in case it breaks, this is still here
            InputStream in = new FileInputStream(new File("client_secret.json"));
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
            Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

            //Try to close input stream since I don't think it was ever closed?
            in.close();

            return credential;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
    private static Mono<Credential> authorize(GuildSettings g) {
        return Mono.fromCallable(() -> {
            if (g.getEncryptedAccessToken().equalsIgnoreCase("N/a"))
                return null;

            AESEncryption encryption = new AESEncryption(g);
            String accessToken = Authorization.getAuth().requestNewAccessToken(g, encryption);

            Credential credential = new GoogleCredential();
            credential.setAccessToken(accessToken);
            return credential;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public static Mono<Calendar> getCalendarService(GuildSettings g) {
        return Mono.fromCallable(() -> {
            if (g == null) {
                return authorize().map(cred ->
                    new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                        .setApplicationName(APPLICATION_NAME)
                        .build());
            }

            if (g.useExternalCalendar()) {
                return authorize(g).map(cred ->
                    new Calendar.
                        Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), cred)
                        .setApplicationName(APPLICATION_NAME)
                        .build());
            } else {
                return authorize().map(cred ->
                    new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                        .setApplicationName(APPLICATION_NAME)
                        .build());
            }
        }).flatMap(Function.identity());
    }
}