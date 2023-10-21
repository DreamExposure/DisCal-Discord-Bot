package org.dreamexposure.discal.core.`object`.new

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.database.ApiData
import org.dreamexposure.discal.core.extensions.asInstantMilli
import org.dreamexposure.discal.core.extensions.asStringListFromDatabase
import org.dreamexposure.discal.core.`object`.new.security.Scope
import java.time.Instant

data class ApiKey(
        val userId: Snowflake,
        val key: String,
        val blocked: Boolean,
        val timeIssued: Instant,
        val scopes: List<Scope>,
) {
    constructor(data: ApiData): this(
        userId = Snowflake.of(data.apiKey),
        key = data.apiKey,
        blocked = data.blocked,
        timeIssued = data.timeIssued.asInstantMilli(),
        scopes = data.scopes.asStringListFromDatabase().map(Scope::valueOf),
    )
}
