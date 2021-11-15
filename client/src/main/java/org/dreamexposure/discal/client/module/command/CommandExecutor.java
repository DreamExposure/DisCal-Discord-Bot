package org.dreamexposure.discal.client.module.command;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.RestClient;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.GeneralUtils;
import org.dreamexposure.discal.core.utils.GlobalVal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CommandExecutor {
    private static final List<Command> commands = new ArrayList<>();

    private static final List<String> movedCommands = List.of(
        "help", "discal", "time", "linkcal", "calendarlink", "callink", "linkcallador", "events",
        "rsvp", "dev", "calendar", "cal", "callador", "event", "e"
    );

    private CommandExecutor() {
    }

    //Functional

    /**
     * Registers a command that can be executed.
     *
     * @param _command The command to register.
     */
    public static void registerCommand(final Command _command) {
        commands.add(_command);
    }

    public static Mono<Void> issueCommand(final String cmd, final List<String> argsOr, final MessageCreateEvent event, final GuildSettings settings) {
        final String[] args;
        if (!argsOr.isEmpty()) {
            final String toParse = GeneralUtils.getContent(argsOr, 0);
            args = GeneralUtils.overkillParser(toParse).split(" ");
        } else {
            args = new String[0];
        }

        if (movedCommands.contains(cmd)) {
            return new WarningCommand().issueCommand(args, event, settings);
        } else
            return getCommand(cmd).flatMap(c -> c.issueCommand(args, event, settings));
    }

    /**
     * Gets an ArrayList of all valid commands.
     *
     * @return An ArrayList of all valid commands.
     */
    public static ArrayList<String> getAllCommands() {
        final ArrayList<String> cmds = new ArrayList<>();
        for (final Command c : commands) {
            if (!cmds.contains(c.getCommand()))
                cmds.add(c.getCommand());
        }
        return cmds;
    }

    public static List<Command> getCommands() {
        return commands;
    }

    private static Mono<Command> getCommand(final String cmdNameOrAlias) {
        return Flux.fromIterable(commands)
            .filter(c ->
                c.getCommand().equalsIgnoreCase(cmdNameOrAlias)
                    || c.getAliases().contains(cmdNameOrAlias.toLowerCase()))
            .next();
    }
}

class WarningCommand implements Command {

    @Override
    public String getCommand() {
        return "\u200Bdiscal-command-moved-warning";
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public CommandInfo getCommandInfo() {
        return new CommandInfo("", "", "");
    }

    @Override
    public Mono<Void> issueCommand(String[] args, MessageCreateEvent event, GuildSettings settings) {
        //Check if slash commands are enabled in this server.
        RestClient restClient = event.getClient().getRestClient();
        //noinspection OptionalGetWithoutIsPresent (always present)
        Snowflake guildId = event.getGuildId().get();

        return restClient.getApplicationId().flatMapMany(appId ->
                restClient.getApplicationService().getGuildApplicationCommands(appId, guildId.asLong())
            ).collectList()
            .thenReturn(true)
            .onErrorReturn(false)
            .map(hasAppCommands -> {
                var builder = EmbedCreateSpec.builder()
                    .author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl())
                    .color(GlobalVal.getDiscalColor())
                    .title("DisCal Bot");

                if (hasAppCommands) {
                    builder.description(
                        String.format(enabledMessage,
                            BotSettings.BASE_URL.get() + "/commands",
                            BotSettings.SUPPORT_INVITE.get()
                        )
                    );
                } else {
                    builder.description(
                        String.format(disabledMessage,
                            BotSettings.INVITE_URL.get(),
                            BotSettings.BASE_URL.get() + "/commands",
                            BotSettings.SUPPORT_INVITE.get()
                        )
                    );
                }

                return builder.build();
            }).flatMap(embed -> Messages.sendMessage(embed, event)).then();
    }

    private final String enabledMessage = """
        This command has been converted to a [Slash Command](https://discord.com/blog/slash-commands-are-here).
                
                
        For more information on commands, check out our [Commands Page](%s).
        For support, [join our server](%s).""";

    private final String disabledMessage = """
        This command has been converted to a [Slash Command](https://discord.com/blog/slash-commands-are-here), but they aren't enabled in this guild! A guild admin can [click here to enable them](%s).
        
        
        For more information on commands, check out our [Commands Page](%s).
        For support, [join our server](%s).""";
}
