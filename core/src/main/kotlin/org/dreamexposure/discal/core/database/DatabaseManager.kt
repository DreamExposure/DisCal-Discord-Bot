@file:Suppress("SqlResolve", "DuplicatedCode")

package org.dreamexposure.discal.core.database

import discord4j.common.util.Snowflake
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions.*
import io.r2dbc.spi.Result
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.`object`.event.EventData
import org.dreamexposure.discal.core.`object`.event.RsvpData
import org.dreamexposure.discal.core.`object`.google.GoogleCredentialData
import org.dreamexposure.discal.core.`object`.web.UserAPIAccount
import org.dreamexposure.discal.core.cache.DiscalCache
import org.dreamexposure.discal.core.enums.announcement.AnnouncementModifier
import org.dreamexposure.discal.core.enums.announcement.AnnouncementStyle
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.enums.event.EventColor.Companion.fromNameOrHexOrId
import org.dreamexposure.discal.core.enums.time.TimeFormat
import org.dreamexposure.discal.core.extensions.asStringList
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.novautils.database.DatabaseSettings
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration
import java.time.Instant
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

        val conf = ConnectionPoolConfiguration.builder(factory)
              .maxLifeTime(Duration.ofHours(1))
              .build()

        pool = ConnectionPool(conf)
    }

    private fun <T> connect(connection: Function<Connection, Mono<T>>): Mono<T> {
        return Mono.usingWhen(pool.create(), connection::apply, Connection::close)
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
                                USER_ID = ?, BLOCKED = ?
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
                LOGGER.error(DEFAULT, "Failed to update API account", it)
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
                                CONTROL_ROLE = ?, DISCAL_CHANNEL = ?, ANNOUNCEMENT_STYLE = ?, TIME_FORMAT = ?,
                                LANG = ?, PREFIX = ?, PATRON_GUILD = ?, DEV_GUILD = ?,
                                MAX_CALENDARS = ?, DM_ANNOUNCEMENTS = ?,
                                BRANDED = ? WHERE GUILD_ID = ?
                            """.trimMargin()

                    Mono.from(c.createStatement(updateCommand)
                          .bind(0, settings.controlRole)
                          .bind(1, settings.discalChannel)
                          .bind(2, settings.announcementStyle.value)
                          .bind(3, settings.timeFormat.value)
                          .bind(4, settings.lang)
                          .bind(5, settings.prefix)
                          .bind(6, settings.patronGuild)
                          .bind(7, settings.devGuild)
                          .bind(8, settings.maxCalendars)
                          .bind(9, settings.getDmAnnouncementsString())
                          .bind(10, settings.branded)
                          .bind(11, settings.guildID.asString())
                          .execute()
                    ).flatMap { res -> Mono.from(res.rowsUpdated) }
                          .hasElement()
                          .thenReturn(true)
                } else {
                    val insertCommand = """INSERT INTO ${Tables.GUILD_SETTINGS.table}
                                (GUILD_ID, CONTROL_ROLE, DISCAL_CHANNEL, ANNOUNCEMENT_STYLE, TIME_FORMAT, LANG, PREFIX,
                                PATRON_GUILD, DEV_GUILD, MAX_CALENDARS, DM_ANNOUNCEMENTS, BRANDED)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """.trimMargin()

                    Mono.from(c.createStatement(insertCommand)
                          .bind(0, settings.guildID.asString())
                          .bind(1, settings.controlRole)
                          .bind(2, settings.discalChannel)
                          .bind(3, settings.announcementStyle.value)
                          .bind(4, settings.timeFormat.value)
                          .bind(5, settings.lang)
                          .bind(6, settings.prefix)
                          .bind(7, settings.patronGuild)
                          .bind(8, settings.devGuild)
                          .bind(9, settings.maxCalendars)
                          .bind(10, settings.getDmAnnouncementsString())
                          .bind(11, settings.branded)
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
                        PRIVATE_KEY = ?, ACCESS_TOKEN = ?, REFRESH_TOKEN = ?, EXPIRES_AT
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
                          .bind(9, calData.expiresAt.toEpochMilli())
                          .bind(10, calData.guildId.asString())
                          .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                          .hasElements()
                          .thenReturn(true)
                } else {
                    val insertCommand = """INSERT INTO ${Tables.CALENDARS.table}
                        (GUILD_ID, CALENDAR_NUMBER, HOST, CALENDAR_ID,
                        CALENDAR_ADDRESS, EXTERNAL, CREDENTIAL_ID,
                        PRIVATE_KEY, ACCESS_TOKEN, REFRESH_TOKEN, EXPIRES_AT) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
                        CALENDAR_NUMBER = ?, SUBSCRIBERS_ROLE = ?, SUBSCRIBERS_USER = ?, CHANNEL_ID = ?,
                        ANNOUNCEMENT_TYPE = ?, MODIFIER = ?, EVENT_ID = ?, EVENT_COLOR = ?,
                        HOURS_BEFORE = ?, MINUTES_BEFORE = ?,
                        INFO = ?, ENABLED = ?, INFO_ONLY = ?, PUBLISH = ?
                        WHERE ANNOUNCEMENT_ID = ?
                    """.trimMargin()

                    Mono.from(c.createStatement(updateCommand)
                          .bind(0, announcement.calendarNumber)
                          .bind(1, announcement.subscriberRoleIds.asStringList())
                          .bind(2, announcement.subscriberUserIds.asStringList())
                          .bind(3, announcement.announcementChannelId)
                          .bind(4, announcement.type.name)
                          .bind(5, announcement.modifier.name)
                          .bind(6, announcement.eventId)
                          .bind(7, announcement.eventColor.name)
                          .bind(8, announcement.hoursBefore)
                          .bind(9, announcement.minutesBefore)
                          .bind(10, announcement.info)
                          .bind(11, announcement.enabled)
                          .bind(12, announcement.infoOnly)
                          .bind(13, announcement.publish)
                          .bind(14, announcement.announcementId.toString())
                          .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                          .hasElements()
                          .thenReturn(true)
                } else {
                    val insertCommand = """INSERT INTO ${Tables.ANNOUNCEMENTS.table}
                        (ANNOUNCEMENT_ID, CALENDAR_NUMBER, GUILD_ID, SUBSCRIBERS_ROLE, SUBSCRIBERS_USER,
                        CHANNEL_ID, ANNOUNCEMENT_TYPE, MODIFIER, EVENT_ID, EVENT_COLOR,
                        HOURS_BEFORE, MINUTES_BEFORE, INFO, ENABLED, INFO_ONLY, PUBLISH)
                        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimMargin()

                    Mono.from(c.createStatement(insertCommand)
                          .bind(0, announcement.announcementId.toString())
                          .bind(1, announcement.calendarNumber)
                          .bind(2, announcement.guildId.asString())
                          .bind(3, announcement.subscriberRoleIds.asStringList())
                          .bind(4, announcement.subscriberUserIds.asStringList())
                          .bind(5, announcement.announcementChannelId)
                          .bind(6, announcement.type.name)
                          .bind(7, announcement.modifier.name)
                          .bind(8, announcement.eventId)
                          .bind(9, announcement.eventColor.name)
                          .bind(10, announcement.hoursBefore)
                          .bind(11, announcement.minutesBefore)
                          .bind(12, announcement.info)
                          .bind(13, announcement.enabled)
                          .bind(14, announcement.infoOnly)
                          .bind(15, announcement.publish)
                          .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                          .hasElements()
                          .thenReturn(true)
                }
            }.doOnError {
                LOGGER.error(DEFAULT, "Failed to update announcement", it)
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
                        CALENDAR_NUMBER = ?, IMAGE_LINK = ?, EVENT_END = ?
                        WHERE EVENT_ID = ?
                    """.trimMargin()

                    Mono.from(c.createStatement(updateCommand)
                          .bind(0, data.calendarNumber)
                          .bind(1, data.imageLink)
                          .bind(2, data.eventEnd)
                          .bind(3, id)
                          .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                          .hasElements()
                          .thenReturn(true)
                } else {
                    val insertCommand = """INSERT INTO ${Tables.EVENTS.table}
                        (GUILD_ID, EVENT_ID, CALENDAR_NUMBER, EVENT_END, IMAGE_LINK)
                        VALUES(?, ?, ?, ?, ?)
                    """.trimMargin()

                    Mono.from(c.createStatement(insertCommand)
                          .bind(0, data.guildId.asString())
                          .bind(1, id)
                          .bind(2, data.calendarNumber)
                          .bind(3, data.eventEnd)
                          .bind(4, data.imageLink)
                          .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                          .hasElements()
                          .thenReturn(true)
                }.doOnError {
                    LOGGER.error(DEFAULT, "Failed to update event data", it)
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
                        CALENDAR_NUMBER = ?, EVENT_END = ?, GOING_ON_TIME = ?, GOING_LATE = ?,
                        NOT_GOING = ?, UNDECIDED = ?, RSVP_LIMIT = ?, RSVP_ROLE = ?
                        WHERE EVENT_ID = ?
                    """.trimMargin()

                    Mono.just(c.createStatement(updateCommand)
                          .bind(0, data.calendarNumber)
                          .bind(1, data.eventEnd)
                          .bind(2, data.goingOnTime.asStringList())
                          .bind(3, data.goingLate.asStringList())
                          .bind(4, data.notGoing.asStringList())
                          .bind(5, data.undecided.asStringList())
                          .bind(6, data.limit)
                          .bind(8, data.eventId)
                    ).doOnNext { statement ->
                        if (data.roleId == null)
                            statement.bindNull(7, Long::class.java)
                        else
                            statement.bind(7, data.roleId!!.asString())
                    }.flatMap {
                        Mono.from(it.execute())
                    }.flatMapMany(Result::getRowsUpdated)
                          .hasElements()
                          .thenReturn(true)
                } else {
                    val insertCommand = """INSERT INTO ${Tables.RSVP.table}
                        (GUILD_ID, EVENT_ID, CALENDAR_NUMBER, EVENT_END, GOING_ON_TIME, GOING_LATE,
                        NOT_GOING, UNDECIDED, RSVP_LIMIT, RSVP_ROLE)
                        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimMargin()

                    Mono.just(c.createStatement(insertCommand)
                          .bind(0, data.guildId.asString())
                          .bind(1, data.eventId)
                          .bind(2, data.calendarNumber)
                          .bind(3, data.eventEnd)
                          .bind(4, data.goingOnTime.asStringList())
                          .bind(5, data.goingLate.asStringList())
                          .bind(6, data.notGoing.asStringList())
                          .bind(7, data.undecided.asStringList())
                          .bind(8, data.limit)
                    ).doOnNext { statement ->
                        if (data.roleId == null)
                            statement.bindNull(9, Long::class.java)
                        else
                            statement.bind(9, data.roleId!!.asString())
                    }.flatMap {
                        Mono.from(it.execute())
                    }.flatMapMany(Result::getRowsUpdated)
                          .hasElements()
                          .thenReturn(true)
                }.doOnError {
                    LOGGER.error(DEFAULT, "Failed to update rsvp data", it)
                }.onErrorResume { Mono.just(false) }
            }
        }
    }

    fun updateCredentialData(credData: GoogleCredentialData): Mono<Boolean> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.CREDS.table} WHERE CREDENTIAL_NUMBER = ?"
            Mono.from(c.createStatement(query)
                  .bind(0, credData.credentialNumber)
                  .execute()
            ).flatMapMany { res ->
                res.map { row, _ -> row }
            }.hasElements().flatMap { exists ->
                if (exists) {
                    val updateCommand = """UPDATE ${Tables.CREDS.table} SET
                        REFRESH_TOKEN = ?, ACCESS_TOKEN = ?, EXPIRES_AT = ?
                        WHERE CREDENTIAL_NUMBER = ?""".trimMargin()

                    Mono.from(c.createStatement(updateCommand)
                          .bind(0, credData.encryptedRefreshToken)
                          .bind(1, credData.encryptedAccessToken)
                          .bind(2, credData.expiresAt.toEpochMilli())
                          .bind(3, credData.credentialNumber)
                          .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                          .hasElements()
                          .thenReturn(true)
                } else {
                    val insertCommand = """INSERT INTO ${Tables.CREDS.table}
                        |(CREDENTIAL_NUMBER, REFRESH_TOKEN, ACCESS_TOKEN, EXPIRES_AT)
                        |VALUES(?, ?, ?, ?)""".trimMargin()

                    Mono.from(c.createStatement(insertCommand)
                          .bind(0, credData.credentialNumber)
                          .bind(1, credData.encryptedRefreshToken)
                          .bind(2, credData.encryptedAccessToken)
                          .bind(3, credData.expiresAt.toEpochMilli())
                          .execute()
                    ).flatMapMany(Result::getRowsUpdated)
                          .hasElements()
                          .thenReturn(true)
                }.doOnError {
                    LOGGER.error(DEFAULT, "Failed to update credential data", it)
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
            val query = "SELECT * FROM ${Tables.GUILD_SETTINGS.table} WHERE GUILD_ID = ?"

            Mono.from(c.createStatement(query)
                  .bind(0, guildId.asString())
                  .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val controlRole = row["CONTROL_ROLE", String::class.java]!!
                    val discalChannel = row["DISCAL_CHANNEL", String::class.java]!!
                    val announcementStyle = AnnouncementStyle.fromValue(row["ANNOUNCEMENT_STYLE", Int::class.java]!!)
                    val timeFormat = TimeFormat.fromValue(row["TIME_FORMAT", Int::class.java]!!)
                    val lang = row["LANG", String::class.java]!!
                    val prefix = row["PREFIX", String::class.java]!!
                    val patron = row["PATRON_GUILD", Boolean::class.java]!!
                    val dev = row["DEV_GUILD", Boolean::class.java]!!
                    val maxCals = row["MAX_CALENDARS", Int::class.java]!!
                    val dmAnnouncementsString = row["DM_ANNOUNCEMENTS", String::class.java]!!
                    val branded = row["BRANDED", Boolean::class.java]!!

                    val settings = GuildSettings(
                          guildId, controlRole, discalChannel, announcementStyle, timeFormat,
                          lang, prefix, patron, dev, maxCals, branded
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
                LOGGER.error(DEFAULT, "Failed to get guild settings", it)
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

                    CalendarData(guildId, calNumber, host, calId, calAddr, external,
                          credId, privateKey, accessToken, refreshToken, expiresAt)
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
            val query = "SELECT * FROM ${Tables.CALENDARS.table} WHERE GUILD_ID = ?"

            Mono.from(c.createStatement(query)
                  .bind(0, guildId.asString())
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

                    CalendarData(guildId, calNumber, host, calId, calAddr, external,
                          credId, privateKey, accessToken, refreshToken, expiresAt)
                }
            }.collectList().retryWhen(Retry.max(3)
                  .filter(IllegalStateException::class::isInstance)
                  .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get all guild calendars", it)
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
                    val calendars = row.get(0, Long::class.java)!!
                    return@map calendars.toInt()
                }
            }.next().retryWhen(Retry.max(3)
                  .filter(IllegalStateException::class::isInstance)
                  .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get calendar count", it)
            }.onErrorReturn(-1)
        }
    }

    fun getEventData(guildId: Snowflake, eventId: String): Mono<EventData> {
        var eventIdLookup = eventId
        if (eventId.contains("_"))
            eventIdLookup = eventId.split("_")[0]

        return connect { c ->
            val query = "SELECT * FROM ${Tables.EVENTS.table} WHERE GUILD_ID = ? AND EVENT_ID = ?"

            Mono.from(c.createStatement(query)
                  .bind(0, guildId.asString())
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
            }.defaultIfEmpty(EventData(guildId, eventId = eventIdLookup))
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
                    val calNumber = row["CALENDAR_NUMBER", Int::class.java]!!

                    val data = RsvpData(guildId, eventId, calNumber)

                    data.eventEnd = row["EVENT_END", Long::class.java]!!
                    data.setGoingOnTimeFromString(row["GOING_ON_TIME", String::class.java]!!)
                    data.setGoingLateFromString(row["GOING_LATE", String::class.java]!!)
                    data.setNotGoingFromString(row["NOT_GOING", String::class.java]!!)
                    data.setUndecidedFromString(row["UNDECIDED", String::class.java]!!)
                    data.limit = row["RSVP_LIMIT", Int::class.java]!!

                    //Handle new rsvp role
                    if (row.get("RSVP_ROLE") != null)
                        data.setRole(Snowflake.of(row["RSVP_ROLE", Long::class.java]!!))

                    data
                }
            }.next().retryWhen(Retry.max(3)
                  .filter(IllegalStateException::class::isInstance)
                  .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get rsvp data", it)
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
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java]!!)
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java]!!)
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]!!
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java]!!)
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java]!!)
                    a.eventId = row["EVENT_ID", String::class.java]!!
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java]!!)
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]!!
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]!!
                    a.info = row["INFO", String::class.java]!!
                    a.enabled = row["ENABLED", Boolean::class.java]!!
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]!!
                    a.publish = row["PUBLISH", Boolean::class.java]!!

                    a
                }
            }.next().retryWhen(Retry.max(3)
                  .filter(IllegalStateException::class::isInstance)
                  .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get announcement", it)
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
                    a.calendarNumber = row["CALENDAR_NUMBER", Int::class.java]!!
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java]!!)
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java]!!)
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]!!
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java]!!)
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java]!!)
                    a.eventId = row["EVENT_ID", String::class.java]!!
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java]!!)
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]!!
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]!!
                    a.info = row["INFO", String::class.java]!!
                    a.enabled = row["ENABLED", Boolean::class.java]!!
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]!!
                    a.publish = row["PUBLISH", Boolean::class.java]!!

                    a
                }
            }.collectList().retryWhen(Retry.max(3)
                  .filter(IllegalStateException::class::isInstance)
                  .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get announcements for guild", it)
            }.onErrorReturn(mutableListOf())
        }.defaultIfEmpty(mutableListOf())
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
                    a.calendarNumber = row["CALENDAR_NUMBER", Int::class.java]!!
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java]!!)
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java]!!)
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]!!
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java]!!)
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java]!!)
                    a.eventId = row["EVENT_ID", String::class.java]!!
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java]!!)
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]!!
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]!!
                    a.info = row["INFO", String::class.java]!!
                    a.enabled = row["ENABLED", Boolean::class.java]!!
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]!!
                    a.publish = row["PUBLISH", Boolean::class.java]!!

                    a
                }
            }.collectList().retryWhen(Retry.max(3)
                  .filter(IllegalStateException::class::isInstance)
                  .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get guild's announcements by type", it)
            }.onErrorReturn(mutableListOf())
        }.defaultIfEmpty(mutableListOf())
    }

    fun getAnnouncements(): Mono<List<Announcement>> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.ANNOUNCEMENTS.table}"

            Mono.from(c.createStatement(query)
                  .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String::class.java))
                    val guildId = Snowflake.of(row["GUILD_ID", String::class.java]!!)

                    val a = Announcement(guildId, announcementId)
                    a.calendarNumber = row["CALENDAR_NUMBER", Int::class.java]!!
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java]!!)
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java]!!)
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]!!
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java]!!)
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java]!!)
                    a.eventId = row["EVENT_ID", String::class.java]!!
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java]!!)
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]!!
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]!!
                    a.info = row["INFO", String::class.java]!!
                    a.enabled = row["ENABLED", Boolean::class.java]!!
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]!!
                    a.publish = row["PUBLISH", Boolean::class.java]!!

                    a
                }
            }.collectList().retryWhen(Retry.max(3)
                  .filter(IllegalStateException::class::isInstance)
                  .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get all announcements", it)
            }.onErrorReturn(mutableListOf())
        }.defaultIfEmpty(mutableListOf())
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
                    val guildId = Snowflake.of(row["GUILD_ID", String::class.java]!!)

                    val a = Announcement(guildId, announcementId)
                    a.calendarNumber = row["CALENDAR_NUMBER", Int::class.java]!!
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java]!!)
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java]!!)
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]!!
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java]!!)
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java]!!)
                    a.eventId = row["EVENT_ID", String::class.java]!!
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java]!!)
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]!!
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]!!
                    a.info = row["INFO", String::class.java]!!
                    a.enabled = row["ENABLED", Boolean::class.java]!!
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]!!
                    a.publish = row["PUBLISH", Boolean::class.java]!!

                    a
                }
            }.collectList().retryWhen(Retry.max(3)
                  .filter(IllegalStateException::class::isInstance)
                  .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get announcements by type", it)
            }.onErrorReturn(mutableListOf())
        }.defaultIfEmpty(mutableListOf())
    }

    fun getEnabledAnnouncements(): Mono<List<Announcement>> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.ANNOUNCEMENTS.table} WHERE ENABLED = 1"

            Mono.from(c.createStatement(query)
                  .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val announcementId = UUID.fromString(row.get("ANNOUNCEMENT_ID", String::class.java))
                    val guildId = Snowflake.of(row["GUILD_ID", String::class.java]!!)

                    val a = Announcement(guildId, announcementId)
                    a.calendarNumber = row["CALENDAR_NUMBER", Int::class.java]!!
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java]!!)
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java]!!)
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]!!
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java]!!)
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java]!!)
                    a.eventId = row["EVENT_ID", String::class.java]!!
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java]!!)
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]!!
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]!!
                    a.info = row["INFO", String::class.java]!!
                    a.enabled = row["ENABLED", Boolean::class.java]!!
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]!!
                    a.publish = row["PUBLISH", Boolean::class.java]!!

                    a
                }
            }.collectList().retryWhen(Retry.max(3)
                  .filter(IllegalStateException::class::isInstance)
                  .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get enabled announcements", it)
            }.onErrorReturn(mutableListOf())
        }.defaultIfEmpty(mutableListOf())
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
                    a.calendarNumber = row["CALENDAR_NUMBER", Int::class.java]!!
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java]!!)
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java]!!)
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]!!
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java]!!)
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java]!!)
                    a.eventId = row["EVENT_ID", String::class.java]!!
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java]!!)
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]!!
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]!!
                    a.info = row["INFO", String::class.java]!!
                    a.enabled = row["ENABLED", Boolean::class.java]!!
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]!!
                    a.publish = row["PUBLISH", Boolean::class.java]!!

                    a
                }
            }.collectList().retryWhen(Retry.max(3)
                  .filter(IllegalStateException::class::isInstance)
                  .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get guild's enabled announcements", it)
            }.onErrorReturn(mutableListOf())
        }.defaultIfEmpty(mutableListOf())
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
                    val guildId = Snowflake.of(row["GUILD_ID", String::class.java]!!)

                    val a = Announcement(guildId, announcementId)
                    a.calendarNumber = row["CALENDAR_NUMBER", Int::class.java]!!
                    a.setSubscriberRoleIdsFromString(row["SUBSCRIBERS_ROLE", String::class.java]!!)
                    a.setSubscriberUserIdsFromString(row["SUBSCRIBERS_USER", String::class.java]!!)
                    a.announcementChannelId = row["CHANNEL_ID", String::class.java]!!
                    a.type = AnnouncementType.valueOf(row["ANNOUNCEMENT_TYPE", String::class.java]!!)
                    a.modifier = AnnouncementModifier.valueOf(row["MODIFIER", String::class.java]!!)
                    a.eventId = row["EVENT_ID", String::class.java]!!
                    a.eventColor = fromNameOrHexOrId(row["EVENT_COLOR", String::class.java]!!)
                    a.hoursBefore = row["HOURS_BEFORE", Int::class.java]!!
                    a.minutesBefore = row["MINUTES_BEFORE", Int::class.java]!!
                    a.info = row["INFO", String::class.java]!!
                    a.enabled = row["ENABLED", Boolean::class.java]!!
                    a.infoOnly = row["INFO_ONLY", Boolean::class.java]!!
                    a.publish = row["PUBLISH", Boolean::class.java]!!

                    a
                }
            }.collectList().retryWhen(Retry.max(3)
                  .filter(IllegalStateException::class::isInstance)
                  .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get enabled announcements by type", it)
            }.onErrorReturn(mutableListOf())
        }.defaultIfEmpty(mutableListOf())
    }

    fun getAnnouncementCount(): Mono<Int> {
        return connect { c ->
            val query = "SELECT COUNT(*) FROM ${Tables.ANNOUNCEMENTS.table}"

            Mono.from(c.createStatement(query)
                  .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val announcements = row[0, Long::class.java]!!
                    return@map announcements.toInt()
                }
            }.next().retryWhen(Retry.max(3)
                  .filter(IllegalStateException::class::isInstance)
                  .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get announcement count", it)
            }.onErrorReturn(-1)
        }.defaultIfEmpty(-1)
    }

    fun getCredentialData(credNumber: Int): Mono<GoogleCredentialData> {
        return connect { c ->
            val query = "SELECT * FROM ${Tables.CREDS.table} WHERE CREDENTIAL_NUMBER = ?"

            Mono.from(c.createStatement(query)
                  .bind(0, credNumber)
                  .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    val refresh = row["REFRESH_TOKEN", String::class.java]!!
                    val access = row["ACCESS_TOKEN", String::class.java]!!
                    val expires = Instant.ofEpochMilli(row["EXPIRES_AT", Long::class.java]!!)

                    GoogleCredentialData(credNumber, refresh, access, expires)
                }
            }.next().retryWhen(Retry.max(3)
                  .filter(IllegalStateException::class::isInstance)
                  .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get enabled announcements by type", it)
            }.onErrorResume { Mono.empty() }
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
                      LOGGER.error(DEFAULT, "Failed to delete announcements", it)
                  }.onErrorReturn(false)
        }.defaultIfEmpty(false)
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
                      LOGGER.error(DEFAULT, "Failed to delete announcements for event", it)
                  }.onErrorReturn(false)
        }.defaultIfEmpty(false)
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
                      LOGGER.error(DEFAULT, "Failed to delete event data", it)
                  }.onErrorReturn(false)
        }.defaultIfEmpty(false)
    }

    fun deleteAllEventData(guildId: Snowflake, calNumber: Int): Mono<Boolean> {
        return connect { c ->
            val query = "DELETE FROM ${Tables.EVENTS.table} WHERE GUILD_ID = ? AND CALENDAR_NUMBER = ?"

            Mono.from(c.createStatement(query)
                  .bind(0, guildId.asString())
                  .bind(1, calNumber)
                  .execute()
            ).flatMapMany(Result::getRowsUpdated)
                  .hasElements()
                  .thenReturn(true)
                  .doOnError {
                      LOGGER.error(DEFAULT, "Failed to delete all event data for guild", it)
                  }.onErrorReturn(false)
        }.defaultIfEmpty(false)
    }

    fun deleteAllAnnouncementData(guildId: Snowflake, calNumber: Int): Mono<Boolean> {
        return connect { c ->
            val query = "DELETE FROM ${Tables.ANNOUNCEMENTS.table} WHERE GUILD_ID = ? AND CALENDAR_NUMBER = ?"

            Mono.from(c.createStatement(query)
                  .bind(0, guildId.asString())
                  .bind(1, calNumber)
                  .execute()
            ).flatMapMany(Result::getRowsUpdated)
                  .hasElements()
                  .thenReturn(true)
                  .doOnError {
                      LOGGER.error(DEFAULT, "Failed to delete all announcements for guild", it)
                  }.onErrorReturn(false)
        }.defaultIfEmpty(false)
    }

    fun deleteAllRsvpData(guildId: Snowflake, calNumber: Int): Mono<Boolean> {
        return connect { c ->
            val query = "DELETE FROM ${Tables.RSVP.table} WHERE GUILD_ID = ? AND CALENDAR_NUMBER = ?"

            Mono.from(c.createStatement(query)
                  .bind(0, guildId.asString())
                  .bind(1, calNumber)
                  .execute()
            ).flatMapMany(Result::getRowsUpdated)
                  .hasElements()
                  .thenReturn(true)
                  .doOnError {
                      LOGGER.error(DEFAULT, "Failed to delete all rsvps for guild", it)
                  }.onErrorReturn(false)
        }.defaultIfEmpty(false)
    }

    fun removeRsvpRole(guildId: Snowflake, roleId: Snowflake): Mono<Boolean> {
        return connect { c ->
            val query = "UPDATE ${Tables.RSVP.table} SET RSVP_ROLE = ? WHERE GUILD_ID = ? AND RSVP_ROLE = ?"

            Mono.from(c.createStatement(query)
                  .bindNull(0, Long::class.java)
                  .bind(1, guildId.asString())
                  .bind(2, roleId.asString())
                  .execute()
            ).flatMapMany(Result::getRowsUpdated)
                  .hasElements()
                  .thenReturn(true)
                  .doOnError {
                      LOGGER.error(DEFAULT, "Failed update all rsvp with role for guild ", it)
                  }.onErrorReturn(false)
        }.defaultIfEmpty(false)
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
                      LOGGER.error(DEFAULT, "Failed to delete calendar", it)
                  }.onErrorReturn(false)
        }.defaultIfEmpty(false)
    }
}

private enum class Tables constructor(val table: String) {
    API("${BotSettings.SQL_PREFIX.get()}api"),
    GUILD_SETTINGS("${BotSettings.SQL_PREFIX.get()}guild_settings"),
    CALENDARS("${BotSettings.SQL_PREFIX.get()}calendars"),
    ANNOUNCEMENTS("${BotSettings.SQL_PREFIX.get()}announcements"),
    EVENTS("${BotSettings.SQL_PREFIX.get()}events"),
    RSVP("${BotSettings.SQL_PREFIX.get()}rsvp"),
    CREDS("${BotSettings.SQL_PREFIX.get()}credentials")
}
