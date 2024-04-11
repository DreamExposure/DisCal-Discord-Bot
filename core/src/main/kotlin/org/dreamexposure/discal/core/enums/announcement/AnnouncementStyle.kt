package org.dreamexposure.discal.core.enums.announcement

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = AnnouncementStyleAsIntSerializer::class)
@Deprecated("Use new subclassed enum in GuildSettings")
enum class AnnouncementStyle(val value: Int = 1) {
    FULL(1), SIMPLE(2), EVENT(3);

    companion object {
        fun isValid(i: Int): Boolean {
            return i in 1..3
        }

        fun fromValue(i: Int): AnnouncementStyle {
            return when (i) {
                1 -> FULL
                2 -> SIMPLE
                3 -> EVENT
                else -> FULL
            }
        }
    }
}

object AnnouncementStyleAsIntSerializer : KSerializer<AnnouncementStyle> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AnnouncementStyle", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: AnnouncementStyle) = encoder.encodeInt(value.value)

    override fun deserialize(decoder: Decoder): AnnouncementStyle = AnnouncementStyle.fromValue(decoder.decodeInt())
}
