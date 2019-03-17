package org.dreamexposure.discal.core.file;

import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.BotSettings;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class ReadFile {
	@SuppressWarnings("ConstantConditions")
	public static JSONObject readAllLangFiles() {
		JSONObject langs = new JSONObject();

		try {
			File langDir = new File(BotSettings.LANG_PATH.get());

			for (File f: langDir.listFiles()) {
				// Open the file
				FileReader fr = new FileReader(f);

				byte[] encoded = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
				String contents = new String(encoded, StandardCharsets.UTF_8);

				JSONObject json = new JSONObject(contents);

				langs.put(json.getString("Language"), json);

				fr.close();
			}
		} catch (Exception e) {
			Logger.getLogger().exception(null, "Failed to load lang files!", e, ReadFile.class);
		}
		return langs;
	}
}