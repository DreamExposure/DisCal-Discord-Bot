package com.cloudcraftgaming.discal.api.file;

import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.logger.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class ReadFile {
	@SuppressWarnings({"unchecked", "ConstantConditions"})
	public static Map<String, Map<String, String>> readAllLangFiles() {
		Map<String, Map<String, String>> langs = new HashMap<>();

		try {
			File langDir = new File(BotSettings.LANG_PATH.get());

			for (File f: langDir.listFiles()) {
				// Open the file
				FileReader fr = new FileReader(f);

				Type type = new TypeToken<Map<String, String>>() {
				}.getType();

				Map<String, String> map = new Gson().fromJson(fr, type);
				langs.put(map.get("Language"), map);

				fr.close();
			}
		} catch (Exception e) {
			Logger.getLogger().exception(null, "Failed to load lang files!", e, ReadFile.class, true);
		}
		return langs;
	}
}