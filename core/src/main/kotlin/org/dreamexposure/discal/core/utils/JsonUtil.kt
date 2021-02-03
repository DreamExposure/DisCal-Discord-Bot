package org.dreamexposure.discal.core.utils

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.json.JSONObject
import kotlin.jvm.internal.Reflection

@OptIn(InternalSerializationApi::class)
object JsonUtil {
    val format = Json {
        encodeDefaults = true
    }

    fun <T> encodeToJSON(clazz: Class<T>, data: T): JSONObject {
        val kClass = Reflection.createKotlinClass(clazz)
        val serializer = kClass.serializer() as KSerializer<T>

        return JSONObject(format.encodeToString(serializer, data))
    }

    fun <T> encodeToString(clazz: Class<T>, data: T): String = encodeToJSON(clazz, data).toString()

    fun <T> decodeFromJSON(clazz: Class<T>, json: JSONObject): T = decodeFromString(clazz, json.toString())

    fun <T> decodeFromString(clazz: Class<T>, str: String): T {
        val kClass = Reflection.createKotlinClass(clazz)
        val serializer = kClass.serializer()

        return format.decodeFromString(serializer, str) as T
    }
}
