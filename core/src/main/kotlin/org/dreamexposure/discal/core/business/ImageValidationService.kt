package org.dreamexposure.discal.core.business

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dreamexposure.discal.core.logger.LOGGER
import org.springframework.stereotype.Component
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URI
import java.time.Duration
import javax.imageio.ImageIO
import javax.imageio.ImageReader

@Component
class ImageValidationService {
    suspend fun validate(url: String, allowGif: Boolean): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val image = ImageIO.read(URI.create(url).toURL())
            image != null
        } catch (_: IOException) {
            if (allowGif) validateGif(url)
            else false
        } catch (_: MalformedURLException) {
            false
        } catch (_: FileNotFoundException) {
            false
        } catch (ex: Exception) {
            LOGGER.error("Image validation failed with unexpected exception", ex)
            false
        }
    }

    private suspend fun validateGif(url: String): Boolean = withContext(Dispatchers.IO) {
        val connection = URI.create(url).toURL().openConnection()
        connection.connectTimeout = Duration.ofSeconds(3).toMillis().toInt()
        connection.readTimeout = Duration.ofSeconds(3).toMillis().toInt()

        readGif(connection.inputStream)?.equals(".gif", true) == true
    }

    private suspend fun readGif(input: InputStream): String? = withContext(Dispatchers.IO) {
        val stream = ImageIO.createImageInputStream(input)
        val iterator = ImageIO.getImageReaders(stream)
        if (!iterator.hasNext()) return@withContext null

        var reader: ImageReader? = null
        try {
            reader = iterator.next()

            reader.setInput(stream, true, true)
            reader.read(0, reader.defaultReadParam)
        } catch (_: Exception) {

        } finally {
            reader?.dispose()
        }

        reader?.formatName
    }
}