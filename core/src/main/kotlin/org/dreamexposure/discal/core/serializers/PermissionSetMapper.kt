package org.dreamexposure.discal.core.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import discord4j.rest.util.PermissionSet

class PermissionSetMapper: SimpleModule() {
    init {
        addSerializer(PermissionSetSerializer())
        addDeserializer(PermissionSet::class.java, PermissionSetDeserializer())
    }

    class PermissionSetSerializer : StdSerializer<PermissionSet>(PermissionSet::class.java) {
        override fun serialize(value: PermissionSet?, gen: JsonGenerator?, provider: SerializerProvider?) {
            gen?.writeNumber(value?.rawValue ?: PermissionSet.none().rawValue)
        }
    }

    class PermissionSetDeserializer: StdDeserializer<PermissionSet>(PermissionSet::class.java) {
        override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): PermissionSet {
            val raw = p?.valueAsLong
            return if (raw != null) PermissionSet.of(raw) else throw IllegalStateException()
        }
    }
}