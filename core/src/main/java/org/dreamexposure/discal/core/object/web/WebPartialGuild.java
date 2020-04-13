package org.dreamexposure.discal.core.object.web;

public class WebPartialGuild {
    private final long id;
    private final String name;
    private final String iconUrl;

    public WebPartialGuild(long id, String name, String iconUrl) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
    }

    //Getters
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return iconUrl;
    }
}
