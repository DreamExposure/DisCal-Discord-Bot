package org.dreamexposure.discal.core.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.http.HttpStatus

class SpringHttpStatusMapper: SimpleModule() {
    init {
        addSerializer(SpringHttpStatusSerializer())
        addDeserializer(HttpStatus::class.java, SpringHttpStatusDeserializer())
    }

    class SpringHttpStatusSerializer : StdSerializer<HttpStatus>(HttpStatus::class.java) {
        override fun serialize(value: HttpStatus?, gen: JsonGenerator?, provider: SerializerProvider?) {
            gen?.writeNumber(value?.value() ?: throw IllegalStateException())
        }
    }

    class SpringHttpStatusDeserializer : StdDeserializer<HttpStatus>(HttpStatus::class.java) {
        override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): HttpStatus? {
            val raw = p?.valueAsInt
            return if (raw != null) HttpStatus.valueOf(raw) else throw IllegalStateException()
        }
    }
}