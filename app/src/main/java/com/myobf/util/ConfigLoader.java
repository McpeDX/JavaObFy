package com.myobf.util;

import com.google.gson.Gson;
import com.myobf.model.Configuration;

import java.io.FileReader;
import java.io.IOException;

public class ConfigLoader {

    public static Configuration loadConfig(String path) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(path)) {
            return gson.fromJson(reader, Configuration.class);
        }
    }
}
