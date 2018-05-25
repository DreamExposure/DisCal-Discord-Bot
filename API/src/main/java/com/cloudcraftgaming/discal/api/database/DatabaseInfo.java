package com.cloudcraftgaming.discal.api.database;

import java.sql.Connection;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("unused")
class DatabaseInfo {
	private MySQL mySQL;
	private Connection con;
	private String prefix;

	/**
	 * Creates a new DatabaseInfo Object
	 *
	 * @param _mySQL  The MySQL server element.
	 * @param _con    The connection to the MySQL server.
	 * @param _prefix The prefix for all tables.
	 */
	DatabaseInfo(MySQL _mySQL, Connection _con, String _prefix) {
		mySQL = _mySQL;
		con = _con;
		prefix = _prefix;
	}

	/**
	 * Gets the MySQL server currently connected
	 *
	 * @return The MySQL server currently connect.
	 */
	MySQL getMySQL() {
		return mySQL;
	}

	/**
	 * Gets the current connection to the MySQL server.
	 *
	 * @return The current connection to the MySQL server.
	 */
	Connection getConnection() {
		return con;
	}

	/**
	 * Gets the prefix for all tables.
	 *
	 * @return The prefix for all tables.
	 */
	String getPrefix() {
		return prefix;
	}
}