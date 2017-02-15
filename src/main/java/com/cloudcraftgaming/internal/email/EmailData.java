package com.cloudcraftgaming.internal.email;

/**
 * Created by Nova Fox on 2/15/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EmailData {
    private final String username;
    private final String password;

    public EmailData(String _username, String _password) {
        username = _username;
        password = _password;
    }

    String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }
}