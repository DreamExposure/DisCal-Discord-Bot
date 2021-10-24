package org.dreamexposure.discal.core.`object`.rest

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = RestErrorSerializer::class)
enum class RestError(
        val code: Int,
        val message: String,
) {
    INTERNAL_SERVER_ERROR(0, "Internal Server Error"),
    BAD_REQUEST(1, "Bad request"),


    ACCESS_REVOKED(1001, "Access to resource revoked"),
    NOT_FOUND(1002, "Resource not found");


    companion object {
        fun fromCode(code: Int): RestError {
            return values().firstOrNull { it.code == code } ?: INTERNAL_SERVER_ERROR
        }
    }
}

@Serializable
data class RestErrorData(
        val code: Int,
        val message: String,
)

class RestErrorSerializer: KSerializer<RestError> {
    private val delegateSerializer =  RestErrorData.serializer()
    override val descriptor: SerialDescriptor = SerialDescriptor("RestError", delegateSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: RestError) {
        val data = RestErrorData(value.code, value.message)
        encoder.encodeSerializableValue(delegateSerializer, data)
    }

    override fun deserialize(decoder: Decoder): RestError {
        val data = decoder.decodeSerializableValue(delegateSerializer)
        return RestError.fromCode(data.code)
    }

}
