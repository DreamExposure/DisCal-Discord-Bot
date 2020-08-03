package org.dreamexposure.discal.core.object.web;

public class UserAPIAccount {
    private String userId;
    private String APIKey;
    private boolean blocked;
    private long timeIssued;

    //Getters
    public String getUserId() {
        return this.userId;
    }

    public String getAPIKey() {
        return this.APIKey;
    }

    public boolean isBlocked() {
        return this.blocked;
    }

    public long getTimeIssued() {
        return this.timeIssued;
    }

    //Setters
    public void setUserId(final String _userId) {
        this.userId = _userId;
    }

    public void setAPIKey(final String _apiKey) {
        this.APIKey = _apiKey;
    }

    public void setBlocked(final boolean _blocked) {
        this.blocked = _blocked;
    }

    public void setTimeIssued(final long _time) {
        this.timeIssued = _time;
    }
}