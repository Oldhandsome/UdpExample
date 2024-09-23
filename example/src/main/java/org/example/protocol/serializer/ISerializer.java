package org.example.protocol.serializer;

public interface ISerializer {

    String serialize(Object obj);

    Object deserialize(String message, Class<?> msgClass);
}
