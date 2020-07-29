package org.dreamexposure.discal.core.object.command;

import java.util.HashMap;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class CommandInfo {
    private final String name;
    private final String description;
    private final String example;

    private final HashMap<String, String> subCommands = new HashMap<>();

    public CommandInfo(String name, String description, String example) {
        this.name = name;
        this.description = description;
        this.example = example;
    }

    //Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getExample() {
        return example;
    }

    public HashMap<String, String> getSubCommands() {
        return subCommands;
    }
}