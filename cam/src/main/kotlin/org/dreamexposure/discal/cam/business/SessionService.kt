package org.dreamexposure.discal.cam.business

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.database.SessionData
import org.dreamexposure.discal.core.database.SessionRepository
import org.dreamexposure.discal.core.`object`.WebSession
import org.springframework.stereotype.Component
import java.time.Instant


@Component
class SessionService(
    private val sessionRepository: SessionRepository,
) {
    // TODO: I do want to add caching, but need to figure out how I want to do that

    suspend fun createSession(session: WebSession): WebSession {
        return sessionRepository.save(SessionData(
            token = session.token,
            userId = session.user.asLong(),
            expiresAt = session.expiresAt,
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            scopes = session.scopes.joinToString(",") { it.name }
        )).map(::WebSession).awaitSingle()
    }

    suspend fun getSession(token: String): WebSession? {
        return sessionRepository.findByToken(token)
            .map(::WebSession)
            .awaitSingleOrNull()
    }

    suspend fun getSessions(userId: Snowflake): List<WebSession> {
        return sessionRepository.findAllByUserId(userId.asLong())
            .map(::WebSession)
            .collectList()
            .awaitSingle()
    }

    suspend fun deleteSession(token: String) {
        sessionRepository.deleteByToken(token).awaitSingleOrNull()
    }

    suspend fun deleteAllSessions(userId: Snowflake) {
        sessionRepository.deleteAllByUserId(userId.asLong()).awaitSingleOrNull()
    }

    suspend fun deleteExpiredSessions() {
        sessionRepository.deleteAllByExpiresAtIsLessThan(Instant.now()).awaitSingleOrNull()
    }

    suspend fun removeAndInsertSession(session: WebSession): WebSession {
        deleteAllSessions(session.user)

        return createSession(session)
    }
}
