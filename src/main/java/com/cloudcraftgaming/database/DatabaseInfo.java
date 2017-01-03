package com.cloudcraftgaming.database;

import java.sql.Connection;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("unused")
public class DatabaseInfo {
    private MySQL mySQL;
    private Connection con;
    private String prefix;

    public DatabaseInfo(MySQL _mySQL, Connection _con, String _prefix) {
        mySQL = _mySQL;
        con = _con;
        prefix = _prefix;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public Connection getConnection() {
        return con;
    }

    public String getPrefix() {
        return prefix;
    }
}