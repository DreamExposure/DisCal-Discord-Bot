package org.dreamexposure.discal.server.network.discord

import com.fasterxml.jackson.module.kotlin.readValue
import discord4j.common.JacksonResources
import discord4j.discordjson.json.ApplicationCommandData
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.RestClient
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component

@Component
class GlobalCommandRegistrar(
        private val restClient: RestClient
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        val d4jMapper = JacksonResources.create()

        val matcher = PathMatchingResourcePatternResolver()
        val applicationService = restClient.applicationService
        val applicationId = restClient.applicationId.block()!!
        val discordCommands = applicationService.getGlobalApplicationCommands(applicationId)
                .collectMap(ApplicationCommandData::name)
                .block()!!

        var added = 0
        var removed = 0
        var updated = 0

        val commands = mutableMapOf<String, ApplicationCommandRequest>()
        for (res in matcher.getResources("commands/*.json")) {
            val request = d4jMapper.objectMapper.readValue<ApplicationCommandRequest>(res.inputStream)
            commands[request.name()] = request

            if (discordCommands[request.name()] == null) {
                added++
                applicationService.createGlobalApplicationCommand(applicationId, request).block()
            }
        }

        for ((discordCommandName, discordCommand) in discordCommands) {
            val discordCommandId = discordCommand.id().toLong()
            val command = commands[discordCommandName]
            if (command == null) { // Removed command.json, delete global command
                removed++
                applicationService.deleteGlobalApplicationCommand(applicationId, discordCommandId).block()
                continue
            }

            if (hasChanged(discordCommand, command)) {
                updated++
                applicationService.modifyGlobalApplicationCommand(applicationId, discordCommandId, command).block()
            }
        }

        //Send log message with details on changes...
        LOGGER.info(DEFAULT, "Slash commands: $added Added | $updated Updated | $removed Removed")
    }

    private fun hasChanged(discordCommand: ApplicationCommandData, command: ApplicationCommandRequest): Boolean {
        //Check type
        val dCommandType = discordCommand.type().toOptional().orElse(1)
        val commandType = command.type().toOptional().orElse(1)
        if (dCommandType != commandType) return true

        //Check description
        if (discordCommand.description() != command.description().toOptional().orElse("")) return true

        //Check default perm
        val dCommandPerm = discordCommand.defaultPermission().toOptional().orElse(true)
        val commandPerm = command.defaultPermission().toOptional().orElse(true)
        if (dCommandPerm != commandPerm) return true

        //Check options
        val discordOptions = discordCommand.options().toOptional().orElse(emptyList())
        val commandOptions = command.options().toOptional().orElse(emptyList())

        //This is messy and recursive but it should work
        return !optionsEqual(discordOptions, commandOptions)
    }

    private fun optionsEqual(options1: List<ApplicationCommandOptionData>, options2: List<ApplicationCommandOptionData>): Boolean {
        if (options1.isEmpty() && options2.isEmpty()) return true // No sub-options, they're equal
        if (options1.size != options2.size) return false // Lists are different sizes, must update

        //Loop through options and compare recursively
        for ((index, opt1) in options1.withIndex()) {
            if (!optionsEqual(opt1, options2[index])) return false // sub-opts don't match. needs to be updated
        }

        //If we make it here, everything should be equal
        return true
    }

    //Returns false if not matching
    private fun optionsEqual(option1: ApplicationCommandOptionData, option2: ApplicationCommandOptionData): Boolean {
        //compare type
        if (option1.type() != option2.type()) return false

        //compare name
        if (option1.name() != option2.name()) return false

        //compare description
        if (option1.description() != option2.description()) return false

        //compare required bool
        if (option1.required().toOptional().orElse(false) != option2.required().toOptional().orElse(false)) return false

        //TODO: compare channel types -- have to wait for d4j 3.2.1

        //compare choices
        val choices1 = option1.choices().toOptional().orElse(emptyList())
        val choices2 = option2.choices().toOptional().orElse(emptyList())

        if (!choicesEqual(choices1, choices2)) return false

        //compare sub-options
        val subOpts1 = option1.options().toOptional().orElse(emptyList())
        val subOpts2 = option2.options().toOptional().orElse(emptyList())

        //Recursive!!!!!!!
        return optionsEqual(subOpts1, subOpts2)
    }

    private fun choicesEqual(choices1: List<ApplicationCommandOptionChoiceData>, choices2: List<ApplicationCommandOptionChoiceData>): Boolean {
        if (choices1.isEmpty() && choices2.isEmpty()) return true //both empty, both equal

        if (choices1.size != choices2.size) return false //sizes don't match, needs updating

        //Compare the choices one-by-one...
        for ((index, c1) in choices1.withIndex()) {
            val c2 = choices2[index]

            if (c1.name() != c2.name()) return false //names not equal
            if (c1.value() != c2.value()) return false //values not equal
        }

        //If we get here, they must be equal
        return true
    }
}
