package org.dreamexposure.discal.core.utils;

import org.springframework.core.io.ClassPathResource;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import discord4j.rest.util.Snowflake;
import okhttp3.MediaType;

public class GlobalConst {
    static {
        String version1;
        String d4jVersion1;

        ClassPathResource resource = new ClassPathResource("application.properties");
        Properties p = new Properties();
        try (InputStream inputStream = resource.getInputStream()) {
            p.load(inputStream);

            version1 = p.getProperty("application.version");
            d4jVersion1 = "Discord4J v" + p.getProperty("library.discord4j.version");
        } catch (IOException e) {
            version1 = "Unknown";
            d4jVersion1 = "Unknown";
        }
        version = version1;
        d4jVersion = d4jVersion1;
    }

    public static final String version;
    public static final String d4jVersion;

    public static String iconUrl;
    public static final String lineBreak = System.getProperty("line.separator");
    public static final Snowflake novaId = Snowflake.of(130510525770629121L);
    public static final Snowflake xaanitId = Snowflake.of(233611560545812480L);
    public static final Snowflake calId = Snowflake.of(142107863307780097L);
    public static final Snowflake dreamId = Snowflake.of(142107863307780097L);

    public static final Color discalColor = new Color(56, 138, 237);
    public static final String discalSite = "https://www.discalbot.com";
    public static final String supportInviteLink = "https://discord.gg/2TFqyuy";
    public static final String discalDashboardLink = "https://www.discalbot.com/dashboard";

    public static final long oneMinuteMs = 60000;
    public static final long oneHourMs = 3600000;
    public static final long oneDayMs = 86400000;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
}