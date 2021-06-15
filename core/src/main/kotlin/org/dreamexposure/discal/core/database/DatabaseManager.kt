@file:Suppress("SqlResolve", "DuplicatedCode")

package org.dreamexposure.discal.core.database

import discord4j.common.util.Snowflake
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions.*
import io.r2dbc.spi.Result
import io.r2dbc.spi.ValidationDepth
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.`object`.event.EventData
import org.dreamexposure.discal.core.`object`.event.RsvpData
import org.dreamexposure.discal.core.`object`.web.UserAPIAccount
import org.dreamexposure.discal.core.cache.DiscalCache
import org.dreamexposure.discal.core.enums.announcement.AnnouncementModifier
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.enums.event.EventColor.Companion.fromNameOrHexOrId
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.novautils.database.DatabaseSettings
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.util.*
import java.util.function.Function

object DatabaseManager {
    private val settings: DatabaseSettings = DatabaseSettings(
            BotSettings.SQL_HOST.get(),
            BotSettings.SQL_PORT.get(),
            BotSettings.SQL_DB.get(),
            BotSettings.SQL_USER.get(),
            BotSettings.SQL_PASS.get(),
            BotSettings.SQL_PREFIX.get())

    private val pool: ConnectionPool

    init {
        val factory = ConnectionFactories.get(builder()
                .option(DRIVER, "pool")
                .option(PROTOCOL, "mysql")
                .option(HOST, settings.hostname)
                .option(PORT, settings.port.toInt())
                .option(USER, settings.user)
                .option(PASSWORD, settings.password)
                .option(DATABASE, settings.database)
                .build())

        val conf = ConnectionPoolConfiguration.builder(factory).build()

        pool = ConnectionPool(conf)
    }

    private fun <T> connect(connection: Function<Connection, Mono<T>>): Mono<T> {
        return pool.create().flatMap { c ->
            connection.apply(c).flatMap { item ->
                Mono.from(c.validate(ValidationDepth.LOCAL)).flatMap { validate ->
                    if (validate) {
                        Mono.from(c.close()).thenReturn(item)
                    } else {
                        Mono.just(item)
                    }
                }
            }
        }
    }

    fun disconnectFromMySQL() = pool.dispose()

    fun updateAPIAccount(acc: UserAPIAccount): Mono<Boolean> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.API.table} WHERE API_KEY = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, acc.APIKey)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ -> row }
            }.hasElements().flatMap { exists ->
                if (exists) {
                    val updateCommand = """ UPDATE ${Tables.API.table} SET
                                USER_ID = ?, BLOCKED = ?,
                                WHERE API_KEY = ?
                                """.trimMargin()

                    Mono.from(c.createStatement(updateCommand)
                            .bind(0, acc.userId)
                            .bind(1, acc.blocked)
                            .bind(2, acc.APIKey)
                            .execute()
                    ).flatMap { res -> Mono.from(res.rowsUpdated) }
                            .thenReturn(true)
                } else {
                    val insertCommand = """INSERT INTO ${Tables.API.table}
                                (USER_ID, API_KEY, BLOCKED, TIME_ISSUED)
                                VALUES (?, ?, ?, ?)
                            """.trimMargin()

                    Mono.from(c.createStatement(insertCommand)
                            .bind(0, acc.userId)
                            .bind(1, acc.APIKey)
                            .bind(2, acc.blocked)
                            .bind(3, acc.timeIssued)
                            .execute()
                    ).flatMap { res -> Mono.from(res.rowsUpdated) }
                            .thenReturn(true)
                }
            }.doOnError {
                LogFeed.log(LogObject.forException("Failed to update API Account", it, this::class.java))
            }.onErrorResume { Mono.just(false) }
        }
    }

    fun updateSettings(settings: GuildSettings): Mono<Boolean> {
        DiscalCache.guildSettings[settings.guildID] = settings

        return connect { c ->
            val query = "SELECT * FROM ${Tables.GUILD_SETTINGS.table} WHERE GUILD_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, settings.guildID.asString())
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ -> row }
            }.hasElements().flatMap { exists ->
                if (exists) {
                    val updateCommand = """UPDATE ${Tables.GUILD_SETTINGS.table} SET
                                CONTROL_ROLE = ?, DISCAL_CHANNEL = ?, SIMPLE_ANNOUNCEMENT = ?,
                                LANG = ?, PREFIX = ?, PATRON_GUILD = ?, DEV_GUILD = ?,
                                MAX_CALENDARS = ?, DM_ANNOUNCEMENTS = ?, 12_HOUR = ?,
                                BRANDED = ? WHERE GUILD_ID = ?
                            """.trimMargin()

                    Mono.from(c.createStatement(updateCommand)
                            .bind(0, settings.controlRole)
                            .bind(1, settings.discalChannel)
                            .bind(2, settings.simpleAnnouncements)
                            .bind(3, settings.lang)
                            .bind(4, settings.prefix)
                            .bind(5, settings.patronGuild)
                            .bind(6, settings.devGuild)
                            .bind(7, settings.maxCalendars)
                            .bind(8, settings.getDmAnnouncementsString())
                            .bind(9, settings.twelveHour)
                            .bind(10, settings.branded)
                            .bind(11, settings.guildID.asString())
                            .execute()
                    ).flatMap { res -> Mono.from(res.rowsUpdated) }
                            .hasElement()
                            .thenReturn(true)
                } else {
                    val insertCommand = """INSERT INTO ${Tables.GUILD_SETTINGS.table}
                                (GUILD_ID, CONTROL_ROLE, DISCAL_CHANNEL, SIMPLE_ANNOUNCEMENT, LANG, PREFIX,
                                PATRON_GUILD, DEV_GUILD, MAX_CALENDARS, DM_ANNOUNCEMENTS, 12_HOUR, BRANDED)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """.trimMargin()

                    Mono.from(c.createStatement(insertCommand)
                            .bind(0, settings.guildID.asString())
                            .bind(1, settings.controlRole)
                            .bind(2, settings.discalChannel)
                            .bind(3, settings.simpleAnnouncements)
                            .bind(4, settings.lang)
                            .bind(5, settings.prefix)
                            .bind(6, settings.patronGuild)
                            .bind(7, settings.devGuild)
                            .bind(8, settings.maxCalendars)
                            .bind(9, settings.getDmAnnouncementsString())
                            .bind(10, settings.twelveHour)
                            .bind(11, settings.branded)
                            .execute()
                    ).flatMap { res -> Mono.from(res.rowsUpdated) }
                            .hasElement()
                            .thenReturn(true)
                }
            }.doOnError {
                LogFeed.log(LogObject.forException("Failed to update guild settings", it, this::class.java))
            }.onErrorResume { Mono.just(false) }
        }
    }

    fun updateCalendar(calData: CalendarData): Mono<Boolean> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.CALENDARS.table} WHERE GUILD_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, calData.guildId.asString())
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ -> row }
            }.hasElements().flatMap { exists ->
                if (exists) {
                    val updateCommand = """UPDATE ${Tables.CALENDARS.table} SET
                        CALENDAR_NUMBER = ?, HOST = ?, CALENDAR_ID = ?,
                        CALENDAR_ADDRESS = ?, EXTERNAL = ?, CREDENTIAL_ID = ?,
                        PRIVATE_KEY = ?, ACCESS_TOKEN = ?, REFRESH_TOKEN = ?
                        WHERE GUILD_ID = ?
                    """.trimMargin()

                    Mono.from(c.createStatement(updateCommand)
                            .bind(0, calData.calendarNumber)
                            .bind(1, calData.host.name)
                            .bind(2, calData.calendarId)
                            .bind(3, calData.calendarAddress)
                            .bind(4, calData.external)
                            .bind(5, calData.credentialId)
                            .bind(6, calData.privateKey)
                            .bind(7, calData.encryptedAccessToken)
                            .bind(8, calData.encryptedRefreshToken)
                            .bind(9, calData.guildId.asString())
                            .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                            .hasElements()
                            .thenReturn(true)
                } else {
                    val insertCommand = """INSERT INTO ${Tables.CALENDARS.table}
                        (GUILD_ID, CALENDAR_NUMBER, HOST, CALENDAR_ID,
                        CALENDAR_ADDRESS, EXTERNAL, CREDENTIAL_ID,
                        PRIVATE_KEY, ACCESS_TOKEN, REFRESH_TOKEN) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimMargin()

                    Mono.from(c.createStatement(insertCommand)
                            .bind(0, calData.guildId.asString())
                            .bind(1, calData.calendarNumber)
                            .bind(2, calData.host.name)
                            .bind(3, calData.calendarId)
                            .bind(4, calData.calendarAddress)
                            .bind(5, calData.external)
                            .bind(6, calData.credentialId)
                            .bind(7, calData.privateKey)
                            .bind(8, calData.encryptedAccessToken)
                            .bind(9, calData.encryptedRefreshToken)
                            .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                            .hasElements()
                            .thenReturn(true)
                }
            }.doOnError {
                LogFeed.log(LogObject.forException("Failed to update calendar data", it, this::class.java))
            }.onErrorResume { Mono.just(false) }
        }
    }

    fun updateAnnouncement(announcement: Announcement): Mono<Boolean> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.ANNOUNCEMENTS.table} WHERE ANNOUNCEMENT_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, announcement.announcementId.toString())
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ -> row }
            }.hasElements().flatMap { exists ->
                if (exists) {
                    val updateCommand = """UPDATE ${Tables.ANNOUNCEMENTS.table} SET
                        SUBSCRIBERS_ROLE = ?, SUBSCRIBERS_USER = ?, CHANNEL_ID = ?,
                        ANNOUNCEMENT_TYPE = ?, MODIFIER = ?, EVENT_ID = ?, EVENT_COLOR = ?,
                        HOURS_BEFORE = ?, MINUTES_BEFORE = ?,
                        INFO = ?, ENABLED = ?, INFO_ONLY = ?, PUBLISH = ?
                        WHERE ANNOUNCEMENT_ID = ?
                    """.trimMargin()

                    Mono.from(c.createStatement(updateCommand)
                            .bind(0, announcement.getSubscriberRoleIdString())
                            .bind(1, announcement.getSubscriberUserIdString())
                            .bind(2, announcement.announcementChannelId)
                            .bind(3, announcement.type.name)
                            .bind(4, announcement.modifier.name)
                            .bind(5, announcement.eventId)
                            .bind(6, announcement.eventColor.name)
                            .bind(7, announcement.hoursBefore)
                            .bind(8, announcement.minutesBefore)
                            .bind(9, announcement.info)
                            .bind(10, announcement.enabled)
                            .bind(11, announcement.infoOnly)
                            .bind(12, announcement.publish)
                            .bind(13, announcement.announcementId.toString())
                            .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                            .hasElements()
                            .thenReturn(true)
                } else {
                    val insertCommand = """INSERT INTO ${Tables.ANNOUNCEMENTS.table}
                        (ANNOUNCEMENT_ID, GUILD_ID, SUBSCRIBERS_ROLE, SUBSCRIBERS_USER,
                        CHANNEL_ID, ANNOUNCEMENT_TYPE, MODIFIER, EVENT_ID, EVENT_COLOR,
                        HOURS_BEFORE, MINUTES_BEFORE, INFO, ENABLED, INFO_ONLY, PUBLISH)
                        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimMargin()

                    Mono.from(c.createStatement(insertCommand)
                            .bind(0, announcement.announcementId.toString())
                            .bind(1, announcement.guildId.asString())
                            .bind(2, announcement.getSubscriberRoleIdString())
                            .bind(3, announcement.getSubscriberUserIdString())
                            .bind(4, announcement.announcementChannelId)
                            .bind(5, announcement.type.name)
                            .bind(6, announcement.modifier.name)
                            .bind(7, announcement.eventId)
                            .bind(8, announcement.eventColor.name)
                            .bind(9, announcement.hoursBefore)
                            .bind(10, announcement.minutesBefore)
                            .bind(11, announcement.info)
                            .bind(12, announcement.enabled)
                            .bind(13, announcement.infoOnly)
                            .bind(14, announcement.publish)
                            .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                            .hasElements()
                            .thenReturn(true)
                }
            }.doOnError {
                LogFeed.log(LogObject.forException("Failed to update announcement", it, this::class.java))
            }.onErrorResume { Mono.just(false) }
        }
    }

    fun updateEventData(data: EventData): Mono<Boolean> {
        if (!data.shouldBeSaved()) return Mono.just(false)

        val id = if (data.eventId.contains("_"))
            data.eventId.split("_")[0]
        else
            data.eventId

        return connect { c ->
            val query = "SELECT * FROM ${Tables.EVENTS.table} WHERE EVENT_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, id)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ -> row }
            }.hasElements().flatMap { exists ->
                if (exists) {
                    val updateCommand = """UPDATE ${Tables.EVENTS.table} SET
                        IMAGE_LINK = ?, EVENT_END = ?
                        WHERE EVENT_ID = ?
                    """.trimMargin()

                    Mono.from(c.createStatement(updateCommand)
                            .bind(0, data.imageLink)
                            .bind(1, data.eventEnd)
                            .bind(2, id)
                            .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                            .hasElements()
                            .thenReturn(true)
                } else {
                    val insertCommand = """INSERT INTO ${Tables.EVENTS.table}
                        (GUILD_ID, EVENT_ID, EVENT_END, IMAGE_LINK)
                        VALUES(?, ?, ?, ?)
                    """.trimMargin()

                    Mono.from(c.createStatement(insertCommand)
                            .bind(0, data.guildId.asString())
                            .bind(1, id)
                            .bind(2, data.eventEnd)
                            .bind(3, data.imageLink)
                            .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                            .hasElements()
                            .thenReturn(true)
                }.doOnError {
                    LogFeed.log(LogObject.forException("Failed to update event data", it, this::class.java))
                }.onErrorResume { Mono.just(false) }
            }
        }
    }

    fun updateRsvpData(data: RsvpData): Mono<Boolean> {
        if (!data.shouldBeSaved()) return Mono.just(false)

        return connect { c ->
            val query = "SELECT * FROM ${Tables.RSVP.table} WHERE EVENT_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, data.eventId)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ -> row }
            }.hasElements().flatMap { exists ->
                if (exists) {
                    val updateCommand = """UPDATE ${Tables.RSVP.table} SET
                        EVENT_END = ?, GOING_ON_TIME = ?, GOING_LATE = ?,
                        NOT_GOING = ?, UNDECIDED = ?, RSVP_LIMIT = ?, RSVP_ROLE = ?
                        WHERE EVENT_ID = ?
                    """.trimMargin()

                    Mono.just(c.createStatement(updateCommand)
                            .bind(0, data.eventEnd)
                            .bind(1, data.getGoingOnTimeString())
                            .bind(2, data.getGoingLateString())
                            .bind(3, data.getNotGoingString())
                            .bind(4, data.getUndecidedString())
                            .bind(5, data.limit)
                            .bind(7, data.eventId)
                    ).doOnNext { statement ->
                        if (data.roleId == null)
                            statement.bindNull(6, Long::class.java)
                        else
                            statement.bind(6, data.roleId!!.asString())
                    }.flatMap {
                        Mono.from(it.execute())
                    }.flatMapMany(Result::getRowsUpdated)
                            .hasElements()
                            .thenReturn(true)
                } else {
                    val insertCommand = """INSERT INTO ${Tables.RSVP.table}
                        (GUILD_ID, EVENT_ID, EVENT_END, GOING_ON_TIME, GOING_LATE,
                        NOT_GOING, UNDECIDED, RSVP_LIMIT, RSVP_ROLE)
                        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimMargin()

                    Mono.just(c.createStatement(insertCommand)
                            .bind(0, data.guildId.asString())
                            .bind(1, data.eventId)
                            .bind(2, data.eventEnd)
                            .bind(3, data.getGoingOnTimeString())
                            .bind(4, data.getGoingLateString())
                            .bind(5, data.getNotGoingString())
                            .bind(6, data.getUndecidedString())
                            .bind(7, data.limit)
                    ).doOnNext { statement ->
                        if (data.roleId == null)
                            statement.bindNull(8, Long::class.java)
                        else
                            statement.bind(8, data.roleId!!.asString())
                    }.flatMap {
                        Mono.from(it.execute())
                    }.flatMapMany(Result::getRowsUpdated)
                            .hasElements()
                            .thenReturn(true)
                }.doOnError {
                    LogFeed.log(LogObject.forException("Failed to update rsvp data", it, this::class.java))
                }.onErrorResume { Mono.just(false) }
            }
        }
    }

    fun getAPIAccount(APIKey: String): Mono<UserAPIAccount> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.API.table} WHERE API_KEY = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, APIKey)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    UserAPIAccount(
                            row["USER_ID", String::class.java],
                            APIKey,
                            row["BLOCKED", Boolean::class.java],
                            row["TIME_ISSUED", Long::class.java]
                    )
                }
            }.next().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get API key data", it, this::class.java))
            }.onErrorResume { Mono.empty() }
        }
    }

    fun getSettings(guildId: Snowflake): Mono<GuildSettings> {
        if (DiscalCache.guildSettings.containsKey(guildId))
            return Mono.just(DiscalCache.guildSettings[guildId])

        return connect { c ->
            val query = "SELECT * FROM ${Tables.GUILD_SETTINGS.table} WHERE GUILD_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, guildId.asString())
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val controlRole = row["CONTROL_ROLE", String::class.java]
                    val discalChannel = row["DISCAL_CHANNEL", String::class.java]
                    val simpleAnnouncements = row["SIMPLE_ANNOUNCEMENT", Boolean::class.java]
                    val lang = row["LANG", String::class.java]
                    val prefix = row["PREFIX", String::class.java]
                    val patron = row["PATRON_GUILD", Boolean::class.java]
                    val dev = row["DEV_GUILD", Boolean::class.java]
                    val maxCals = row["MAX_CALENDARS", Int::class.java]
                    val dmAnnouncementsString = row["DM_ANNOUNCEMENTS", String::class.java]
                    val twelveHour = row["12_HOUR", Boolean::class.java]
                    val branded = row["BRANDED", Boolean::class.java]

                    val settings = GuildSettings(
                            guildId, controlRole, discalChannel, simpleAnnouncements,
                            lang, prefix, patron, dev, maxCals, twelveHour, branded
                    )

                    settings.setDmAnnouncementsString(dmAnnouncementsString)

                    //Store in cache...
                    DiscalCache.guildSettings[guildId] = settings

                    settings
                }
            }.next().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get guild settings", it, this::class.java))
            }.onErrorReturn(GuildSettings.empty(guildId)).defaultIfEmpty(GuildSettings.empty(guildId))
        }
    }

    fun getMainCalendar(guildId: Snowflake): Mono<CalendarData> = getCalendar(guildId, 1)

    fun getCalendar(guildId: Snowflake, calendarNumber: Int): Mono<CalendarData> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.CALENDARS.table} WHERE GUILD_ID = ? AND CALENDAR_NUMBER = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, guildId.asString())
                    .bind(1, calendarNumber)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val calId = row["CALENDAR_ID", String::class.java]
                    val calNumber = row["CALENDAR_NUMBER", Int::class.java]
                    val calAddr = row["CALENDAR_ADDRESS", String::class.java]
                    val host = CalendarHost.valueOf(row["HOST", String::class.java])
                    val external = row["EXTERNAL", Boolean::class.java]
                    val credId = row["CREDENTIAL_ID", Int::class.java]
                    val privateKey = row["PRIVATE_KEY", String::class.java]
                    val accessToken = row["ACCESS_TOKEN", String::class.java]
                    val refreshToken = row["REFRESH_TOKEN", String::class.java]

                    CalendarData(guildId, calNumber, host, calId, calAddr, external,
                            credId, privateKey, accessToken, refreshToken)
                }
            }.next().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get all guild calendars", it, this::class.java))
            }.onErrorResume { Mono.empty() }
        }
    }

    fun getAllCalendars(guildId: Snowflake): Mono<List<CalendarData>> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.CALENDARS.table} WHERE GUILD_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, guildId.asString())
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val calId = row["CALENDAR_ID", String::class.java]
                    val calNumber = row["CALENDAR_NUMBER", Int::class.java]
                    val calAddr = row["CALENDAR_ADDRESS", String::class.java]
                    val host = CalendarHost.valueOf(row["HOST", String::class.java])
                    val external = row["EXTERNAL", Boolean::class.java]
                    val credId = row["CREDENTIAL_ID", Int::class.java]
                    val privateKey = row["PRIVATE_KEY", String::class.java]
                    val accessToken = row["ACCESS_TOKEN", String::class.java]
                    val refreshToken = row["REFRESH_TOKEN", String::class.java]

                    CalendarData(guildId, calNumber, host, calId, calAddr, external,
                            credId, privateKey, accessToken, refreshToken)
                }
            }.collectList().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get all guild calendars", it, this::class.java))
            }.onErrorReturn(mutableListOf())
        }
    }

    fun getCalendarCount(): Mono<Int> {
        return connect { c ->
            val query = "SELECT COUNT(*) FROM ${Tables.CALENDARS.table}"

            Mono.from(c.createStatement(query)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val calendars = row.get(0, Long::class.java)
                    return@map calendars.toInt()
                }
            }.next().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get calendar count", it, this::class.java))
            }.onErrorReturn(-1)
        }
    }

    fun getEventData(guildId: Snowflake, eventId: String): Mono<EventData> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.EVENTS.table} WHERE GUILD_ID = ? AND EVENT_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, guildId.asString())
                    .bind(1, eventId)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->

                    val id = row["EVENT_ID", String::class.java]
                    val end = row["EVENT_END", Long::class.java]
                    val img = row["IMAGE_LINK", String::class.java]

                    EventData(guildId, id, end, img)
                }
            }.next().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get event data", it, this::class.java))
            }.onErrorResume { Mono.empty() }
        }
    }

    fun getRsvpData(guildId: Snowflake, eventId: String): Mono<RsvpData> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.RSVP.table} WHERE GUILD_ID = ? AND EVENT_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, guildId.asString())
                    .bind(1, eventId)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val data = RsvpData(guildId, eventId)

                    data.eventEnd = row["EVENT_END", Long::class.java]
                    data.setGoingOnTimeFromString(row["GOING_ON_TIME", String::class.java])
                    data.setGoingLateFromString(row["GOING_LATE", String::class.java])
                    data.setNotGoingFromString(row["NOT_GOING", String::class.java])
                    data.setUndecidedFromString(row["UNDECIDED", String::class.java])
                    data.limit = row["RSVP_LIMIT", Int::class.java]

                    //Handle new rsvp role
                    val roleId = row["RSVP_ROLE", Long::class.java]
                    if (roleId != null) data.setRole(Snowflake.of(roleId))

                    data
                }
            }.next().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get rsvp data", it, this::class.java))
            }.onErrorResume {
                Mono.empty()
            }.defaultIfEmpty(RsvpData(guildId, eventId))
        }
    }

    fun getAnnouncement(announcementId: UUID, guildId: Snowflake): Mono<Announcement> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.ANNOUNCEMENTS.table} WHERE GUILD_ID = ? and ANNOUNCEMENT_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, guildId.asString())
                    .bind(1, announcementId.toString())
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val a = Announcement(guildId, announcementId)
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java])
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java])
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java])
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java])
                    a.eventId = row["EVENT_ID", String::class.java]
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java])
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]
                    a.info = row["INFO", String::class.java]
                    a.enabled = row["ENABLED", Boolean::class.java]
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]
                    a.publish = row["PUBLISH", Boolean::class.java]

                    a
                }
            }.next().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get announcement", it, this::class.java))
            }.onErrorResume { Mono.empty() }
        }
    }

    fun getAnnouncements(guildId: Snowflake): Mono<List<Announcement>> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.ANNOUNCEMENTS.table} WHERE GUILD_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, guildId.asString())
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String::class.java))

                    val a = Announcement(guildId, announcementId)
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java])
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java])
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java])
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java])
                    a.eventId = row["EVENT_ID", String::class.java]
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java])
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]
                    a.info = row["INFO", String::class.java]
                    a.enabled = row["ENABLED", Boolean::class.java]
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]
                    a.publish = row["PUBLISH", Boolean::class.java]

                    a
                }
            }.collectList().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get announcements for guild", it, this::class.java))
            }.onErrorReturn(mutableListOf())
        }
    }

    fun getAnnouncements(guildId: Snowflake, type: AnnouncementType): Mono<List<Announcement>> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.ANNOUNCEMENTS.table} WHERE GUILD_ID = ? AND ANNOUNCEMENT_TYPE = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, guildId.asString())
                    .bind(1, type.name)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String::class.java))

                    val a = Announcement(guildId, announcementId)
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java])
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java])
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java])
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java])
                    a.eventId = row["EVENT_ID", String::class.java]
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java])
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]
                    a.info = row["INFO", String::class.java]
                    a.enabled = row["ENABLED", Boolean::class.java]
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]
                    a.publish = row["PUBLISH", Boolean::class.java]

                    a
                }
            }.collectList().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get ann's for guild by type", it, this::class.java))
            }.onErrorReturn(mutableListOf())
        }
    }

    fun getAnnouncements(): Mono<List<Announcement>> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.ANNOUNCEMENTS.table}"

            Mono.from(c.createStatement(query)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String::class.java))
                    val guildId = Snowflake.of(row.get("GUILD_ID", String::class.java))

                    val a = Announcement(guildId, announcementId)
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java])
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java])
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java])
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java])
                    a.eventId = row["EVENT_ID", String::class.java]
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java])
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]
                    a.info = row["INFO", String::class.java]
                    a.enabled = row["ENABLED", Boolean::class.java]
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]
                    a.publish = row["PUBLISH", Boolean::class.java]

                    a
                }
            }.collectList().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get all announcements", it, this::class.java))
            }.onErrorReturn(mutableListOf())
        }
    }

    fun getAnnouncements(type: AnnouncementType): Mono<List<Announcement>> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.ANNOUNCEMENTS.table} WHERE ANNOUNCEMENT_TYPE = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, type.name)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String::class.java))
                    val guildId = Snowflake.of(row.get("GUILD_ID", String::class.java))

                    val a = Announcement(guildId, announcementId)
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java])
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java])
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java])
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java])
                    a.eventId = row["EVENT_ID", String::class.java]
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java])
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]
                    a.info = row["INFO", String::class.java]
                    a.enabled = row["ENABLED", Boolean::class.java]
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]
                    a.publish = row["PUBLISH", Boolean::class.java]

                    a
                }
            }.collectList().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get announcements by type", it, this::class.java))
            }.onErrorReturn(mutableListOf())
        }
    }

    fun getEnabledAnnouncements(): Mono<List<Announcement>> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.ANNOUNCEMENTS.table} WHERE ENABLED = 1"

            Mono.from(c.createStatement(query)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String::class.java))
                    val guildId = Snowflake.of(row.get("GUILD_ID", String::class.java))

                    val a = Announcement(guildId, announcementId)
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java])
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java])
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java])
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java])
                    a.eventId = row["EVENT_ID", String::class.java]
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java])
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]
                    a.info = row["INFO", String::class.java]
                    a.enabled = row["ENABLED", Boolean::class.java]
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]
                    a.publish = row["PUBLISH", Boolean::class.java]

                    a
                }
            }.collectList().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get enabled announcements", it, this::class.java))
            }.onErrorReturn(mutableListOf())
        }
    }

    fun getEnabledAnnouncements(guildId: Snowflake): Mono<List<Announcement>> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.ANNOUNCEMENTS.table} WHERE ENABLED = 1 and GUILD_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, guildId.asString())
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String::class.java))

                    val a = Announcement(guildId, announcementId)
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java])
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java])
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java])
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java])
                    a.eventId = row["EVENT_ID", String::class.java]
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java])
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]
                    a.info = row["INFO", String::class.java]
                    a.enabled = row["ENABLED", Boolean::class.java]
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]
                    a.publish = row["PUBLISH", Boolean::class.java]

                    a
                }
            }.collectList().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get enabled ann's for guild", it, this::class.java))
            }.onErrorReturn(mutableListOf())
        }
    }

    fun getEnabledAnnouncements(announcementType: AnnouncementType): Mono<List<Announcement>> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.ANNOUNCEMENTS.table} WHERE ENABLED = 1 and ANNOUNCEMENT_TYPE = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, announcementType.name)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String::class.java))
                    val guildId = Snowflake.of(row.get("GUILD_ID", String::class.java))

                    val a = Announcement(guildId, announcementId)
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java])
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java])
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java])
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java])
                    a.eventId = row["EVENT_ID", String::class.java]
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java])
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]
                    a.info = row["INFO", String::class.java]
                    a.enabled = row["ENABLED", Boolean::class.java]
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]
                    a.publish = row["PUBLISH", Boolean::class.java]

                    a
                }
            }.collectList().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get enabled ann's by type", it, this::class.java))
            }.onErrorReturn(mutableListOf())
        }
    }

    fun getAnnouncementCount(): Mono<Int> {
        return connect { c ->
            val query = "SELECT COUNT(*) FROM ${Tables.ANNOUNCEMENTS.table}"

            Mono.from(c.createStatement(query)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val announcements = row[0, Long::class.java]
                    return@map announcements.toInt()
                }
            }.next().retryWhen(Retry.max(3)
                    .filter(IllegalStateException::class::isInstance)
                    .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LogFeed.log(LogObject.forException("Failed to get announcement count", it, this::class.java))
            }.onErrorReturn(-1)
        }
    }

    fun deleteAnnouncement(announcementId: String): Mono<Boolean> {
        return connect { c ->
            val query = "DELETE FROM ${Tables.ANNOUNCEMENTS.table} WHERE ANNOUNCEMENT_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, announcementId)
                    .execute()
            ).flatMapMany(Result::getRowsUpdated)
                    .hasElements()
                    .thenReturn(true)
                    .doOnError {
                        LogFeed.log(LogObject.forException("Failed to delete announcement", it, this::class.java))
                    }.onErrorReturn(false)
        }
    }

    fun deleteAnnouncementsForEvent(guildId: Snowflake, eventId: String): Mono<Boolean> {
        return connect { c ->
            val query = "DELETE FROM ${Tables.ANNOUNCEMENTS.table} WHERE EVENT_ID = ? AND GUILD_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, eventId)
                    .bind(1, guildId.asString())
                    .execute()
            ).flatMapMany(Result::getRowsUpdated)
                    .hasElements()
                    .thenReturn(true)
                    .doOnError {
                        LogFeed.log(LogObject.forException("Failed to delete ann's for event", it, this::class.java))
                    }.onErrorReturn(false)
        }
    }

    fun deleteEventData(eventId: String): Mono<Boolean> {
        if (eventId.contains("_")) return Mono.empty() // Don't delete if child event of recurring parent.
        return connect { c ->
            val query = "DELETE FROM ${Tables.EVENTS.table} WHERE EVENT_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, eventId)
                    .execute()
            ).flatMapMany(Result::getRowsUpdated)
                    .hasElements()
                    .thenReturn(true)
                    .doOnError {
                        LogFeed.log(LogObject.forException("Failed to delete event data", it, this::class.java))
                    }.onErrorReturn(false)
        }
    }

    fun deleteAllEventData(guildId: Snowflake): Mono<Boolean> {
        return connect { c ->
            val query = "DELETE FROM ${Tables.EVENTS.table} WHERE GUILD_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, guildId.asString())
                    .execute()
            ).flatMapMany(Result::getRowsUpdated)
                    .hasElements()
                    .thenReturn(true)
                    .doOnError {
                        LogFeed.log(LogObject
                                .forException("Failed to delete all event data for guild", it, this::class.java))
                    }.onErrorReturn(false)
        }
    }

    fun deleteAllAnnouncementData(guildId: Snowflake): Mono<Boolean> {
        return connect { c ->
            val query = "DELETE FROM ${Tables.ANNOUNCEMENTS.table} WHERE GUILD_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, guildId.asString())
                    .execute()
            ).flatMapMany(Result::getRowsUpdated)
                    .hasElements()
                    .thenReturn(true)
                    .doOnError {
                        LogFeed.log(LogObject.forException("Failed to delete all ann for guild", it, this::class.java))
                    }.onErrorReturn(false)
        }
    }

    fun deleteAllRsvpData(guildId: Snowflake): Mono<Boolean> {
        return connect { c ->
            val query = "DELETE FROM ${Tables.RSVP.table} WHERE GUILD_ID = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, guildId.asString())
                    .execute()
            ).flatMapMany(Result::getRowsUpdated)
                    .hasElements()
                    .thenReturn(true)
                    .doOnError {
                        LogFeed.log(LogObject.forException("Failed to delete all rsvps for guild", it, this::class.java))
                    }.onErrorReturn(false)

        }
    }

    fun removeRsvpRole(guildId: Snowflake, roleId: Snowflake): Mono<Boolean> {
        return connect { c ->
            val query = "UPDATE ${Tables.RSVP.table} SET RSVP ROLE = ? WHERE GUILD_ID = ? AND RSVP_ROLE = ?"

            Mono.from(c.createStatement(query)
                    .bindNull(0, Long::class.java)
                    .bind(1, guildId.asString())
                    .bind(2, roleId.asString())
                    .execute()
            ).flatMapMany(Result::getRowsUpdated)
                    .hasElements()
                    .thenReturn(true)
                    .doOnError {
                        LogFeed.log(LogObject
                                .forException("Failed to up[date all rsvp with role for guild", it, this::class.java))
                    }.onErrorReturn(false)
        }
    }

    fun deleteCalendar(data: CalendarData): Mono<Boolean> {
        return connect { c ->
            val query = "DELETE FROM ${Tables.CALENDARS.table} WHERE GUILD_ID = ? AND CALENDAR_ADDRESS = ?"

            Mono.from(c.createStatement(query)
                    .bind(0, data.guildId.asString())
                    .bind(1, data.calendarAddress)
                    .execute()
            ).flatMapMany(Result::getRowsUpdated)
                    .hasElements()
                    .thenReturn(true)
                    .doOnError {
                        LogFeed.log(LogObject.forException("Failed to delete calendar", it, this::class.java))
                    }.onErrorReturn(false)
        }
    }
}

private enum class Tables constructor(val table: String) {
    API("${BotSettings.SQL_PREFIX.get()}api"),
    GUILD_SETTINGS("${BotSettings.SQL_PREFIX.get()}guild_settings"),
    CALENDARS("${BotSettings.SQL_PREFIX.get()}calendars"),
    ANNOUNCEMENTS("${BotSettings.SQL_PREFIX.get()}announcements"),
    EVENTS("${BotSettings.SQL_PREFIX.get()}events"),
    RSVP("${BotSettings.SQL_PREFIX.get()}rsvp"),
}
