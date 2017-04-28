package com.cloudcraftgaming.discal.internal.file;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.internal.data.BotSettings;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.utils.Message;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class ReadFile {

    public static BotSettings readBotSettings(String fileAndPath) {
        BotSettings settings = new BotSettings();
        try {
            // Open the file that is the first
            FileInputStream fstream = new FileInputStream(fileAndPath);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line

            int line = 0;
            while ((strLine = br.readLine()) != null) {
                if (line == 0) {
                    settings.setBotToken(strLine);
                }
                if (line == 1) {
                    settings.setDbHostName(strLine);
                }
                if (line == 2) {
                    settings.setDbPort(strLine);
                }
                if (line == 3) {
                    settings.setDbDatabase(strLine);
                }
                if (line == 4) {
                    settings.setDbPrefix(strLine);
                }
                if (line == 5) {
                    settings.setDbUser(strLine);
                }
                if (line == 6) {
                    settings.setDbPass(strLine);
                }
                if (line == 9) {
                    settings.setBotsPwToken(strLine);
                }
                if (line == 10) {
                    settings.setGoogleClientId(strLine);
                }
                if (line == 11) {
                    settings.setGoogleClientSecret(strLine);
                }
                if (line == 12) {
                    settings.setRunDatabaseUpdater(strLine.equalsIgnoreCase("true"));
                }
                line++;

            }
            //Close the input stream
			br.close();
            in.close();
            fstream.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return settings;
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public static Map<String, Map<String, String>> readAllLangFiles() {
    	Map<String, Map<String, String>> langs = new HashMap<>();

    	try {
			File langDir = new File(Main.langPath);

			for (File f : langDir.listFiles()) {
				// Open the file
				FileReader fr = new FileReader(f);

				//Debug shits
				BufferedReader br = new BufferedReader(fr);

				String line;
				StringBuilder res = new StringBuilder();
				while((line = br.readLine()) != null)
					res.append(Message.lineBreak).append(line);

				ExceptionHandler.sendDebug(null, res.toString(), "What Lang files read as.", ReadFile.class);


				Type type = new TypeToken<Map<String, String>>() {
				}.getType();
				Map<String, String> map = Main.gson.fromJson(fr, type);
				langs.put(map.get("Language"), map);

				fr.close();
			}
		} catch (Exception e) {
			ExceptionHandler.sendException(null, "Failed to load lang files!", e, ReadFile.class);
		}
        return langs;
    }
}