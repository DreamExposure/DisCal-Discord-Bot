package com.cloudcraftgaming.discal.internal.email;

/**
 * Created by Nova Fox on 2/15/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EmailData {
    private final String username;
    private final String password;

    /**
     * Creates a new EmailData Object with the required information.
     * @param _username The username (email) of the sending email address.
     * @param _password The password of the sending email address.
     */
    public EmailData(String _username, String _password) {
        username = _username;
        password = _password;
    }

    /**
     * Gets the username (email) of the sending email address.
     * @return The username (email) of the sending email address.
     */
    String getUsername() {
        return username;
    }

    /**
     * Gets the password of the sending email address.
     * @return The password of the sending email address.
     */
    String getPassword() {
        return password;
    }
}