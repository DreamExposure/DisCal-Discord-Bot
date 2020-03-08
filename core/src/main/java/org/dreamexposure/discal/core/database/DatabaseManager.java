package org.dreamexposure.discal.core.database;

import org.dreamexposure.discal.core.crypto.KeyGenerator;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.object.event.RsvpData;
import org.dreamexposure.discal.core.object.web.UserAPIAccount;
import org.dreamexposure.novautils.database.DatabaseInfo;
import org.dreamexposure.novautils.database.DatabaseSettings;
import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import discord4j.core.object.util.Snowflake;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"UnusedReturnValue", "SqlNoDataSourceInspection", "Duplicates", "SqlResolve", "unused"})
public class DatabaseManager {
	private static DatabaseManager instance;
	private DatabaseInfo masterInfo;
	private DatabaseInfo slaveInfo;

	private Map<Snowflake, GuildSettings> guildSettingsCache = new HashMap<>();

	private DatabaseManager() {
	} //Prevent initialization.

	/**
	 * Gets the instance of the {@link DatabaseManager}.
	 *
	 * @return The instance of the {@link DatabaseManager}
	 */
	public static DatabaseManager getManager() {
		if (instance == null)
			instance = new DatabaseManager();

		return instance;
	}

	/**
	 * Connects to the MySQL server specified.
	 */
	public void connectToMySQL() {
		try {
			DatabaseSettings masterSettings = new DatabaseSettings(BotSettings.SQL_MASTER_HOST.get(), BotSettings.SQL_MASTER_PORT.get(), BotSettings.SQL_DB.get(), BotSettings.SQL_MASTER_USER.get(), BotSettings.SQL_MASTER_PASS.get(), BotSettings.SQL_PREFIX.get());

			DatabaseSettings slaveSettings = new DatabaseSettings(BotSettings.SQL_SLAVE_HOST.get(), BotSettings.SQL_SLAVE_PORT.get(), BotSettings.SQL_DB.get(), BotSettings.SQL_SLAVE_USER.get(), BotSettings.SQL_SLAVE_PASS.get(), BotSettings.SQL_PREFIX.get());

			masterInfo = org.dreamexposure.novautils.database.DatabaseManager.connectToMySQL(masterSettings);
			slaveInfo = org.dreamexposure.novautils.database.DatabaseManager.connectToMySQL(slaveSettings);

			System.out.println("Connected to MySQL database!");
		} catch (Exception e) {
			System.out.println("Failed to connect to MySQL database! Is it properly configured?");
			e.printStackTrace();
			Logger.getLogger().exception(null, "Connecting to MySQL server failed.", e, true, this.getClass());
		}
	}

	public void handleMigrations() {
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("prefix", BotSettings.SQL_PREFIX.get());

		try {
			Flyway flyway = Flyway.configure()
					.dataSource(masterInfo.getSource())
					.cleanDisabled(true)
					.baselineOnMigrate(true)
					.table(BotSettings.SQL_PREFIX.get() + "schema_history")
					.placeholders(placeholders)
					.load();
			int sm = flyway.migrate();
			Logger.getLogger().debug("Migrations Successful, " + sm + " migrations applied!", true);
		} catch (Exception e) {
			Logger.getLogger().exception(null, "Migrations Failure", e, true, getClass());
			System.exit(2);
		}
	}

	/**
	 * Disconnects from the MySQL server if still connected.
	 */
	public void disconnectFromMySQL() {
		if (masterInfo != null && slaveInfo != null) {
			try {
				org.dreamexposure.novautils.database.DatabaseManager.disconnectFromMySQL(masterInfo);
				org.dreamexposure.novautils.database.DatabaseManager.disconnectFromMySQL(slaveInfo);
				System.out.println("Successfully disconnected from MySQL Database!");
			} catch (Exception e) {
				Logger.getLogger().exception(null, "Disconnecting from MySQL failed.", e, true, this.getClass());
				System.out.println("MySQL Connection may not have closed properly! Data may be invalidated!");
			}
		}
	}

	public boolean updateAPIAccount(UserAPIAccount acc) {
		try (final Connection masterConnection = masterInfo.getSource().getConnection()) {
			String tableName = String.format("%sapi", masterInfo.getSettings().getPrefix());

			String query = "SELECT * FROM " + tableName + " WHERE API_KEY = ?";
			final Connection slaveConnection = slaveInfo.getSource().getConnection();
			PreparedStatement statement = slaveConnection.prepareStatement(query);
			statement.setString(1, acc.getAPIKey());

			ResultSet res = statement.executeQuery();

			boolean hasStuff = res.next();

			if (!hasStuff || res.getString("API_KEY") == null) {
				//Data not present, add to DB.
				String insertCommand = "INSERT INTO " + tableName +
						"(USER_ID, API_KEY, BLOCKED, TIME_ISSUED, USES)" +
						" VALUES (?, ?, ?, ?, ?)";
				PreparedStatement ps = masterConnection.prepareStatement(insertCommand);
				ps.setString(1, acc.getUserId());
				ps.setString(2, acc.getAPIKey());
				ps.setBoolean(3, acc.isBlocked());
				ps.setLong(4, acc.getTimeIssued());
				ps.setInt(5, acc.getUses());

				ps.executeUpdate();
				ps.close();
				statement.close();
				slaveConnection.close();
			} else {
				//Data present, update.
				String update = "UPDATE " + tableName
						+ " SET USER_ID = ?, BLOCKED = ?,"
						+ " USES = ? WHERE API_KEY = ?";
				PreparedStatement ps = masterConnection.prepareStatement(update);

				ps.setString(1, acc.getUserId());
				ps.setBoolean(2, acc.isBlocked());
				ps.setInt(3, acc.getUses());
				ps.setString(4, acc.getAPIKey());

				ps.executeUpdate();

				ps.close();
				statement.close();
				slaveConnection.close();
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.getLogger().exception(null, "Failed to update API account", e, true, this.getClass());
		}
		return false;
	}

	public boolean updateSettings(GuildSettings settings) {
		guildSettingsCache.remove(settings.getGuildID());
		guildSettingsCache.put(settings.getGuildID(), settings);

		if (settings.getPrivateKey().equalsIgnoreCase("N/a"))
			settings.setPrivateKey(KeyGenerator.csRandomAlphaNumericString(16));

		try (final Connection masterConnection = masterInfo.getSource().getConnection()) {
			String dataTableName = String.format("%sguild_settings", masterInfo.getSettings().getPrefix());

			String query = "SELECT * FROM " + dataTableName + " WHERE GUILD_ID = ?";
			final Connection slaveConnection = slaveInfo.getSource().getConnection();
			PreparedStatement statement = slaveConnection.prepareStatement(query);
			statement.setString(1, settings.getGuildID().asString());
			ResultSet res = statement.executeQuery();

			boolean hasStuff = res.next();

			if (!hasStuff || res.getString("GUILD_ID") == null) {
				//Data not present, add to DB.
				String insertCommand = "INSERT INTO " + dataTableName +
						"(GUILD_ID, EXTERNAL_CALENDAR, PRIVATE_KEY, ACCESS_TOKEN, REFRESH_TOKEN, CONTROL_ROLE, DISCAL_CHANNEL, SIMPLE_ANNOUNCEMENT, LANG, PREFIX, PATRON_GUILD, DEV_GUILD, MAX_CALENDARS, DM_ANNOUNCEMENTS, 12_HOUR, BRANDED)" +
						" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				PreparedStatement ps = masterConnection.prepareStatement(insertCommand);
				ps.setString(1, settings.getGuildID().asString());
				ps.setBoolean(2, settings.useExternalCalendar());
				ps.setString(3, settings.getPrivateKey());
				ps.setString(4, settings.getEncryptedAccessToken());
				ps.setString(5, settings.getEncryptedRefreshToken());
				ps.setString(6, settings.getControlRole());
				ps.setString(7, settings.getDiscalChannel());
				ps.setBoolean(8, settings.usingSimpleAnnouncements());
				ps.setString(9, settings.getLang());
				ps.setString(10, settings.getPrefix());
				ps.setBoolean(11, settings.isPatronGuild());
				ps.setBoolean(12, settings.isDevGuild());
				ps.setInt(13, settings.getMaxCalendars());
				ps.setString(14, settings.getDmAnnouncementsString());
				ps.setBoolean(15, settings.useTwelveHour());
				ps.setBoolean(16, settings.isBranded());


				ps.executeUpdate();
				ps.close();
				statement.close();
				slaveConnection.close();
			} else {
				//Data present, update.
				String update = "UPDATE " + dataTableName
						+ " SET EXTERNAL_CALENDAR = ?, PRIVATE_KEY = ?,"
						+ " ACCESS_TOKEN = ?, REFRESH_TOKEN = ?,"
						+ " CONTROL_ROLE = ?, DISCAL_CHANNEL = ?, SIMPLE_ANNOUNCEMENT = ?,"
						+ " LANG = ?, PREFIX = ?, PATRON_GUILD = ?, DEV_GUILD = ?,"
						+ " MAX_CALENDARS = ?, DM_ANNOUNCEMENTS = ?, 12_HOUR = ?,"
						+ " BRANDED = ? WHERE GUILD_ID = ?";
				PreparedStatement ps = masterConnection.prepareStatement(update);

				ps.setBoolean(1, settings.useExternalCalendar());
				ps.setString(2, settings.getPrivateKey());
				ps.setString(3, settings.getEncryptedAccessToken());
				ps.setString(4, settings.getEncryptedRefreshToken());
				ps.setString(5, settings.getControlRole());
				ps.setString(6, settings.getDiscalChannel());
				ps.setBoolean(7, settings.usingSimpleAnnouncements());
				ps.setString(8, settings.getLang());
				ps.setString(9, settings.getPrefix());
				ps.setBoolean(10, settings.isPatronGuild());
				ps.setBoolean(11, settings.isDevGuild());
				ps.setInt(12, settings.getMaxCalendars());
				ps.setString(13, settings.getDmAnnouncementsString());
				ps.setBoolean(14, settings.useTwelveHour());
				ps.setBoolean(15, settings.isBranded());
				ps.setString(16, settings.getGuildID().asString());

				ps.executeUpdate();

				ps.close();
				statement.close();
				slaveConnection.close();
			}
			return true;
		} catch (SQLException e) {
			System.out.println("Failed to input data into database! Error Code: 00101");
			Logger.getLogger().exception(null, "Failed to update/insert guild settings.", e, true, this.getClass());
			e.printStackTrace();
		}
		return false;
	}

	public boolean updateCalendar(CalendarData calData) {
		try (final Connection masterConnection = masterInfo.getSource().getConnection()) {
			String calendarTableName = String.format("%scalendars", masterInfo.getSettings().getPrefix());

			String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = ?";
			final Connection slaveConnection = slaveInfo.getSource().getConnection();
			PreparedStatement statement = slaveConnection.prepareStatement(query);
			statement.setString(1, calData.getGuildId().asString());

			ResultSet res = statement.executeQuery();

			boolean hasStuff = res.next();

			if (!hasStuff || res.getString("GUILD_ID") == null) {
				//Data not present, add to DB.
				String insertCommand = "INSERT INTO " + calendarTableName +
						"(GUILD_ID, CALENDAR_NUMBER, CALENDAR_ID, CALENDAR_ADDRESS, EXTERNAL)" +
						" VALUES (?, ?, ?, ?, ?)";
				PreparedStatement ps = masterConnection.prepareStatement(insertCommand);
				ps.setString(1, calData.getGuildId().asString());
				ps.setInt(2, calData.getCalendarNumber());
				ps.setString(3, calData.getCalendarId());
				ps.setString(4, calData.getCalendarAddress());
				ps.setBoolean(5, calData.isExternal());

				ps.executeUpdate();
				ps.close();
				statement.close();
				slaveConnection.close();
			} else {
				//Data present, update.
				String update = "UPDATE " + calendarTableName
						+ " SET CALENDAR_NUMBER = ?, CALENDAR_ID = ?,"
						+ " CALENDAR_ADDRESS = ?, EXTERNAL = ?"
						+ " WHERE GUILD_ID = ?";
				PreparedStatement ps = masterConnection.prepareStatement(update);
				ps.setInt(1, calData.getCalendarNumber());
				ps.setString(2, calData.getCalendarId());
				ps.setString(3, calData.getCalendarAddress());
				ps.setBoolean(4, calData.isExternal());
				ps.setString(5, calData.getGuildId().asString());

				ps.executeUpdate();

				ps.close();
				statement.close();
				slaveConnection.close();
			}
			return true;
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to update/insert calendar data.", e, true, this.getClass());
		}
		return false;
	}

	/**
	 * Updates or Adds the specified {@link Announcement} Object to the database.
	 *
	 * @param announcement The announcement object to add to the database.
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	public boolean updateAnnouncement(Announcement announcement) {
		try (final Connection masterConnection = masterInfo.getSource().getConnection()) {
			String announcementTableName = String.format("%sannouncements", masterInfo.getSettings().getPrefix());

			String query = "SELECT * FROM " + announcementTableName + " WHERE ANNOUNCEMENT_ID = ?";
			final Connection slaveConnection = slaveInfo.getSource().getConnection();
			PreparedStatement statement = slaveConnection.prepareStatement(query);
			statement.setString(1, announcement.getAnnouncementId().toString());

			ResultSet res = statement.executeQuery();

			boolean hasStuff = res.next();

			if (!hasStuff || res.getString("ANNOUNCEMENT_ID") == null) {
				//Data not present, add to db.
				String insertCommand = "INSERT INTO " + announcementTableName +
						"(ANNOUNCEMENT_ID, GUILD_ID, SUBSCRIBERS_ROLE, SUBSCRIBERS_USER, CHANNEL_ID, ANNOUNCEMENT_TYPE, EVENT_ID, EVENT_COLOR, HOURS_BEFORE, MINUTES_BEFORE, INFO, ENABLED, INFO_ONLY)" +
						" VALUE (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				PreparedStatement ps = masterConnection.prepareStatement(insertCommand);
				ps.setString(1, announcement.getAnnouncementId().toString());
				ps.setString(2, announcement.getGuildId().asString());
				ps.setString(3, announcement.getSubscriberRoleIdString());
				ps.setString(4, announcement.getSubscriberUserIdString());
				ps.setString(5, announcement.getAnnouncementChannelId());
				ps.setString(6, announcement.getAnnouncementType().name());
				ps.setString(7, announcement.getEventId());
				ps.setString(8, announcement.getEventColor().name());
				ps.setInt(9, announcement.getHoursBefore());
				ps.setInt(10, announcement.getMinutesBefore());
				ps.setString(11, announcement.getInfo());
				ps.setBoolean(12, announcement.isEnabled());
				ps.setBoolean(13, announcement.isInfoOnly());

				ps.executeUpdate();
				ps.close();
				statement.close();
				slaveConnection.close();
			} else {
				//Data present, update.

				String update = "UPDATE " + announcementTableName
						+ " SET SUBSCRIBERS_ROLE = ?, SUBSCRIBERS_USER = ?, CHANNEL_ID = ?,"
						+ " ANNOUNCEMENT_TYPE = ?, EVENT_ID = ?, EVENT_COLOR = ?, "
						+ " HOURS_BEFORE = ?, MINUTES_BEFORE = ?,"
						+ " INFO = ?, ENABLED = ?, INFO_ONLY = ?"
						+ " WHERE ANNOUNCEMENT_ID = ?";
				PreparedStatement ps = masterConnection.prepareStatement(update);

				ps.setString(1, announcement.getSubscriberRoleIdString());
				ps.setString(2, announcement.getSubscriberUserIdString());
				ps.setString(3, announcement.getAnnouncementChannelId());
				ps.setString(4, announcement.getAnnouncementType().name());
				ps.setString(5, announcement.getEventId());
				ps.setString(6, announcement.getEventColor().name());
				ps.setInt(7, announcement.getHoursBefore());
				ps.setInt(8, announcement.getMinutesBefore());
				ps.setString(9, announcement.getInfo());
				ps.setBoolean(10, announcement.isEnabled());
				ps.setBoolean(11, announcement.isInfoOnly());

				ps.setString(12, announcement.getAnnouncementId().toString());

				ps.executeUpdate();

				ps.close();
				statement.close();
				slaveConnection.close();
			}
			return true;
		} catch (SQLException e) {
			System.out.print("Failed to input announcement data! Error Code: 00201");
			Logger.getLogger().exception(null, "Failed to update/insert announcement.", e, true, this.getClass());
			e.printStackTrace();
		}
		return false;
	}

	public boolean updateEventData(EventData data) {
		try (final Connection masterConnection = masterInfo.getSource().getConnection()) {
			String eventTableName = String.format("%sevents", masterInfo.getSettings().getPrefix());

			if (data.getEventId().contains("_")) {
				data.setEventId(data.getEventId().split("_")[0]);
			}

			String query = "SELECT * FROM " + eventTableName + " WHERE EVENT_ID = ?";
			final Connection slaveConnection = slaveInfo.getSource().getConnection();
			PreparedStatement statement = slaveConnection.prepareStatement(query);
			statement.setString(1, data.getEventId());

			ResultSet res = statement.executeQuery();

			boolean hasStuff = res.next();

			if (!hasStuff || res.getString("EVENT_ID") == null) {
				//Data not present, add to DB.
				String insertCommand = "INSERT INTO " + eventTableName +
						"(GUILD_ID, EVENT_ID, EVENT_END, IMAGE_LINK)" +
						" VALUES (?, ?, ?, ?)";
				PreparedStatement ps = masterConnection.prepareStatement(insertCommand);
				ps.setString(1, data.getGuildId().asString());
				ps.setString(2, data.getEventId());
				ps.setLong(3, data.getEventEnd());
				ps.setString(4, data.getImageLink());

				ps.executeUpdate();
				ps.close();
				statement.close();
				slaveConnection.close();
			} else {
				//Data present, update.
				String update = "UPDATE " + eventTableName
						+ " SET IMAGE_LINK = ?, EVENT_END = ?"
						+ " WHERE EVENT_ID = ?";
				PreparedStatement ps = masterConnection.prepareStatement(update);

				ps.setString(1, data.getImageLink());
				ps.setLong(2, data.getEventEnd());
				ps.setString(3, data.getEventId());

				ps.executeUpdate();

				ps.close();
				statement.close();
				slaveConnection.close();
			}
			return true;
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to update/insert event data.", e, true, this.getClass());
		}
		return false;
	}

	public boolean updateRsvpData(RsvpData data) {
		try (final Connection masterConnection = masterInfo.getSource().getConnection()) {
			String rsvpTableName = String.format("%srsvp", masterInfo.getSettings().getPrefix());

			String query = "SELECT * FROM " + rsvpTableName + " WHERE EVENT_ID = ?";
			final Connection slaveConnection = slaveInfo.getSource().getConnection();
			PreparedStatement statement = slaveConnection.prepareStatement(query);
			statement.setString(1, data.getEventId());

			ResultSet res = statement.executeQuery();

			boolean hasStuff = res.next();

			if (!hasStuff || res.getString("EVENT_ID") == null) {
				//Data not present, add to DB.
				String insertCommand = "INSERT INTO " + rsvpTableName +
						"(GUILD_ID, EVENT_ID, EVENT_END, GOING_ON_TIME, GOING_LATE, NOT_GOING, UNDECIDED)" +
						" VALUES (?, ?, ?, ?, ?, ?, ?)";
				PreparedStatement ps = masterConnection.prepareStatement(insertCommand);
				ps.setString(1, data.getGuildId().asString());
				ps.setString(2, data.getEventId());
				ps.setLong(3, data.getEventEnd());
				ps.setString(4, data.getGoingOnTimeString());
				ps.setString(5, data.getGoingLateString());
				ps.setString(6, data.getNotGoingString());
				ps.setString(7, data.getUndecidedString());

				ps.executeUpdate();
				ps.close();
				statement.close();
				slaveConnection.close();
			} else {
				//Data present, update.
				String update = "UPDATE " + rsvpTableName
						+ " SET EVENT_END = ?,"
						+ " GOING_ON_TIME = ?,"
						+ " GOING_LATE = ?,"
						+ " NOT_GOING = ?,"
						+ " UNDECIDED = ?"
						+ " WHERE EVENT_ID = ?";
				PreparedStatement ps = masterConnection.prepareStatement(update);

				ps.setLong(1, data.getEventEnd());
				ps.setString(2, data.getGoingOnTimeString());
				ps.setString(3, data.getGoingLateString());
				ps.setString(4, data.getNotGoingString());
				ps.setString(5, data.getUndecidedString());
				ps.setString(6, data.getEventId());

				ps.executeUpdate();

				ps.close();
				statement.close();
				slaveConnection.close();
			}
			return true;
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to update/insert event data.", e, true, this.getClass());
		}
		return false;
	}

	public UserAPIAccount getAPIAccount(String APIKey) {
		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String dataTableName = String.format("%sapi", slaveInfo.getSettings().getPrefix());

			String query = "SELECT * FROM " + dataTableName + " WHERE API_KEY = ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, APIKey);

			ResultSet res = statement.executeQuery();

			boolean hasStuff = res.next();

			if (hasStuff && res.getString("API_KEY") != null) {
				UserAPIAccount account = new UserAPIAccount();
				account.setAPIKey(APIKey);
				account.setUserId(res.getString("USER_ID"));
				account.setBlocked(res.getBoolean("BLOCKED"));
				account.setTimeIssued(res.getLong("TIME_ISSUED"));
				account.setUses(res.getInt("USES"));

				statement.close();

				return account;
			} else {
				//Data not present.
				statement.close();
				return null;
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get API Account.", e, true, this.getClass());
		}
		return null;
	}

	public GuildSettings getSettings(Snowflake guildId) {
		if (guildSettingsCache.containsKey(guildId))
			return guildSettingsCache.get(guildId);

		boolean shouldStore = false;

		GuildSettings settings = new GuildSettings(guildId);
		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String dataTableName = String.format("%sguild_settings", slaveInfo.getSettings().getPrefix());

			String query = "SELECT * FROM " + dataTableName + " WHERE GUILD_ID = ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, guildId.asString());

			ResultSet res = statement.executeQuery();

			boolean hasStuff = res.next();

			if (hasStuff && res.getString("GUILD_ID") != null) {
				shouldStore = true;
				settings.setUseExternalCalendar(res.getBoolean("EXTERNAL_CALENDAR"));
				settings.setPrivateKey(res.getString("PRIVATE_KEY"));
				settings.setEncryptedAccessToken(res.getString("ACCESS_TOKEN"));
				settings.setEncryptedRefreshToken(res.getString("REFRESH_TOKEN"));
				settings.setControlRole(res.getString("CONTROL_ROLE"));
				settings.setDiscalChannel(res.getString("DISCAL_CHANNEL"));
				settings.setSimpleAnnouncements(res.getBoolean("SIMPLE_ANNOUNCEMENT"));
				settings.setLang(res.getString("LANG"));
				settings.setPrefix(res.getString("PREFIX"));
				settings.setPatronGuild(res.getBoolean("PATRON_GUILD"));
				settings.setDevGuild(res.getBoolean("DEV_GUILD"));
				settings.setMaxCalendars(res.getInt("MAX_CALENDARS"));
				settings.setDmAnnouncementsFromString(res.getString("DM_ANNOUNCEMENTS"));
				settings.setTwelveHour(res.getBoolean("12_HOUR"));
				settings.setBranded(res.getBoolean("BRANDED"));

				statement.close();
			} else {
				//Data not present.
				statement.close();
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get Guild Settings.", e, true, this.getClass());
		}

		if (shouldStore) {
			guildSettingsCache.remove(guildId); //just incase its still there...
			guildSettingsCache.put(guildId, settings);
		}

		return settings;
	}

	public CalendarData getMainCalendar(Snowflake guildId) {
		CalendarData calData = new CalendarData(guildId, 1);
		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String calendarTableName = String.format("%scalendars", slaveInfo.getSettings().getPrefix());

			String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = ? AND CALENDAR_NUMBER = 1";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, guildId.asString());

			ResultSet res = statement.executeQuery();
			boolean hasStuff = res.next();

			if (hasStuff && res.getString("GUILD_ID") != null) {
				calData.setCalendarId(res.getString("CALENDAR_ID"));
				calData.setCalendarAddress(res.getString("CALENDAR_ADDRESS"));
				calData.setExternal(res.getBoolean("EXTERNAL"));
			}
			statement.close();
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get calendar settings.", e, true, this.getClass());
		}
		return calData;
	}

	public CalendarData getCalendar(Snowflake guildId, int calendarNumber) {
		CalendarData calData = new CalendarData(guildId, calendarNumber);
		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String calendarTableName = String.format("%scalendars", slaveInfo.getSettings().getPrefix());

			String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = ? AND CALENDAR_NUMBER = ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, guildId.asString());
			statement.setInt(2, calendarNumber);

			ResultSet res = statement.executeQuery();
			boolean hasStuff = res.next();

			if (hasStuff && res.getString("GUILD_ID") != null) {
				calData.setCalendarId(res.getString("CALENDAR_ID"));
				calData.setCalendarAddress(res.getString("CALENDAR_ADDRESS"));
				calData.setExternal(res.getBoolean("EXTERNAL"));
			}

			statement.close();
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get calendar data", e, true, this.getClass());
		}
		return calData;
	}

	public ArrayList<CalendarData> getAllCalendars(Snowflake guildId) {
		ArrayList<CalendarData> calendars = new ArrayList<>();
		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String calendarTableName = String.format("%scalendars", slaveInfo.getSettings().getPrefix());

			String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, guildId.asString());

			ResultSet res = statement.executeQuery();

			while (res.next()) {
				CalendarData calData = new CalendarData(guildId, res.getInt("CALENDAR_NUMBER"));
				calData.setCalendarId(res.getString("CALENDAR_ID"));
				calData.setCalendarAddress(res.getString("CALENDAR_ADDRESS"));
				calData.setExternal(res.getBoolean("EXTERNAL"));
				calendars.add(calData);
			}
			statement.close();
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get all guild calendars.", e, true, this.getClass());
		}
		return calendars;
	}

	public int getCalendarCount() {
		int amount = -1;
		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String calendarTableName = String.format("%scalendars", slaveInfo.getSettings().getPrefix());

			String query = "SELECT COUNT(*) FROM " + calendarTableName + ";";
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet res = statement.executeQuery();

			if (res.next())
				amount = res.getInt(1);
			else
				amount = 0;


			res.close();
			statement.close();
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get calendar count", e, true, this.getClass());
		}
		return amount;
	}

	public EventData getEventData(Snowflake guildId, String eventId) {
		EventData data = new EventData(guildId);

		if (eventId.contains("_"))
			eventId = eventId.split("_")[0];


		data.setEventId(eventId);

		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String eventTableName = String.format("%sevents", slaveInfo.getSettings().getPrefix());

			String query = "SELECT * FROM " + eventTableName + " WHERE GUILD_ID= ?";
			PreparedStatement statement = connection.prepareStatement(query);

			statement.setString(1, guildId.asString());
			ResultSet res = statement.executeQuery();

			while (res.next()) {
				if (res.getString("EVENT_ID").equals(eventId)) {
					data.setEventEnd(res.getLong("EVENT_END"));
					data.setImageLink(res.getString("IMAGE_LINK"));
					break;
				}
			}
			statement.close();
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get event data", e, true, this.getClass());
		}
		return data;
	}

	public RsvpData getRsvpData(Snowflake guildId, String eventId) {
		RsvpData data = new RsvpData(guildId);
		data.setEventId(eventId);
		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String rsvpTableName = String.format("%srsvp", slaveInfo.getSettings().getPrefix());

			String query = "SELECT * FROM " + rsvpTableName + " WHERE GUILD_ID= ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, guildId.asString());

			ResultSet res = statement.executeQuery();

			while (res.next()) {
				if (res.getString("EVENT_ID").equals(eventId)) {
					data.setEventEnd(res.getLong("EVENT_END"));
					data.setGoingOnTimeFromString(res.getString("GOING_ON_TIME"));
					data.setGoingLateFromString(res.getString("GOING_LATE"));
					data.setNotGoingFromString(res.getString("NOT_GOING"));
					data.setUndecidedFromString(res.getString("UNDECIDED"));
					break;
				}
			}
			statement.close();
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get RSVP data for event", e, true, this.getClass());
		}
		return data;
	}

	/**
	 * Gets the {@link Announcement} Object with the corresponding ID for the specified Guild.
	 *
	 * @param announcementId The ID of the announcement.
	 * @param guildId        The ID of the guild the Announcement belongs to.
	 * @return The {@link Announcement} with the specified ID if it exists, otherwise <c>null</c>.
	 */
	public Announcement getAnnouncement(UUID announcementId, Snowflake guildId) {
		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String announcementTableName = String.format("%sannouncements", slaveInfo.getSettings().getPrefix());

			String query = "SELECT * FROM " + announcementTableName + " WHERE ANNOUNCEMENT_ID = ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, announcementId.toString());

			ResultSet res = statement.executeQuery();

			boolean hasStuff = res.next();

			if (hasStuff && res.getString("ANNOUNCEMENT_ID") != null) {
				Announcement announcement = new Announcement(announcementId, guildId);
				announcement.setSubscriberRoleIdsFromString(res.getString("SUBSCRIBERS_ROLE"));
				announcement.setSubscriberUserIdsFromString(res.getString("SUBSCRIBERS_USER"));
				announcement.setAnnouncementChannelId(res.getString("CHANNEL_ID"));
				announcement.setAnnouncementType(AnnouncementType.valueOf(res.getString("ANNOUNCEMENT_TYPE")));
				announcement.setEventId(res.getString("EVENT_ID"));
				announcement.setEventColor(EventColor.fromNameOrHexOrID(res.getString("EVENT_COLOR")));
				announcement.setHoursBefore(res.getInt("HOURS_BEFORE"));
				announcement.setMinutesBefore(res.getInt("MINUTES_BEFORE"));
				announcement.setInfo(res.getString("INFO"));
				announcement.setEnabled(res.getBoolean("ENABLED"));
				announcement.setInfoOnly(res.getBoolean("INFO_ONLY"));

				statement.close();
				return announcement;
			}
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get announcement data.", e, true, this.getClass());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets an {@link ArrayList} of {@link Announcement}s belonging to the specific Guild.
	 *
	 * @param guildId The ID of the guild whose data is to be retrieved.
	 * @return An ArrayList of Announcements that belong to the specified Guild.
	 */
	public ArrayList<Announcement> getAnnouncements(Snowflake guildId) {
		ArrayList<Announcement> announcements = new ArrayList<>();
		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String announcementTableName = String.format("%sannouncements", slaveInfo.getSettings().getPrefix());

			String query = "SELECT * FROM " + announcementTableName + " WHERE GUILD_ID = ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, guildId.asString());

			ResultSet res = statement.executeQuery();

			while (res.next()) {
				if (res.getString("ANNOUNCEMENT_ID") != null) {
					Announcement announcement = new Announcement(UUID.fromString(res.getString("ANNOUNCEMENT_ID")), guildId);
					announcement.setSubscriberRoleIdsFromString(res.getString("SUBSCRIBERS_ROLE"));
					announcement.setSubscriberUserIdsFromString(res.getString("SUBSCRIBERS_USER"));
					announcement.setAnnouncementChannelId(res.getString("CHANNEL_ID"));
					announcement.setAnnouncementType(AnnouncementType.valueOf(res.getString("ANNOUNCEMENT_TYPE")));
					announcement.setEventId(res.getString("EVENT_ID"));
					announcement.setEventColor(EventColor.fromNameOrHexOrID(res.getString("EVENT_COLOR")));
					announcement.setHoursBefore(res.getInt("HOURS_BEFORE"));
					announcement.setMinutesBefore(res.getInt("MINUTES_BEFORE"));
					announcement.setInfo(res.getString("INFO"));
					announcement.setEnabled(res.getBoolean("ENABLED"));
					announcement.setInfoOnly(res.getBoolean("INFO_ONLY"));

					announcements.add(announcement);
				}
			}

			statement.close();
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get all guild announcements.", e, true, this.getClass());
			e.printStackTrace();
		}
		return announcements;
	}

	public ArrayList<Announcement> getAnnouncements() {
		ArrayList<Announcement> announcements = new ArrayList<>();
		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String announcementTableName = String.format("%sannouncements", slaveInfo.getSettings().getPrefix());

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " + announcementTableName);
			ResultSet res = stmt.executeQuery();

			while (res.next()) {
				if (res.getString("ANNOUNCEMENT_ID") != null) {
					Announcement announcement = new Announcement(UUID.fromString(res.getString("ANNOUNCEMENT_ID")), Snowflake.of(res.getString("GUILD_ID")));
					announcement.setSubscriberRoleIdsFromString(res.getString("SUBSCRIBERS_ROLE"));
					announcement.setSubscriberUserIdsFromString(res.getString("SUBSCRIBERS_USER"));
					announcement.setAnnouncementChannelId(res.getString("CHANNEL_ID"));
					announcement.setAnnouncementType(AnnouncementType.valueOf(res.getString("ANNOUNCEMENT_TYPE")));
					announcement.setEventId(res.getString("EVENT_ID"));
					announcement.setEventColor(EventColor.fromNameOrHexOrID(res.getString("EVENT_COLOR")));
					announcement.setHoursBefore(res.getInt("HOURS_BEFORE"));
					announcement.setMinutesBefore(res.getInt("MINUTES_BEFORE"));
					announcement.setInfo(res.getString("INFO"));
					announcement.setEnabled(res.getBoolean("ENABLED"));
					announcement.setInfoOnly(res.getBoolean("INFO_ONLY"));

					announcements.add(announcement);
				}
			}

			stmt.close();
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get all announcements.", e, true, this.getClass());
		}

		return announcements;
	}

	public ArrayList<Announcement> getAnnouncements(AnnouncementType type) {
		ArrayList<Announcement> announcements = new ArrayList<>();
		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String announcementTableName = String.format("%sannouncements", slaveInfo.getSettings().getPrefix());

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " + announcementTableName + " WHERE ANNOUNCEMENT_TYPE = ?");
			stmt.setString(1, type.name());
			ResultSet res = stmt.executeQuery();

			while (res.next()) {
				if (res.getString("ANNOUNCEMENT_ID") != null) {
					Announcement announcement = new Announcement(UUID.fromString(res.getString("ANNOUNCEMENT_ID")), Snowflake.of(res.getString("GUILD_ID")));
					announcement.setSubscriberRoleIdsFromString(res.getString("SUBSCRIBERS_ROLE"));
					announcement.setSubscriberUserIdsFromString(res.getString("SUBSCRIBERS_USER"));
					announcement.setAnnouncementChannelId(res.getString("CHANNEL_ID"));
					announcement.setAnnouncementType(type);
					announcement.setEventId(res.getString("EVENT_ID"));
					announcement.setEventColor(EventColor.fromNameOrHexOrID(res.getString("EVENT_COLOR")));
					announcement.setHoursBefore(res.getInt("HOURS_BEFORE"));
					announcement.setMinutesBefore(res.getInt("MINUTES_BEFORE"));
					announcement.setInfo(res.getString("INFO"));
					announcement.setEnabled(res.getBoolean("ENABLED"));
					announcement.setInfoOnly(res.getBoolean("INFO_ONLY"));

					announcements.add(announcement);
				}
			}

			stmt.close();
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get all announcements by type.", e, true, this.getClass());
		}

		return announcements;
	}

	public ArrayList<Announcement> getEnabledAnnouncements() {
		ArrayList<Announcement> announcements = new ArrayList<>();
		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String announcementTableName = String.format("%sannouncements", slaveInfo.getSettings().getPrefix());

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " + announcementTableName + " WHERE ENABLED = 1");
			ResultSet res = stmt.executeQuery();

			while (res.next()) {
				if (res.getString("ANNOUNCEMENT_ID") != null) {
					Announcement announcement = new Announcement(UUID.fromString(res.getString("ANNOUNCEMENT_ID")), Snowflake.of(res.getString("GUILD_ID")));
					announcement.setSubscriberRoleIdsFromString(res.getString("SUBSCRIBERS_ROLE"));
					announcement.setSubscriberUserIdsFromString(res.getString("SUBSCRIBERS_USER"));
					announcement.setAnnouncementChannelId(res.getString("CHANNEL_ID"));
					announcement.setAnnouncementType(AnnouncementType.valueOf(res.getString("ANNOUNCEMENT_TYPE")));
					announcement.setEventId(res.getString("EVENT_ID"));
					announcement.setEventColor(EventColor.fromNameOrHexOrID(res.getString("EVENT_COLOR")));
					announcement.setHoursBefore(res.getInt("HOURS_BEFORE"));
					announcement.setMinutesBefore(res.getInt("MINUTES_BEFORE"));
					announcement.setInfo(res.getString("INFO"));
					announcement.setInfoOnly(res.getBoolean("INFO_ONLY"));

					//The announcement is obviously enabled if we have gotten here lol
					announcement.setEnabled(true);

					announcements.add(announcement);
				}
			}

			stmt.close();
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get all enabled announcements.", e, true, this.getClass());
		}

		return announcements;
	}

	public ArrayList<Announcement> getEnabledAnnouncements(AnnouncementType type) {
		ArrayList<Announcement> announcements = new ArrayList<>();
		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String announcementTableName = String.format("%sannouncements", slaveInfo.getSettings().getPrefix());

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " + announcementTableName + " WHERE ENABLED = 1 AND ANNOUNCEMENT_TYPE = ?");
			stmt.setString(1, type.name());
			ResultSet res = stmt.executeQuery();

			while (res.next()) {
				if (res.getString("ANNOUNCEMENT_ID") != null) {
					Announcement announcement = new Announcement(UUID.fromString(res.getString("ANNOUNCEMENT_ID")), Snowflake.of(res.getString("GUILD_ID")));
					announcement.setSubscriberRoleIdsFromString(res.getString("SUBSCRIBERS_ROLE"));
					announcement.setSubscriberUserIdsFromString(res.getString("SUBSCRIBERS_USER"));
					announcement.setAnnouncementChannelId(res.getString("CHANNEL_ID"));
					announcement.setAnnouncementType(type);
					announcement.setEventId(res.getString("EVENT_ID"));
					announcement.setEventColor(EventColor.fromNameOrHexOrID(res.getString("EVENT_COLOR")));
					announcement.setHoursBefore(res.getInt("HOURS_BEFORE"));
					announcement.setMinutesBefore(res.getInt("MINUTES_BEFORE"));
					announcement.setInfo(res.getString("INFO"));
					announcement.setInfoOnly(res.getBoolean("INFO_ONLY"));

					//The announcement is obviously enabled if we have gotten here lol
					announcement.setEnabled(true);

					announcements.add(announcement);
				}
			}

			stmt.close();
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get all enabled announcements by type.", e, true, this.getClass());
		}

		return announcements;
	}

	public ArrayList<Announcement> getEnabledAnnouncements(Snowflake guildId) {
		ArrayList<Announcement> announcements = new ArrayList<>();
		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String announcementTableName = String.format("%sannouncements", slaveInfo.getSettings().getPrefix());

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " + announcementTableName + " WHERE ENABLED = 1 AND GUILD_ID = ?");
			stmt.setString(1, guildId.asString());
			ResultSet res = stmt.executeQuery();

			while (res.next()) {
				if (res.getString("ANNOUNCEMENT_ID") != null) {
					Announcement announcement = new Announcement(UUID.fromString(res.getString("ANNOUNCEMENT_ID")), guildId);
					announcement.setSubscriberRoleIdsFromString(res.getString("SUBSCRIBERS_ROLE"));
					announcement.setSubscriberUserIdsFromString(res.getString("SUBSCRIBERS_USER"));
					announcement.setAnnouncementChannelId(res.getString("CHANNEL_ID"));
					announcement.setAnnouncementType(AnnouncementType.fromValue(res.getString("ANNOUNCEMENT_TYPE")));
					announcement.setEventId(res.getString("EVENT_ID"));
					announcement.setEventColor(EventColor.fromNameOrHexOrID(res.getString("EVENT_COLOR")));
					announcement.setHoursBefore(res.getInt("HOURS_BEFORE"));
					announcement.setMinutesBefore(res.getInt("MINUTES_BEFORE"));
					announcement.setInfo(res.getString("INFO"));
					announcement.setInfoOnly(res.getBoolean("INFO_ONLY"));

					//The announcement is obviously enabled if we have gotten here lol
					announcement.setEnabled(true);

					announcements.add(announcement);
				}
			}

			stmt.close();
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get all enabled announcements by type.", e, true, this.getClass());
		}

		return announcements;
	}

	public int getAnnouncementCount() {
		int amount = -1;
		try (final Connection connection = slaveInfo.getSource().getConnection()) {
			String announcementTableName = String.format("%sannouncements", slaveInfo.getSettings().getPrefix());

			String query = "SELECT COUNT(*) FROM " + announcementTableName + ";";
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet res = statement.executeQuery();

			if (res.next())
				amount = res.getInt(1);
			else
				amount = 0;

			res.close();
			statement.close();
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to get announcement count", e, true, this.getClass());
		}
		return amount;
	}

	/**
	 * Deletes the specified announcement from the Database.
	 *
	 * @param announcementId The ID of the announcement to delete.
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	public boolean deleteAnnouncement(String announcementId) {
		try (final Connection connection = masterInfo.getSource().getConnection()) {
			String announcementTableName = String.format("%sannouncements", masterInfo.getSettings().getPrefix());

			String query = "DELETE FROM " + announcementTableName + " WHERE ANNOUNCEMENT_ID = ?";
			PreparedStatement preparedStmt = connection.prepareStatement(query);
			preparedStmt.setString(1, announcementId);

			preparedStmt.execute();
			preparedStmt.close();
			return true;
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to delete announcement.", e, true, this.getClass());
		}
		return false;
	}

	public boolean deleteAnnouncementsForEvent(Snowflake guildId, String eventId) {
		try (final Connection connection = masterInfo.getSource().getConnection()) {
			String announcementTableName = String.format("%sannouncements", masterInfo.getSettings().getPrefix());

			String query = "DELETE FROM " + announcementTableName + " WHERE EVENT_ID = ? AND GUILD_ID = ? AND ANNOUNCEMENT_TYPE = ?";
			PreparedStatement preparedStmt = connection.prepareStatement(query);
			preparedStmt.setString(1, eventId);
			preparedStmt.setString(2, guildId.asString());
			preparedStmt.setString(3, AnnouncementType.SPECIFIC.name());

			preparedStmt.execute();
			preparedStmt.close();
			return true;
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to delete announcements for specific event.", e, true, this.getClass());
		}
		return false;
	}

	public boolean deleteEventData(String eventId) {
		try (final Connection connection = masterInfo.getSource().getConnection()) {
			String eventTable = String.format("%sevents", masterInfo.getSettings().getPrefix());

			//Check if recurring...
			if (eventId.contains("_"))
				return false; //Don't delete if child event of recurring event.


			String query = "DELETE FROM " + eventTable + " WHERE EVENT_ID = ?";
			PreparedStatement preparedStmt = connection.prepareStatement(query);
			preparedStmt.setString(1, eventId);

			preparedStmt.execute();
			preparedStmt.close();
			return true;
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to delete event data.", e, true, this.getClass());
		}
		return false;
	}

	public boolean deleteAllEventData(Snowflake guildId) {
		try (final Connection connection = masterInfo.getSource().getConnection()) {
			String eventTable = String.format("%sevents", masterInfo.getSettings().getPrefix());

			String query = "DELETE FROM " + eventTable + " WHERE GUILD_ID = ?";
			PreparedStatement preparedStmt = connection.prepareStatement(query);
			preparedStmt.setString(1, guildId.asString());

			preparedStmt.execute();
			preparedStmt.close();
			return true;
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to delete all event data for guild.", e, true, this.getClass());
		}
		return false;
	}

	public boolean deleteAllAnnouncementData(Snowflake guildId) {
		try (final Connection connection = masterInfo.getSource().getConnection()) {
			String announcementTable = String.format("%sannouncements", masterInfo.getSettings().getPrefix());

			String query = "DELETE FROM " + announcementTable + " WHERE GUILD_ID = ?";
			PreparedStatement preparedStmt = connection.prepareStatement(query);
			preparedStmt.setString(1, guildId.asString());

			preparedStmt.execute();
			preparedStmt.close();
			return true;
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to delete all announcements for guild.", e, true, this.getClass());
		}
		return false;
	}

	public boolean deleteAllRSVPData(Snowflake guildId) {
		try (final Connection connection = masterInfo.getSource().getConnection()) {
			String rsvpTable = String.format("%srsvp", masterInfo.getSettings().getPrefix());

			String query = "DELETE FROM " + rsvpTable + " WHERE GUILD_ID = ?";
			PreparedStatement preparedStmt = connection.prepareStatement(query);
			preparedStmt.setString(1, guildId.asString());

			preparedStmt.execute();
			preparedStmt.close();
			return true;
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to delete all RSVP data for guild.", e, true, this.getClass());
		}
		return false;
	}

	public boolean deleteCalendar(CalendarData data) {
		try (final Connection connection = masterInfo.getSource().getConnection()) {
			String calendarTable = String.format("%scalendars", masterInfo.getSettings().getPrefix());

			String query = "DELETE FROM " + calendarTable + " WHERE GUILD_ID = ? AND CALENDAR_ADDRESS = ?";
			PreparedStatement preparedStmt = connection.prepareStatement(query);
			preparedStmt.setString(1, data.getGuildId().asString());
			preparedStmt.setString(2, data.getCalendarAddress());

			preparedStmt.execute();
			preparedStmt.close();
			return true;
		} catch (SQLException e) {
			Logger.getLogger().exception(null, "Failed to delete calendar from database for guild.", e, true, this.getClass());
		}
		return false;
	}

	public void clearCache() {
		guildSettingsCache.clear();
	}
}