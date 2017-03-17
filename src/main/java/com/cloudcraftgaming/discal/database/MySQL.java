package com.cloudcraftgaming.discal.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connects to and uses a MySQL database
 *
 * @author -_Husky_-
 * @author tips48
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class MySQL extends Database {
    private final String user;
    private final String database;
    private final String password;
    private final String port;
    private final String hostname;
    private final String prefix;

    /**
     * Creates a new MySQL instance
     *
     * @param hostname
     *            Name of the host
     * @param port
     *            Port number
     * @param username
     *            Username
     * @param password
     *            Password
     */
    public MySQL(String hostname, String port, String prefix, String username,
                 String password) {
        this(hostname, port, null, prefix, username, password);
    }

    /**
     * Creates a new MySQL instance for a specific database
     *
     * @param hostname
     *            Name of the host
     * @param port
     *            Port number
     * @param database
     *            Database name
     * @param username
     *            Username
     * @param password
     *            Password
     */
    public MySQL(String hostname, String port, String database, String prefix, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = username;
        this.password = password;
        this.prefix = prefix;
    }

    @Override
    public Connection openConnection() throws SQLException,
            ClassNotFoundException {
        if (checkConnection()) {
            return connection;
        }
        String connectionURL = "jdbc:mysql://"
                + this.hostname + ":" + this.port;
        if (database != null) {
            connectionURL = connectionURL + "/" + this.database + "?autoReconnect=true&useSSL=false";
        }

        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection(connectionURL,
                this.user, this.password);
        return connection;
    }

    public String getPrefix() {
        return prefix;
    }
}