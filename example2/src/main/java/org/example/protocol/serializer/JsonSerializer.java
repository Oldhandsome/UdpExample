package org.example.protocol.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonSerializer implements ISerializer {
    private final Gson gson;

    private JsonSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    public static JsonSerializer getInstance() {
        return Inner.serializer;
    }

    public String serialize(Object obj) {
        return gson.toJson(obj);
    }

    public <T> T deserialize(String message, Class<T> msgClass) {
        return gson.fromJson(message, msgClass);
    }

    /**
     * 用于获取唯一实例
     */
    public static class Inner {
        private static final JsonSerializer serializer = new JsonSerializer();
    }
}