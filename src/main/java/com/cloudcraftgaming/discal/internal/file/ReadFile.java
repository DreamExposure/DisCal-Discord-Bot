package com.cloudcraftgaming.discal.internal.file;

import com.cloudcraftgaming.discal.database.MySQL;
import com.cloudcraftgaming.discal.internal.email.EmailData;

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

    /**
     * To prevent the highly sensitive data from being hard coded into the .jar.
     * This will read a .txt file with the following information encoded in it.
     * (In order, one per line):
     * Hostname, Port, Database, Prefix, Username, Password
     *
     * @param fileAndPath the path and file of the file to read from.
     * @return A MySQL object with the data.
     */
    public static MySQL readDatabaseSettings(String fileAndPath) {
        try {
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new FileInputStream(fileAndPath);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            String hostName = null;
            String port = null;
            String database = null;
            String prefix = null;
            String user = null;
            String pass = null;

            int line = 0;
            while ((strLine = br.readLine()) != null)   {
                if (line == 0) {
                    hostName = strLine;
                }
                if (line == 1) {
                    port = strLine;
                }
                if (line == 2) {
                    database = strLine;
                }
                if (line == 3) {
                    prefix = strLine;
                }
                if (line == 4) {
                    user = strLine;
                }
                if (line == 5) {
                    pass = strLine;
                }
                line++;

            }
            //Close the input stream
            in.close();

            //Return a new MySQL object
            return new MySQL(hostName, port, database, prefix, user, pass);
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * To prevent the highly sensitive data from being hard coded into the .jar
     * This will read a .txt file with the following information encoded in it.
     * (In order, one per line):
     * Username (email), password
     * @param fileAndPath The path and file of the file to read from.
     * @return An EmailData Object with the data.
     */
    public static EmailData readEmailLogin(String fileAndPath) {
        try {
            FileInputStream fstream = new FileInputStream(fileAndPath);
            //Get data
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read file line by line
            String user = null;
            String pass = null;

            int line = 0;
            while ((strLine = br.readLine()) != null) {
                if (line == 0) {
                    user = strLine;
                }
                if (line == 1) {
                    pass = strLine;
                }
                line++;
            }
            //Close input stream
            in.close();

            return new EmailData(user, pass);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }
}