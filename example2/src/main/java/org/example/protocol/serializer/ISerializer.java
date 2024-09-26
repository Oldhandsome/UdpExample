package org.example.protocol.serializer;

public interface ISerializer {

    String serialize(Object obj);

    <T> T deserialize(String message, Class<T> msgClass);
}
