@file:Suppress("DuplicatedCode")

package org.dreamexposure.discal.core.database

import discord4j.common.util.Snowflake
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions.*
import io.r2dbc.spi.Result
import org.dreamexposure.discal.core.cache.DiscalCache
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.enums.announcement.AnnouncementStyle
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.enums.time.TimeFormat
import org.dreamexposure.discal.core.extensions.setFromString
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.`object`.event.EventData
import org.dreamexposure.discal.core.`object`.web.UserAPIAccount
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.intellij.lang.annotations.Language
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration
import java.time.Instant
import java.util.function.Function

object DatabaseManager {
    private val pool: ConnectionPool

    init {
        val factory = ConnectionFactories.get(
            builder()
                .option(DRIVER, "pool")
                .option(PROTOCOL, "mysql")
                .from(parse(Config.SQL_URL.getString()))
                .option(USER, Config.SQL_USERNAME.getString())
                .option(PASSWORD, Config.SQL_PASSWORD.getString())
                .build()
        )

        val conf = ConnectionPoolConfiguration.builder()
            .connectionFactory(factory)
            .maxLifeTime(Duration.ofHours(1))
            .build()

        pool = ConnectionPool(conf)
    }

    //FIXME: attempt to fix constant open/close of connections
    private fun <T> connect(connection: Function<Connection, Mono<T>>): Mono<T> {
        return Mono.usingWhen(pool.create(), connection::apply, Connection::close)
    }

    fun disconnectFromMySQL() = pool.dispose()

    fun updateAPIAccount(acc: UserAPIAccount): Mono<Boolean> {
        return connect { c ->
            Mono.from(
                c.createStatement(Queries.SELECT_API_KEY)
                    .bind(0, acc.APIKey)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ -> row }
            }.hasElements().flatMap { exists ->
                if (exists) {
                    val updateCommand = """ UPDATE ${Tables.API} SET
                                USER_ID = ?, BLOCKED = ?
                                WHERE API_KEY = ?
                                """.trimMargin()

                    Mono.from(
                        c.createStatement(updateCommand)
                            .bind(0, acc.userId)
                            .bind(1, acc.blocked)
                            .bind(2, acc.APIKey)
                            .execute()
                    ).flatMap { res -> Mono.from(res.rowsUpdated) }
                        .thenReturn(true)
                } else {
                    val insertCommand = """INSERT INTO ${Tables.API}
                                (USER_ID, API_KEY, BLOCKED, TIME_ISSUED)
                                VALUES (?, ?, ?, ?)
                            """.trimMargin()

                    Mono.from(
                        c.createStatement(insertCommand)
                            .bind(0, acc.userId)
                            .bind(1, acc.APIKey)
                            .bind(2, acc.blocked)
                            .bind(3, acc.timeIssued)
                            .execute()
                    ).flatMap { res -> Mono.from(res.rowsUpdated) }
                        .thenReturn(true)
                }
            }.doOnError {
                LOGGER.error(DEFAULT, "Failed to update API account", it)
            }.onErrorResume { Mono.just(false) }
        }
    }

    fun updateSettings(settings: GuildSettings): Mono<Boolean> {
        DiscalCache.guildSettings[settings.guildID] = settings

        return connect { c ->
            Mono.from(
                c.createStatement(Queries.SELECT_GUILD_SETTINGS)
                    .bind(0, settings.guildID.asLong())
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ -> row }
            }.hasElements().flatMap { exists ->
                if (exists) {
                    val updateCommand = """UPDATE ${Tables.GUILD_SETTINGS} SET
                                CONTROL_ROLE = ?, ANNOUNCEMENT_STYLE = ?, TIME_FORMAT = ?,
                                LANG = ?, PREFIX = ?, PATRON_GUILD = ?, DEV_GUILD = ?,
                                MAX_CALENDARS = ?, DM_ANNOUNCEMENTS = ?,
                                BRANDED = ?, event_keep_duration = ? WHERE GUILD_ID = ?
                            """.trimMargin()

                    Mono.from(
                        c.createStatement(updateCommand)
                            .bind(0, settings.controlRole)
                            .bind(1, settings.announcementStyle.value)
                            .bind(2, settings.timeFormat.value)
                            .bind(3, settings.lang)
                            .bind(4, settings.prefix)
                            .bind(5, settings.patronGuild)
                            .bind(6, settings.devGuild)
                            .bind(7, settings.maxCalendars)
                            .bind(8, settings.getDmAnnouncementsString())
                            .bind(9, settings.branded)
                            .bind(10, settings.eventKeepDuration)
                            .bind(11, settings.guildID.asLong())
                            .execute()
                    ).flatMap { res -> Mono.from(res.rowsUpdated) }
                        .hasElement()
                        .thenReturn(true)
                } else {
                    val insertCommand = """INSERT INTO ${Tables.GUILD_SETTINGS}
                                (GUILD_ID, CONTROL_ROLE, ANNOUNCEMENT_STYLE, TIME_FORMAT, LANG, PREFIX,
                                PATRON_GUILD, DEV_GUILD, MAX_CALENDARS, DM_ANNOUNCEMENTS, BRANDED, event_keep_duration)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """.trimMargin()

                    Mono.from(
                        c.createStatement(insertCommand)
                            .bind(0, settings.guildID.asLong())
                            .bind(1, settings.controlRole)
                            .bind(2, settings.announcementStyle.value)
                            .bind(3, settings.timeFormat.value)
                            .bind(4, settings.lang)
                            .bind(5, settings.prefix)
                            .bind(6, settings.patronGuild)
                            .bind(7, settings.devGuild)
                            .bind(8, settings.maxCalendars)
                            .bind(9, settings.getDmAnnouncementsString())
                            .bind(10, settings.branded)
                            .bind(11, settings.eventKeepDuration)
                            .execute()
                    ).flatMap { res -> Mono.from(res.rowsUpdated) }
                        .hasElement()
                        .thenReturn(true)
                }
            }.doOnError {
                LOGGER.error(DEFAULT, "Failed to update guild settings", it)
            }.onErrorResume { Mono.just(false) }
        }
    }

    fun updateCalendar(calData: CalendarData): Mono<Boolean> {
        return connect { c ->
            Mono.from(
                c.createStatement(Queries.SELECT_CALENDAR_BY_GUILD)
                    .bind(0, calData.guildId.asLong())
                    .bind(1, calData.calendarNumber)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ -> row }
            }.hasElements().flatMap { exists ->
                if (exists) {
                    val updateCommand = """UPDATE ${Tables.CALENDARS} SET
                        HOST = ?, CALENDAR_ID = ?,
                        CALENDAR_ADDRESS = ?, EXTERNAL = ?, CREDENTIAL_ID = ?,
                        PRIVATE_KEY = ?, ACCESS_TOKEN = ?, REFRESH_TOKEN = ?, EXPIRES_AT = ?
                        WHERE GUILD_ID = ? AND CALENDAR_NUMBER = ?
                    """.trimMargin()

                    Mono.from(
                        c.createStatement(updateCommand)
                            .bind(0, calData.host.name)
                            .bind(1, calData.calendarId)
                            .bind(2, calData.calendarAddress)
                            .bind(3, calData.external)
                            .bind(4, calData.credentialId)
                            .bind(5, calData.privateKey)
                            .bind(6, calData.encryptedAccessToken)
                            .bind(7, calData.encryptedRefreshToken)
                            .bind(8, calData.expiresAt.toEpochMilli())
                            .bind(9, calData.guildId.asLong())
                            .bind(10, calData.calendarNumber)
                            .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                        .hasElements()
                        .thenReturn(true)
                } else {
                    val insertCommand = """INSERT INTO ${Tables.CALENDARS}
                        (GUILD_ID, CALENDAR_NUMBER, HOST, CALENDAR_ID,
                        CALENDAR_ADDRESS, EXTERNAL, CREDENTIAL_ID,
                        PRIVATE_KEY, ACCESS_TOKEN, REFRESH_TOKEN, EXPIRES_AT) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimMargin()

                    Mono.from(
                        c.createStatement(insertCommand)
                            .bind(0, calData.guildId.asLong())
                            .bind(1, calData.calendarNumber)
                            .bind(2, calData.host.name)
                            .bind(3, calData.calendarId)
                            .bind(4, calData.calendarAddress)
                            .bind(5, calData.external)
                            .bind(6, calData.credentialId)
                            .bind(7, calData.privateKey)
                            .bind(8, calData.encryptedAccessToken)
                            .bind(9, calData.encryptedRefreshToken)
                            .bind(10, calData.expiresAt.toEpochMilli())
                            .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                        .hasElements()
                        .thenReturn(true)
                }
            }.doOnError {
                LOGGER.error(DEFAULT, "Failed to update calendar data", it)
            }.onErrorResume { Mono.just(false) }
        }
    }

    fun updateEventData(data: EventData): Mono<Boolean> {
        val id = if (data.eventId.contains("_"))
            data.eventId.split("_")[0]
        else
            data.eventId

        return connect { c ->
            Mono.from(
                c.createStatement(Queries.SELECT_EVENT_BY_GUILD)
                    .bind(0, data.guildId.asLong())
                    .bind(1, id)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ -> row }
            }.hasElements().flatMap { exists ->
                if (exists) {
                    val updateCommand = """UPDATE ${Tables.EVENTS} SET
                        CALENDAR_NUMBER = ?, IMAGE_LINK = ?, EVENT_END = ?
                        WHERE EVENT_ID = ? AND GUILD_ID = ?
                    """.trimMargin()

                    Mono.from(
                        c.createStatement(updateCommand)
                            .bind(0, data.calendarNumber)
                            .bind(1, data.imageLink)
                            .bind(2, data.eventEnd)
                            .bind(3, id)
                            .bind(4, data.guildId.asLong())
                            .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                        .hasElements()
                        .thenReturn(true)
                } else if (data.shouldBeSaved()) {
                    val insertCommand = """INSERT INTO ${Tables.EVENTS}
                        (GUILD_ID, EVENT_ID, CALENDAR_NUMBER, EVENT_END, IMAGE_LINK)
                        VALUES(?, ?, ?, ?, ?)
                    """.trimMargin()

                    Mono.from(
                        c.createStatement(insertCommand)
                            .bind(0, data.guildId.asLong())
                            .bind(1, id)
                            .bind(2, data.calendarNumber)
                            .bind(3, data.eventEnd)
                            .bind(4, data.imageLink)
                            .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                        .hasElements()
                        .thenReturn(true)
                } else {
                    Mono.just(false)
                }.doOnError {
                    LOGGER.error(DEFAULT, "Failed to update event data", it)
                }.onErrorResume { Mono.just(false) }
            }
        }
    }


    fun getAPIAccount(APIKey: String): Mono<UserAPIAccount> {
        return connect { c ->
            Mono.from(
                c.createStatement(Queries.SELECT_API_KEY)
                    .bind(0, APIKey)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    UserAPIAccount(
                        row["USER_ID", String::class.java]!!,
                        APIKey,
                        row["BLOCKED", Boolean::class.java]!!,
                        row["TIME_ISSUED", Long::class.java]!!
                    )
                }
            }.next().retryWhen(Retry.max(3)
                .filter(IllegalStateException::class::isInstance)
                .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get API-key data", it)
            }.onErrorResume { Mono.empty() }
        }
    }

    fun getSettings(guildId: Snowflake): Mono<GuildSettings> {
        if (DiscalCache.guildSettings.containsKey(guildId))
            return Mono.just(DiscalCache.guildSettings[guildId]!!)

        return connect { c ->
            Mono.from(
                c.createStatement(Queries.SELECT_GUILD_SETTINGS)
                    .bind(0, guildId.asLong())
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val controlRole = row["CONTROL_ROLE", String::class.java]!!
                    val announcementStyle = AnnouncementStyle.fromValue(row["ANNOUNCEMENT_STYLE", Int::class.java]!!)
                    val timeFormat = TimeFormat.fromValue(row["TIME_FORMAT", Int::class.java]!!)
                    val lang = row["LANG", String::class.java]!!
                    val prefix = row["PREFIX", String::class.java]!!
                    val patron = row["PATRON_GUILD", Boolean::class.java]!!
                    val dev = row["DEV_GUILD", Boolean::class.java]!!
                    val maxCals = row["MAX_CALENDARS", Int::class.java]!!
                    val dmAnnouncementsString = row["DM_ANNOUNCEMENTS", String::class.java]!!
                    val branded = row["BRANDED", Boolean::class.java]!!
                    val eventKeepDuration = row["event_keep_duration", Boolean::class.java]!!

                    val settings = GuildSettings(
                        guildId, controlRole, announcementStyle, timeFormat,
                        lang, prefix, patron, dev, maxCals, branded, eventKeepDuration,
                    )

                    settings.dmAnnouncements.setFromString(dmAnnouncementsString)

                    //Store in cache...
                    DiscalCache.guildSettings[guildId] = settings

                    settings
                }
            }.next().retryWhen(Retry.max(3)
                .filter(IllegalStateException::class::isInstance)
                .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get guild settings", it)
            }.onErrorReturn(GuildSettings.empty(guildId)).defaultIfEmpty(GuildSettings.empty(guildId))
        }
    }

    fun getMainCalendar(guildId: Snowflake): Mono<CalendarData> = getCalendar(guildId, 1)

    fun getCalendar(guildId: Snowflake, calendarNumber: Int): Mono<CalendarData> {
        return connect { c ->
            Mono.from(
                c.createStatement(Queries.SELECT_CALENDAR_BY_GUILD)
                    .bind(0, guildId.asLong())
                    .bind(1, calendarNumber)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val calId = row["CALENDAR_ID", String::class.java]!!
                    val calNumber = row["CALENDAR_NUMBER", Int::class.java]!!
                    val calAddr = row["CALENDAR_ADDRESS", String::class.java]!!
                    val host = CalendarHost.valueOf(row["HOST", String::class.java]!!)
                    val external = row["EXTERNAL", Boolean::class.java]!!
                    val credId = row["CREDENTIAL_ID", Int::class.java]!!
                    val privateKey = row["PRIVATE_KEY", String::class.java]!!
                    val accessToken = row["ACCESS_TOKEN", String::class.java]!!
                    val refreshToken = row["REFRESH_TOKEN", String::class.java]!!
                    val expiresAt = Instant.ofEpochMilli(row["EXPIRES_AT", Long::class.java]!!)

                    CalendarData(
                        guildId, calNumber, host, calId, calAddr, external,
                        credId, privateKey, accessToken, refreshToken, expiresAt
                    )
                }
            }.next().retryWhen(Retry.max(3)
                .filter(IllegalStateException::class::isInstance)
                .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get all guild calendars", it)
            }.onErrorResume { Mono.empty() }
        }
    }

    fun getAllCalendars(guildId: Snowflake): Mono<List<CalendarData>> {
        return connect { c ->
            Mono.from(
                c.createStatement(Queries.SELECT_ALL_CALENDARS_BY_GUILD)
                    .bind(0, guildId.asLong())
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val calId = row["CALENDAR_ID", String::class.java]!!
                    val calNumber = row["CALENDAR_NUMBER", Int::class.java]!!
                    val calAddr = row["CALENDAR_ADDRESS", String::class.java]!!
                    val host = CalendarHost.valueOf(row["HOST", String::class.java]!!)
                    val external = row["EXTERNAL", Boolean::class.java]!!
                    val credId = row["CREDENTIAL_ID", Int::class.java]!!
                    val privateKey = row["PRIVATE_KEY", String::class.java]!!
                    val accessToken = row["ACCESS_TOKEN", String::class.java]!!
                    val refreshToken = row["REFRESH_TOKEN", String::class.java]!!
                    val expiresAt = Instant.ofEpochMilli(row["EXPIRES_AT", Long::class.java]!!)

                    CalendarData(
                        guildId, calNumber, host, calId, calAddr, external,
                        credId, privateKey, accessToken, refreshToken, expiresAt
                    )
                }
            }.collectList().retryWhen(Retry.max(3)
                .filter(IllegalStateException::class::isInstance)
                .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get all guild calendars", it)
            }.onErrorReturn(mutableListOf())
        }
    }

    fun getCalendarCount(guildId: Snowflake): Mono<Int> {
        return connect { c ->
            Mono.from(
                c.createStatement(Queries.SELECT_CALENDAR_COUNT_BY_GUILD)
                    .bind(0, guildId.asLong())
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val calendars = row.get(0, Long::class.java)!!
                    return@map calendars.toInt()
                }
            }.next().retryWhen(Retry.max(3)
                .filter(IllegalStateException::class::isInstance)
                .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get calendar count", it)
            }.onErrorReturn(-1)
        }.defaultIfEmpty(0)
    }

    fun getEventData(guildId: Snowflake, eventId: String): Mono<EventData> {
        var eventIdLookup = eventId
        if (eventId.contains("_"))
            eventIdLookup = eventId.split("_")[0]

        return connect { c ->
            Mono.from(
                c.createStatement(Queries.SELECT_EVENT_BY_GUILD)
                    .bind(0, guildId.asLong())
                    .bind(1, eventIdLookup)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->

                    val id = row["EVENT_ID", String::class.java]!!
                    val calNum = row["CALENDAR_NUMBER", Int::class.java]!!
                    val end = row["EVENT_END", Long::class.java]!!
                    val img = row["IMAGE_LINK", String::class.java]!!

                    EventData(guildId, id, calNum, end, img)
                }
            }.next().retryWhen(Retry.max(3)
                .filter(IllegalStateException::class::isInstance)
                .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get event data", it)
            }.onErrorResume {
                Mono.empty()
            }
        }.defaultIfEmpty(EventData(guildId, eventId = eventIdLookup))
    }

    fun deleteAnnouncementsForEvent(guildId: Snowflake, eventId: String): Mono<Boolean> {
        return connect { c ->
            Mono.from(
                c.createStatement(Queries.DELETE_ANNOUNCEMENTS_FOR_EVENT)
                    .bind(0, eventId)
                    .bind(1, guildId.asLong())
                    .execute()
            ).flatMapMany(Result::getRowsUpdated)
                .hasElements()
                .thenReturn(true)
                .doOnError {
                    LOGGER.error(DEFAULT, "Failed to delete announcements for event", it)
                }.onErrorReturn(false)
        }.defaultIfEmpty(false)
    }

    fun deleteEventData(eventId: String): Mono<Boolean> {
        if (eventId.contains("_")) return Mono.empty() // Don't delete if child event of recurring parent.
        return connect { c ->
            Mono.from(
                c.createStatement(Queries.DELETE_EVENT_DATA)
                    .bind(0, eventId)
                    .execute()
            ).flatMapMany(Result::getRowsUpdated)
                .hasElements()
                .thenReturn(true)
                .doOnError {
                    LOGGER.error(DEFAULT, "Failed to delete event data", it)
                }.onErrorReturn(false)
        }.defaultIfEmpty(false)
    }

    /* Utility Deletion Methods */

    fun deleteCalendarAndRelatedData(calendarData: CalendarData): Mono<Boolean> {
        return connect { c ->
            Mono.from(
                c.createStatement(Queries.FULL_CALENDAR_DELETE) //Monolith 8 statement query
                    // calendar delete bindings
                    .bind(0, calendarData.guildId.asLong())
                    .bind(1, calendarData.calendarNumber)
                    // event delete bindings
                    .bind(2, calendarData.guildId.asLong())
                    .bind(3, calendarData.calendarNumber)
                    // rsvp delete bindings
                    .bind(4, calendarData.guildId.asLong())
                    .bind(5, calendarData.calendarNumber)
                    // announcement delete bindings
                    .bind(6, calendarData.guildId.asLong())
                    .bind(7, calendarData.calendarNumber)
                    // delete static message bindings
                    .bind(8, calendarData.guildId.asLong())
                    .bind(9, calendarData.calendarNumber)
                    // decrement calendar bindings
                    .bind(10, calendarData.calendarNumber)
                    .bind(11, calendarData.guildId.asLong())
                    // decrement event bindings
                    .bind(12, calendarData.calendarNumber)
                    .bind(13, calendarData.guildId.asLong())
                    // decrement rsvp bindings
                    .bind(14, calendarData.calendarNumber)
                    .bind(15, calendarData.guildId.asLong())
                    // decrement announcement bindings
                    .bind(16, calendarData.calendarNumber)
                    .bind(17, calendarData.guildId.asLong())
                    // decrement static message bindings
                    .bind(18, calendarData.calendarNumber)
                    .bind(19, calendarData.guildId.asLong())
                    .execute()
            ).flatMapMany(Result::getRowsUpdated)
                .hasElements()
                .thenReturn(true)
                .doOnError {
                    LOGGER.error(DEFAULT, "Full calendar delete failed!", it)
                }.onErrorReturn(false)
        }.defaultIfEmpty(true) // If nothing was updated and no error was emitted, it's safe to return this worked.
    }

    fun deleteAllDataForGuild(guildId: Snowflake): Mono<Boolean> {
        return connect { c ->
            Mono.from(
                c.createStatement(Queries.DELETE_EVERYTHING_FOR_GUILD) //Monolith 6 statement query
                    // settings delete bindings
                    .bind(0, guildId.asLong())
                    // calendar delete bindings
                    .bind(1, guildId.asLong())
                    // event delete bindings
                    .bind(2, guildId.asLong())
                    // rsvp delete bindings
                    .bind(3, guildId.asLong())
                    // announcement delete bindings
                    .bind(4, guildId.asLong())
                    // static message delete bindings
                    .bind(5, guildId.asLong())
                    .execute()
            ).flatMapMany(Result::getRowsUpdated)
                .hasElements()
                .thenReturn(true)
                .doOnError {
                    LOGGER.error(DEFAULT, "Full data delete failed!", it)
                }.onErrorReturn(false)
        }.defaultIfEmpty(true) // If nothing was updated and no error was emitted, it's safe to return this worked.
    }

    /* Event Data */

    fun getEventsData(guildId: Snowflake, eventIds: List<String>): Mono<Map<String, EventData>> {
        // clean up IDs
        val idsToUse = mutableListOf<String>()
        eventIds.forEach {
            val id = if (it.contains("_")) it.split("_")[0] else it

            if (!idsToUse.contains(id)) idsToUse.add(id)
        }

        if (idsToUse.isEmpty()) return Mono.just(emptyMap())

        // Convert our list of IDs to sql escaped string
        val builder = StringBuilder()
        idsToUse.withIndex().forEach {
            if (it.index != idsToUse.size - 1) {
                builder.append("'${it.value}', ")
            } else {
                builder.append("'${it.value}'")
            }
        }

        return connect { c ->
            Mono.from(
                //Have to do it this way, sql injection is not possible as these IDs are not user input
                c.createStatement(Queries.SELECT_MANY_EVENT_DATA.replace("?", builder.toString()))
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val id = row["EVENT_ID", String::class.java]!!
                    val calNum = row["CALENDAR_NUMBER", Int::class.java]!!
                    val end = row["EVENT_END", Long::class.java]!!
                    val img = row["IMAGE_LINK", String::class.java]!!

                    EventData(guildId, id, calNum, end, img)
                }
            }.retryWhen(Retry.max(3)
                .filter(IllegalStateException::class::isInstance)
                .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get many event data", it)
            }.onErrorResume {
                Mono.empty()
            }.collectMap {
                it.eventId
            }.defaultIfEmpty(emptyMap())
        }
    }
}

private object Queries {
    @Language("MySQL")
    val SELECT_API_KEY = """SELECT * FROM ${Tables.API}
        WHERE API_KEY = ?
        """.trimMargin()

    @Language("MySQL")
    val SELECT_GUILD_SETTINGS = """SELECT * FROM ${Tables.GUILD_SETTINGS}
        WHERE GUILD_ID = ?
        """.trimMargin()

    @Language("MySQL")
    val SELECT_CALENDAR_BY_GUILD = """SELECT * FROM ${Tables.CALENDARS}
        WHERE GUILD_ID = ? AND CALENDAR_NUMBER = ?
       """.trimMargin()

    @Language("MySQL")
    val SELECT_ALL_CALENDARS_BY_GUILD = """SELECT * FROM ${Tables.CALENDARS}
        WHERE GUILD_ID = ?
        """.trimMargin()

    @Language("MySQL")
    val SELECT_CALENDAR_COUNT_BY_GUILD = """SELECT COUNT(*) FROM ${Tables.CALENDARS}
        WHERE GUILD_ID = ?
        """.trimMargin()

    @Language("MySQL")
    val SELECT_EVENT_BY_GUILD = """SELECT * FROM ${Tables.EVENTS}
        WHERE GUILD_ID = ? AND EVENT_ID = ?
        """.trimMargin()

    @Language("MySQL")
    val DELETE_ANNOUNCEMENTS_FOR_EVENT = """DELETE FROM ${Tables.ANNOUNCEMENTS}
        WHERE EVENT_ID = ? AND GUILD_ID = ?
        """.trimMargin()

    @Language("MySQL")
    val DELETE_EVENT_DATA = """DELETE FROM ${Tables.EVENTS}
        WHERE EVENT_ID = ?
        """.trimMargin()

    @Language("MySQL")
    val DELETE_ALL_EVENT_DATA = """DELETE FROM ${Tables.EVENTS}
        WHERE GUILD_ID = ? AND CALENDAR_NUMBER = ?
        """.trimMargin()

    @Language("MySQL")
    val DELETE_ALL_ANNOUNCEMENT_DATA = """DELETE FROM ${Tables.ANNOUNCEMENTS}
        WHERE GUILD_ID = ? AND CALENDAR_NUMBER = ?
        """.trimMargin()

    @Language("MySQL")
    val DELETE_ALL_RSVP_DATA = """DELETE FROM ${Tables.RSVP}
        WHERE GUILD_ID = ? AND CALENDAR_NUMBER = ?
        """.trimMargin()

    @Language("MySQL")
    val DELETE_CALENDAR = """DELETE FROM ${Tables.CALENDARS} 
        WHERE GUILD_ID = ? AND calendar_number = ?
        """.trimMargin()

    @Language("MySQL")
    val DECREMENT_CALENDARS = """UPDATE ${Tables.CALENDARS} 
        SET calendar_number = calendar_number - 1
        WHERE calendar_number >=? AND guild_id = ?
        """.trimMargin()

    @Language("MySQL")
    val DECREMENT_ANNOUNCEMENTS = """UPDATE ${Tables.ANNOUNCEMENTS}
        SET calendar_number = calendar_number - 1
        WHERE calendar_number >=? AND guild_id = ?
        """.trimMargin()

    @Language("MySQL")
    val DECREMENT_EVENTS = """UPDATE ${Tables.EVENTS}
        SET calendar_number = calendar_number - 1
        WHERE calendar_number >=? AND guild_id = ?
        """.trimMargin()

    @Language("MySQL")
    val DECREMENT_RSVPS = """UPDATE ${Tables.RSVP}
        SET calendar_number = calendar_number - 1
        WHERE calendar_number >=? AND guild_id = ?
        """.trimMargin()

    @Language("MySQL")
    val DECREMENT_STATIC_MESSAGES = """UPDATE ${Tables.STATIC_MESSAGES}
        SET calendar_number = calendar_number - 1
        WHERE calendar_number >=? AND guild_id = ?
        """.trimMargin()

    @Language("MySQL")
    val DELETE_ALL_STATIC_MESSAGES = """DELETE FROM ${Tables.STATIC_MESSAGES}
        WHERE guild_id = ? AND calendar_number = ?
        """.trimMargin()

    @Language("MySQL")
    val FULL_CALENDAR_DELETE = """
        $DELETE_CALENDAR;$DELETE_ALL_EVENT_DATA;$DELETE_ALL_RSVP_DATA;$DELETE_ALL_ANNOUNCEMENT_DATA;$DELETE_ALL_STATIC_MESSAGES;
        $DECREMENT_CALENDARS;$DECREMENT_EVENTS;$DECREMENT_RSVPS;$DECREMENT_ANNOUNCEMENTS;$DECREMENT_STATIC_MESSAGES
    """.trimIndent()


    @Language("MySQL")
    val SELECT_MANY_EVENT_DATA = """SELECT * FROM ${Tables.EVENTS}
        WHERE event_id in (?)
    """.trimMargin()

    /* Delete everything */

    @Language("MySQL")
    val DELETE_EVERYTHING_FOR_GUILD = """
        delete from ${Tables.GUILD_SETTINGS} where GUILD_ID=?;
        delete from ${Tables.CALENDARS} where GUILD_ID=?;
        delete from ${Tables.EVENTS} where GUILD_ID=?;
        delete from ${Tables.RSVP} where GUILD_ID=?;
        delete from ${Tables.ANNOUNCEMENTS} where GUILD_ID=?;
        delete from ${Tables.STATIC_MESSAGES} where guild_id=?;
    """.trimMargin()
}

private object Tables {
    /* The language annotations are there because IntelliJ is dumb and assumes this needs to be proper MySQL */

    @Language("Kotlin")
    const val API: String = "api"

    @Language("Kotlin")
    const val GUILD_SETTINGS = "guild_settings"

    @Language("Kotlin")
    const val CALENDARS = "calendars"

    @Language("Kotlin")
    const val ANNOUNCEMENTS = "announcements"

    @Language("Kotlin")
    const val EVENTS = "events"

    @Language("Kotlin")
    const val RSVP = "rsvp"

    @Language("Kotlin")
    const val STATIC_MESSAGES = "static_messages"
}
