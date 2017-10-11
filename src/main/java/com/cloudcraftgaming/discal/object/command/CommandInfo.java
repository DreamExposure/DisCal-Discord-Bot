package com.cloudcraftgaming.discal.object.command;

import java.util.HashMap;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CommandInfo {
	private final String name;
	private String description;
	private String example;

	private HashMap<String, String> subCommands = new HashMap<>();

	public CommandInfo(String _name) {
		name = _name;
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

	//Setters
	public void setDescription(String _description) {
		description = _description;
	}

	public void setExample(String _example) {
		example = _example;
	}
}