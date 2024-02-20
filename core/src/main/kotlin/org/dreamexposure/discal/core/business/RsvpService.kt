package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.rest.http.client.ClientException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.RsvpCache
import org.dreamexposure.discal.core.database.RsvpData
import org.dreamexposure.discal.core.database.RsvpRepository
import org.dreamexposure.discal.core.exceptions.NotFoundException
import org.dreamexposure.discal.core.extensions.asStringList
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.Rsvp
import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.beans.factory.BeanFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RsvpService(
    private val rsvpRepository: RsvpRepository,
    private val rsvpCache: RsvpCache,
    private val embedService: EmbedService,
    private val beanFactory: BeanFactory,
) {
    private val discordClient: DiscordClient
        get() = beanFactory.getBean(DiscordClient::class.java)

    suspend fun getRsvp(guildId: Snowflake, eventId: String): Rsvp {
        var rsvp = rsvpCache.get(guildId, eventId)
        if (rsvp != null) return rsvp

        rsvp = rsvpRepository.findByGuildIdAndEventId(guildId.asLong(), eventId)
            .map(::Rsvp)
            .defaultIfEmpty(Rsvp(guildId, eventId))
            .awaitSingle()

        rsvpCache.put(guildId, eventId, rsvp)
        return rsvp
    }

    suspend fun createRsvp(rsvp: Rsvp): Rsvp {
        LOGGER.debug("Creating new rsvp data for guild:{} event:{}", rsvp.guildId.asString(), rsvp.eventId)

        val saved = rsvpRepository.save(
            RsvpData(
                guildId = rsvp.guildId.asLong(),
                eventId = rsvp.eventId,
                calendarNumber = rsvp.calendarNumber,

                eventEnd = rsvp.eventEnd.toEpochMilli(),

                goingOnTime = rsvp.goingOnTime.map(Snowflake::asString).asStringList(),
                goingLate = rsvp.goingLate.map(Snowflake::asString).asStringList(),
                notGoing = rsvp.notGoing.map(Snowflake::asString).asStringList(),
                undecided = rsvp.undecided.map(Snowflake::asString).asStringList(),
                waitlist = rsvp.waitlist.map(Snowflake::asString).asStringList(),

                rsvpLimit = rsvp.limit.coerceAtLeast(-1),
                rsvpRole = rsvp.role?.asLong(),
            )
        ).map(::Rsvp).awaitSingle()

        rsvpCache.put(rsvp.guildId, rsvp.eventId, saved)
        return saved
    }

    suspend fun updateRsvp(rsvp: Rsvp): Rsvp {
        LOGGER.debug("Updating rsvp data for guild:{} event:{}", rsvp.guildId.asString(), rsvp.eventId)

        var new = rsvp.copy(limit = rsvp.limit.coerceAtLeast(-1))
        val old = getRsvp(new.guildId, new.eventId)

        val removeOldRoleFrom = mutableSetOf<Snowflake>()
        val addNewRoleTo = mutableSetOf<Snowflake>()
        val toDm = mutableSetOf<Snowflake>()

        // Validate that role exists if changed
        if (new.role != null && old.role != new.role) {
            val exists = discordClient.getRoleById(new.guildId, new.role!!).data
                .transform(ClientException.emptyOnStatus(GlobalVal.STATUS_NOT_FOUND))
                .hasElement()
                .awaitSingle()
            if (!exists) throw NotFoundException("Role not found for guild:${new.guildId.asString()} role:${new.role!!.asString()}")
        }

        // Handle role change (remove roles, store to-add in list for later)
        if (old.role != null && old.role != new.role) {
            // Need to remove old role from all users going to event
            removeOldRoleFrom.addAll(old.goingOnTime)
            removeOldRoleFrom.addAll(old.goingLate)
        }
        if (new.role != null && new.role != old.role) {
            // Need to add new role to all users going to event
            addNewRoleTo.addAll(new.goingOnTime)
            addNewRoleTo.addAll(new.goingLate)
        }

        // Handle removals (first just in case they are using the limit)
        if (old.role != null) {
            removeOldRoleFrom += old.goingOnTime.filterNot(new.goingOnTime::contains)
            removeOldRoleFrom += old.goingLate.filterNot(new.goingLate::contains)
        }

        // Handle additions (add these users to role-add list)
        if (new.role != null) {
            addNewRoleTo += new.goingOnTime.filterNot(old.goingOnTime::contains)
            addNewRoleTo += new.goingLate.filterNot(old.goingLate::contains)
        }

        // Handle waitlist in order
        while (new.hasRoom() && new.waitlist.isNotEmpty()) {
            val userId = new.waitlist.first()

            new = new.copy(waitlist = new.waitlist.drop(1), goingOnTime = new.goingOnTime + userId)
            addNewRoleTo += userId
            toDm += userId
        }

        // Update db
        rsvpRepository.updateByGuildIdAndEventId(
            guildId = new.guildId.asLong(),
            eventId = new.eventId,
            calendarNumber = new.calendarNumber,

            eventEnd = new.eventEnd.toEpochMilli(),

            goingOnTime = new.goingOnTime.map(Snowflake::asString).asStringList(),
            goingLate = new.goingLate.map(Snowflake::asString).asStringList(),
            notGoing = new.notGoing.map(Snowflake::asString).asStringList(),
            undecided = new.undecided.map(Snowflake::asString).asStringList(),
            waitlist = new.waitlist.map(Snowflake::asString).asStringList(),

            rsvpLimit = new.limit,
            rsvpRole = new.role?.asLong(),
        ).awaitSingleOrNull()

        rsvpCache.put(new.guildId, new.eventId, new)


        // Do Discord actions

        // Do role removal
        removeOldRoleFrom.forEach { userId ->
            discordClient.getGuildById(new.guildId)
                .removeMemberRole(userId, old.role, "Removed RSVP to event with ID ${new.eventId}")
                .doOnError {
                    LOGGER.debug(
                        "Failed to remove role:${old.role?.asString()} from user:${userId.asString()}",
                        it
                    )
                }
                .onErrorResume(ClientException::class.java) { Mono.empty() }
                .subscribe()
        }

        // Do role adds
        addNewRoleTo.forEach { userId ->
            discordClient.getGuildById(new.guildId)
                .addMemberRole(userId, new.role, "RSVP'd to event with ID: ${new.eventId}")
                .doOnError {
                    LOGGER.debug(
                        "Failed to add role:${old.role?.asString()} to user:${userId.asString()}",
                        it
                    )
                }
                .onErrorResume(ClientException::class.java) { Mono.empty() }
                .subscribe()
        }

        // Send out DMs
        toDm.forEach { userId ->
            val embed = embedService.rsvpDmFollowupEmbed(new, userId)

            discordClient.getUserById(userId).privateChannel.flatMap { channelData ->
                discordClient.getChannelById(Snowflake.of(channelData.id()))
                    .createMessage(embed.asRequest())
            }.doOnError {
                LOGGER.error("Failed to DM user for RSVP followup for event:${new.eventId}", it)
            }.onErrorResume {
                Mono.empty()
            }.subscribe()
        }

        return new
    }

    suspend fun upsertRsvp(rsvp: Rsvp): Rsvp {
        val exists = rsvpRepository.existsByGuildIdAndEventId(rsvp.guildId.asLong(), rsvp.eventId).awaitSingle()

        return if (exists) updateRsvp(rsvp) else createRsvp(rsvp)
    }


    suspend fun removeRoleForAll(guildId: Snowflake, roleId: Snowflake) {
        LOGGER.debug("Removing role:{} from all rsvp data for guild:{}", roleId.asString(), guildId.asString())

        rsvpRepository.removeRoleByGuildIdAndRsvpRole(guildId.asLong(), roleId.asLong()).awaitSingleOrNull()

        rsvpCache.getAll(guildId)
            .filter { it.role == roleId }
            .map { it.copy(role = null) }
            .forEach { rsvpCache.put(guildId, it.eventId, it) }
    }

}
