package org.dreamexposure.discal.client.module.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.GlobalVal;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class HelpCommand implements Command {
    /**
     * Gets the command this Object is responsible for.
     *
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "help";
    }

    /**
     * Gets the short aliases of the command this object is responsible for.
     * <br>
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
        return new CommandInfo("help",
            "Displays help (duh)",
            "!help (command) (sub-command)");
    }

    /**
     * Issues the command this Object is responsible for.
     *
     * @param args  The command arguments.
     * @param event The event received.
     * @return {@code true} if successful, else {@code false}.
     */
    @Override
    public Mono<Void> issueCommand(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return Mono.just(args).flatMap(ignore -> {
            if (args.length < 1) {
                var builder = EmbedCreateSpec.builder()
                    .author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl())
                    .title("DisCal Command Help")
                    .footer("Check out the official site for more command info!", null)
                    .url(BotSettings.BASE_URL.get() + "/commands")
                    .color(GlobalVal.getDiscalColor());

                for (final Command c : CommandExecutor.getCommands()) {
                    if (!c.getAliases().isEmpty()) {
                        final String al = c.getAliases().toString();
                        builder.addField(c.getCommand() + " " + al, c.getCommandInfo().getDescription(), true);
                    } else {
                        builder.addField(c.getCommand(), c.getCommandInfo().getDescription(), true);
                    }
                }
                return Messages.sendMessage(builder.build(), event);
            } else if (args.length == 1) {
                return CommandExecutor.getCommand(args[0])
                    .flatMap(cmd -> Messages.sendMessage(this.getCommandInfoEmbed(cmd), event));
            } else if (args.length == 2) {
                return CommandExecutor.getCommand(args[0])
                    .filter(command -> command.getCommandInfo().getSubCommands().containsKey(args[1].toLowerCase()))
                    .flatMap(command -> Messages.sendMessage(this.getSubCommandEmbed(command, args[1].toLowerCase()), event))
                    .switchIfEmpty(Messages.sendMessage(
                        Messages.getMessage("Notifications.Args.InvalidSubCommand", settings), event)
                    );
            } else {
                return Mono.empty();
            }
        }).then();
    }

    //Embed formatters
    private EmbedCreateSpec getCommandInfoEmbed(final Command cmd) {
        var builder = EmbedCreateSpec.builder()
            .author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl())
            .addField("Command", cmd.getCommand(), true)
            .addField("Description", cmd.getCommandInfo().getDescription(), true)
            .addField("Example", cmd.getCommandInfo().getExample(), true)
            .footer("<> = required | () = optional", null)
            .url(BotSettings.BASE_URL.get() + "/commands")
            .color(GlobalVal.getDiscalColor());


        //Loop through sub commands
        if (!cmd.getCommandInfo().getSubCommands().isEmpty()) {
            String subs = cmd.getCommandInfo().getSubCommands().keySet().toString();
            subs = subs.replace("[", "").replace("]", "");
            builder.addField("Sub-Commands", subs, false);
        }

        return builder.build();
    }

    private EmbedCreateSpec getSubCommandEmbed(final Command cmd, final String subCommand) {
        return EmbedCreateSpec.builder()
            .author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl())
            .addField("Command", cmd.getCommand(), true)
            .addField("Sub Command", subCommand, true)
            .addField("Usage", cmd.getCommandInfo().getSubCommands().get(subCommand), false)
            .footer("<> = required | () = optional", null)
            .url(BotSettings.BASE_URL.get() + "/commands")
            .color(GlobalVal.getDiscalColor())
            .build();
    }
}
