package org.dreamexposure.discal.core.crypto

import org.apache.commons.codec.binary.Base64
import org.dreamexposure.discal.core.logger.LOGGER
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESEncryption(privateKey: String) {
    @Suppress("SpellCheckingInspection")
    private val key1: String = "E4B39r8F57F1Csde"

    private val ivParameterSpec = IvParameterSpec(key1.toByteArray(StandardCharsets.UTF_8))
    private val secretKeySpec = SecretKeySpec(privateKey.toByteArray(StandardCharsets.UTF_8), "AES")
    private var cipher: Cipher?

    init {
        try {
            this.cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        } catch (ignore: Exception) {
            this.cipher = null
        }
    }

    fun encrypt(data: String): String {
        return try {
            this.cipher?.init(Cipher.ENCRYPT_MODE, this.secretKeySpec, this.ivParameterSpec)
            val encrypted = this.cipher?.doFinal(data.toByteArray(StandardCharsets.UTF_8))

            Base64.encodeBase64String(encrypted)
        } catch (e: Exception) {
            LOGGER.error("Encrypt failure", e)
            "FAILURE"
        }
    }

    fun decrypt(encryptedData: String): String {
        return try {
            this.cipher?.init(Cipher.DECRYPT_MODE, this.secretKeySpec, this.ivParameterSpec)
            val decryptedBytes = this.cipher?.doFinal(Base64.decodeBase64(encryptedData))

            return String(decryptedBytes!!)
        } catch (e: Exception) {
            LOGGER.error("Decrypt failure", e)
            "FAILURE"
        }
    }
}
