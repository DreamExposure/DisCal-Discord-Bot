package org.dreamexposure.discal.client.listeners.discord;

import org.dreamexposure.discal.client.module.command.CommandExecutor;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.utils.PermissionChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class MessageCreateListener {
    public static Mono<Void> handle(MessageCreateEvent event) {
        return Mono.just(event.getMessage())
            .filter(message -> !message.getContent().isEmpty())
            .filterWhen(message ->
                Mono.justOrEmpty(event.getMember()
                    .map(member -> !member.isBot()))
            )
            .map(Message::getContent)
            .flatMap(content ->
                DatabaseManager.getSettings(event.getGuildId().get()).flatMap(settings -> {
                    if (content.startsWith(settings.getPrefix())) {
                        return Mono.from(PermissionChecker.isCorrectChannel(event, settings))
                            .filter(correct -> correct)
                            .flatMap(ignore -> {
                                String[] cmdAndArgs = content.trim().split("\\s+");
                                if (cmdAndArgs.length > 1) {
                                    //command with args
                                    String cmd = cmdAndArgs[0].replace(settings.getPrefix(), "");
                                    List<String> args = Arrays.asList(cmdAndArgs).subList(1, cmdAndArgs.length);

                                    //issue command
                                    return CommandExecutor.issueCommand(cmd, args, event, settings);
                                } else if (cmdAndArgs.length == 1) {
                                    //Only command, no args
                                    String cmd = cmdAndArgs[0].replace(settings.getPrefix(), "");

                                    //Issue command
                                    return CommandExecutor.issueCommand(cmd, new ArrayList<>(), event, settings);
                                }
                                return Mono.empty();
                            });

                    } else if (!event.getMessage().mentionsEveryone()
                        && !content.contains("@here")
                        && (content.startsWith("<@" + BotSettings.ID.get() + ">")
                        || content.startsWith("<@!" + BotSettings.ID.get() + ">"))) {
                        return Mono.from(PermissionChecker.isCorrectChannel(event, settings))
                            .filter(correct -> correct)
                            .flatMap(ignore -> {
                                String[] cmdAndArgs = content.split("\\s+");
                                if (cmdAndArgs.length > 2) {
                                    //DisCal mentioned with command and args
                                    String cmd = cmdAndArgs[1];
                                    List<String> args = Arrays.asList(cmdAndArgs).subList(2, cmdAndArgs.length);

                                    //issue command
                                    return CommandExecutor.issueCommand(cmd, args, event, settings);
                                } else if (cmdAndArgs.length == 2) {
                                    //DisCal mentioned with command and no args
                                    String cmd = cmdAndArgs[1];
                                    //Issue command
                                    return CommandExecutor.issueCommand(cmd, new ArrayList<>(), event, settings);
                                } else if (cmdAndArgs.length == 1) {
                                    //DisCal mentioned, nothing else
                                    return CommandExecutor.issueCommand("DisCal", new ArrayList<>(), event, settings);
                                }

                                return Mono.empty();
                            });
                    } else {
                        //Bot not mentioned, and this is not a command, ignore this
                        return Mono.empty();
                    }
                }))
            .doOnError(e -> LogFeed //Basically make sure we don't zombie
                .log(LogObject.forException("Error Handling message event",
                    event.getMessage().getContent(), e, MessageCreateListener.class)
                )
            ).then();
    }
}
