package org.dreamexposure.discal.core.enums.time

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TimeFormatAsIntSerializer::class)
enum class TimeFormat(
    val value: Int,
    val fullSimple: String,
    val full: String,
    val date: String,
    val longDate: String,
    val time: String,
    val dayOfWeek: String
) {
    TWENTY_FOUR_HOUR(
        value = 1,
        fullSimple = "yyyy/MM/dd hh:mm:ss",
        full = "LLLL dd yyyy '@' HH:mm",
        date = "yyyy/MM/dd",
        longDate = "dd LLLL yyyy",
        time = "HH:mm",
        dayOfWeek = "EEEE '-' dd LLLL yyyy"),
    TWELVE_HOUR(
        value = 2,
        fullSimple = "yyyy/MM/dd hh:mm:ss a",
        full = "LLLL dd yyyy '@' hh:mm a",
        date = "yyyy/MM/dd",
        longDate = "dd LLLL yyyy",
        time = "hh:mm a",
        dayOfWeek = "EEEE '-' dd LLLL yyyy");

    companion object {
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
