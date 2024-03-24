package org.dreamexposure.discal.core.`object`.new

import discord4j.common.util.Snowflake

abstract class WizardState<T>(
    open val guildId: Snowflake,
    open val userId: Snowflake,
    open val editing: Boolean,
    open val entity: T,
)

data class AnnouncementWizardState(
    override val guildId: Snowflake,
    override val userId: Snowflake,
    override val editing: Boolean,
    override val entity: Announcement,
) : WizardState<Announcement>(guildId, userId, editing, entity)
