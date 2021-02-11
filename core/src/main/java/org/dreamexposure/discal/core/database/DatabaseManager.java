package org.dreamexposure.discal.core.database;

import org.dreamexposure.discal.core.enums.announcement.AnnouncementModifier;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.object.event.RsvpData;
import org.dreamexposure.discal.core.object.web.UserAPIAccount;
import org.dreamexposure.novautils.database.DatabaseSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import discord4j.common.util.Snowflake;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.ValidationDepth;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.PORT;
import static io.r2dbc.spi.ConnectionFactoryOptions.PROTOCOL;
import static io.r2dbc.spi.ConnectionFactoryOptions.SSL;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"UnusedReturnValue", "unused", "ConstantConditions", "SqlResolve", "MagicNumber"})
public class DatabaseManager {
    private final static DatabaseSettings settings;

    private final static ConnectionPool master;

    private final static ConnectionPool slave;

    private static final Map<Snowflake, GuildSettings> guildSettingsCache = new ConcurrentHashMap<>();

    static {
        settings = new DatabaseSettings("", "", BotSettings.SQL_DB.get(),
            "", "", BotSettings.SQL_PREFIX.get());

        final ConnectionFactory masterFact = ConnectionFactories.get(ConnectionFactoryOptions.builder()
            .option(DRIVER, "pool")
            .option(PROTOCOL, "mysql")
            .option(HOST, BotSettings.SQL_MASTER_HOST.get())
            .option(PORT, Integer.parseInt(BotSettings.SQL_MASTER_PORT.get()))
            .option(USER, BotSettings.SQL_MASTER_USER.get())
            .option(PASSWORD, BotSettings.SQL_MASTER_PASS.get())
            .option(DATABASE, settings.getDatabase())
            .option(SSL, false)
            .build());
        final ConnectionPoolConfiguration masterConf = ConnectionPoolConfiguration.builder(masterFact)
            .build();
        master = new ConnectionPool(masterConf);

        final ConnectionFactory slaveFact = ConnectionFactories.get(ConnectionFactoryOptions.builder()
            .option(DRIVER, "pool")
            .option(PROTOCOL, "mysql")
            .option(HOST, BotSettings.SQL_SLAVE_HOST.get())
            .option(PORT, Integer.parseInt(BotSettings.SQL_SLAVE_PORT.get()))
            .option(USER, BotSettings.SQL_SLAVE_USER.get())
            .option(PASSWORD, BotSettings.SQL_SLAVE_PASS.get())
            .option(DATABASE, settings.getDatabase())
            .option(SSL, false)
            .build());
        final ConnectionPoolConfiguration slaveConf = ConnectionPoolConfiguration.builder(slaveFact)
            .build();
        slave = new ConnectionPool(slaveConf);
    }

    private static <T> Mono<T> connect(final ConnectionPool connectionPool,
                                       final Function<Connection, Mono<T>> connection) {
        return connectionPool.create().flatMap(c -> connection.apply(c)
            .flatMap(item -> Mono.from(c.validate(ValidationDepth.LOCAL))
                .flatMap(validate -> {
                    if (validate) {
                        return Mono.from(c.close()).thenReturn(item);
                    } else {
                        return Mono.just(item);
                    }
                })));
    }

    private DatabaseManager() {
    } //Prevent initialization.

    public static void disconnectFromMySQL() {
        master.dispose();
        slave.dispose();
    }

    public static Mono<Boolean> updateAPIAccount(final UserAPIAccount acc) {
        final String table = String.format("%sapi", settings.getPrefix());

        return connect(slave, c -> {
            final String query = "SELECT * FROM " + table + " WHERE API_KEY = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, acc.getAPIKey())
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> row))
            .hasElements()
            .flatMap(exists -> {
                if (exists) {
                    final String updateCommand = "UPDATE " + table
                        + " SET USER_ID = ?, BLOCKED = ?,"
                        + " WHERE API_KEY = ?";

                    return connect(master, c -> Mono.from(c.createStatement(updateCommand)
                        .bind(0, acc.getUserId())
                        .bind(1, acc.getBlocked())
                        .bind(2, acc.getAPIKey())
                        .execute())
                    ).flatMap(res -> Mono.from(res.getRowsUpdated()))
                        .thenReturn(true);
                } else {
                    final String insertCommand = "INSERT INTO " + table +
                        "(USER_ID, API_KEY, BLOCKED, TIME_ISSUED)" +
                        " VALUES (?, ?, ?, ?)";

                    return connect(master, c -> Mono.from(c.createStatement(insertCommand)
                        .bind(0, acc.getUserId())
                        .bind(1, acc.getAPIKey())
                        .bind(2, acc.getBlocked())
                        .bind(3, acc.getTimeIssued())
                        .execute())
                    ).flatMap(res -> Mono.from(res.getRowsUpdated()))
                        .thenReturn(true);
                }
            }).onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to update API Account", e, DatabaseManager.class));
                return Mono.just(false);
            });
    }

    public static Mono<Boolean> updateSettings(final GuildSettings set) {
        guildSettingsCache.remove(set.getGuildID());
        guildSettingsCache.put(set.getGuildID(), set);

        final String table = String.format("%sguild_settings", settings.getPrefix());

        return connect(slave, c -> {
            final String query = "SELECT * FROM " + table + " WHERE GUILD_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, set.getGuildID().asString())
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> row))
            .hasElements()
            .flatMap(exists -> {
                if (exists) {
                    final String update = "UPDATE " + table
                        + " SET CONTROL_ROLE = ?, DISCAL_CHANNEL = ?, SIMPLE_ANNOUNCEMENT = ?,"
                        + " LANG = ?, PREFIX = ?, PATRON_GUILD = ?, DEV_GUILD = ?,"
                        + " MAX_CALENDARS = ?, DM_ANNOUNCEMENTS = ?, 12_HOUR = ?,"
                        + " BRANDED = ? WHERE GUILD_ID = ?";

                    return connect(master, c -> Mono.from(c.createStatement(update)
                        .bind(0, set.getControlRole())
                        .bind(1, set.getDiscalChannel())
                        .bind(2, set.getSimpleAnnouncements())
                        .bind(3, set.getLang())
                        .bind(4, set.getPrefix())
                        .bind(5, set.getPatronGuild())
                        .bind(6, set.getDevGuild())
                        .bind(7, set.getMaxCalendars())
                        .bind(8, set.getDmAnnouncementsString())
                        .bind(9, set.getTwelveHour())
                        .bind(10, set.getBranded())
                        .bind(11, set.getGuildID().asString())
                        .execute())
                    ).flatMap(res -> Mono.from(res.getRowsUpdated()))
                        .hasElement()
                        .thenReturn(true);
                } else {
                    final String insertCommand = "INSERT INTO " + table + "(GUILD_ID, " +
                        "CONTROL_ROLE, DISCAL_CHANNEL, SIMPLE_ANNOUNCEMENT, LANG, " +
                        "PREFIX, PATRON_GUILD, DEV_GUILD, MAX_CALENDARS, " +
                        "DM_ANNOUNCEMENTS, 12_HOUR, BRANDED) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    return connect(master, c -> Mono.from(c.createStatement(insertCommand)
                        .bind(0, set.getGuildID().asString())
                        .bind(1, set.getControlRole())
                        .bind(2, set.getDiscalChannel())
                        .bind(3, set.getSimpleAnnouncements())
                        .bind(4, set.getLang())
                        .bind(5, set.getPrefix())
                        .bind(6, set.getPatronGuild())
                        .bind(7, set.getDevGuild())
                        .bind(8, set.getMaxCalendars())
                        .bind(9, set.getDmAnnouncementsString())
                        .bind(10, set.getTwelveHour())
                        .bind(11, set.getBranded())
                        .execute())
                    ).flatMap(res -> Mono.from(res.getRowsUpdated()))
                        .hasElement()
                        .thenReturn(true);
                }
            }).onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to update guild settings", e, DatabaseManager.class));
                return Mono.just(false);
            });
    }

    public static Mono<Boolean> updateCalendar(final CalendarData calData) {
        final String table = String.format("%scalendars", settings.getPrefix());

        return connect(slave, c -> {
            final String query = "SELECT * FROM " + table + " WHERE GUILD_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, calData.getGuildId().asString())
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> row))
            .hasElements()
            .flatMap(exists -> {
                if (exists) {
                    final String update = "UPDATE " + table
                        + " SET CALENDAR_NUMBER = ?, CALENDAR_ID = ?,"
                        + " CALENDAR_ADDRESS = ?, EXTERNAL = ?, CREDENTIAL_ID = ?,"
                        + " PRIVATE_KEY = ?, ACCESS_TOKEN = ?, REFRESH TOKEN = ?"
                        + " WHERE GUILD_ID = ?";

                    return connect(master, c -> Mono.from(c.createStatement(update)
                        .bind(0, calData.getCalendarNumber())
                        .bind(1, calData.getCalendarId())
                        .bind(2, calData.getCalendarAddress())
                        .bind(3, calData.getExternal())
                        .bind(4, calData.getCredentialId())
                        .bind(5, calData.getPrivateKey())
                        .bind(6, calData.getEncryptedAccessToken())
                        .bind(7, calData.getEncryptedRefreshToken())
                        .bind(8, calData.getGuildId().asString())
                        .execute())
                    ).flatMap(res -> Mono.from(res.getRowsUpdated()))
                        .hasElement()
                        .thenReturn(true);
                } else {
                    final String insertCommand = "INSERT INTO " + table
                        + "(GUILD_ID, CALENDAR_NUMBER, CALENDAR_ID, " +
                        "CALENDAR_ADDRESS, EXTERNAL, CREDENTIAL_ID, " +
                        "PRIVATE_KEY, ACCESS_TOKEN, REFRESH_TOKEN)" + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    return connect(master, c -> Mono.from(c.createStatement(insertCommand)
                        .bind(0, calData.getGuildId().asString())
                        .bind(1, calData.getCalendarNumber())
                        .bind(2, calData.getCalendarId())
                        .bind(3, calData.getCalendarAddress())
                        .bind(4, calData.getExternal())
                        .bind(5, calData.getCredentialId())
                        .bind(6, calData.getPrivateKey())
                        .bind(7, calData.getEncryptedAccessToken())
                        .bind(8, calData.getEncryptedRefreshToken())
                        .execute())
                    ).flatMap(res -> Mono.from(res.getRowsUpdated()))
                        .hasElement()
                        .thenReturn(true);
                }
            }).onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to update calendar data", e, DatabaseManager.class));
                return Mono.just(false);
            });
    }

    public static Mono<Boolean> updateAnnouncement(final Announcement announcement) {
        final String table = String.format("%sannouncements", settings.getPrefix());

        return connect(slave, c -> {
            final String query = "SELECT * FROM " + table + " WHERE ANNOUNCEMENT_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, announcement.getAnnouncementId().toString())
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> row))
            .hasElements()
            .flatMap(exists -> {
                if (exists) {
                    final String update = "UPDATE " + table
                        + " SET SUBSCRIBERS_ROLE = ?, SUBSCRIBERS_USER = ?, CHANNEL_ID = ?,"
                        + " ANNOUNCEMENT_TYPE = ?, MODIFIER = ?, EVENT_ID = ?, EVENT_COLOR = ?, "
                        + " HOURS_BEFORE = ?, MINUTES_BEFORE = ?,"
                        + " INFO = ?, ENABLED = ?, INFO_ONLY = ?, PUBLISH = ?"
                        + " WHERE ANNOUNCEMENT_ID = ?";

                    return connect(master, c -> Mono.from(c.createStatement(update)
                        .bind(0, announcement.getSubscriberRoleIdString())
                        .bind(1, announcement.getSubscriberUserIdString())
                        .bind(2, announcement.getAnnouncementChannelId())
                        .bind(3, announcement.getType().name())
                        .bind(4, announcement.getModifier().name())
                        .bind(5, announcement.getEventId())
                        .bind(6, announcement.getEventColor().name())
                        .bind(7, announcement.getHoursBefore())
                        .bind(8, announcement.getMinutesBefore())
                        .bind(9, announcement.getInfo())
                        .bind(10, announcement.getEnabled())
                        .bind(11, announcement.getInfoOnly())
                        .bind(12, announcement.getPublish())
                        .bind(13, announcement.getAnnouncementId().toString())
                        .execute())
                    ).flatMap(res -> Mono.from(res.getRowsUpdated()))
                        .thenReturn(true);
                } else {
                    final String insertCommand = "INSERT INTO " + table +
                        "(ANNOUNCEMENT_ID, GUILD_ID, SUBSCRIBERS_ROLE, SUBSCRIBERS_USER, " +
                        "CHANNEL_ID, ANNOUNCEMENT_TYPE, MODIFIER, EVENT_ID, EVENT_COLOR, " +
                        "HOURS_BEFORE, MINUTES_BEFORE, INFO, ENABLED, INFO_ONLY, PUBLISH)" +
                        " VALUE (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    return connect(master, c -> Mono.from(c.createStatement(insertCommand)
                        .bind(0, announcement.getAnnouncementId().toString())
                        .bind(1, announcement.getGuildId().asString())
                        .bind(2, announcement.getSubscriberRoleIdString())
                        .bind(3, announcement.getSubscriberUserIdString())
                        .bind(4, announcement.getAnnouncementChannelId())
                        .bind(5, announcement.getType().name())
                        .bind(6, announcement.getModifier().name())
                        .bind(7, announcement.getEventId())
                        .bind(8, announcement.getEventColor().name())
                        .bind(9, announcement.getHoursBefore())
                        .bind(10, announcement.getMinutesBefore())
                        .bind(11, announcement.getInfo())
                        .bind(12, announcement.getEnabled())
                        .bind(13, announcement.getInfoOnly())
                        .bind(14, announcement.getPublish())
                        .execute())
                    ).flatMap(res -> Mono.from(res.getRowsUpdated()))
                        .thenReturn(true);
                }
            }).onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to update announcement", e, DatabaseManager.class));
                return Mono.just(false);
            });
    }

    public static Mono<Boolean> updateEventData(final EventData data) {
        if (!data.shouldBeSaved()) return Mono.just(false);

        final String table = String.format("%sevents", settings.getPrefix());
        String id = data.getEventId();
        if (data.getEventId().contains("_")) {
            id = data.getEventId().split("_")[0];
        }
        final String idToUse = id;

        return connect(slave, c -> {
            final String query = "SELECT * FROM " + table + " WHERE EVENT_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, idToUse)
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> row))
            .hasElements()
            .flatMap(exists -> {
                if (exists) {
                    final String updateCommand = "UPDATE " + table
                        + " SET IMAGE_LINK = ?, EVENT_END = ?"
                        + " WHERE EVENT_ID = ?";

                    return connect(master, c -> Mono.from(c.createStatement(updateCommand)
                        .bind(0, data.getImageLink())
                        .bind(1, data.getEventEnd())
                        .bind(2, idToUse)
                        .execute())
                    ).flatMap(res -> Mono.from(res.getRowsUpdated()))
                        .thenReturn(true);
                } else {
                    final String insertCommand = "INSERT INTO " + table +
                        "(GUILD_ID, EVENT_ID, EVENT_END, IMAGE_LINK)" +
                        " VALUES (?, ?, ?, ?)";

                    return connect(master, c -> Mono.from(c.createStatement(insertCommand)
                        .bind(0, data.getGuildId().asString())
                        .bind(1, idToUse)
                        .bind(2, data.getEventEnd())
                        .bind(3, data.getImageLink())
                        .execute())
                    ).flatMap(res -> Mono.from(res.getRowsUpdated()))
                        .thenReturn(true);
                }
            }).onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to update event data", e, DatabaseManager.class));
                return Mono.just(false);
            });
    }

    public static Mono<Boolean> updateRsvpData(final RsvpData data) {
        final String table = String.format("%srsvp", settings.getPrefix());

        return connect(slave, c -> {
            final String query = "SELECT * FROM " + table + " WHERE EVENT_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, data.getEventId())
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> row))
            .hasElements()
            .flatMap(exists -> {
                if (exists) {
                    final String update = "UPDATE " + table
                        + " SET EVENT_END = ?,"
                        + " GOING_ON_TIME = ?,"
                        + " GOING_LATE = ?,"
                        + " NOT_GOING = ?,"
                        + " UNDECIDED = ?,"
                        + " RSVP_LIMIT = ?"
                        + " WHERE EVENT_ID = ?";

                    return connect(master, c -> Mono.from(c.createStatement(update)
                        .bind(0, data.getEventEnd())
                        .bind(1, data.getGoingOnTimeString())
                        .bind(2, data.getGoingLateString())
                        .bind(3, data.getNotGoingString())
                        .bind(4, data.getUndecidedString())
                        .bind(5, data.getLimit())
                        .bind(6, data.getEventId())
                        .execute())
                    ).flatMap(res -> Mono.from(res.getRowsUpdated()))
                        .thenReturn(true);
                } else {
                    final String insertCommand = "INSERT INTO " + table +
                        "(GUILD_ID, EVENT_ID, EVENT_END, GOING_ON_TIME, GOING_LATE, " +
                        "NOT_GOING, UNDECIDED, RSVP_LIMIT)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                    return connect(master, c -> Mono.from(c.createStatement(insertCommand)
                        .bind(0, data.getGuildId().asString())
                        .bind(1, data.getEventId())
                        .bind(2, data.getEventEnd())
                        .bind(3, data.getGoingOnTimeString())
                        .bind(4, data.getGoingLateString())
                        .bind(5, data.getNotGoingString())
                        .bind(6, data.getUndecidedString())
                        .bind(7, data.getLimit())
                        .execute())
                    ).flatMap(res -> Mono.from(res.getRowsUpdated()))
                        .thenReturn(true);
                }
            }).onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to update rsvp data", e, DatabaseManager.class));
                return Mono.just(false);
            });
    }

    public static Mono<UserAPIAccount> getAPIAccount(final String APIKey) {
        return connect(slave, c -> {
            final String dataTableName = String.format("%sapi", settings.getPrefix());
            final String query = "SELECT * FROM " + dataTableName + " WHERE API_KEY = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, APIKey)
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> new UserAPIAccount(
            row.get("USER_ID", String.class),
            APIKey,
            row.get("BLOCKED", Boolean.class),
            row.get("TIME_ISSUED", Long.class)
        )))
            .next()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get API user data", e, DatabaseManager.class));
                return Mono.empty();
            });
    }

    public static Mono<GuildSettings> getSettings(final Snowflake guildId) {
        if (guildSettingsCache.containsKey(guildId))
            return Mono.just(guildSettingsCache.get(guildId));

        return connect(slave, c -> {
            final String dataTableName = String.format("%sguild_settings", settings.getPrefix());
            final String query = "SELECT * FROM " + dataTableName + " WHERE GUILD_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, guildId.asString())
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> {
            String controlRole = row.get("CONTROL_ROLE", String.class);
            String discalChannel = row.get("DISCAL_CHANNEL", String.class);
            boolean simpleAnnouncements = row.get("SIMPLE_ANNOUNCEMENT", Boolean.class);
            String lang = row.get("LANG", String.class);
            String prefix = row.get("PREFIX", String.class);
            boolean patron = row.get("PATRON_GUILD", Boolean.class);
            boolean dev = row.get("DEV_GUILD", Boolean.class);
            int maxCals = row.get("MAX_CALENDARS", Integer.class);
            String dmAnnouncementsString = row.get("DM_ANNOUNCEMENTS", String.class);
            boolean twelveHour = row.get("12_HOUR", Boolean.class);
            boolean branded = row.get("BRANDED", Boolean.class);

            GuildSettings settings = new GuildSettings(
                guildId, controlRole, discalChannel, simpleAnnouncements,
                lang, prefix, patron, dev, maxCals, twelveHour, branded
            );

            settings.setDmAnnouncementsString(dmAnnouncementsString);

            //Store in cache...
            guildSettingsCache.remove(guildId);
            guildSettingsCache.put(guildId, settings);

            return settings;
        }))
            .next()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .defaultIfEmpty(GuildSettings.empty(guildId))
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get guild settings", e, DatabaseManager.class));
                return Mono.just(GuildSettings.empty(guildId));
            });
    }

    public static Mono<CalendarData> getMainCalendar(final Snowflake guildId) {
        return connect(slave, c -> {
            final String calendarTableName = String.format("%scalendars", settings.getPrefix());
            final String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = ? " +
                "AND CALENDAR_NUMBER = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, guildId.asString())
                .bind(1, 1)
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> {
            final String calId = row.get("CALENDAR_ID", String.class);
            final String calAddr = row.get("CALENDAR_ADDRESS", String.class);
            final boolean external = row.get("EXTERNAL", Boolean.class);
            final int credId = row.get("CREDENTIAL_ID", Integer.class);
            final String privateKey = row.get("PRIVATE_KEY", String.class);
            final String accessToken = row.get("ACCESS_TOKEN", String.class);
            final String refreshToken = row.get("REFRESH_TOKEN", String.class);

            return new CalendarData(guildId, 1, calId, calAddr, external, credId,
                privateKey, accessToken, refreshToken);
        }))
            .next()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get calendar data", e, DatabaseManager.class));
                return Mono.empty();
            });
    }

    public static Mono<CalendarData> getCalendar(final Snowflake guildId, final int calendarNumber) {
        return connect(slave, c -> {
            final String calendarTableName = String.format("%scalendars", settings.getPrefix());
            final String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = ? AND " +
                "CALENDAR_NUMBER = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, guildId.asString())
                .bind(1, calendarNumber)
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> {
            final String calId = row.get("CALENDAR_ID", String.class);
            final String calAddr = row.get("CALENDAR_ADDRESS", String.class);
            final boolean external = row.get("EXTERNAL", Boolean.class);
            final int credId = row.get("CREDENTIAL_ID", Integer.class);
            final String privateKey = row.get("PRIVATE_KEY", String.class);
            final String accessToken = row.get("ACCESS_TOKEN", String.class);
            final String refreshToken = row.get("REFRESH_TOKEN", String.class);

            return new CalendarData(guildId, calendarNumber, calId, calAddr, external, credId,
                privateKey, accessToken, refreshToken);
        }))
            .next()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get calendar data", e, DatabaseManager.class));
                return Mono.empty();
            });
    }

    public static Mono<List<CalendarData>> getAllCalendars(final Snowflake guildId) {
        return connect(slave, c -> {
            final String calendarTableName = String.format("%scalendars", settings.getPrefix());
            final String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, guildId.asString())
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> {
            final String calId = row.get("CALENDAR_ID", String.class);
            final int calNumber = row.get("CALENDAR_NUMBER", Integer.class);
            final String calAddr = row.get("CALENDAR_ADDRESS", String.class);
            final boolean external = row.get("EXTERNAL", Boolean.class);
            final int credId = row.get("CREDENTIAL_ID", Integer.class);
            final String privateKey = row.get("PRIVATE_KEY", String.class);
            final String accessToken = row.get("ACCESS_TOKEN", String.class);
            final String refreshToken = row.get("REFRESH_TOKEN", String.class);

            return new CalendarData(guildId, calNumber, calId, calAddr, external, credId,
                privateKey, accessToken, refreshToken);
        }))
            .collectList()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get all guild calendars", e, DatabaseManager.class));
                return Mono.just(new ArrayList<>());
            });
    }

    public static Mono<Integer> getCalendarCount() {
        return connect(slave, c -> {
            final String calendarTableName = String.format("%scalendars", settings.getPrefix());
            final String query = "SELECT COUNT(*) FROM " + calendarTableName + ";";

            return Mono.from(c.createStatement(query).execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> {
            final Long calendars = row.get(0, Long.class);

            return calendars == null ? 0 : calendars.intValue();
        }))
            .next()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get calendar count", e, DatabaseManager.class));
                return Mono.just(-1);
            });
    }

    public static Mono<EventData> getEventData(final Snowflake guildId, final String eventId) {
        return connect(slave, c -> {
            String copiedEventId = eventId;
            if (copiedEventId.contains("_"))
                copiedEventId = copiedEventId.split("_")[0];

            final String tableName = String.format("%sevents", settings.getPrefix());
            final String query = "SELECT * FROM " + tableName + " WHERE GUILD_ID= ? AND EVENT_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, guildId.asString())
                .bind(1, copiedEventId)
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> {
            final String id = row.get("EVENT_ID", String.class);
            final long end = row.get("EVENT_END", Long.class);
            final String img = row.get("IMAGE_LINK", String.class);

            return new EventData(guildId, id, end, img);
        }))
            .next()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get event data", e, DatabaseManager.class));
                return Mono.empty();
            });
    }

    public static Mono<RsvpData> getRsvpData(final Snowflake guildId, final String eventId) {
        return connect(slave, c -> {
            final String rsvpTableName = String.format("%srsvp", settings.getPrefix());
            final String query = "SELECT * FROM " + rsvpTableName + " WHERE GUILD_ID= ? AND EVENT_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, guildId.asString())
                .bind(1, eventId)
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> {
            final RsvpData data = new RsvpData(guildId, eventId);
            data.setEventEnd(row.get("EVENT_END", Long.class));
            data.setGoingOnTimeFromString(row.get("GOING_ON_TIME", String.class));
            data.setGoingLateFromString(row.get("GOING_LATE", String.class));
            data.setNotGoingFromString(row.get("NOT_GOING", String.class));
            data.setUndecidedFromString(row.get("UNDECIDED", String.class));
            data.setLimit(row.get("RSVP_LIMIT", Integer.class));

            return data;
        }))
            .next()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get rsvp data", e, DatabaseManager.class));
                return Mono.empty();
            })
            .defaultIfEmpty(new RsvpData(guildId, eventId));
    }

    public static Mono<Announcement> getAnnouncement(final UUID announcementId, final Snowflake guildId) {
        return connect(slave, c -> {
            final String announcementTableName = String.format("%sannouncements", settings.getPrefix());
            final String query = "SELECT * FROM " + announcementTableName + " WHERE ANNOUNCEMENT_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, announcementId.toString())
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> {
            final Announcement a = new Announcement(guildId, announcementId);
            a.setSubscriberRoleIdsFromString(row.get("SUBSCRIBERS_ROLE", String.class));
            a.setSubscriberUserIdsFromString(row.get("SUBSCRIBERS_USER", String.class));
            a.setAnnouncementChannelId(row.get("CHANNEL_ID", String.class));
            a.setType(AnnouncementType.valueOf(row.get("ANNOUNCEMENT_TYPE", String.class)));
            a.setModifier(AnnouncementModifier.valueOf(row.get("MODIFIER", String.class)));
            a.setEventId(row.get("EVENT_ID", String.class));
            a.setEventColor(EventColor.Companion.fromNameOrHexOrId(row.get("EVENT_COLOR", String.class)));
            a.setHoursBefore(row.get("HOURS_BEFORE", Integer.class));
            a.setMinutesBefore(row.get("MINUTES_BEFORE", Integer.class));
            a.setInfo(row.get("INFO", String.class));
            a.setEnabled(row.get("ENABLED", Boolean.class));
            a.setInfoOnly(row.get("INFO_ONLY", Boolean.class));
            a.setPublish(row.get("PUBLISH", Boolean.class));

            return a;
        }))
            .next()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get announcement", e, DatabaseManager.class));
                return Mono.empty();
            });
    }

    public static Mono<List<Announcement>> getAnnouncements(final Snowflake guildId) {
        return connect(slave, c -> {
            final String announcementTableName = String.format("%sannouncements", settings.getPrefix());
            final String query = "SELECT * FROM " + announcementTableName + " WHERE GUILD_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, guildId.asString())
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> {
            final UUID announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String.class));

            final Announcement a = new Announcement(guildId, announcementId);
            a.setSubscriberRoleIdsFromString(row.get("SUBSCRIBERS_ROLE", String.class));
            a.setSubscriberUserIdsFromString(row.get("SUBSCRIBERS_USER", String.class));
            a.setAnnouncementChannelId(row.get("CHANNEL_ID", String.class));
            a.setType(AnnouncementType.valueOf(row.get("ANNOUNCEMENT_TYPE", String.class)));
            a.setModifier(AnnouncementModifier.valueOf(row.get("MODIFIER", String.class)));
            a.setEventId(row.get("EVENT_ID", String.class));
            a.setEventColor(EventColor.Companion.fromNameOrHexOrId(row.get("EVENT_COLOR", String.class)));
            a.setHoursBefore(row.get("HOURS_BEFORE", Integer.class));
            a.setMinutesBefore(row.get("MINUTES_BEFORE", Integer.class));
            a.setInfo(row.get("INFO", String.class));
            a.setEnabled(row.get("ENABLED", Boolean.class));
            a.setInfoOnly(row.get("INFO_ONLY", Boolean.class));
            a.setPublish(row.get("PUBLISH", Boolean.class));

            return a;
        }))
            .collectList()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get all announcements for guild", e,
                    DatabaseManager.class));

                return Mono.just(new ArrayList<>());
            });
    }

    public static Mono<List<Announcement>> getAnnouncements() {
        return connect(slave, c -> {
            final String announcementTableName = String.format("%sannouncements", settings.getPrefix());
            final String query = "SELECT * FROM " + announcementTableName + ";";

            return Mono.from(c.createStatement(query)
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> {
            final UUID announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String.class));
            final Snowflake guildId = Snowflake.of(row.get("GUILD_ID", String.class));

            final Announcement a = new Announcement(guildId, announcementId);
            a.setSubscriberRoleIdsFromString(row.get("SUBSCRIBERS_ROLE", String.class));
            a.setSubscriberUserIdsFromString(row.get("SUBSCRIBERS_USER", String.class));
            a.setAnnouncementChannelId(row.get("CHANNEL_ID", String.class));
            a.setType(AnnouncementType.valueOf(row.get("ANNOUNCEMENT_TYPE", String.class)));
            a.setModifier(AnnouncementModifier.valueOf(row.get("MODIFIER", String.class)));
            a.setEventId(row.get("EVENT_ID", String.class));
            a.setEventColor(EventColor.Companion.fromNameOrHexOrId(row.get("EVENT_COLOR", String.class)));
            a.setHoursBefore(row.get("HOURS_BEFORE", Integer.class));
            a.setMinutesBefore(row.get("MINUTES_BEFORE", Integer.class));
            a.setInfo(row.get("INFO", String.class));
            a.setEnabled(row.get("ENABLED", Boolean.class));
            a.setInfoOnly(row.get("INFO_ONLY", Boolean.class));
            a.setPublish(row.get("PUBLISH", Boolean.class));

            return a;
        }))
            .collectList()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get announcements by type", e, DatabaseManager.class));

                return Mono.just(new ArrayList<>());
            });
    }

    public static Mono<List<Announcement>> getAnnouncements(final AnnouncementType type) {
        return connect(slave, c -> {
            final String announcementTableName = String.format("%sannouncements", settings.getPrefix());
            final String query = "SELECT * FROM " + announcementTableName
                + " WHERE ANNOUNCEMENT_TYPE = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, type.name())
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> {
            final UUID announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String.class));
            final Snowflake guildId = Snowflake.of(row.get("GUILD_ID", String.class));

            final Announcement a = new Announcement(guildId, announcementId);
            a.setSubscriberRoleIdsFromString(row.get("SUBSCRIBERS_ROLE", String.class));
            a.setSubscriberUserIdsFromString(row.get("SUBSCRIBERS_USER", String.class));
            a.setAnnouncementChannelId(row.get("CHANNEL_ID", String.class));
            a.setType(AnnouncementType.valueOf(row.get("ANNOUNCEMENT_TYPE", String.class)));
            a.setModifier(AnnouncementModifier.valueOf(row.get("MODIFIER", String.class)));
            a.setEventId(row.get("EVENT_ID", String.class));
            a.setEventColor(EventColor.Companion.fromNameOrHexOrId(row.get("EVENT_COLOR", String.class)));
            a.setHoursBefore(row.get("HOURS_BEFORE", Integer.class));
            a.setMinutesBefore(row.get("MINUTES_BEFORE", Integer.class));
            a.setInfo(row.get("INFO", String.class));
            a.setEnabled(row.get("ENABLED", Boolean.class));
            a.setInfoOnly(row.get("INFO_ONLY", Boolean.class));
            a.setPublish(row.get("PUBLISH", Boolean.class));

            return a;
        }))
            .collectList()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get announcements by type", e, DatabaseManager.class));

                return Mono.just(new ArrayList<>());
            });
    }

    public static Mono<List<Announcement>> getEnabledAnnouncements() {
        return connect(slave, c -> {
            final String announcementTableName = String.format("%sannouncements", settings.getPrefix());
            final String query = "SELECT * FROM " + announcementTableName + " WHERE ENABLED = 1";

            return Mono.from(c.createStatement(query)
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> {
            final UUID announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String.class));
            final Snowflake guildId = Snowflake.of(row.get("GUILD_ID", String.class));

            final Announcement a = new Announcement(guildId, announcementId);
            a.setSubscriberRoleIdsFromString(row.get("SUBSCRIBERS_ROLE", String.class));
            a.setSubscriberUserIdsFromString(row.get("SUBSCRIBERS_USER", String.class));
            a.setAnnouncementChannelId(row.get("CHANNEL_ID", String.class));
            a.setType(AnnouncementType.valueOf(row.get("ANNOUNCEMENT_TYPE", String.class)));
            a.setModifier(AnnouncementModifier.valueOf(row.get("MODIFIER", String.class)));
            a.setEventId(row.get("EVENT_ID", String.class));
            a.setEventColor(EventColor.Companion.fromNameOrHexOrId(row.get("EVENT_COLOR", String.class)));
            a.setHoursBefore(row.get("HOURS_BEFORE", Integer.class));
            a.setMinutesBefore(row.get("MINUTES_BEFORE", Integer.class));
            a.setInfo(row.get("INFO", String.class));
            a.setEnabled(row.get("ENABLED", Boolean.class));
            a.setInfoOnly(row.get("INFO_ONLY", Boolean.class));
            a.setPublish(row.get("PUBLISH", Boolean.class));

            return a;
        }))
            .collectList()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get enabled announcements", e, DatabaseManager.class));

                return Mono.just(new ArrayList<>());
            });
    }

    public static Mono<List<Announcement>> getEnabledAnnouncements(final AnnouncementType type) {
        return connect(slave, c -> {
            final String announcementTableName = String.format("%sannouncements", settings.getPrefix());
            final String query = "SELECT * FROM " + announcementTableName
                + " WHERE ENABLED = 1 AND ANNOUNCEMENT_TYPE = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, type.name())
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> {
            final UUID announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String.class));
            final Snowflake guildId = Snowflake.of(row.get("GUILD_ID", String.class));

            final Announcement a = new Announcement(guildId, announcementId);
            a.setSubscriberRoleIdsFromString(row.get("SUBSCRIBERS_ROLE", String.class));
            a.setSubscriberUserIdsFromString(row.get("SUBSCRIBERS_USER", String.class));
            a.setAnnouncementChannelId(row.get("CHANNEL_ID", String.class));
            a.setType(AnnouncementType.valueOf(row.get("ANNOUNCEMENT_TYPE", String.class)));
            a.setModifier(AnnouncementModifier.valueOf(row.get("MODIFIER", String.class)));
            a.setEventId(row.get("EVENT_ID", String.class));
            a.setEventColor(EventColor.Companion.fromNameOrHexOrId(row.get("EVENT_COLOR", String.class)));
            a.setHoursBefore(row.get("HOURS_BEFORE", Integer.class));
            a.setMinutesBefore(row.get("MINUTES_BEFORE", Integer.class));
            a.setInfo(row.get("INFO", String.class));
            a.setEnabled(row.get("ENABLED", Boolean.class));
            a.setInfoOnly(row.get("INFO_ONLY", Boolean.class));
            a.setPublish(row.get("PUBLISH", Boolean.class));

            return a;
        }))
            .collectList()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get announcements by type", e, DatabaseManager.class));

                return Mono.just(new ArrayList<>());
            });
    }

    public static Mono<List<Announcement>> getEnabledAnnouncements(final Snowflake guildId) {
        return connect(slave, c -> {
            final String announcementTableName = String.format("%sannouncements", settings.getPrefix());
            final String query = "SELECT * FROM " + announcementTableName
                + " WHERE ENABLED = 1 AND GUILD_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, guildId.asString())
                .execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> {
            final UUID announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String.class));

            final Announcement a = new Announcement(guildId, announcementId);
            a.setSubscriberRoleIdsFromString(row.get("SUBSCRIBERS_ROLE", String.class));
            a.setSubscriberUserIdsFromString(row.get("SUBSCRIBERS_USER", String.class));
            a.setAnnouncementChannelId(row.get("CHANNEL_ID", String.class));
            a.setType(AnnouncementType.valueOf(row.get("ANNOUNCEMENT_TYPE", String.class)));
            a.setModifier(AnnouncementModifier.valueOf(row.get("MODIFIER", String.class)));
            a.setEventId(row.get("EVENT_ID", String.class));
            a.setEventColor(EventColor.Companion.fromNameOrHexOrId(row.get("EVENT_COLOR", String.class)));
            a.setHoursBefore(row.get("HOURS_BEFORE", Integer.class));
            a.setMinutesBefore(row.get("MINUTES_BEFORE", Integer.class));
            a.setInfo(row.get("INFO", String.class));
            a.setEnabled(row.get("ENABLED", Boolean.class));
            a.setInfoOnly(row.get("INFO_ONLY", Boolean.class));
            a.setPublish(row.get("PUBLISH", Boolean.class));

            return a;
        }))
            .collectList()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get enabled announcements for guild", e,
                    DatabaseManager.class));

                return Mono.just(new ArrayList<>());
            });
    }

    public static Mono<Integer> getAnnouncementCount() {
        return connect(slave, c -> {
            final String announcementTableName = String.format("%sannouncements", settings.getPrefix());
            final String query = "SELECT COUNT(*) FROM " + announcementTableName + ";";

            return Mono.from(c.createStatement(query).execute());
        }).flatMapMany(res -> res.map((row, rowMetadata) -> {
            final Long announcements = row.get(0, Long.class);
            return announcements == null ? 0 : announcements.intValue();
        }))
            .next()
            .retryWhen(Retry.max(3)
                .filter(IllegalStateException.class::isInstance)
                .filter(e -> e.getMessage() != null && e.getMessage().contains("Request queue was disposed"))
            )
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to get announcement count", e, DatabaseManager.class));
                return Mono.just(-1);
            });
    }

    public static Mono<Boolean> deleteAnnouncement(final String announcementId) {
        return connect(master, c -> {
            final String announcementTableName = String.format("%sannouncements", settings.getPrefix());
            final String query = "DELETE FROM " + announcementTableName + " WHERE ANNOUNCEMENT_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, announcementId)
                .execute());
        }).flatMapMany(Result::getRowsUpdated)
            .then(Mono.just(true))
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to delete announcement", e, DatabaseManager.class));
                return Mono.just(false);
            });
    }

    public static Mono<Boolean> deleteAnnouncementsForEvent(final Snowflake guildId, final String eventId) {
        return connect(master, c -> {
            final String announcementTableName = String.format("%sannouncements", settings.getPrefix());
            final String query = "DELETE FROM " + announcementTableName + " WHERE EVENT_ID = ? AND " +
                "GUILD_ID = ? AND ANNOUNCEMENT_TYPE = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, eventId)
                .bind(1, guildId.asString())
                .bind(2, AnnouncementType.SPECIFIC.name())
                .execute());
        }).flatMapMany(Result::getRowsUpdated)
            .then(Mono.just(true))
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to delete announcements for event", e,
                    DatabaseManager.class));
                return Mono.just(false);
            });
    }

    public static Mono<Boolean> deleteEventData(final String eventId) {
        return connect(master, c -> {
            final String eventTable = String.format("%sevents", settings.getPrefix());
            //Check if recurring...
            if (eventId.contains("_"))
                return Mono.empty(); //Don't delete if child event of recurring event.
            final String query = "DELETE FROM " + eventTable + " WHERE EVENT_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, eventId)
                .execute());
        }).flatMapMany(Result::getRowsUpdated)
            .then(Mono.just(true))
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to delete event data", e, DatabaseManager.class));
                return Mono.just(false);
            });
    }

    public static Mono<Boolean> deleteAllEventData(final Snowflake guildId) {
        return connect(master, c -> {
            final String eventTable = String.format("%sevents", settings.getPrefix());
            final String query = "DELETE FROM " + eventTable + " WHERE GUILD_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, guildId.asString())
                .execute());

        }).flatMapMany(Result::getRowsUpdated)
            .then(Mono.just(true))
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to delete all event data for guild", e,
                    DatabaseManager.class));
                return Mono.just(false);
            });
    }

    public static Mono<Boolean> deleteAllAnnouncementData(final Snowflake guildId) {
        return connect(master, c -> {
            final String announcementTable = String.format("%sannouncements", settings.getPrefix());
            final String query = "DELETE FROM " + announcementTable + " WHERE GUILD_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, guildId.asString())
                .execute());
        }).flatMapMany(Result::getRowsUpdated)
            .then(Mono.just(true))
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to delete all announcements for guild", e,
                    DatabaseManager.class));
                return Mono.just(false);
            });
    }

    public static Mono<Boolean> deleteAllRSVPData(final Snowflake guildId) {
        return connect(master, c -> {
            final String rsvpTable = String.format("%srsvp", settings.getPrefix());
            final String query = "DELETE FROM " + rsvpTable + " WHERE GUILD_ID = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, guildId.asString())
                .execute());
        }).flatMapMany(Result::getRowsUpdated)
            .then(Mono.just(true))
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to delete all rsvps for guild", e, DatabaseManager.class));
                return Mono.just(false);
            });
    }

    public static Mono<Boolean> deleteCalendar(final CalendarData data) {
        return connect(master, c -> {
            final String calendarTable = String.format("%scalendars", settings.getPrefix());
            final String query = "DELETE FROM " + calendarTable + " WHERE GUILD_ID = ? AND " +
                "CALENDAR_ADDRESS = ?";

            return Mono.from(c.createStatement(query)
                .bind(0, data.getGuildId().asString())
                .bind(1, data.getCalendarAddress())
                .execute());
        }).flatMapMany(Result::getRowsUpdated)
            .then(Mono.just(true))
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("Failed to delete calendar", e, DatabaseManager.class));
                return Mono.just(false);
            });
    }

    public static void clearCache() {
        guildSettingsCache.clear();
    }
}
