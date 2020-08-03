package org.dreamexposure.discal.core.object.web;

public class AuthenticationState {
    private final boolean success;

    private int status;

    private String reason;

    private boolean fromDiscalNetwork;

    private boolean readOnly;

    private String keyUsed;

    public AuthenticationState(final boolean _success) {
        this.success = _success;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public int getStatus() {
        return this.status;
    }

    public String getReason() {
        return this.reason;
    }

    public String getKeyUsed() {
        return this.keyUsed;
    }

    public boolean isFromDiscalNetwork() {
        return this.fromDiscalNetwork;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public AuthenticationState setStatus(final int _status) {
        this.status = _status;
        return this;
    }

    public AuthenticationState setReason(final String _reason) {
        this.reason = _reason;
        return this;
    }

    public AuthenticationState setKeyUsed(final String _key) {
        this.keyUsed = _key;
        return this;
    }

    public AuthenticationState setFromDisCalNetwork(final boolean _fromDisCal) {
        this.fromDiscalNetwork = _fromDisCal;
        return this;
    }

    public AuthenticationState setIsReadOnly(final boolean _readOnly) {
        this.readOnly = _readOnly;
        return this;
    }

    public String toJson() {
        return "{\"message\": \"" + this.reason + "\"}";
    }
}
