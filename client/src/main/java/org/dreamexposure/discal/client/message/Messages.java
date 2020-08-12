package org.dreamexposure.discal.client.message;

import org.dreamexposure.discal.core.file.ReadFile;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
public class Messages {
    private static JSONObject langs;

    //Lang handling
    public static Mono<Boolean> reloadLangs() {
        return ReadFile.readAllLangFiles()
            .doOnNext(l -> langs = l)
            .thenReturn(true)
            .doOnError(e ->
                LogFeed.log(LogObject.forException("[LANGS]",
                    "Failed to reload lang files!",
                    e,
                    Messages.class)
                ))
            .onErrorReturn(false);
    }

    @SuppressWarnings("unchecked")
    public static List<String> getLangs() {

        return new CopyOnWriteArrayList<String>(langs.keySet());
    }

    public static boolean isSupported(final String _value) {
        final JSONArray names = langs.names();
        for (int i = 0; i < names.length(); i++) {
            if (_value.equalsIgnoreCase(names.getString(i)))
                return true;
        }
        return false;
    }

    public static String getValidLang(final String _value) {
        final JSONArray names = langs.names();
        for (int i = 0; i < names.length(); i++) {
            if (_value.equalsIgnoreCase(names.getString(i)))
                return names.getString(i);
        }
        return "ENGLISH";
    }

    public static String getMessage(final String key, final GuildSettings settings) {
        final JSONObject messages;

        if (settings.getLang() != null && langs.has(settings.getLang()))
            messages = langs.getJSONObject(settings.getLang());
        else
            messages = langs.getJSONObject("ENGLISH");

        if (messages.has(key))
            return messages.getString(key).replace("%lb%", GlobalConst.lineBreak);
        else
            return "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!! Message requested: " + key;
    }

    public static String getMessage(final String key, final String var, final String replace, final GuildSettings settings) {
        final JSONObject messages;

        if (settings.getLang() != null && langs.has(settings.getLang()))
            messages = langs.getJSONObject(settings.getLang());
        else
            messages = langs.getJSONObject("ENGLISH");

        if (messages.has(key))
            return messages.getString(key).replace(var, replace).replace("%lb%", GlobalConst.lineBreak);
        else
            return "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!! Message requested: " + key;
    }

    //Message sending
    public static Mono<Message> sendMessage(final String message, final MessageCreateEvent event) {
        return event.getMessage().getChannel()
            .flatMap(c -> c.createMessage(spec -> spec.setContent(message)))
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    public static Mono<Message> sendMessage(final Consumer<EmbedCreateSpec> embed,
                                            final MessageCreateEvent event) {
        return event.getMessage().getChannel()
            .flatMap(c -> c.createMessage(spec -> spec.setEmbed(embed)))
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    public static Mono<Message> sendMessage(final String message, final Consumer<EmbedCreateSpec> embed,
                                            final MessageCreateEvent event) {
        return event.getMessage().getChannel()
            .flatMap(c -> c.createMessage(spec -> spec.setContent(message).setEmbed(embed)))
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    public static Mono<Message> sendMessage(final String message, final TextChannel channel) {
        return channel.createMessage(spec -> spec.setContent(message))
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    public static Mono<Message> sendMessage(final Consumer<EmbedCreateSpec> embed, final TextChannel channel) {
        return channel.createMessage(spec -> spec.setEmbed(embed))
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    public static Mono<Message> sendMessage(final String message, final Consumer<EmbedCreateSpec> embed,
                                            final TextChannel channel) {
        return channel.createMessage(spec -> spec.setContent(message).setEmbed(embed))
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    //Direct message sending
    public static Mono<Message> sendDirectMessage(final String message, final User user) {
        return user.getPrivateChannel()
            .flatMap(c -> c.createMessage(spec -> spec.setContent(message)))
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    public static Mono<Message> sendDirectMessage(final Consumer<EmbedCreateSpec> embed, final User user) {
        return user.getPrivateChannel()
            .flatMap(c -> c.createMessage(spec -> spec.setEmbed(embed)))
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    public static Mono<Message> sendDirectMessage(final String message, final Consumer<EmbedCreateSpec> embed,
                                                  final User user) {
        return user.getPrivateChannel()
            .flatMap(c -> c.createMessage(spec -> spec.setContent(message).setEmbed(embed)))
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    //Message editing
    public static Mono<Message> editMessage(final String message, final Message original) {
        return original
            .edit(spec -> spec.setContent(message));
    }

    public static Mono<Message> editMessage(final String message, final Consumer<EmbedCreateSpec> embed,
                                            final Message original) {
        return original
            .edit(spec -> spec.setContent(message).setEmbed(embed));
    }

    public static Mono<Message> editMessage(final String message, final MessageCreateEvent event) {
        return event.getMessage()
            .edit(spec -> spec.setContent(message));
    }

    public static Mono<Message> editMessage(final String message, final Consumer<EmbedCreateSpec> embed,
                                            final MessageCreateEvent event) {
        return event.getMessage()
            .edit(spec -> spec.setContent(message).setEmbed(embed));
    }

    //Message deleting
    public static Mono<Void> deleteMessage(final Message message) {
        return Mono.justOrEmpty(message).flatMap(Message::delete)
            .onErrorResume(e -> Mono.empty());
    }

    public static Mono<Void> deleteMessage(final MessageCreateEvent event) {
        return deleteMessage(event.getMessage());
    }
}