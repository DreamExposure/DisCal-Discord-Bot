package org.dreamexposure.discal.core.database;

import org.dreamexposure.discal.core.crypto.KeyGenerator;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import discord4j.rest.util.Snowflake;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.ValidationDepth;
import reactor.core.publisher.Mono;

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
@SuppressWarnings({"UnusedReturnValue", "Duplicates", "unused", "ConstantConditions", "SqlResolve"})
public class DatabaseManager {
	private final static DatabaseSettings settings;

	private final static ConnectionPool master;

	private final static ConnectionPool slave;

	private static final Map<Snowflake, GuildSettings> guildSettingsCache = new HashMap<>();

	static {
		settings = new DatabaseSettings("", "", BotSettings.SQL_DB.get(),
				"", "", BotSettings.SQL_PREFIX.get());

		ConnectionFactory masterFact = ConnectionFactories.get(ConnectionFactoryOptions.builder()
				.option(DRIVER, "pool")
				.option(PROTOCOL, "mysql")
				.option(HOST, BotSettings.SQL_MASTER_HOST.get())
				.option(PORT, Integer.parseInt(BotSettings.SQL_MASTER_PORT.get()))
				.option(USER, BotSettings.SQL_MASTER_USER.get())
				.option(PASSWORD, BotSettings.SQL_MASTER_PASS.get())
				.option(DATABASE, settings.getDatabase())
				.option(SSL, false)
				.build());
		ConnectionPoolConfiguration masterConf = ConnectionPoolConfiguration.builder(masterFact)
				.build();
		master = new ConnectionPool(masterConf);

		ConnectionFactory slaveFact = ConnectionFactories.get(ConnectionFactoryOptions.builder()
				.option(DRIVER, "pool")
				.option(PROTOCOL, "mysql")
				.option(HOST, BotSettings.SQL_SLAVE_HOST.get())
				.option(PORT, Integer.parseInt(BotSettings.SQL_SLAVE_PORT.get()))
				.option(USER, BotSettings.SQL_SLAVE_USER.get())
				.option(PASSWORD, BotSettings.SQL_SLAVE_PASS.get())
				.option(DATABASE, settings.getDatabase())
				.option(SSL, false)
				.build());
		ConnectionPoolConfiguration slaveConf = ConnectionPoolConfiguration.builder(slaveFact)
				.build();
		slave = new ConnectionPool(slaveConf);
	}

	private static <T> Mono<T> connect(ConnectionPool connectionPool,
									   Function<Connection, Mono<T>> connection) {
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

	public static Mono<Boolean> updateAPIAccount(UserAPIAccount acc) {
		String table = String.format("%sapi", settings.getPrefix());

		return connect(slave, c -> {
			String query = "SELECT * FROM " + table + " WHERE API_KEY = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, acc.getAPIKey())
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> row))
				.hasElements()
				.flatMap(exists -> {
					if (exists) {
						String updateCommand = "UPDATE " + table
								+ " SET USER_ID = ?, BLOCKED = ?,"
								+ " WHERE API_KEY = ?";

						return connect(master, c -> Mono.from(c.createStatement(updateCommand)
								.bind(0, acc.getUserId())
								.bind(1, acc.isBlocked())
								.bind(2, acc.getAPIKey())
								.execute())
						).flatMap(res -> Mono.from(res.getRowsUpdated()))
								.thenReturn(true);
					} else {
						String insertCommand = "INSERT INTO " + table +
								"(USER_ID, API_KEY, BLOCKED, TIME_ISSUED)" +
								" VALUES (?, ?, ?, ?)";

						return connect(master, c -> Mono.from(c.createStatement(insertCommand)
								.bind(0, acc.getUserId())
								.bind(1, acc.getAPIKey())
								.bind(2, acc.isBlocked())
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

	public static Mono<Boolean> updateSettings(GuildSettings set) {
		guildSettingsCache.remove(set.getGuildID());
		guildSettingsCache.put(set.getGuildID(), set);

		if (set.getPrivateKey().equalsIgnoreCase("N/a"))
			set.setPrivateKey(KeyGenerator.csRandomAlphaNumericString(16));

		String table = String.format("%sguild_settings", settings.getPrefix());

		return connect(slave, c -> {
			String query = "SELECT * FROM " + table + " WHERE GUILD_ID = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, set.getGuildID().asString())
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> row))
				.hasElements()
				.flatMap(exists -> {
					if (exists) {
						String update = "UPDATE " + table
								+ " SET EXTERNAL_CALENDAR = ?, PRIVATE_KEY = ?,"
								+ " ACCESS_TOKEN = ?, REFRESH_TOKEN = ?,"
								+ " CONTROL_ROLE = ?, DISCAL_CHANNEL = ?, SIMPLE_ANNOUNCEMENT = ?,"
								+ " LANG = ?, PREFIX = ?, PATRON_GUILD = ?, DEV_GUILD = ?,"
								+ " MAX_CALENDARS = ?, DM_ANNOUNCEMENTS = ?, 12_HOUR = ?,"
								+ " BRANDED = ? WHERE GUILD_ID = ?";

						return connect(master, c -> Mono.from(c.createStatement(update)
								.bind(0, set.useExternalCalendar())
								.bind(1, set.getPrivateKey())
								.bind(2, set.getEncryptedAccessToken())
								.bind(3, set.getEncryptedRefreshToken())
								.bind(4, set.getControlRole())
								.bind(5, set.getDiscalChannel())
								.bind(6, set.usingSimpleAnnouncements())
								.bind(7, set.getLang())
								.bind(8, set.getPrefix())
								.bind(9, set.isPatronGuild())
								.bind(10, set.isDevGuild())
								.bind(11, set.getMaxCalendars())
								.bind(12, set.getDmAnnouncementsString())
								.bind(13, set.useTwelveHour())
								.bind(14, set.isBranded())
								.bind(15, set.getGuildID().asString())
								.execute())
						).flatMap(res -> Mono.from(res.getRowsUpdated()))
								.hasElement()
								.thenReturn(true);
					} else {
						String insertCommand = "INSERT INTO " + table + "(GUILD_ID, " +
								"EXTERNAL_CALENDAR, PRIVATE_KEY, ACCESS_TOKEN, REFRESH_TOKEN, " +
								"CONTROL_ROLE, DISCAL_CHANNEL, SIMPLE_ANNOUNCEMENT, LANG, " +
								"PREFIX, PATRON_GUILD, DEV_GUILD, MAX_CALENDARS, " +
								"DM_ANNOUNCEMENTS, 12_HOUR, BRANDED) " +
								"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

						return connect(master, c -> Mono.from(c.createStatement(insertCommand)
								.bind(0, set.useExternalCalendar())
								.bind(1, set.getPrivateKey())
								.bind(2, set.getEncryptedAccessToken())
								.bind(3, set.getEncryptedRefreshToken())
								.bind(4, set.getControlRole())
								.bind(5, set.getDiscalChannel())
								.bind(6, set.usingSimpleAnnouncements())
								.bind(7, set.getLang())
								.bind(8, set.getPrefix())
								.bind(9, set.isPatronGuild())
								.bind(10, set.isDevGuild())
								.bind(11, set.getMaxCalendars())
								.bind(12, set.getDmAnnouncementsString())
								.bind(13, set.useTwelveHour())
								.bind(14, set.isBranded())
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

	public static Mono<Boolean> updateCalendar(CalendarData calData) {
		String table = String.format("%scalendars", settings.getPrefix());

		return connect(slave, c -> {
			String query = "SELECT * FROM " + table + " WHERE GUILD_ID = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, calData.getGuildId().asString())
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> row))
				.hasElements()
				.flatMap(exists -> {
					if (exists) {
						String update = "UPDATE " + table
								+ " SET CALENDAR_NUMBER = ?, CALENDAR_ID = ?,"
								+ " CALENDAR_ADDRESS = ?, EXTERNAL = ?"
								+ " WHERE GUILD_ID = ?";

						return connect(master, c -> Mono.from(c.createStatement(update)
								.bind(0, calData.getCalendarNumber())
								.bind(1, calData.getCalendarId())
								.bind(2, calData.getCalendarAddress())
								.bind(3, calData.isExternal())
								.bind(4, calData.getGuildId().asString())
								.execute())
						).flatMap(res -> Mono.from(res.getRowsUpdated()))
								.hasElement()
								.thenReturn(true);
					} else {
						String insertCommand = "INSERT INTO " + table
								+ "(GUILD_ID, CALENDAR_NUMBER, CALENDAR_ID, " +
								"CALENDAR_ADDRESS, EXTERNAL)" + " VALUES (?, ?, ?, ?, ?)";

						return connect(master, c -> Mono.from(c.createStatement(insertCommand)
								.bind(0, calData.getGuildId().asString())
								.bind(1, calData.getCalendarNumber())
								.bind(2, calData.getCalendarId())
								.bind(3, calData.getCalendarAddress())
								.bind(4, calData.isExternal())
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

	public static Mono<Boolean> updateAnnouncement(Announcement announcement) {
		String table = String.format("%sannouncements", settings.getPrefix());

		return connect(slave, c -> {
			String query = "SELECT * FROM " + table + " WHERE ANNOUNCEMENT_ID = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, announcement.getAnnouncementId().toString())
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> row))
				.hasElements()
				.flatMap(exists -> {
					if (exists) {
						String update = "UPDATE " + table
								+ " SET SUBSCRIBERS_ROLE = ?, SUBSCRIBERS_USER = ?, CHANNEL_ID = ?,"
								+ " ANNOUNCEMENT_TYPE = ?, EVENT_ID = ?, EVENT_COLOR = ?, "
								+ " HOURS_BEFORE = ?, MINUTES_BEFORE = ?,"
								+ " INFO = ?, ENABLED = ?, INFO_ONLY = ?"
								+ " WHERE ANNOUNCEMENT_ID = ?";

						return connect(master, c -> Mono.from(c.createStatement(update)
								.bind(0, announcement.getSubscriberRoleIdString())
								.bind(1, announcement.getSubscriberUserIdString())
								.bind(2, announcement.getAnnouncementChannelId())
								.bind(3, announcement.getAnnouncementType().name())
								.bind(4, announcement.getEventId())
								.bind(5, announcement.getEventColor().name())
								.bind(6, announcement.getHoursBefore())
								.bind(7, announcement.getMinutesBefore())
								.bind(8, announcement.getInfo())
								.bind(9, announcement.isEnabled())
								.bind(10, announcement.isInfoOnly())
								.bind(11, announcement.getAnnouncementId().toString())
								.execute())
						).flatMap(res -> Mono.from(res.getRowsUpdated()))
								.thenReturn(true);
					} else {
						String insertCommand = "INSERT INTO " + table +
								"(ANNOUNCEMENT_ID, GUILD_ID, SUBSCRIBERS_ROLE, SUBSCRIBERS_USER, " +
								"CHANNEL_ID, ANNOUNCEMENT_TYPE, EVENT_ID, EVENT_COLOR, " +
								"HOURS_BEFORE, MINUTES_BEFORE, INFO, ENABLED, INFO_ONLY)" +
								" VALUE (?, 	?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

						return connect(master, c -> Mono.from(c.createStatement(insertCommand)
								.bind(0, announcement.getAnnouncementId().toString())
								.bind(1, announcement.getGuildId().asString())
								.bind(2, announcement.getSubscriberRoleIdString())
								.bind(3, announcement.getSubscriberUserIdString())
								.bind(4, announcement.getAnnouncementChannelId())
								.bind(5, announcement.getAnnouncementType().name())
								.bind(6, announcement.getEventId())
								.bind(7, announcement.getEventColor().name())
								.bind(8, announcement.getHoursBefore())
								.bind(9, announcement.getMinutesBefore())
								.bind(10, announcement.getInfo())
								.bind(11, announcement.isEnabled())
								.bind(12, announcement.isInfoOnly())
								.execute())
						).flatMap(res -> Mono.from(res.getRowsUpdated()))
								.thenReturn(true);
					}
				}).onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to update announcement", e, DatabaseManager.class));
					return Mono.just(false);
				});
	}

	public static Mono<Boolean> updateEventData(EventData data) {
		String table = String.format("%sevents", settings.getPrefix());
		String id = data.getEventId();
		if (data.getEventId().contains("_")) {
			id = data.getEventId().split("_")[0];
		}
		String idToUse = id;

		return connect(slave, c -> {
			String query = "SELECT * FROM " + table + " WHERE EVENT_ID = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, idToUse)
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> row))
				.hasElements()
				.flatMap(exists -> {
					if (exists) {
						String updateCommand = "UPDATE " + table
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
						String insertCommand = "INSERT INTO " + table +
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

	public static Mono<Boolean> updateRsvpData(RsvpData data) {
		String table = String.format("%srsvp", settings.getPrefix());

		return connect(slave, c -> {
			String query = "SELECT * FROM " + table + " WHERE EVENT_ID = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, data.getEventId())
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> row))
				.hasElements()
				.flatMap(exists -> {
					if (exists) {
						String update = "UPDATE " + table
								+ " SET EVENT_END = ?,"
								+ " GOING_ON_TIME = ?,"
								+ " GOING_LATE = ?,"
								+ " NOT_GOING = ?,"
								+ " UNDECIDED = ?"
								+ " WHERE EVENT_ID = ?";

						return connect(master, c -> Mono.from(c.createStatement(update)
								.bind(0, data.getEventEnd())
								.bind(1, data.getGoingOnTimeString())
								.bind(2, data.getGoingLateString())
								.bind(3, data.getNotGoingString())
								.bind(4, data.getUndecidedString())
								.bind(5, data.getEventId())
								.execute())
						).flatMap(res -> Mono.from(res.getRowsUpdated()))
								.thenReturn(true);
					} else {
						String insertCommand = "INSERT INTO " + table +
								"(GUILD_ID, EVENT_ID, EVENT_END, GOING_ON_TIME, GOING_LATE, " +
								"NOT_GOING, UNDECIDED)" +
								" VALUES (?, ?, ?, ?, ?, ?, ?)";

						return connect(master, c -> Mono.from(c.createStatement(insertCommand)
								.bind(0, data.getGuildId().asString())
								.bind(1, data.getEventId())
								.bind(2, data.getEventEnd())
								.bind(3, data.getGoingOnTimeString())
								.bind(4, data.getGoingLateString())
								.bind(5, data.getNotGoingString())
								.bind(6, data.getUndecidedString())
								.execute())
						).flatMap(res -> Mono.from(res.getRowsUpdated()))
								.thenReturn(true);
					}
				}).onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to update rsvp data", e, DatabaseManager.class));
					return Mono.just(false);
				});
	}

	public static Mono<UserAPIAccount> getAPIAccount(String APIKey) {
		return connect(slave, c -> {
			String dataTableName = String.format("%sapi", settings.getPrefix());
			String query = "SELECT * FROM " + dataTableName + " WHERE API_KEY = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, APIKey)
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			UserAPIAccount account = new UserAPIAccount();
			account.setAPIKey(APIKey);
			account.setUserId(row.get("USER_ID", String.class));
			account.setBlocked(row.get("BLOCKED", Boolean.class));
			account.setTimeIssued(row.get("TIME_ISSUED", Long.class));

			return account;
		}))
				.next()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get API user data", e, DatabaseManager.class));
					return Mono.empty();
				});
	}

	public static Mono<GuildSettings> getSettings(Snowflake guildId) {
		if (guildSettingsCache.containsKey(guildId))
			return Mono.just(guildSettingsCache.get(guildId));

		return connect(slave, c -> {
			String dataTableName = String.format("%sguild_settings", settings.getPrefix());
			String query = "SELECT * FROM " + dataTableName + " WHERE GUILD_ID = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, guildId.asString())
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			GuildSettings set = new GuildSettings(guildId);

			set.setUseExternalCalendar(row.get("EXTERNAL_CALENDAR", Boolean.class));
			set.setPrivateKey(row.get("PRIVATE_KEY", String.class));
			set.setEncryptedAccessToken(row.get("ACCESS_TOKEN", String.class));
			set.setEncryptedRefreshToken(row.get("REFRESH_TOKEN", String.class));
			set.setControlRole(row.get("CONTROL_ROLE", String.class));
			set.setDiscalChannel(row.get("DISCAL_CHANNEL", String.class));
			set.setSimpleAnnouncements(row.get("SIMPLE_ANNOUNCEMENT", Boolean.class));
			set.setLang(row.get("LANG", String.class));
			set.setPrefix(row.get("PREFIX", String.class));
			set.setPatronGuild(row.get("PATRON_GUILD", Boolean.class));
			set.setDevGuild(row.get("DEV_GUILD", Boolean.class));
			set.setMaxCalendars(row.get("MAX_CALENDARS", Integer.class));
			set.setDmAnnouncementsFromString(row.get("DM_ANNOUNCEMENTS", String.class));
			set.setTwelveHour(row.get("12_HOUR", Boolean.class));
			set.setBranded(row.get("BRANDED", Boolean.class));

			//Store in cache...
			guildSettingsCache.remove(guildId);
			guildSettingsCache.put(guildId, set);

			return set;
		}))
				.next()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get guild settings", e, DatabaseManager.class));
					return Mono.just(new GuildSettings(guildId));
				});
	}

	public static Mono<CalendarData> getMainCalendar(Snowflake guildId) {
		return connect(slave, c -> {
			String calendarTableName = String.format("%scalendars", settings.getPrefix());
			String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = ? " +
					"AND CALENDAR_NUMBER = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, guildId.asString())
					.bind(1, 1)
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			String calId = row.get("CALENDAR_ID", String.class);
			String calAddr = row.get("CALENDAR_ADDRESS", String.class);
			boolean external = row.get("EXTERNAL", Boolean.class);
			return CalendarData.fromData(guildId, 1, calId, calAddr, external);
		}))
				.next()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get calendar data", e, DatabaseManager.class));
					return Mono.empty();
				});
	}

	public static Mono<CalendarData> getCalendar(Snowflake guildId, int calendarNumber) {
		return connect(slave, c -> {
			String calendarTableName = String.format("%scalendars", settings.getPrefix());
			String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = ? AND " +
					"CALENDAR_NUMBER = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, guildId.asString())
					.bind(1, calendarNumber)
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			String calId = row.get("CALENDAR_ID", String.class);
			String calAddr = row.get("CALENDAR_ADDRESS", String.class);
			boolean external = row.get("EXTERNAL", Boolean.class);
			return CalendarData.fromData(guildId, calendarNumber, calId, calAddr, external);
		}))
				.next()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get calendar data", e, DatabaseManager.class));
					return Mono.empty();
				});
	}

	public static Mono<List<CalendarData>> getAllCalendars(Snowflake guildId) {
		return connect(slave, c -> {
			String calendarTableName = String.format("%scalendars", settings.getPrefix());
			String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, guildId.asString())
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			String calId = row.get("CALENDAR_ID", String.class);
			String calAddr = row.get("CALENDAR_ADDRESS", String.class);
			boolean external = row.get("EXTERNAL", Boolean.class);

			return CalendarData.fromData(guildId, 1, calId, calAddr, external);
		}))
				.collectList()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get all guild calendars", e, DatabaseManager.class));
					return Mono.just(new ArrayList<>());
				});
	}

	public static Mono<Integer> getCalendarCount() {
		return connect(slave, c -> {
			String calendarTableName = String.format("%scalendars", settings.getPrefix());
			String query = "SELECT COUNT(*) FROM " + calendarTableName + ";";

			return Mono.from(c.createStatement(query).execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			Long calendars = row.get(0, Long.class);

			return calendars == null ? 0 : calendars.intValue();
		}))
				.next()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get calendar count", e, DatabaseManager.class));
					return Mono.just(-1);
				});
	}

	public static Mono<EventData> getEventData(Snowflake guildId, String eventId) {
		return connect(slave, c -> {
			String copiedEventId = eventId;
			if (copiedEventId.contains("_"))
				copiedEventId = copiedEventId.split("_")[0];

			String rsvpTableName = String.format("%srsvp", settings.getPrefix());
			String query = "SELECT * FROM " + rsvpTableName + " WHERE GUILD_ID= ? AND EVENT_ID = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, guildId.asString())
					.bind(1, copiedEventId)
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			String id = row.get("EVENT_ID", String.class);
			long end = row.get("EVENT_END", Long.class);
			String img = row.get("IMAGE_LINK", String.class);

			return EventData.fromImage(guildId, id, end, img);
		}))
				.next()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get event data", e, DatabaseManager.class));
					return Mono.empty();
				});
	}

	public static Mono<RsvpData> getRsvpData(Snowflake guildId, String eventId) {
		return connect(slave, c -> {
			String rsvpTableName = String.format("%srsvp", settings.getPrefix());
			String query = "SELECT * FROM " + rsvpTableName + " WHERE GUILD_ID= ? AND EVENT_ID = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, guildId.asString())
					.bind(1, eventId)
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			RsvpData data = new RsvpData(guildId, eventId);
			data.setEventEnd(row.get("EVENT_END", Long.class));
			data.setGoingOnTimeFromString(row.get("GOING_ON_TIME", String.class));
			data.setGoingLateFromString(row.get("GOING_LATE", String.class));
			data.setNotGoingFromString(row.get("NOT_GOING", String.class));
			data.setUndecidedFromString(row.get("UNDECIDED", String.class));

			return data;
		}))
				.next()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get rsvp data", e, DatabaseManager.class));
					return Mono.empty();
				});
	}

	public static Mono<Announcement> getAnnouncement(UUID announcementId, Snowflake guildId) {
		return connect(slave, c -> {
			String announcementTableName = String.format("%sannouncements", settings.getPrefix());
			String query = "SELECT * FROM " + announcementTableName + " WHERE ANNOUNCEMENT_ID = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, announcementId)
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			Announcement a = new Announcement(announcementId, guildId);
			a.setSubscriberRoleIdsFromString(row.get("SUBSCRIBERS_ROLE", String.class));
			a.setSubscriberUserIdsFromString(row.get("SUBSCRIBERS_USER", String.class));
			a.setAnnouncementChannelId(row.get("CHANNEL_ID", String.class));
			a.setAnnouncementType(AnnouncementType
					.valueOf(row.get("ANNOUNCEMENT_TYPE", String.class))
			);
			a.setEventId(row.get("EVENT_ID", String.class));
			a.setEventColor(EventColor
					.fromNameOrHexOrID(row.get("EVENT_COLOR", String.class))
			);
			a.setHoursBefore(row.get("HOURS_BEFORE", Integer.class));
			a.setMinutesBefore(row.get("MINUTES_BEFORE", Integer.class));
			a.setInfo(row.get("INFO", String.class));
			a.setEnabled(row.get("ENABLED", Boolean.class));
			a.setInfoOnly(row.get("INFO_ONLY", Boolean.class));

			return a;
		}))
				.next()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get announcement", e, DatabaseManager.class));
					return Mono.empty();
				});
	}

	public static Mono<List<Announcement>> getAnnouncements(Snowflake guildId) {
		return connect(slave, c -> {
			String announcementTableName = String.format("%sannouncements", settings.getPrefix());
			String query = "SELECT * FROM " + announcementTableName + " WHERE GUILD_ID = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, guildId.asString())
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			UUID announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String.class));

			Announcement a = new Announcement(announcementId, guildId);
			a.setSubscriberRoleIdsFromString(row.get("SUBSCRIBERS_ROLE", String.class));
			a.setSubscriberUserIdsFromString(row.get("SUBSCRIBERS_USER", String.class));
			a.setAnnouncementChannelId(row.get("CHANNEL_ID", String.class));
			a.setAnnouncementType(AnnouncementType
					.valueOf(row.get("ANNOUNCEMENT_TYPE", String.class))
			);
			a.setEventId(row.get("EVENT_ID", String.class));
			a.setEventColor(EventColor
					.fromNameOrHexOrID(row.get("EVENT_COLOR", String.class))
			);
			a.setHoursBefore(row.get("HOURS_BEFORE", Integer.class));
			a.setMinutesBefore(row.get("MINUTES_BEFORE", Integer.class));
			a.setInfo(row.get("INFO", String.class));
			a.setEnabled(row.get("ENABLED", Boolean.class));
			a.setInfoOnly(row.get("INFO_ONLY", Boolean.class));

			return a;
		}))
				.collectList()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get all announcements for guild", e, DatabaseManager.class));

					return Mono.just(new ArrayList<>());
				});
	}

	public static Mono<List<Announcement>> getAnnouncements() {
		return connect(slave, c -> {
			String announcementTableName = String.format("%sannouncements", settings.getPrefix());
			String query = "SELECT * FROM " + announcementTableName + ";";

			return Mono.from(c.createStatement(query)
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			UUID announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String.class));
			Snowflake guildId = Snowflake.of(row.get("GUILD_ID", String.class));

			Announcement a = new Announcement(announcementId, guildId);
			a.setSubscriberRoleIdsFromString(row.get("SUBSCRIBERS_ROLE", String.class));
			a.setSubscriberUserIdsFromString(row.get("SUBSCRIBERS_USER", String.class));
			a.setAnnouncementChannelId(row.get("CHANNEL_ID", String.class));
			a.setAnnouncementType(AnnouncementType
					.valueOf(row.get("ANNOUNCEMENT_TYPE", String.class))
			);
			a.setEventId(row.get("EVENT_ID", String.class));
			a.setEventColor(EventColor
					.fromNameOrHexOrID(row.get("EVENT_COLOR", String.class))
			);
			a.setHoursBefore(row.get("HOURS_BEFORE", Integer.class));
			a.setMinutesBefore(row.get("MINUTES_BEFORE", Integer.class));
			a.setInfo(row.get("INFO", String.class));
			a.setEnabled(row.get("ENABLED", Boolean.class));
			a.setInfoOnly(row.get("INFO_ONLY", Boolean.class));

			return a;
		}))
				.collectList()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get announcements by type", e, DatabaseManager.class));

					return Mono.just(new ArrayList<>());
				});
	}

	public static Mono<List<Announcement>> getAnnouncements(AnnouncementType type) {
		return connect(slave, c -> {
			String announcementTableName = String.format("%sannouncements", settings.getPrefix());
			String query = "SELECT * FROM " + announcementTableName
					+ " WHERE ANNOUNCEMENT_TYPE = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, type.name())
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			UUID announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String.class));
			Snowflake guildId = Snowflake.of(row.get("GUILD_ID", String.class));

			Announcement a = new Announcement(announcementId, guildId);
			a.setSubscriberRoleIdsFromString(row.get("SUBSCRIBERS_ROLE", String.class));
			a.setSubscriberUserIdsFromString(row.get("SUBSCRIBERS_USER", String.class));
			a.setAnnouncementChannelId(row.get("CHANNEL_ID", String.class));
			a.setAnnouncementType(AnnouncementType
					.valueOf(row.get("ANNOUNCEMENT_TYPE", String.class))
			);
			a.setEventId(row.get("EVENT_ID", String.class));
			a.setEventColor(EventColor
					.fromNameOrHexOrID(row.get("EVENT_COLOR", String.class))
			);
			a.setHoursBefore(row.get("HOURS_BEFORE", Integer.class));
			a.setMinutesBefore(row.get("MINUTES_BEFORE", Integer.class));
			a.setInfo(row.get("INFO", String.class));
			a.setEnabled(row.get("ENABLED", Boolean.class));
			a.setInfoOnly(row.get("INFO_ONLY", Boolean.class));

			return a;
		}))
				.collectList()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get announcements by type", e, DatabaseManager.class));

					return Mono.just(new ArrayList<>());
				});
	}

	public static Mono<List<Announcement>> getEnabledAnnouncements() {
		return connect(slave, c -> {
			String announcementTableName = String.format("%sannouncements", settings.getPrefix());
			String query = "SELECT * FROM " + announcementTableName + " WHERE ENABLED = 1";

			return Mono.from(c.createStatement(query)
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			UUID announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String.class));
			Snowflake guildId = Snowflake.of(row.get("GUILD_ID", String.class));

			Announcement a = new Announcement(announcementId, guildId);
			a.setSubscriberRoleIdsFromString(row.get("SUBSCRIBERS_ROLE", String.class));
			a.setSubscriberUserIdsFromString(row.get("SUBSCRIBERS_USER", String.class));
			a.setAnnouncementChannelId(row.get("CHANNEL_ID", String.class));
			a.setAnnouncementType(AnnouncementType
					.valueOf(row.get("ANNOUNCEMENT_TYPE", String.class))
			);
			a.setEventId(row.get("EVENT_ID", String.class));
			a.setEventColor(EventColor
					.fromNameOrHexOrID(row.get("EVENT_COLOR", String.class))
			);
			a.setHoursBefore(row.get("HOURS_BEFORE", Integer.class));
			a.setMinutesBefore(row.get("MINUTES_BEFORE", Integer.class));
			a.setInfo(row.get("INFO", String.class));
			a.setEnabled(row.get("ENABLED", Boolean.class));
			a.setInfoOnly(row.get("INFO_ONLY", Boolean.class));

			return a;
		}))
				.collectList()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get enabled announcements", e, DatabaseManager.class));

					return Mono.just(new ArrayList<>());
				});
	}

	public static Mono<List<Announcement>> getEnabledAnnouncements(AnnouncementType type) {
		return connect(slave, c -> {
			String announcementTableName = String.format("%sannouncements", settings.getPrefix());
			String query = "SELECT * FROM " + announcementTableName
					+ " WHERE ENABLED = 1 AND ANNOUNCEMENT_TYPE = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, type.name())
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			UUID announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String.class));
			Snowflake guildId = Snowflake.of(row.get("GUILD_ID", String.class));

			Announcement a = new Announcement(announcementId, guildId);
			a.setSubscriberRoleIdsFromString(row.get("SUBSCRIBERS_ROLE", String.class));
			a.setSubscriberUserIdsFromString(row.get("SUBSCRIBERS_USER", String.class));
			a.setAnnouncementChannelId(row.get("CHANNEL_ID", String.class));
			a.setAnnouncementType(AnnouncementType
					.valueOf(row.get("ANNOUNCEMENT_TYPE", String.class))
			);
			a.setEventId(row.get("EVENT_ID", String.class));
			a.setEventColor(EventColor
					.fromNameOrHexOrID(row.get("EVENT_COLOR", String.class))
			);
			a.setHoursBefore(row.get("HOURS_BEFORE", Integer.class));
			a.setMinutesBefore(row.get("MINUTES_BEFORE", Integer.class));
			a.setInfo(row.get("INFO", String.class));
			a.setEnabled(row.get("ENABLED", Boolean.class));
			a.setInfoOnly(row.get("INFO_ONLY", Boolean.class));

			return a;
		}))
				.collectList()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get announcements by type", e, DatabaseManager.class));

					return Mono.just(new ArrayList<>());
				});
	}

	public static Mono<List<Announcement>> getEnabledAnnouncements(Snowflake guildId) {
		return connect(slave, c -> {
			String announcementTableName = String.format("%sannouncements", settings.getPrefix());
			String query = "SELECT * FROM " + announcementTableName
					+ " WHERE ENABLED = 1 AND GUILD_ID = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, guildId.asString())
					.execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			UUID announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String.class));

			Announcement a = new Announcement(announcementId, guildId);
			a.setSubscriberRoleIdsFromString(row.get("SUBSCRIBERS_ROLE", String.class));
			a.setSubscriberUserIdsFromString(row.get("SUBSCRIBERS_USER", String.class));
			a.setAnnouncementChannelId(row.get("CHANNEL_ID", String.class));
			a.setAnnouncementType(AnnouncementType
					.valueOf(row.get("ANNOUNCEMENT_TYPE", String.class))
			);
			a.setEventId(row.get("EVENT_ID", String.class));
			a.setEventColor(EventColor
					.fromNameOrHexOrID(row.get("EVENT_COLOR", String.class))
			);
			a.setHoursBefore(row.get("HOURS_BEFORE", Integer.class));
			a.setMinutesBefore(row.get("MINUTES_BEFORE", Integer.class));
			a.setInfo(row.get("INFO", String.class));
			a.setEnabled(row.get("ENABLED", Boolean.class));
			a.setInfoOnly(row.get("INFO_ONLY", Boolean.class));

			return a;
		}))
				.collectList()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get enabled announcements for guild", e, DatabaseManager.class));

					return Mono.just(new ArrayList<>());
				});
	}

	public static Mono<Integer> getAnnouncementCount() {
		return connect(slave, c -> {
			String announcementTableName = String.format("%sannouncements", settings.getPrefix());
			String query = "SELECT COUNT(*) FROM " + announcementTableName + ";";

			return Mono.from(c.createStatement(query).execute());
		}).flatMapMany(res -> res.map((row, rowMetadata) -> {
			Long announcements = row.get(0, Long.class);
			return announcements == null ? 0 : announcements.intValue();
		}))
				.next()
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to get announcement count", e, DatabaseManager.class));
					return Mono.just(-1);
				});
	}

	public static Mono<Boolean> deleteAnnouncement(String announcementId) {
		return connect(master, c -> {
			String announcementTableName = String.format("%sannouncements", settings.getPrefix());
			String query = "DELETE FROM " + announcementTableName + " WHERE ANNOUNCEMENT_ID = ?";

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

	public static Mono<Boolean> deleteAnnouncementsForEvent(Snowflake guildId, String eventId) {
		return connect(master, c -> {
			String announcementTableName = String.format("%sannouncements", settings.getPrefix());
			String query = "DELETE FROM " + announcementTableName + " WHERE EVENT_ID = ? AND " +
					"GUILD_ID = ? AND ANNOUNCEMENT_TYPE = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, eventId)
					.bind(1, guildId.asString())
					.bind(2, AnnouncementType.SPECIFIC.name())
					.execute());
		}).flatMapMany(Result::getRowsUpdated)
				.then(Mono.just(true))
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to delete announcements for event", e, DatabaseManager.class));
					return Mono.just(false);
				});
	}

	public static Mono<Boolean> deleteEventData(String eventId) {
		return connect(master, c -> {
			String eventTable = String.format("%sevents", settings.getPrefix());
			//Check if recurring...
			if (eventId.contains("_"))
				return Mono.empty(); //Don't delete if child event of recurring event.
			String query = "DELETE FROM " + eventTable + " WHERE EVENT_ID = ?";

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

	public static Mono<Boolean> deleteAllEventData(Snowflake guildId) {
		return connect(master, c -> {
			String eventTable = String.format("%sevents", settings.getPrefix());
			String query = "DELETE FROM " + eventTable + " WHERE GUILD_ID = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, guildId.asString())
					.execute());

		}).flatMapMany(Result::getRowsUpdated)
				.then(Mono.just(true))
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to delete all event data for guild", e, DatabaseManager.class));
					return Mono.just(false);
				});
	}

	public static Mono<Boolean> deleteAllAnnouncementData(Snowflake guildId) {
		return connect(master, c -> {
			String announcementTable = String.format("%sannouncements", settings.getPrefix());
			String query = "DELETE FROM " + announcementTable + " WHERE GUILD_ID = ?";

			return Mono.from(c.createStatement(query)
					.bind(0, guildId.asString())
					.execute());
		}).flatMapMany(Result::getRowsUpdated)
				.then(Mono.just(true))
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("Failed to delete all announcements for guild", e, DatabaseManager.class));
					return Mono.just(false);
				});
	}

	public static Mono<Boolean> deleteAllRSVPData(Snowflake guildId) {
		return connect(master, c -> {
			String rsvpTable = String.format("%srsvp", settings.getPrefix());
			String query = "DELETE FROM " + rsvpTable + " WHERE GUILD_ID = ?";

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

	public static Mono<Boolean> deleteCalendar(CalendarData data) {
		return connect(master, c -> {
			String calendarTable = String.format("%scalendars", settings.getPrefix());
			String query = "DELETE FROM " + calendarTable + " WHERE GUILD_ID = ? AND " +
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