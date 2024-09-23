package org.example.message;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseMessage {
    public final static int COMMON_MSG = 1;
    private final static Map<Integer, Class<? extends BaseMessage>> mapper = new HashMap<>();

    static {
        mapper.put(COMMON_MSG, CommonMessage.class);
    }

    public static Class<? extends BaseMessage> getClassByCode(int code) {
        Class<? extends BaseMessage> clazz = mapper.get(code);
        if (clazz == null) {
            throw new RuntimeException(String.format("could not find a class with code %d!!!", code));
        }
        return clazz;
    }

    public abstract int getCode();

}
