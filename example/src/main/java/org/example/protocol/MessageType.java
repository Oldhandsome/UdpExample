package org.example.protocol;

public interface MessageType {
    /**
     * 传递的消息类型是BaseMessage
     */
    short TYPE_BASE_MESSAGE = 1;

    /**
     * 传递的消息类型是byte[]
     */
    short TYPE_BYTE_ARRAY = 2;
}
