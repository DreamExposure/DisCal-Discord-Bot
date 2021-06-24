package org.dreamexposure.discal.core.file;

import kotlin.text.Charsets;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStreamReader;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class ReadFile {
    public static Mono<JSONObject> readAllLangFiles() {
        return Mono.fromCallable(() -> {
            final JSONObject langs = new JSONObject();

            try {
                var pathMatching = new PathMatchingResourcePatternResolver();

                for (Resource res : pathMatching.getResources("languages/*.json")) {
                    var reader = new InputStreamReader(res.getInputStream(), Charsets.UTF_8);

                    // Open the file
                    final String contents = FileCopyUtils.copyToString(reader);

                    //Close reader
                    reader.close();

                    //Parse json
                    final JSONObject json = new JSONObject(contents);

                    if (!json.getString("Language").equalsIgnoreCase("TEMPLATE"))
                        langs.put(json.getString("Language"), json);
                }
            } catch (final Exception e) {
                LogFeed.log(LogObject.forException("Failed to load lang files", e, ReadFile.class));
            }
            return langs;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
