package org.dreamexposure.discal.core.crypto

import java.security.SecureRandom

object KeyGenerator {
    private val VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456879".toCharArray()
    private val VALID_CHARS_2 = "abcdefghijklmnopqrstuv0123456789".toCharArray()

    @JvmStatic
    fun csRandomAlphaNumericString(numChars: Int): String {
        val secRand = SecureRandom()
        val buff = CharArray(numChars)

        (0 until numChars - 1).forEach { i -> buff[i] = VALID_CHARS[secRand.nextInt(VALID_CHARS.size)] }
        return buff.joinToString("")
    }

    @JvmStatic
    fun generateEventId(): String {
        val secRand = SecureRandom()
        val buff = CharArray(9)

        (0 until 9).forEach { i -> buff[i] = VALID_CHARS_2[secRand.nextInt(VALID_CHARS_2.size)] }
        return "e${buff.joinToString("")}"
    }

    @JvmStatic
    fun generateAnnouncementId(): String {
        val secRand = SecureRandom()
        val buff = CharArray(9)

        (0 until 9).forEach { i -> buff[i] = VALID_CHARS_2[secRand.nextInt(VALID_CHARS_2.size)] }
        return "a${buff.joinToString("")}"
    }
}
