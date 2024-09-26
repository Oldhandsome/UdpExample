package org.example.protocol.packet.msg;

import java.util.HashMap;
import java.util.Map;

public abstract class Message {
    public final static int SIMPLE_MSG = 1;
    private final static Map<Integer, Class<? extends Message>> mapper = new HashMap<>();

    static {
        mapper.put(SIMPLE_MSG, SimpleMessage.class);
    }

    public static Class<? extends Message> getClassByCode(int code) {
        Class<? extends Message> clazz = mapper.get(code);
        if (clazz == null) {
            throw new RuntimeException(String.format("could not find a class with code %d!!!", code));
        }
        return clazz;
    }

    public abstract int getCode();

}
