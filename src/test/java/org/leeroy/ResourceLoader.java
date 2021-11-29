package org.leeroy;

import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ResourceLoader {
    public static String load(String path) {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JsonObject loadJson(String path) {
        return new JsonObject(load(path));
    }
}
