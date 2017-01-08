package com.cloudcraftgaming.database;

import com.cloudcraftgaming.internal.data.BotData;

import java.sql.*;

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
            e.printStackTrace();
        }
    }

    public void disconnectFromMySQL() {
        if (databaseInfo != null) {
            try {
                databaseInfo.getMySQL().closeConnection();
                System.out.println("Successfully disconnected from MySQL Database!");
            } catch (SQLException e) {
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
                    " PRIMARY KEY (GUILD_ID))";
            String createAnnouncementTable = "CREATE TABLE IF NOT EXISTS " + announcementTableName +
                    " (ANNOUNCEMENT_ID VARCHAR(255) not NULL, " +
                    " GUILD_ID VARCHAR(255) not NULL, " +
                    " CHANNEL_ID VARCHAR(255) not NULL, " +
                    " SUBSCRIBERS LONGTEXT not NULL, " +
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
                            "(GUILD_ID, CALENDAR_ID, CALENDAR_ADDRESS)" +
                            " VALUES (?, ?, ?);";
                    PreparedStatement ps = databaseInfo.getConnection().prepareStatement(insertCommand);
                    ps.setString(1, data.getGuildId());
                    ps.setString(2, data.getCalendarId());
                    ps.setString(3, data.getCalendarAddress());

                    ps.executeUpdate();
                    ps.close();
                    statement.close();
                } else {
                    //Data present, update.
                    String updateCMD = "UPDATE " + dataTableName
                            + " SET CALENDAR_ID= '" + data.getCalendarId()
                            + "', CALENDAR_ADDRESS='" + data.getCalendarAddress()
                            + "' WHERE GUILD_ID= '" + data.getGuildId() + "';";
                    statement.executeUpdate(updateCMD);
                    statement.close();
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Failed to input data into database! Error Code: 00101");
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

                if (hasStuff || res.getString("GUILD_ID") != null) {
                    botData.setCalendarId(res.getString("CALENDAR_ID"));
                    botData.setCalendarAddress(res.getString("CALENDAR_ADDRESS"));

                    statement.close();
                } else {
                    //Data not present.
                    statement.close();
                    return botData;
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get data from database! Error code: 00102");
        }
        return botData;
    }
}