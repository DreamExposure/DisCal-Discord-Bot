package org.dreamexposure.discal.core.extensions.discord4j

import discord4j.core.`object`.entity.Member
import discord4j.rest.entity.RestMember

fun Member.hasElevatedPermissions() = getRestMember().hasElevatedPermissions()

fun Member.getRestMember(): RestMember = client.rest().restMember(guildId, memberData)
