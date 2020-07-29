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
    //TODO: Also, find a better working solution for validating images since this fails too much
    public static Mono<Boolean> validate(String url, boolean allowGif) {
        return Mono.fromCallable(() -> {
            Image image = ImageIO.read(new URL(url));
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
    private static Mono<Boolean> validateGif(String url) {
        return Mono.fromCallable(() -> {
            URLConnection connection = new URL(url).openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            InputStream in = connection.getInputStream();

            return readGif(in);
        })
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(Function.identity())
            .map(s -> s.equalsIgnoreCase("gif"));
    }

    @SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
    private static Mono<String> readGif(InputStream input) {
        return Mono.fromCallable(() -> {
            ImageInputStream stream = ImageIO.createImageInputStream(input);
            Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
            if (!iter.hasNext()) {
                return null;
            }
            ImageReader reader = null;
            try {
                reader = iter.next();
                ImageReadParam param = reader.getDefaultReadParam();
                reader.setInput(stream, true, true);
                reader.read(0, param);
            } catch (IOException | NullPointerException ignore) {
            } finally {
                if (reader != null)
                    reader.dispose();
            }
            return reader.getFormatName();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}