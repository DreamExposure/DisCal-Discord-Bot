package org.dreamexposure.discal.core.serializers

import discord4j.common.util.Snowflake
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SnowflakeAsStringSerializer : KSerializer<Snowflake> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Snowflake", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Snowflake) = encoder.encodeString(value.asString())

    override fun deserialize(decoder: Decoder): Snowflake = Snowflake.of(decoder.decodeString())
}
