package com.cloudcraftgaming.discal.database;

import com.cloudcraftgaming.discal.internal.crypto.KeyGenerator;
import com.cloudcraftgaming.discal.internal.data.BotSettings;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.internal.email.EmailSender;
import com.cloudcraftgaming.discal.module.announcement.Announcement;
import com.cloudcraftgaming.discal.module.announcement.AnnouncementType;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("SqlResolve")
public class DatabaseManager {
    private static DatabaseManager instance;
    private DatabaseInfo databaseInfo;

    private DatabaseManager() {} //Prevent initialization.

    /**
     * Gets the instance of the {@link DatabaseManager}.
     * @return The instance of the {@link DatabaseManager}
     */
    public static DatabaseManager getManager() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Connects to the MySQL server specified.
     * @param bs The BotSettings with Db info to connect to.
     */
    public void  connectToMySQL(BotSettings bs) {
        try {
            MySQL mySQL = new MySQL(bs.getDbHostName(), bs.getDbPort(), bs.getDbDatabase(), bs.getDbPrefix(), bs.getDbUser(), bs.getDbPass());

            Connection mySQLConnection = mySQL.openConnection();
            databaseInfo = new DatabaseInfo(mySQL, mySQLConnection, mySQL.getPrefix());
            System.out.println("Connected to MySQL database!");
        } catch (Exception e) {
            System.out.println("Failed to connect to MySQL database! Is it properly configured?");
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
            e.printStackTrace();
        }
    }

    /**
     * Disconnects from the MySQL server if still connected.
     */
    public void disconnectFromMySQL() {
        if (databaseInfo != null) {
            try {
                databaseInfo.getMySQL().closeConnection();
                System.out.println("Successfully disconnected from MySQL Database!");
            } catch (SQLException e) {
                EmailSender.getSender().sendExceptionEmail(e, this.getClass());
                System.out.println("MySQL Connection may not have closed properly! Data may be invalidated!");
            }
        }
    }

    /**
     * Creates all required tables in the database if they do not exist.
     */
    public void createTables() {
        try {
            Statement statement = databaseInfo.getConnection().createStatement();

            String announcementTableName = databaseInfo.getPrefix() + "ANNOUNCEMENTS";
            String calendarTableName = databaseInfo.getPrefix() + "CALENDARS";
            String settingsTableName = databaseInfo.getPrefix() + "GUILD_SETTINGS";
            String createSettingsTable = "CREATE TABLE IF NOT EXISTS " + settingsTableName +
                    "(GUILD_ID VARCHAR(255) not NULL, " +
                    " EXTERNAL_CALENDAR BOOLEAN not NULL, " +
                    " PRIVATE_KEY VARCHAR(16) not NULL, " +
                    " ACCESS_TOKEN LONGTEXT not NULL, " +
                    " REFRESH_TOKEN LONGTEXT not NULL, " +
                    " CONTROL_ROLE LONGTEXT not NULL, " +
                    " DISCAL_CHANNEL LONGTEXT not NULL, " +
                    " PATRON_GUILD BOOLEAN not NULL, " +
                    " DEV_GUILD BOOLEAN not NULL, " +
                    " MAX_CALENDARS INTEGER not NULL, " +
                    " PRIMARY KEY (GUILD_ID))";
            String createAnnouncementTable = "CREATE TABLE IF NOT EXISTS " + announcementTableName +
                    " (ANNOUNCEMENT_ID VARCHAR(255) not NULL, " +
                    " GUILD_ID VARCHAR(255) not NULL, " +
                    " SUBSCRIBERS_ROLE LONGTEXT not NULL, " +
                    " SUBSCRIBERS_USER LONGTEXT not NULL, " +
                    " CHANNEL_ID VARCHAR(255) not NULL, " +
                    " ANNOUNCEMENT_TYPE VARCHAR(255) not NULL, " +
                    " EVENT_ID LONGTEXT not NULL, " +
                    " HOURS_BEFORE INTEGER not NULL, " +
                    " MINUTES_BEFORE INTEGER not NULL, " +
                    " INFO LONGTEXT not NULL, " +
                    " PRIMARY KEY (ANNOUNCEMENT_ID))";
            String createCalendarTable = "CREATE TABLE IF NOT EXISTS " + calendarTableName +
                    " (GUILD_ID VARCHAR(255) not NULL, " +
                    " CALENDAR_NUMBER INTEGER not NULL, " +
                    " CALENDAR_ID VARCHAR(255) not NULL, " +
                    " CALENDAR_ADDRESS LONGTEXT not NULL, " +
                    " PRIMARY KEY (GUILD_ID, CALENDAR_NUMBER))";
            statement.executeUpdate(createAnnouncementTable);
            statement.executeUpdate(createSettingsTable);
            statement.executeUpdate(createCalendarTable);
            statement.close();
            System.out.println("Successfully created needed tables in MySQL database!");
        } catch (SQLException e) {
            System.out.println("Failed to created database tables! Something must be wrong.");
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
            e.printStackTrace();
        }
    }

    public boolean updateSettings(GuildSettings settings) {
        if (settings.getPrivateKey().equalsIgnoreCase("N/a")) {
            settings.setPrivateKey(KeyGenerator.csRandomAlphaNumericString(16));
        }
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String dataTableName = databaseInfo.getPrefix() + "GUILD_SETTINGS";

                Statement statement = databaseInfo.getConnection().createStatement();
                String query = "SELECT * FROM " + dataTableName + " WHERE GUILD_ID = '" + settings.getGuildID() + "';";
                ResultSet res = statement.executeQuery(query);

                Boolean hasStuff = res.next();

                if (!hasStuff || res.getString("GUILD_ID") == null) {
                    //Data not present, add to DB.
                    String insertCommand = "INSERT INTO " + dataTableName +
                            "(GUILD_ID, EXTERNAL_CALENDAR, PRIVATE_KEY, ACCESS_TOKEN, REFRESH_TOKEN, CONTROL_ROLE, DISCAL_CHANNEL, PATRON_GUILD, DEV_GUILD, MAX_CALENDARS)" +
                            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                    PreparedStatement ps = databaseInfo.getConnection().prepareStatement(insertCommand);
                    ps.setString(1, settings.getGuildID());
                    ps.setBoolean(2, settings.useExternalCalendar());
                    ps.setString(3, settings.getPrivateKey());
                    ps.setString(4, settings.getEncryptedAccessToken());
                    ps.setString(5, settings.getEncryptedRefreshToken());
                    ps.setString(6, settings.getControlRole());
                    ps.setString(7, settings.getDiscalChannel());
                    ps.setBoolean(8, settings.isPatronGuild());
                    ps.setBoolean(9, settings.isDevGuild());
                    ps.setInt(10, settings.getMaxCalendars());


                    ps.executeUpdate();
                    ps.close();
                    statement.close();
                } else {
                    //Data present, update.
                    String update = "UPDATE " + dataTableName
                            + " SET EXTERNAL_CALENDAR = ?, PRIVATE_KEY = ?,"
                            + " ACCESS_TOKEN = ?, REFRESH_TOKEN = ?,"
                            + " CONTROL_ROLE = ?, DISCAL_CHANNEL = ?,"
                            + " PATRON_GUILD = ?, DEV_GUILD = ?,"
                            + " MAX_CALENDARS = ?"
                            + " WHERE GUILD_ID = ?";
                    PreparedStatement ps = databaseInfo.getConnection().prepareStatement(update);

                    ps.setBoolean(1, settings.useExternalCalendar());
                    ps.setString(2, settings.getPrivateKey());
                    ps.setString(3, settings.getEncryptedAccessToken());
                    ps.setString(4, settings.getEncryptedRefreshToken());
                    ps.setString(5, settings.getControlRole());
                    ps.setString(6, settings.getDiscalChannel());
                    ps.setBoolean(7, settings.isPatronGuild());
                    ps.setBoolean(8, settings.isDevGuild());
                    ps.setInt(9, settings.getMaxCalendars());
                    ps.setString(10, settings.getGuildID());

                    ps.executeUpdate();

                    statement.close();
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Failed to input data into database! Error Code: 00101");
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateCalendar(CalendarData calData) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String calendarTableName = databaseInfo.getPrefix() + "CALENDARS";

                Statement statement = databaseInfo.getConnection().createStatement();
                String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = '" + calData.getGuildId() + "';";
                ResultSet res = statement.executeQuery(query);

                Boolean hasStuff = res.next();

                if (!hasStuff || res.getString("GUILD_ID") == null) {
                    //Data not present, add to DB.
                    String insertCommand = "INSERT INTO " + calendarTableName +
                            "(GUILD_ID, CALENDAR_NUMBER, CALENDAR_ID, CALENDAR_ADDRESS)" +
                            " VALUES (?, ?, ?, ?);";
                    PreparedStatement ps = databaseInfo.getConnection().prepareStatement(insertCommand);
                    ps.setString(1, calData.getGuildId());
                    ps.setInt(2, calData.getCalendarNumber());
                    ps.setString(3, calData.getCalendarId());
                    ps.setString(4, calData.getCalendarAddress());

                    ps.executeUpdate();
                    ps.close();
                    statement.close();
                } else {
                    //Data present, update.
                    String updateCMD = "UPDATE " + calendarTableName
                            + " SET CALENDAR_NUMBER= '" + calData.getCalendarNumber()
                            + "', CALENDAR_ID='" + calData.getCalendarId()
                            + "', CALENDAR_ADDRESS='" + calData.getCalendarAddress()
                            + "' WHERE GUILD_ID= '" + calData.getGuildId() + "';";
                    statement.executeUpdate(updateCMD);
                    statement.close();
                }
                return true;
            }
        } catch (SQLException e) {
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
        }
        return false;
    }

    /**
     * Updates or Adds the specified {@link Announcement} Object to the database.
     * @param announcement The announcement object to add to the database.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    public Boolean updateAnnouncement(Announcement announcement) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String announcementTableName = databaseInfo.getPrefix() + "ANNOUNCEMENTS";

                Statement statement = databaseInfo.getConnection().createStatement();
                String query = "SELECT * FROM " + announcementTableName + " WHERE ANNOUNCEMENT_ID = '" + announcement.getAnnouncementId() + "';";
                ResultSet res = statement.executeQuery(query);

                Boolean hasStuff = res.next();

                if (!hasStuff || res.getString("ANNOUNCEMENT_ID") == null) {
                    //Data not present, add to db.
                    String insertCommand = "INSERT INTO " + announcementTableName +
                            "(ANNOUNCEMENT_ID, GUILD_ID, SUBSCRIBERS_ROLE, SUBSCRIBERS_USER, CHANNEL_ID, ANNOUNCEMENT_TYPE, EVENT_ID, HOURS_BEFORE, MINUTES_BEFORE, INFO)" +
                            " VALUE (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement ps = databaseInfo.getConnection().prepareStatement(insertCommand);
                    ps.setString(1, announcement.getAnnouncementId().toString());
                    ps.setString(2, announcement.getGuildId());
                    ps.setString(3, announcement.getSubscriberRoleIdString());
                    ps.setString(4, announcement.getSubscriberUserIdString());
                    ps.setString(5, announcement.getAnnouncementChannelId());
                    ps.setString(6, announcement.getAnnouncementType().name());
                    ps.setString(7, announcement.getEventId());
                    ps.setInt(8, announcement.getHoursBefore());
                    ps.setInt(9, announcement.getMinutesBefore());
                    ps.setString(10, announcement.getInfo());

                    ps.executeUpdate();
                    ps.close();
                    statement.close();
                } else {
                    //Data present, update.
                    String updateCMD = "UPDATE " + announcementTableName
                            + " SET SUBSCRIBERS_ROLE= '" + announcement.getSubscriberRoleIdString()
                            + "', SUBSCRIBERS_USER='" + announcement.getSubscriberUserIdString()
                            + "', CHANNEL_ID='" + announcement.getAnnouncementChannelId()
                            + "', ANNOUNCEMENT_TYPE='" + announcement.getAnnouncementType().name()
                            + "', EVENT_ID='" + announcement.getEventId()
                            + "', HOURS_BEFORE='" + announcement.getHoursBefore()
                            + "', MINUTES_BEFORE='" + announcement.getMinutesBefore()
                            + "', INFO='" + announcement.getInfo()
                             + "' WHERE ANNOUNCEMENT_ID='" + announcement.getAnnouncementId() + "';";
                    statement.executeUpdate(updateCMD);
                    statement.close();
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.print("Failed to input announcement data! Error Code: 00201");
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
            e.printStackTrace();
        }
        return false;
    }

    public GuildSettings getSettings(String guildId) {
        GuildSettings settings = new GuildSettings(guildId);
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String dataTableName = databaseInfo.getPrefix() + "GUILD_SETTINGS";

                Statement statement = databaseInfo.getConnection().createStatement();
                String query = "SELECT * FROM " + dataTableName + " WHERE GUILD_ID = '" + guildId + "';";
                ResultSet res = statement.executeQuery(query);

                Boolean hasStuff = res.next();

                if (hasStuff && res.getString("GUILD_ID") != null) {
                    settings.setUseExternalCalendar(res.getBoolean("EXTERNAL_CALENDAR"));
                    settings.setPrivateKey(res.getString("PRIVATE_KEY"));
                    settings.setEncryptedAccessToken(res.getString("ACCESS_TOKEN"));
                    settings.setEncryptedRefreshToken(res.getString("REFRESH_TOKEN"));
                    settings.setControlRole(res.getString("CONTROL_ROLE"));
                    settings.setDiscalChannel(res.getString("DISCAL_CHANNEL"));
                    settings.setPatronGuild(res.getBoolean("PATRON_GUILD"));
                    settings.setDevGuild(res.getBoolean("DEV_GUILD"));
                    settings.setMaxCalendars(res.getInt("MAX_CALENDARS"));

                    statement.close();
                } else {
                    //Data not present.
                    statement.close();
                    return settings;
                }
            }
        } catch (SQLException e) {
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
        }
        return settings;
    }

    public CalendarData getMainCalendar(String guildId) {
        CalendarData calData = new CalendarData(guildId, 1);
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String calendarTableName = databaseInfo.getPrefix() + "CALENDARS";

                Statement statement = databaseInfo.getConnection().createStatement();
                String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = '" + guildId + "';";
                ResultSet res = statement.executeQuery(query);

                while (res.next()) {
                    if (res.getInt("CALENDAR_NUMBER") == 1) {
                        calData.setCalendarId(res.getString("CALENDAR_ID"));
                        calData.setCalendarAddress(res.getString("CALENDAR_ADDRESS"));
                        break;
                    }
                }
                statement.close();
            }
        } catch (SQLException e) {
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
        }
        return calData;
    }

    public CalendarData getCalendar(String guildId, Integer calendarNumber) {
        CalendarData calData = new CalendarData(guildId, calendarNumber);
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String calendarTableName = databaseInfo.getPrefix() + "CALENDARS";

                Statement statement = databaseInfo.getConnection().createStatement();
                String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = '" + guildId + "';";
                ResultSet res = statement.executeQuery(query);

                while (res.next()) {
                    if (res.getInt("CALENDAR_NUMBER") == calendarNumber) {
                        calData.setCalendarId(res.getString("CALENDAR_ID"));
                        calData.setCalendarAddress(res.getString("CALENDAR_ADDRESS"));
                        break;
                    }
                }
                statement.close();
            }
        } catch (SQLException e) {
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
        }
        return calData;
    }

    public ArrayList<CalendarData> getAllCalendars(String guildId) {
        ArrayList<CalendarData> calendars = new ArrayList<>();
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String calendarTableName = databaseInfo.getPrefix() + "CALENDARS";

                Statement statement = databaseInfo.getConnection().createStatement();
                String query = "SELECT * FROM " + calendarTableName + " WHERE GUILD_ID = '" + guildId + "';";
                ResultSet res = statement.executeQuery(query);

                while (res.next()) {
                    CalendarData calData = new CalendarData(guildId, res.getInt("CALENDAR_NUMBER"));
                    calData.setCalendarId(res.getString("CALENDAR_ID"));
                    calData.setCalendarAddress(res.getString("CALENDAR_ADDRESS"));
                    calendars.add(calData);
                }
                statement.close();
            }
        } catch (SQLException e) {
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
        }
        return calendars;
    }

    /**
     * Gets the {@link Announcement} Object with the corresponding ID for the specified Guild.
     * @param announcementId The ID of the announcement.
     * @param guildId The ID of the guild the Announcement belongs to.
     * @return The {@link Announcement} with the specified ID if it exists, otherwise <c>null</c>.
     */
    public Announcement getAnnouncement(UUID announcementId, String guildId) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String announcementTableName = databaseInfo.getPrefix() + "ANNOUNCEMENTS";

                Statement statement = databaseInfo.getConnection().createStatement();
                String query = "SELECT * FROM " + announcementTableName + " WHERE ANNOUNCEMENT_ID = '" + announcementId.toString() + "';";
                ResultSet res = statement.executeQuery(query);

                Boolean hasStuff = res.next();

                if (hasStuff && res.getString("ANNOUNCEMENT_ID") != null) {
                    Announcement announcement = new Announcement(announcementId, guildId);
                    announcement.setSubscriberRoleIdsFromString(res.getString("SUBSCRIBERS_ROLE"));
                    announcement.setSubscriberUserIdsFromString(res.getString("SUBSCRIBERS_USER"));
                    announcement.setAnnouncementChannelId(res.getString("CHANNEL_ID"));
                    announcement.setAnnouncementType(AnnouncementType.valueOf(res.getString("ANNOUNCEMENT_TYPE")));
                    announcement.setEventId(res.getString("EVENT_ID"));
                    announcement.setHoursBefore(res.getInt("HOURS_BEFORE"));
                    announcement.setMinutesBefore(res.getInt("MINUTES_BEFORE"));
                    announcement.setInfo(res.getString("INFO"));

                    statement.close();
                    return announcement;
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get announcement from database! Error code: 00202");
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets an {@link ArrayList} of {@link Announcement}s belonging to the specific Guild.
     * @param guildId The ID of the guild whose data is to be retrieved.
     * @return An ArrayList of Announcements that belong to the specified Guild.
     */
    public ArrayList<Announcement> getAnnouncements(String guildId) {
        ArrayList<Announcement> announcements = new ArrayList<>();
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String announcementTableName = databaseInfo.getPrefix() + "ANNOUNCEMENTS";

                Statement statement = databaseInfo.getConnection().createStatement();
                String query = "SELECT * FROM " + announcementTableName + " WHERE GUILD_ID = '" + guildId + "';";
                ResultSet res = statement.executeQuery(query);

                while (res.next()) {
                    if (res.getString("ANNOUNCEMENT_ID") != null) {
                        Announcement announcement = new Announcement(UUID.fromString(res.getString("ANNOUNCEMENT_ID")), guildId);
                        announcement.setSubscriberRoleIdsFromString(res.getString("SUBSCRIBERS_ROLE"));
                        announcement.setSubscriberUserIdsFromString(res.getString("SUBSCRIBERS_USER"));
                        announcement.setAnnouncementChannelId(res.getString("CHANNEL_ID"));
                        announcement.setAnnouncementType(AnnouncementType.valueOf(res.getString("ANNOUNCEMENT_TYPE")));
                        announcement.setEventId(res.getString("EVENT_ID"));
                        announcement.setHoursBefore(res.getInt("HOURS_BEFORE"));
                        announcement.setMinutesBefore(res.getInt("MINUTES_BEFORE"));
                        announcement.setInfo(res.getString("INFO"));

                        announcements.add(announcement);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get announcements from database! Error code: 00203");
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
            e.printStackTrace();
        }
        return announcements;
    }

    /**
     * Deletes the specified announcement from the Database.
     * @param announcementId The ID of the announcement to delete.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    public Boolean deleteAnnouncement(String announcementId) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String announcementTableName = databaseInfo.getPrefix() + "ANNOUNCEMENTS";

                String query = "DELETE FROM " + announcementTableName + " WHERE ANNOUNCEMENT_ID = ?";
                PreparedStatement preparedStmt = databaseInfo.getConnection().prepareStatement(query);
                preparedStmt.setString(1, announcementId);

                preparedStmt.execute();
                preparedStmt.close();
                return true;
            }
        } catch (SQLException e) {
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
        }
        return false;
    }

    public void runDatabaseUpdateIfNeeded() {}
}