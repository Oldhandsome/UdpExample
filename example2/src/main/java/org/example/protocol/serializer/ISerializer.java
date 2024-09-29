package org.example.protocol.serializer;

public interface ISerializer {

    byte[] serialize(Object obj);

    <T> T deserialize(byte[] message, Class<T> msgClass);

}
