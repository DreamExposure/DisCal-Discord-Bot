package org.dreamexposure.discal.core.utils;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class ImageUtils {

    public static final int THREE_SECOND_TIMEOUT = 3000;

    //TODO: Also, find a better working solution for validating images since this fails too much
    public static Mono<Boolean> validate(final String url, final boolean allowGif) {
        return Mono.fromCallable(() -> {
            final Image image = ImageIO.read(new URL(url));
            return image != null;
        })
            .subscribeOn(Schedulers.boundedElastic())
            .onErrorResume(IOException.class, e -> {
                if (allowGif)
                    return validateGif(url);
                else
                    return Mono.just(false);
            });
    }

    @SuppressWarnings("BlockingMethodInNonBlockingContext")
    private static Mono<Boolean> validateGif(final String url) {
        return Mono.fromCallable(() -> {
            final URLConnection connection = new URL(url).openConnection();
            connection.setConnectTimeout(THREE_SECOND_TIMEOUT);
            connection.setReadTimeout(THREE_SECOND_TIMEOUT);
            final InputStream in = connection.getInputStream();

            return readGif(in);
        })
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(Function.identity())
            .map("gif"::equalsIgnoreCase);
    }

    private static Mono<String> readGif(final InputStream input) {
        return Mono.fromCallable(() -> {
            final ImageInputStream stream = ImageIO.createImageInputStream(input);
            final Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
            if (!iter.hasNext()) {
                return null;
            }
            ImageReader reader = null;
            try {
                reader = iter.next();
                final ImageReadParam param = reader.getDefaultReadParam();
                reader.setInput(stream, true, true);
                reader.read(0, param);
            } catch (final IOException | NullPointerException ignore) {
            } finally {
                if (reader != null)
                    reader.dispose();
            }
            return reader.getFormatName();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}