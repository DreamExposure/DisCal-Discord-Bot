package org.dreamexposure.discal.core.database

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface RsvpRepository: R2dbcRepository<RsvpData, String> {
    fun existsByGuildIdAndEventId(guildId: Long, eventId: String): Mono<Boolean>

    fun findByGuildIdAndEventId(guildId: Long, eventId: String): Mono<RsvpData>


    @Query("""
        UPDATE rsvp
        SET calendar_number = :calendarNumber,
            event_end = :eventEnd,
            going_on_time = :goingOnTime,
            going_late = :goingLate,
            not_going = :notGoing,
            undecided = :undecided,
            waitlist = :waitlist,
            rsvp_limit = :rsvpLimit,
            rsvp_role = :rsvpRole
        WHERE guild_id = :guildId AND event_id = :eventId
    """)
    fun updateByGuildIdAndEventId(
        guildId: Long,
        eventId: String,
        calendarNumber: Int,
        eventEnd: Long,
        goingOnTime: String,
        goingLate: String,
        notGoing: String,
        undecided: String,
        waitlist: String,
        rsvpLimit: Int,
        rsvpRole: Long?,
        ): Mono<Int>

    @Query("""
        UPDATE rsvp
        SET rsvp_role = null
        WHERE guild_id = :guildId AND rsvp_role = :rsvpRole
    """)
    fun removeRoleByGuildIdAndRsvpRole(guildId: Long, rsvpRole: Long): Mono<Int>
}
