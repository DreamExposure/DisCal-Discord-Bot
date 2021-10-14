package org.dreamexposure.discal.core.utils

import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.time.Duration
import javax.imageio.ImageIO
import javax.imageio.ImageReader

//TODO: Remove jvm static
object ImageValidator {
    @JvmStatic
    fun validate(url: String, allowGif: Boolean): Mono<Boolean> {
        return Mono.fromCallable {
            val image = ImageIO.read(URL(url))
            image != null
        }.subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(IOException::class.java) {
                    if (allowGif) validateGif(url)
                    else Mono.just(false)
                }.onErrorReturn(MalformedURLException::class.java, false)
                .onErrorReturn(FileNotFoundException::class.java, false)
    }

    private fun validateGif(url: String): Mono<Boolean> {
        return Mono.fromCallable {
            val connection = URL(url).openConnection()
            connection.connectTimeout = Duration.ofSeconds(3).toMillis().toInt()
            connection.readTimeout = Duration.ofSeconds(3).toMillis().toInt()

            connection.getInputStream()
        }.subscribeOn(Schedulers.boundedElastic())
                .flatMap(ImageValidator::readGif)
                .map { it.equals(".gif", true) }
    }

    private fun readGif(input: InputStream): Mono<String> {
        return Mono.fromCallable {
            val stream = ImageIO.createImageInputStream(input)
            val iterator = ImageIO.getImageReaders(stream)
            if (!iterator.hasNext())
                return@fromCallable null

            var reader: ImageReader? = null
            try {
                reader = iterator.next()

                reader.setInput(stream, true, true)
                reader.read(0, reader.defaultReadParam)
            } catch (ignore: Exception) {

            } finally {
                reader?.dispose()
            }
            if (reader == null) return@fromCallable "invalid_file_type"
            return@fromCallable reader.formatName
        }
    }
}
