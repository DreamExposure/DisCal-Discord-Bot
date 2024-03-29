package org.dreamexposure.discal.core.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.time.Duration

class DurationMapper: SimpleModule() {
    init {
        addSerializer(DurationSerializer())
        addDeserializer(Duration::class.java, DurationDeserializer())
    }

    class DurationSerializer : StdSerializer<Duration>(Duration::class.java) {
        override fun serialize(value: Duration?, gen: JsonGenerator?, provider: SerializerProvider?) {
            gen?.writeString(value?.toMillis().toString())
        }
    }

    class DurationDeserializer: StdDeserializer<Duration>(Duration::class.java) {
        override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Duration {
            val raw = p?.valueAsString
            return if (raw != null) Duration.ofMillis(raw.toLong()) else throw IllegalStateException()
        }
    }
}
