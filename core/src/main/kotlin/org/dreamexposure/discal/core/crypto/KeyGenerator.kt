package org.dreamexposure.discal.core.crypto

import java.nio.charset.StandardCharsets
import java.security.SecureRandom

@Suppress("SpellCheckingInspection")
object KeyGenerator {
    private val VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456879".toCharArray()
    private val VALID_CHARS_2 = "abcdefghijklmnopqrstuv0123456789".toCharArray()

    @JvmStatic
    fun csRandomAlphaNumericString(numChars: Int): String {
        val secRand = SecureRandom()
        val buff = ByteArray(numChars)

        buff.indices.forEach { index -> buff[index] = VALID_CHARS[secRand.nextInt(VALID_CHARS.size)].code.toByte() }

        return buff.toString(StandardCharsets.UTF_8)
    }

    @JvmStatic
    fun generateEventId(): String {
        val secRand = SecureRandom()
        val buff = ByteArray(9)

        buff.indices.forEach { index -> buff[index] = VALID_CHARS_2[secRand.nextInt(VALID_CHARS_2.size)].code.toByte() }

        return "e${buff.toString(StandardCharsets.UTF_8)}"
    }

    fun generateAnnouncementId(): String {
        val secRand = SecureRandom()
        val buff = CharArray(9)

        (0 until 9).forEach { i -> buff[i] = VALID_CHARS_2[secRand.nextInt(VALID_CHARS_2.size)] }
        return "a${buff.joinToString("")}"
    }
}
