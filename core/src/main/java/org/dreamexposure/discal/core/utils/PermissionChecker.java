package org.dreamexposure.discal.core.utils;

import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 1/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class PermissionChecker {
	/**
	 * Checks if the user who sent the received message has the proper role to use a command.
	 *
	 * @param event The Event received to check for the user and guild.
	 * @return <code>true</code> if the user has the proper role, otherwise <code>false</code>.
	 */
	public static Mono<Boolean> hasSufficientRole(MessageCreateEvent event, GuildSettings settings) {
		if (settings.getControlRole().equalsIgnoreCase("everyone"))
			return Mono.just(true);

		return Mono.justOrEmpty(event.getMember())
				.flatMapMany(Member::getRoles)
				.map(Role::getId)
				.any(snowflake -> snowflake.equals(Snowflake.of(settings.getControlRole())));

	}

	public static Mono<Boolean> hasSufficientRole(Member member, GuildSettings settings) {
		if (settings.getControlRole().equalsIgnoreCase("everyone"))
			return Mono.just(true);

		return Mono.from(member.getRoles()
				.map(Role::getId)
				.any(snowflake -> snowflake.equals(Snowflake.of(settings.getControlRole())))
		);
	}

	public static Mono<Boolean> hasManageServerRole(MessageCreateEvent event) {
		return Mono.justOrEmpty(event.getMember())
				.flatMap(Member::getBasePermissions)
				.map(perms -> perms.contains(Permission.MANAGE_GUILD)
						|| perms.contains(Permission.ADMINISTRATOR))
				.defaultIfEmpty(false);
	}

	public static Mono<Boolean> hasManageServerRole(Member m) {
		return m.getBasePermissions()
				.map(perms -> perms.contains(Permission.MANAGE_GUILD)
						|| perms.contains(Permission.ADMINISTRATOR)
				);
	}

	/**
	 * Checks if the user sent the command in a DisCal channel (if set).
	 *
	 * @param event The event received to check for the correct channel.
	 * @return <code>true</code> if in correct channel, otherwise <code>false</code>.
	 */
	public static Mono<Boolean> isCorrectChannel(MessageCreateEvent event, GuildSettings settings) {
		if (settings.getDiscalChannel().equalsIgnoreCase("all"))
			return Mono.just(true);

		return Mono.from(event.getMessage().getChannel()
				.map(Channel::getId)
				.map(snowflake -> snowflake.equals(Snowflake.of(settings.getDiscalChannel())))
				.onErrorResume(e -> Mono.just(true)) //If channel not found, allow.
		);
	}

	public static Mono<Boolean> botHasMessageManagePerms(MessageCreateEvent event) {
		return event.getGuild()
				.flatMap(guild -> guild.getMemberById(Snowflake.of(BotSettings.ID.get()))
						.flatMap(Member::getBasePermissions)
						.map(perms -> perms.contains(Permission.MANAGE_MESSAGES))
				);
	}
}
