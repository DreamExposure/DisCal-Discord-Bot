package org.dreamexposure.discal.core.crypto

import org.apache.commons.codec.binary.Base64
import org.dreamexposure.discal.core.logger.LOGGER
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
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

    fun encryptReactive(data: String): Mono<String> {
        return Mono.fromCallable {
            this.cipher?.init(Cipher.ENCRYPT_MODE, this.secretKeySpec, this.ivParameterSpec)
            val encrypted = this.cipher?.doFinal(data.toByteArray(StandardCharsets.UTF_8))

            Base64.encodeBase64String(encrypted)
        }.doOnError {
            LOGGER.error("Encrypt failure", it)
        }.doOnError {
            Mono.error<Void>(IllegalStateException("Encrypt Failure", it))
        }.subscribeOn(Schedulers.single())
    }

    fun decryptReactive(data: String): Mono<String> {
        return Mono.fromCallable {
            this.cipher?.init(Cipher.DECRYPT_MODE, this.secretKeySpec, this.ivParameterSpec)
            val decrypted = this.cipher?.doFinal(Base64.decodeBase64(data))

            String(decrypted!!, StandardCharsets.UTF_8)
        }.doOnError {
            LOGGER.error("Decrypt failure", it)
        }.doOnError {
            Mono.error<Void>(IllegalStateException("Decrypt Failure", it))
        }.subscribeOn(Schedulers.single())
    }

    @Deprecated("Use reactive version")
    fun encrypt(data: String): String {
        return try {
            //FIXME: Race condition?
            this.cipher?.init(Cipher.ENCRYPT_MODE, this.secretKeySpec, this.ivParameterSpec)
            val encrypted = this.cipher?.doFinal(data.toByteArray(StandardCharsets.UTF_8))

            Base64.encodeBase64String(encrypted)
        } catch (e: Exception) {
            throw IllegalStateException("Encrypt Failure", e)
        }
    }

    @Deprecated("Use reactive version")
    fun decrypt(encryptedData: String): String {
        return try {
            //FIXME: race condition?
            this.cipher?.init(Cipher.DECRYPT_MODE, this.secretKeySpec, this.ivParameterSpec)
            val decryptedBytes = this.cipher?.doFinal(Base64.decodeBase64(encryptedData))

            String(decryptedBytes!!)
        } catch (e: Exception) {
            LOGGER.error("Decrypt failure", e)
            throw IllegalStateException("Decrypt Failure", e)
        }
    }
}
