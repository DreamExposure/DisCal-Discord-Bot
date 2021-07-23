package org.dreamexposure.discal.client.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.http.client.ClientException;
import org.dreamexposure.discal.core.file.ReadFile;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.utils.GlobalVal;
import org.json.JSONArray;
import org.json.JSONObject;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    public static List<String> getLangs() {
        return new CopyOnWriteArrayList<>(langs.keySet());
    }

    public static boolean isSupported(String _value) {
        JSONArray names = langs.names();
        for (int i = 0; i < names.length(); i++) {
            if (_value.equalsIgnoreCase(names.getString(i)))
                return true;
        }
        return false;
    }

    public static String getValidLang(String _value) {
        JSONArray names = langs.names();
        for (int i = 0; i < names.length(); i++) {
            if (_value.equalsIgnoreCase(names.getString(i)))
                return names.getString(i);
        }
        return "ENGLISH";
    }

    public static String getMessage(String key, GuildSettings settings) {
        JSONObject messages;

        if (langs.has(settings.getLang()))
            messages = langs.getJSONObject(settings.getLang());
        else
            messages = langs.getJSONObject("ENGLISH");

        if (messages.has(key))
            return messages.getString(key).replace("%lb%", GlobalVal.getLineBreak());
        else
            return "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!! Message requested: " + key;
    }

    public static String getMessage(String key, String var, String replace, GuildSettings settings) {
        JSONObject messages;

        if (langs.has(settings.getLang()))
            messages = langs.getJSONObject(settings.getLang());
        else
            messages = langs.getJSONObject("ENGLISH");

        if (messages.has(key))
            return messages.getString(key).replace(var, replace).replace("%lb%", GlobalVal.getLineBreak());
        else
            return "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!! Message requested: " + key;
    }

    //Message sending
    public static Mono<Message> sendMessage(String message, MessageCreateEvent event) {
        return event.getMessage().getChannel()
            .flatMap(c -> c.createMessage(message))
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    public static Mono<Message> sendMessage(EmbedCreateSpec embed, MessageCreateEvent event) {
        return event.getMessage().getChannel()
            .flatMap(c -> c.createEmbed(embed))
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    public static Mono<Message> sendMessage(String message, EmbedCreateSpec embed, MessageCreateEvent event) {
        return event.getMessage().getChannel()
            .flatMap(c -> c.createMessage(message)
                .withEmbed(embed)
            )
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    public static Mono<Message> sendMessage(String message, GuildMessageChannel channel) {
        return channel.createMessage(message)
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    public static Mono<Message> sendMessage(EmbedCreateSpec embed, GuildMessageChannel channel) {
        return channel.createEmbed(embed)
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    public static Mono<Message> sendMessage(String message, EmbedCreateSpec embed, GuildMessageChannel channel) {
        return channel.createMessage(message).withEmbed(embed)
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    //Direct message sending
    public static Mono<Message> sendDirectMessage(String message, User user) {
        return user.getPrivateChannel()
            .flatMap(c -> c.createMessage(message))
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    public static Mono<Message> sendDirectMessage(EmbedCreateSpec embed, User user) {
        return user.getPrivateChannel()
            .flatMap(c -> c.createEmbed(embed))
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    public static Mono<Message> sendDirectMessage(String message, EmbedCreateSpec embed, User user) {
        return user.getPrivateChannel()
            .flatMap(c -> c.createMessage(message).withEmbed(embed))
            .onErrorResume(ClientException.class, e -> Mono.empty());
    }

    //Message editing
    public static Mono<Message> editMessage(String message, Message original) {
        return original.edit().withContentOrNull(message);
    }

    public static Mono<Message> editMessage(String message, EmbedCreateSpec embed, Message original) {
        return original.edit().withContentOrNull(message).withEmbedOrNull(embed);
    }

    public static Mono<Message> editMessage(String message, MessageCreateEvent event) {
        return event.getMessage().edit().withContentOrNull(message);
    }

    public static Mono<Message> editMessage(String message, EmbedCreateSpec embed, MessageCreateEvent event) {
        return event.getMessage().edit().withContentOrNull(message).withEmbedOrNull(embed);
    }

    //Message deleting
    public static Mono<Void> deleteMessage(Message message) {
        return Mono.justOrEmpty(message)
            .flatMap(Message::delete)
            .onErrorResume(e -> Mono.empty());
    }

    public static Mono<Void> deleteMessage(MessageCreateEvent event) {
        return deleteMessage(event.getMessage());
    }
}
