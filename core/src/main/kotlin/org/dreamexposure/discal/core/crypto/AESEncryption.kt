package org.dreamexposure.discal.core.crypto

import org.apache.commons.codec.binary.Base64
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESEncryption(calData: CalendarData) {
    //Public key, its fine if this is here, I don't even have access to private keys
    private val key1: String = "E4B39r8F57F1Csde"

    private val ivParameterSpec: IvParameterSpec
    private val secretKeySpec: SecretKeySpec
    private var cipher: Cipher?

    init {
        val key2 = calData.privateKey
        this.ivParameterSpec = IvParameterSpec(key1.toByteArray(StandardCharsets.UTF_8))
        this.secretKeySpec = SecretKeySpec(key2.toByteArray(StandardCharsets.UTF_8), "AES")

        try {
            this.cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        } catch (ignore: Exception) {
            this.cipher = null
        }
    }

    fun encrypt(data: String): String {
        return try {
            this.cipher?.init(Cipher.ENCRYPT_MODE, this.secretKeySpec, this.ivParameterSpec)
            val encrypted = this.cipher?.doFinal(data.toByteArray(Charset.defaultCharset()))

            Base64.encodeBase64String(encrypted)
        } catch (ignore: Exception) {
            "FAILURE"
        }
    }

    fun decrypt(encryptedData: String): String {
        return try {
            this.cipher?.init(Cipher.DECRYPT_MODE, this.secretKeySpec, this.ivParameterSpec)
            val decryptedBytes = this.cipher?.doFinal(Base64.decodeBase64(encryptedData))

            return String(decryptedBytes!!)
        } catch (ignore: Exception) {
            "FAILURE"
        }
    }
}
