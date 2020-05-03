package org.dreamexposure.discal.client.module.command;

import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.crypto.KeyGenerator;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.object.web.UserAPIAccount;
import org.dreamexposure.discal.core.utils.GlobalConst;

import java.util.ArrayList;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 4/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class DevCommand implements Command {
    /**
     * Gets the command this Object is responsible for.
     *
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "dev";
    }

    /**
     * Gets the short aliases of the command this object is responsible for.
     * </br>
     * This will return an empty ArrayList if none are present
     *
     * @return The aliases of the command.
     */
    @Override
    public ArrayList<String> getAliases() {
        return new ArrayList<>();
    }

    /**
     * Gets the info on the command (not sub command) to be used in help menus.
     *
     * @return The command info.
     */
    @Override
    public CommandInfo getCommandInfo() {
        CommandInfo ci = new CommandInfo(
            "dev",
            "Used for developer commands. Only able to be used by registered developers",
            "!dev <function> (value)"
        );

        ci.getSubCommands().put("reloadLangs", "Reloads the lang files across the network.");
        ci.getSubCommands().put("patron", "Sets a guild as a patron.");
        ci.getSubCommands().put("dev", "Sets a guild as a test/dev guild.");
        ci.getSubCommands().put("maxcal", "Sets the max amount of calendars a guild may have.");
        ci.getSubCommands().put("leave", "Leaves the specified guild.");
        ci.getSubCommands().put("eval", "Evaluates the given code.");
        ci.getSubCommands().put("api-register", "Register new API key");
        ci.getSubCommands().put("api-block", "Block API usage by key");
        ci.getSubCommands().put("settings", "Checks the settings of the specified Guild.");

        return ci;
    }

    /**
     * Issues the command this Object is responsible for.
     *
     * @param args  The command arguments.
     * @param event The event received.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public Mono<Void> issueCommand(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return event.getMessage().getAuthorAsMember()
            .map(Member::getId)
            .filter(id ->
                id.equals(GlobalConst.novaId)
                    || id.equals(GlobalConst.xaanitId)
                    || id.equals(GlobalConst.calId)
                    || id.equals(GlobalConst.dreamId)
            ).flatMap(id -> {
                if (args.length < 1) {
                    return Messages.sendMessage("Please specify the function you would like to execute. " +
                        "To view valid functions use `!help dev`", event);
                } else {
                    switch (args[0].toLowerCase()) {
                        case "reloadlangs":
                            return moduleReloadLangs(event);
                        case "patron":
                            return modulePatron(args, event);
                        case "dev":
                            return moduleDevGuild(args, event);
                        case "api-register":
                            return registerApiKey(args, event);
                        case "api-block":
                            return blockAPIKey(args, event);
                        case "settings":
                            return moduleCheckSettings(args, event);
                        default:
                            return Messages.sendMessage("Invalid sub command! Use `!help dev` to view valid sub commands!", event);
                    }
                }
            })
            .defaultIfEmpty(Messages.sendMessage("You are not a registered DisCal developer! " +
                "If this is a mistake please contact Nova!", event))
            .then();
    }

    private Mono<Void> modulePatron(String[] args, MessageCreateEvent event) {
        return Mono.defer(() -> {
            if (args.length == 2) {
                return Mono.just(Long.valueOf(args[1]))
                    .map(Snowflake::of)
                    .flatMap(DatabaseManager::getSettings)
                    .doOnNext(s -> s.setPatronGuild(s.isPatronGuild()))
                    .flatMap(DatabaseManager::updateSettings)
                    .then(Messages.sendMessage("isPatronGuild value updated! Client's cache needs to be invalidated", event))
                    .onErrorResume(NumberFormatException.class, e ->
                        Messages.sendMessage("Specified ID is not a valid LONG", event));
            } else {
                return Messages.sendMessage(
                    "Please specify the ID of the guild to set as a patron guild with `!dev patron <ID>`", event);
            }
        }).then();
    }

    private Mono<Void> moduleDevGuild(String[] args, MessageCreateEvent event) {
        return Mono.defer(() -> {
            if (args.length == 2) {
                return Mono.just(Long.valueOf(args[1]))
                    .map(Snowflake::of)
                    .flatMap(DatabaseManager::getSettings)
                    .doOnNext(s -> s.setDevGuild(s.isDevGuild()))
                    .flatMap(DatabaseManager::updateSettings)
                    .then(Messages.sendMessage("isDevGuild value updated! Client's cache needs to be invalidated", event))
                    .onErrorResume(NumberFormatException.class, e ->
                        Messages.sendMessage("Specified ID is not a valid LONG", event));
            } else {
                return Messages.sendMessage(
                    "Please specify the ID of the guild to set as a dev guild with `!dev dev <ID>`", event);
            }
        }).then();
    }

    //TODO: Actually re-implement this...
    private Mono<Void> moduleReloadLangs(final MessageCreateEvent event) {
        return Messages.sendMessage("This needs to get rewritten later", event).then();
    }

    private Mono<Void> registerApiKey(String[] args, MessageCreateEvent event) {
        return Mono.defer(() -> {
            if (args.length == 2) {
                String userId = args[1];

                UserAPIAccount acc = new UserAPIAccount();
                acc.setUserId(userId);
                acc.setAPIKey(KeyGenerator.csRandomAlphaNumericString(64));
                acc.setTimeIssued(System.currentTimeMillis());

                return DatabaseManager.updateAPIAccount(acc)
                    .flatMap(success -> {
                        if (success) {
                            Mono<Message> confirm = Messages.sendMessage("Check your DMs for the new API key!", event);
                            Mono<Message> dm = event.getMessage().getAuthorAsMember()
                                .flatMap(m -> Messages.sendDirectMessage(acc.getAPIKey(), m));

                            return Mono.when(confirm, dm);
                        } else {
                            return Messages.sendMessage("Error occurred! Could not register new API key!", event);
                        }
                    });
            } else {
                return Messages.sendMessage("Please specify the USER ID linked to the key", event);
            }
        }).then();
    }

    private Mono<Void> blockAPIKey(String[] args, MessageCreateEvent event) {
        return Mono.defer(() -> {
            if (args.length == 2) {
                String key = args[1];

                return Messages.sendMessage("Blocking API key...", event)
                    .then(DatabaseManager.getAPIAccount(key))
                    .doOnNext(acc -> acc.setBlocked(true))
                    .flatMap(DatabaseManager::updateAPIAccount)
                    .flatMap(success -> {
                        if (success)
                            return Messages.sendMessage("Successfully blocked API key!", event);
                        else
                            return Messages.sendMessage("Error occurred! Could not block API key!", event);
                    })
                    .switchIfEmpty(Messages.sendMessage("API key not found", event));
            } else {
                return Messages.sendMessage("Please specify the API KEY!", event);
            }
        }).then();
    }

    //TODO: Figure this shit out because it would be a life saver for helping with debugging for users
    private Mono<Void> moduleCheckSettings(String[] args, MessageCreateEvent event) {
        return Mono.defer(() -> {
            if (args.length == 2) {
                //String id = args[1];

                return Messages.sendMessage("HEY! This command is being redone cuz of networking!", event);

            } else {
                return Messages.sendMessage("Please specify the ID of the guild to check settings for!", event);
            }
        }).then();
    }
}