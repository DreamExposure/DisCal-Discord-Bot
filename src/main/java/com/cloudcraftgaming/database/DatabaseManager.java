package com.cloudcraftgaming.database;

import com.cloudcraftgaming.Main;
import com.cloudcraftgaming.internal.data.BotData;
import com.cloudcraftgaming.internal.email.EmailSender;
import com.cloudcraftgaming.module.announcement.Announcement;
import com.cloudcraftgaming.module.announcement.AnnouncementType;
import sx.blah.discord.handle.obj.IGuild;

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

    private DatabaseManager() {}

    public static DatabaseManager getManager() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void connectToMySQL(MySQL mySQL) {
        try {
            Connection mySQLConnection = mySQL.openConnection();
            databaseInfo = new DatabaseInfo(mySQL, mySQLConnection, mySQL.getPrefix());
            System.out.println("Connected to MySQL database!");
        } catch (Exception e) {
            System.out.println("Failed to connect to MySQL database! Is it properly configured?");
            EmailSender.getSender().sendExceptionEmail(e);
            e.printStackTrace();
        }
    }

    public void disconnectFromMySQL() {
        if (databaseInfo != null) {
            try {
                databaseInfo.getMySQL().closeConnection();
                System.out.println("Successfully disconnected from MySQL Database!");
            } catch (SQLException e) {
                EmailSender.getSender().sendExceptionEmail(e);
                System.out.println("MySQL Connection may not have closed properly! Data may be invalidated!");
            }
        }
    }

    public Boolean createTables() {
        try {
            Statement statement = databaseInfo.getConnection().createStatement();

            String dataTableName = databaseInfo.getPrefix() + "DATA";
            String announcementTableName = databaseInfo.getPrefix() + "ANNOUNCEMENTS";
            String createDataTable = "CREATE TABLE IF NOT EXISTS " + dataTableName +
                    " (GUILD_ID VARCHAR(255) not NULL, " +
                    " CALENDAR_ID VARCHAR(255) not NULL, " +
                    " CALENDAR_ADDRESS LONGTEXT not NULL, " +
                    " CONTROL_ROLE LONGTEXT not NULL, " +
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
                    " PRIMARY KEY (ANNOUNCEMENT_ID))";
            statement.executeUpdate(createDataTable);
            statement.executeUpdate(createAnnouncementTable);
            statement.close();
            System.out.println("Successfully created needed tables in MySQL database!");
            return true;
        } catch (SQLException e) {
            System.out.println("Failed to created database tables! Something must be wrong.");
            EmailSender.getSender().sendExceptionEmail(e);
            e.printStackTrace();
        }
        return false;
    }

    public Boolean updateData(BotData data) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String dataTableName = databaseInfo.getPrefix() + "DATA";

                Statement statement = databaseInfo.getConnection().createStatement();
                String query = "SELECT * FROM " + dataTableName + " WHERE GUILD_ID = '" + data.getGuildId() + "';";
                ResultSet res = statement.executeQuery(query);

                Boolean hasStuff = res.next();

                if (!hasStuff || res.getString("GUILD_ID") == null) {
                    //Data not present, add to DB.
                    String insertCommand = "INSERT INTO " + dataTableName +
                            "(GUILD_ID, CALENDAR_ID, CALENDAR_ADDRESS, CONTROL_ROLE)" +
                            " VALUES (?, ?, ?, ?);";
                    PreparedStatement ps = databaseInfo.getConnection().prepareStatement(insertCommand);
                    ps.setString(1, data.getGuildId());
                    ps.setString(2, data.getCalendarId());
                    ps.setString(3, data.getCalendarAddress());
                    ps.setString(4, data.getControlRole());

                    ps.executeUpdate();
                    ps.close();
                    statement.close();
                } else {
                    //Data present, update.
                    String updateCMD = "UPDATE " + dataTableName
                            + " SET CALENDAR_ID= '" + data.getCalendarId()
                            + "', CALENDAR_ADDRESS='" + data.getCalendarAddress()
                            + "', CONTROL_ROLE='" + data.getControlRole()
                            + "' WHERE GUILD_ID= '" + data.getGuildId() + "';";
                    statement.executeUpdate(updateCMD);
                    statement.close();
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Failed to input data into database! Error Code: 00101");
            EmailSender.getSender().sendExceptionEmail(e);
            e.printStackTrace();
        }
        return false;
    }

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
                            "(ANNOUNCEMENT_ID, GUILD_ID, SUBSCRIBERS_ROLE, SUBSCRIBERS_USER, CHANNEL_ID, ANNOUNCEMENT_TYPE, EVENT_ID, HOURS_BEFORE, MINUTES_BEFORE)" +
                            " VALUE (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                             + "' WHERE ANNOUNCEMENT_ID='" + announcement.getAnnouncementId() + "';";
                    statement.executeUpdate(updateCMD);
                    statement.close();
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.print("Failed to input announcement data! Error Code: 00201");
            EmailSender.getSender().sendExceptionEmail(e);
            e.printStackTrace();
        }
        return false;
    }

    public BotData getData(String guildId) {
        BotData botData = new BotData(guildId);
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String dataTableName = databaseInfo.getPrefix() + "DATA";

                Statement statement = databaseInfo.getConnection().createStatement();
                String query = "SELECT * FROM " + dataTableName + " WHERE GUILD_ID = '" + botData.getGuildId() + "';";
                ResultSet res = statement.executeQuery(query);

                Boolean hasStuff = res.next();

                if (hasStuff && res.getString("GUILD_ID") != null) {
                    botData.setCalendarId(res.getString("CALENDAR_ID"));
                    botData.setCalendarAddress(res.getString("CALENDAR_ADDRESS"));
                    botData.setControlRole(res.getString("CONTROL_ROLE"));

                    statement.close();
                } else {
                    //Data not present.
                    statement.close();
                    return botData;
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get data from database! Error code: 00102");
            EmailSender.getSender().sendExceptionEmail(e);
        }
        return botData;
    }

    public Announcement getAnnouncement(UUID announcementId, String guildId) {
        Announcement announcement = new Announcement(announcementId, guildId);
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String announcementTableName = databaseInfo.getPrefix() + "ANNOUNCEMENTS";

                Statement statement = databaseInfo.getConnection().createStatement();
                String query = "SELECT * FROM " + announcementTableName + " WHERE ANNOUNCEMENT_ID = '" + announcementId.toString() + "';";
                ResultSet res = statement.executeQuery(query);

                Boolean hasStuff = res.next();

                if (hasStuff || res.getString("ANNOUNCEMENT_ID") != null) {
                    announcement.setSubscriberRoleIdsFromString(res.getString("SUBSCRIBERS_ROLE"));
                    announcement.setSubscriberUserIdsFromString(res.getString("SUBSCRIBERS_USER"));
                    announcement.setAnnouncementChannelId(res.getString("CHANNEL_ID"));
                    announcement.setAnnouncementType(AnnouncementType.valueOf(res.getString("ANNOUNCEMENT_TYPE")));
                    announcement.setEventId(res.getString("EVENT_ID"));
                    announcement.setHoursBefore(res.getInt("HOURS_BEFORE"));
                    announcement.setMinutesBefore(res.getInt("MINUTES_BEFORE"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get announcement from database! Error code: 00202");
            EmailSender.getSender().sendExceptionEmail(e);
            e.printStackTrace();
        }
        return announcement;
    }

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

                        announcements.add(announcement);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get announcements from database! Error code: 00203");
            EmailSender.getSender().sendExceptionEmail(e);
            e.printStackTrace();
        }
        return announcements;
    }

    public ArrayList<Announcement> getAnnouncements() {
        ArrayList<Announcement> announcements = new ArrayList<>();
        for (IGuild g : Main.client.getGuilds()) {
            for (Announcement a : getAnnouncements(g.getID())) {
                announcements.add(a);
            }
        }
        return announcements;
    }

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
            EmailSender.getSender().sendExceptionEmail(e);
        }
        return false;
    }
}