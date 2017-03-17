package com.cloudcraftgaming.discal.database;

import java.sql.Connection;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("unused")
class DatabaseInfo {
    private MySQL mySQL;
    private Connection con;
    private String prefix;

    DatabaseInfo(MySQL _mySQL, Connection _con, String _prefix) {
        mySQL = _mySQL;
        con = _con;
        prefix = _prefix;
    }

    MySQL getMySQL() {
        return mySQL;
    }

    Connection getConnection() {
        return con;
    }

    String getPrefix() {
        return prefix;
    }
}