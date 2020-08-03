package org.dreamexposure.discal.core.object.web;

public class WebPartialGuild {
    private final long id;
    private final String name;
    private final String iconUrl;

    public WebPartialGuild(final long id, final String name, final String iconUrl) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
    }

    //Getters
    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getIconUrl() {
        return this.iconUrl;
    }
}
