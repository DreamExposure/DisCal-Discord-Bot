package org.dreamexposure.discal.client.listeners.discord;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.dreamexposure.discal.client.module.command.CommandExecutor;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.BotSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.dreamexposure.discal.core.utils.GlobalVal.getDEFAULT;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class MessageCreateListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(MessageCreateListener.class);

    public static Mono<Void> handle(final MessageCreateEvent event) {
        return Mono.just(event.getMessage())
            .filter(message -> !message.getContent().isEmpty())
            .filterWhen(message ->
                Mono.justOrEmpty(event.getMember()
                    .map(member -> !member.isBot()))
            )
            .map(Message::getContent)
            .flatMap(content ->
                DatabaseManager.INSTANCE.getSettings(event.getGuildId().get()).flatMap(settings -> {
                    if (content.startsWith(settings.getPrefix())) {
                        final String[] cmdAndArgs = content.trim().split("\\s+");
                        if (cmdAndArgs.length > 1) {
                            //command with args
                            final String cmd = cmdAndArgs[0].replace(settings.getPrefix(), "");
                            final List<String> args = Arrays.asList(cmdAndArgs).subList(1, cmdAndArgs.length);

                            //issue command
                            return CommandExecutor.issueCommand(cmd, args, event, settings);
                        } else if (cmdAndArgs.length == 1) {
                            //Only command, no args
                            final String cmd = cmdAndArgs[0].replace(settings.getPrefix(), "");

                            //Issue command
                            return CommandExecutor.issueCommand(cmd, new ArrayList<>(), event, settings);
                        }
                        return Mono.empty();
                    } else if (!event.getMessage().mentionsEveryone()
                        && !content.contains("@here")
                        && (content.startsWith("<@" + BotSettings.ID.get() + ">")
                        || content.startsWith("<@!" + BotSettings.ID.get() + ">"))) {
                        final String[] cmdAndArgs = content.split("\\s+");
                        if (cmdAndArgs.length > 2) {
                            //DisCal mentioned with command and args
                            final String cmd = cmdAndArgs[1];
                            final List<String> args = Arrays.asList(cmdAndArgs).subList(2, cmdAndArgs.length);

                            //issue command
                            return CommandExecutor.issueCommand(cmd, args, event, settings);
                        } else if (cmdAndArgs.length == 2) {
                            //DisCal mentioned with command and no args
                            final String cmd = cmdAndArgs[1];
                            //Issue command
                            return CommandExecutor.issueCommand(cmd, new ArrayList<>(), event, settings);
                        } else if (cmdAndArgs.length == 1) {
                            //DisCal mentioned, nothing else
                            return CommandExecutor.issueCommand("DisCal", new ArrayList<>(), event, settings);
                        }

                        return Mono.empty();
                    } else {
                        //Bot not mentioned, and this is not a command, ignore this
                        return Mono.empty();
                    }
                }))
            .doOnError(e -> LOGGER.error(getDEFAULT(), "Error handle message event | " + event.getMessage().getContent(), e))
            .onErrorResume(e -> Mono.empty())
            .then();
    }
}
