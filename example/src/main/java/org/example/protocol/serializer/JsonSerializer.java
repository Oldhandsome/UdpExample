package org.example.protocol.serializer;

import com.google.gson.*;
import org.example.message.BaseMessage;

import java.lang.reflect.Type;
import java.util.Map;

public class JsonSerializer implements ISerializer {
    private final Gson gson;

    private JsonSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(BaseMessage.class, new BaseMessageSerializer());
        gson = gsonBuilder.create();
    }

    public static JsonSerializer getInstance() {
        return Inner.serializer;
    }

    public String serialize(Object obj) {
        if (obj instanceof BaseMessage)
            return gson.toJson(obj, BaseMessage.class);
        throw new IllegalArgumentException("input parameter should inherit the class 'org.example.message.BaseMessage'");

    }

    public Object deserialize(String message, Class<?> msgClass) {
        return gson.fromJson(message, msgClass);
    }

    /**
     * 用于获取唯一实例
     */
    public static class Inner {
        private static final JsonSerializer serializer = new JsonSerializer();
    }

    /**
     * 用于实现BaseMessage的序列化、反序列化
     */
    private static class BaseMessageSerializer implements com.google.gson.JsonSerializer<BaseMessage>, JsonDeserializer<BaseMessage> {

        @Override
        public BaseMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            int code = jsonObject.get("code").getAsInt();
            JsonObject dataJsonObject = jsonObject.get("data").getAsJsonObject();
            return jsonDeserializationContext.deserialize(dataJsonObject, BaseMessage.getClassByCode(code));
        }

        @Override
        public JsonElement serialize(BaseMessage baseMessage, Type type, JsonSerializationContext jsonSerializationContext) {
            return jsonSerializationContext.serialize(Map.of("code", baseMessage.getCode(), "data", baseMessage));
        }
    }

}