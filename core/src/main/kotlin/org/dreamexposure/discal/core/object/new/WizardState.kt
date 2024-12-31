package org.dreamexposure.discal.core.`object`.new

import discord4j.common.util.Snowflake

/*
    This class is abstract with implementations below due to the fact we can't resolve the types when deserializing from cache
 */
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


data class CalendarWizardState(
    override val guildId: Snowflake,
    override val userId: Snowflake,
    override val editing: Boolean,
    override val entity: Calendar,
) : WizardState<Calendar>(guildId, userId, editing, entity)


data class EventWizardState(
    override val guildId: Snowflake,
    override val userId: Snowflake,
    override val editing: Boolean,
    override val entity: Event.PartialEvent,
) : WizardState<Event.PartialEvent>(guildId, userId, editing, entity)

