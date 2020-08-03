package org.dreamexposure.discal.core.object.web;

import org.dreamexposure.discal.core.object.GuildSettings;
import org.json.JSONObject;

import discord4j.core.object.entity.Role;
import discord4j.discordjson.json.RoleData;

/**
 * Created by Nova Fox on 1/6/18.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class WebRole {
    public static WebRole fromRole(final Role r, final GuildSettings settings) {
        final boolean controlRole;
        if (r.isEveryone() && "everyone".equalsIgnoreCase(settings.getControlRole()))
            controlRole = true;
        else
            controlRole = settings.getControlRole().equalsIgnoreCase(r.getId().asString());


        return new WebRole(r.getId().asLong(), r.getName(), r.isManaged(), controlRole, r.isEveryone());
    }

    public static WebRole fromRole(final RoleData r, final GuildSettings settings) {
        final boolean controlRole;
        final boolean everyone = r.id().equals(settings.getGuildID().asString());
        if (everyone && "everyone".equalsIgnoreCase(settings.getControlRole()))
            controlRole = true;
        else
            controlRole = settings.getControlRole().equalsIgnoreCase(r.id());


        return new WebRole(Long.parseLong(r.id()), r.name(), r.managed(), controlRole, everyone);
    }

    public static WebRole fromJson(final JSONObject data) {
        final long id = Long.parseLong(data.getString("id"));
        final String name = data.getString("name");
        final boolean managed = data.getBoolean("managed");
        final boolean controlRole = data.getBoolean("control_role");
        final boolean everyone = data.getBoolean("everyone");

        return new WebRole(id, name, managed, controlRole, everyone);
    }


    private final long id;
    private final String name;

    private final boolean managed;
    private final boolean controlRole;

    private final boolean everyone;

    private WebRole(final long id, final String name, final boolean managed, final boolean controlRole,
                    final boolean everyone) {
        this.id = id;
        this.name = name;
        this.managed = managed;
        this.controlRole = controlRole;
        this.everyone = everyone;
    }

    //Getters
    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean isManaged() {
        return this.managed;
    }

    public boolean isControlRole() {
        return this.controlRole;
    }

    public boolean isEveryone() {
        return this.everyone;
    }

    //functions
    public JSONObject toJson() {
        final JSONObject data = new JSONObject();

        data.put("id", String.valueOf(this.id));
        data.put("name", this.name);
        data.put("managed", this.managed);
        data.put("control_role", this.controlRole);
        data.put("everyone", this.everyone);

        return data;
    }
}