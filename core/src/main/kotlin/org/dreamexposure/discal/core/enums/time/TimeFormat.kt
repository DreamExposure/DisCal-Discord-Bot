package org.dreamexposure.discal.core.enums.time

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TimeFormatAsIntSerializer::class)
enum class TimeFormat(val value: Int = 1, val full: String, val date: String, val longDate: String, val time: String, val dayOfWeek: String) {
    TWENTY_FOUR_HOUR(1, "LLLL dd yyyy '@' HH:mm", "yyyy/MM/dd", "dd LLLL yyyy", "HH:mm", "EEEE '-' dd LLLL yyyy"),
    TWELVE_HOUR(2, "LLLL dd yyyy '@' hh:mm a", "yyyy/MM/dd", "dd LLLL yyyy", "hh:mm a", "EEEE '-' dd LLLL yyyy");

    companion object {
        fun isValid(i: Int): Boolean {
            return i == 1 || i == 2
        }

        fun fromValue(i: Int): TimeFormat {
            return when (i) {
                1 -> TWENTY_FOUR_HOUR
                2 -> TWELVE_HOUR
                else -> TWENTY_FOUR_HOUR
            }
        }
    }
}

object TimeFormatAsIntSerializer : KSerializer<TimeFormat> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TimeFormat", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: TimeFormat) = encoder.encodeInt(value.value)

    override fun deserialize(decoder: Decoder): TimeFormat = TimeFormat.fromValue(decoder.decodeInt())
}
