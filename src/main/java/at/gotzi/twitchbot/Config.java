package at.gotzi.twitchbot;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Config {

    private final Map<String, String> configValues = new HashMap<>();

    private final String obsInfoFilePath;

    private final String preCommandLoadFile;

    public Config(String[] args) {
        this.obsInfoFilePath = args[0];
        this.preCommandLoadFile = args[1];
    }

    public void load() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(obsInfoFilePath));
        bufferedReader.lines().forEach(str -> {
            if (str.charAt(0) != '#')
                configValues.put(str.split("=")[0], str.split("=")[1]);
        });
    }

    public String getObsInfoFilePath() {
        return obsInfoFilePath;
    }

    public String getPreCommandLoadFile() {
        return preCommandLoadFile;
    }

    public Map<String, String> getConfigValues() {
        return configValues;
    }

}
