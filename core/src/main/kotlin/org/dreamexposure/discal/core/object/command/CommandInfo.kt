package org.dreamexposure.discal.core.`object`.command

data class CommandInfo(
        val name: String,
        val description: String,
        val example: String
) {
    val subCommands: MutableMap<String, String> = mutableMapOf()
}
