package com.simtechdata;

import com.simtechdata.network.ServerManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class App {

    public static boolean graal = false;
    public static boolean test = false;

    public static void main(String[] args) throws Exception {
        for(String a : args) {
            String arg = a.toLowerCase();
            switch(arg) {
                case "version", "--version", "/version" -> {
                    showVersion();
                    System.exit(0);
                }
                case "graal", "graalvm" -> {
                    graal = true;
                    graal();
                }
                case "test" -> {
                    test = true;
                }
                default -> {
                    System.out.println("Unknown argument: " + a);
                    System.exit(0);
                }
            }
        }
        if(!graal) {
            startServers();
        }
    }

    private static void startServers() {
        Path path = Paths.get(System.getProperty("user.dir"));
        File settingsFile = path.resolve("settings.ini").toFile();
        try {
            if(settingsFile.exists()) {
                String settingsContents = FileUtils.readFileToString(settingsFile);
                String[] settings = settingsContents.split("\n");
                StringBuilder sb = new StringBuilder();
                boolean getSettings = false;
                int countValid = 0;
                for(int x=0; x<settings.length; x++) {
                    String line = settings[x].trim().toLowerCase();
                    if(getSettings) {
                        boolean valid = line.contains("port") || line.contains("buffer") || line.contains("logfile");
                        if(valid) {
                            sb.append(line).append("\n");;
                            countValid++;
                        }
                        if(line.contains("[server") && countValid < 3) {
                            System.err.println("The settings file has a section without the correct number of settings");
                            System.exit(0);
                        }
                        if (countValid == 3) {
                            startServers(sb.toString());
                            getSettings = false;
                            continue;
                        }
                    }
                    if (line.contains("[server") && !getSettings) {
                        sb = new StringBuilder();
                        sb.append(line).append("\n");
                        countValid = 0;
                        getSettings = true;
                    }
                }
            }
            else {
                FileUtils.writeStringToFile(settingsFile, settingsFile());
                System.out.println("settings.ini does not exist. A sample settings file was written to this folder.");
                System.out.println("Adjust the values in the file as needed and re-run this program");
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void startServers(String settings) {
        String[] lines = settings.split("\n");
        String logFilePath = "";
        int udpPort = 0;
        int bufferSize = 0;
        String header = lines[0];
        for(String line : lines) {
            if (line.contains("=")) {
                String option = line.split("=")[0].toLowerCase().trim();
                String value = line.split("=")[1].trim();
                switch(option) {
                    case "port" -> udpPort = Integer.parseInt(value);
                    case "buffersize" -> bufferSize = Integer.parseInt(value);
                    case "logfile" -> logFilePath = value;
                    default ->{}
                }
            }
        }
        if(!logFilePath.isEmpty() && udpPort > 0 && bufferSize > 0) {
            ServerManager serverManager = new ServerManager(logFilePath, udpPort, bufferSize);
            new Thread(serverManager).start();
        }
        else {
            System.err.println("\nSection " + header + " seems to not contain correct settings.");
            System.err.println("Check settings.ini and try again\n");
        }
    }

    private static String settingsFile() {
        return """
                [Server1]
                port=4444
                bufferSize=512
                logFile=/Users/user/logs/logFile1.txt
                
                [Server2]
                port=4445
                bufferSize=512
                logFile=/Users/user/logs/logFile2.txt
                """;
    }

    private static void showVersion() {
        Properties prop = new Properties();
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input == null) {
                System.out.println("Could not determine current version");
            }
            else {
                prop.load(input);
                System.out.println(prop.getProperty("version"));
            }
        }
        catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    private static void graal() {
        Path logFilePath = Paths.get(System.getProperty("user.home"),"temp","logfileUDPLog.txt");
        int udpPort = 54321;
        int bufferSize = 512;
        ServerManager serverManager = new ServerManager(logFilePath.toString(), udpPort, bufferSize);
        new Thread(serverManager).start();
        System.out.println("Use an app to send some data to this machine on UDP port " + udpPort);
    }
}
