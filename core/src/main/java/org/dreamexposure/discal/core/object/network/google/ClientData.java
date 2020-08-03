package org.dreamexposure.discal.core.object.network.google;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class ClientData {
    private final String clientId;
    private final String clientSecret;

    public ClientData(final String _clientId, final String _clientSecret) {
        this.clientId = _clientId;
        this.clientSecret = _clientSecret;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }
}