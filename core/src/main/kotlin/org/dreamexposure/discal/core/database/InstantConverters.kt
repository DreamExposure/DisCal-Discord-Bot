package org.dreamexposure.discal.core.database

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/*
Custom converter is currently needed for mariadb driver to support Instants
https://github.com/mariadb-corporation/mariadb-connector-r2dbc/issues/90

Credit to @JohnNiang for the code https://github.com/mariadb-corporation/mariadb-connector-r2dbc/issues/90#issuecomment-3731589316
 */
internal enum class InstantConverters {
    ;

    @ReadingConverter
    internal enum class MariaDBReadingConverter : Converter<LocalDateTime, Instant> {
        INSTANCE {
            override fun convert(source: LocalDateTime): Instant {
                val zoneId = ZoneId.systemDefault()
                return source.atZone(zoneId).toInstant()
            }
        };
    }

    @WritingConverter
    internal enum class MariaDBWritingConverter : Converter<Instant, LocalDateTime> {
        INSTANCE {
            override fun convert(source: Instant): LocalDateTime {
                val zoneId = ZoneId.systemDefault().normalized()
                return LocalDateTime.ofInstant(source, zoneId)
            }
        };
    }
}