package org.dreamexposure.discal.core.`object`.event

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Role
import discord4j.rest.http.client.ClientException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.serializers.SnowflakeAsStringSerializer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.CopyOnWriteArrayList

@Serializable
data class RsvpData(
    @Serializable(with = SnowflakeAsStringSerializer::class)
    @SerialName("guild_id")
    val guildId: Snowflake,
    @SerialName("event_id")
    val eventId: String,
    @SerialName("calendar_number")
    val calendarNumber: Int = 1,
) {
    @SerialName("event_end")
    var eventEnd: Long = 0

    var limit: Int = -1
        set(value) {
            field = value.coerceAtLeast(-1)
        }

    @Serializable(with = SnowflakeAsStringSerializer::class)
    @SerialName("role_id")
    var roleId: Snowflake? = null
        private set

    @SerialName("on_time")
    val goingOnTime: MutableList<String> = CopyOnWriteArrayList()

    @SerialName("late")
    val goingLate: MutableList<String> = CopyOnWriteArrayList()

    @SerialName("not_going")
    val notGoing: MutableList<String> = CopyOnWriteArrayList()

    val undecided: MutableList<String> = CopyOnWriteArrayList()

    val waitlist: MutableList<String> = CopyOnWriteArrayList()

    //List string stuffs
    fun setGoingOnTimeFromString(strList: String) {
        this.goingOnTime += strList.split(",").filter(String::isNotBlank)
    }

    fun setGoingLateFromString(strList: String) {
        this.goingLate += strList.split(",").filter(String::isNotBlank)
    }

    fun setNotGoingFromString(strList: String) {
        this.notGoing += strList.split(",").filter(String::isNotBlank)
    }

    fun setUndecidedFromString(strList: String) {
        this.undecided += strList.split(",").filter(String::isNotBlank)
    }

    fun setWaitlistFromString(strList: String) {
        this.waitlist += strList.split(",").filter(String::isNotBlank)
    }

    fun getCurrentCount() = this.goingOnTime.size + this.goingLate.size

    fun hasRoom(userId: String): Boolean {
        return if (limit < 0 || getCurrentCount() + 1 <= limit) true
        //Check if they are in a list that counts toward limit, if true, that means they will fit in the event
        else goingOnTime.contains(userId) || goingLate.contains(userId)
    }

    fun setRole(id: Snowflake, client: DiscordClient): Mono<Void> {
        roleId = id

        return Mono.just(client.getGuildById(guildId)).flatMap { guild ->
            val addOnTimeRoles = Flux.fromIterable(goingOnTime).flatMap { id ->
                guild.addMemberRole(Snowflake.of(id), roleId, "Role added to RSVP for event with ID: $eventId")
            }.then()
            val addLateRole = Flux.fromIterable(goingLate).flatMap { id ->
                guild.addMemberRole(Snowflake.of(id), roleId, "Role added to RSVP for event with ID: $eventId")
            }.then()

            Mono.`when`(addOnTimeRoles, addLateRole)
        }

    }

    fun setRole(role: Role) = setRole(role.id, role.client.rest())

    fun setRole(id: Snowflake?) {
        roleId = id
    }

    fun clearRole(client: DiscordClient): Mono<Void> {
        //Attempt to remove the role from all users RSVP'd...
        return Mono.just(client.getGuildById(guildId)).flatMap { guild ->
            val removeOnTimeRoles = Flux.fromIterable(goingOnTime).flatMap { id ->
                guild.removeMemberRole(Snowflake.of(id), roleId, "Role removed from event with ID: $eventId")
            }.then()
            val removeLateRole = Flux.fromIterable(goingLate).flatMap { id ->
                guild.addMemberRole(Snowflake.of(id), roleId, "Role removed from event with ID: $eventId")
            }

            Mono.`when`(removeOnTimeRoles, removeLateRole).doFinally { this.setRole(null) }
        }
    }

    //Functions
    fun removeCompletely(userId: String, client: DiscordClient, doWaitlistOp: Boolean = false): Mono<Void> {
        goingOnTime.removeAll { userId == it }
        goingLate.removeAll { userId == it }
        notGoing.removeAll { userId == it }
        undecided.removeAll { userId == it }
        waitlist.removeAll { userId == it }

        if (doWaitlistOp && waitlist.isNotEmpty()) {
            val waitlistUser = waitlist.removeFirst()

            TODO("Check waitlist and add user to going if they are in it")
        }

        return if (roleId != null) removeRole(userId, roleId!!, "Removed RSVP to event with ID $eventId", client)
        else Mono.empty()
    }

    fun removeCompletely(member: Member, doWaitlistOp: Boolean = false): Mono<Void> =
        removeCompletely(member.id.asString(), member.client.rest(), doWaitlistOp)

    fun addGoingOnTime(userId: String, client: DiscordClient): Mono<Void> {
        return Mono.just(userId)
            .doOnNext(goingOnTime::add)
            .flatMap {
                if (roleId != null) {
                    addRole(it, roleId!!, "RSVP'd to event with ID: $eventId", client)
                } else Mono.empty()
            }
    }

    fun addGoingOnTime(member: Member): Mono<Void> = addGoingOnTime(member.id.asString(), member.client.rest())

    fun addGoingLate(userId: String, client: DiscordClient): Mono<Void> {
        return Mono.just(userId)
            .doOnNext(goingLate::add)
            .flatMap {
                if (roleId != null) {
                    addRole(it, roleId!!, "RSVP'd to event with ID: $eventId", client)
                } else Mono.empty()
            }
    }

    fun handleWaitListedUser(userId: String, client: DiscordClient): Mono<Void> {
        //TODO: add user to going list

        //TODO: DM user that they have been moved from the waitlist

        //TODO: Assign role if role is set

        TODO("Not yet implemented")
    }

    fun handleWaitListedUser(member: Member): Mono<Void> = handleWaitListedUser(member.id.asString(), member.client.rest())

    fun fillRemaining(client: DiscordClient): Mono<Void> {
        //TODO: Determine if there are remaining slots open

        //TODO: Add users from waitlist one by one until there are no more slots open

        TODO("Not yet implemented")
    }

    fun addGoingLate(member: Member): Mono<Void> = addGoingLate(member.id.asString(), member.client.rest())

    fun shouldBeSaved(): Boolean {
        return this.goingOnTime.isNotEmpty()
            || this.goingLate.isNotEmpty()
            || this.notGoing.isNotEmpty()
            || this.undecided.isNotEmpty()
            || this.waitlist.isNotEmpty()
            || limit != -1
            || roleId != null
    }

    private fun addRole(userId: String, roleId: Snowflake, reason: String, client: DiscordClient): Mono<Void> {
        return client.getGuildById(this.guildId)
            .addMemberRole(Snowflake.of(userId), roleId, reason)
            .onErrorResume(ClientException::class.java) { Mono.empty() }
    }

    private fun removeRole(userId: String, roleId: Snowflake, reason: String, client: DiscordClient): Mono<Void> {
        return client.getGuildById(this.guildId)
            .removeMemberRole(Snowflake.of(userId), roleId, reason)
            .onErrorResume(ClientException::class.java) { Mono.empty() }
    }
}
