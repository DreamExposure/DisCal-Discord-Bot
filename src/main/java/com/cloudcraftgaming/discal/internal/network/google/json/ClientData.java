package com.cloudcraftgaming.discal.internal.network.google.json;

/**
 * Created by Nova Fox on 3/27/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class ClientData {
    private final String clientId;
    private final String clientSecret;

    public ClientData(String _clientId, String _clientSecret) {
        clientId = _clientId;
        clientSecret = _clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}