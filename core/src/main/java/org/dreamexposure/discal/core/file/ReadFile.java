package org.dreamexposure.discal.core.file;

import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.json.JSONObject;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class ReadFile {
    @SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed"})
    public static Mono<JSONObject> readAllLangFiles() {
        return Mono.fromCallable(() -> {
            final JSONObject langs = new JSONObject();

            try {
                for (final File file : getResourceFolderFiles("languages")) {
                    // Open the file
                    final FileReader fr = new FileReader(file);

                    final byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                    final String contents = new String(encoded, StandardCharsets.UTF_8);

                    final JSONObject json = new JSONObject(contents);

                    langs.put(json.getString("Language"), json);

                    fr.close();
                }
            } catch (final Exception e) {
                LogFeed.log(LogObject.forException("Failed to load lang files", e, ReadFile.class));
            }
            return langs;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private static File[] getResourceFolderFiles(String folder) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(folder);
        String path = url.getPath();
        return new File(path).listFiles();
    }
}
