package org.dreamexposure.discal.core.extensions.discord4j

import discord4j.core.`object`.entity.Member
import discord4j.rest.entity.RestMember
import discord4j.rest.util.PermissionSet
import java.util.function.Predicate

fun Member.hasPermissions(pred: Predicate<PermissionSet>) = getRestMember().hasPermissions(pred)

fun Member.hasElevatedPermissions() = getRestMember().hasElevatedPermissions()

fun Member.hasControlRole() = getRestMember().hasControlRole()

fun Member.getRestMember(): RestMember = client.rest().restMember(guildId, memberData)
