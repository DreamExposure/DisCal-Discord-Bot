package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.database.SessionData
import org.dreamexposure.discal.core.database.SessionRepository
import org.dreamexposure.discal.core.`object`.WebSession
import org.springframework.stereotype.Component
import java.time.Instant


@Component
class DefaultSessionService(
    private val sessionRepository: SessionRepository,
) : SessionService {
    // TODO: I do want to add caching, but need to figure out how I want to do that

    override suspend fun createSession(session: WebSession): WebSession {
        return sessionRepository.save(SessionData(
            token = session.token,
            userId = session.user.asLong(),
            expiresAt = session.expiresAt,
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
        )).map(::WebSession).awaitSingle()
    }

    override suspend fun getSession(token: String): WebSession? {
        return sessionRepository.findByToken(token)
            .map(::WebSession)
            .awaitSingleOrNull()
    }

    override suspend fun getSessions(userId: Snowflake): List<WebSession> {
        return sessionRepository.findAllByUserId(userId.asLong())
            .map(::WebSession)
            .collectList()
            .awaitSingle()
    }

    override suspend fun deleteSession(token: String) {
        sessionRepository.deleteByToken(token).awaitSingleOrNull()
    }

    override suspend fun deleteAllSessions(userId: Snowflake) {
        sessionRepository.deleteAllByUserId(userId.asLong()).awaitSingleOrNull()
    }

    override suspend fun deleteExpiredSessions() {
        sessionRepository.deleteAllByExpiresAtIsLessThan(Instant.now()).awaitSingleOrNull()
    }
}

interface SessionService {
    suspend fun createSession(session: WebSession): WebSession

    suspend fun getSession(token: String): WebSession?

    suspend fun getSessions(userId: Snowflake): List<WebSession>
    suspend fun deleteSession(token: String)

    suspend fun deleteAllSessions(userId: Snowflake)

    suspend fun deleteExpiredSessions()

    suspend fun removeAndInsertSession(session: WebSession): WebSession {
        deleteAllSessions(session.user)

        return createSession(session)
    }
}
