package com.cloudcraftgaming.discal.internal.file;

import com.cloudcraftgaming.discal.internal.data.BotSettings;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

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
            while ((strLine = br.readLine()) != null)   {
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
                if (line == 7) {
                    settings.setEmailUser(strLine);
                }
                if (line == 8) {
                    settings.setEmailPass(strLine);
                }

                if (line == 9) {
                    settings.setBotsPwToken(strLine);
                }
                line++;

            }
            //Close the input stream
            in.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return settings;
    }
}