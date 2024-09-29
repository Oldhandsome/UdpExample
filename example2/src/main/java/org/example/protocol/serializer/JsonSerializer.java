package org.example.protocol.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class JsonSerializer implements ISerializer {
    private final Gson gson;
    private final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

    private JsonSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    public static JsonSerializer getInstance() {
        return Inner.serializer;
    }

    @Override
    public byte[] serialize(Object obj) {
        logger.debug("serialize obj: {}", obj.toString());
        return gson.toJson(obj).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] messageBytes, Class<T> msgClass) {
        String json = new String(messageBytes, StandardCharsets.UTF_8);
        logger.debug("deserialize msg: {}", json);
        return gson.fromJson(json, msgClass);
    }

    /**
     * 用于获取唯一实例
     */
    public static class Inner {
        private static final JsonSerializer serializer = new JsonSerializer();
    }
}