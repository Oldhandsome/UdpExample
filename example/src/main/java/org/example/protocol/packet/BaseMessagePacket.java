package org.example.protocol.packet;


import io.netty.channel.AddressedEnvelope;
import org.example.message.BaseMessage;

import java.net.InetSocketAddress;

/**
 * 用于封装BaseMessage
 * */
public class BaseMessagePacket implements AddressedEnvelope<BaseMessage, InetSocketAddress> {

    private final BaseMessage content;

    private final InetSocketAddress recipient;

    /**
     * @param content 带传递的消息
     * @param recipient 接受者的地址
     * */
    public BaseMessagePacket(BaseMessage content, InetSocketAddress recipient) {
        this.content = content;
        this.recipient = recipient;
    }

    @Override
    public BaseMessage content() {
        return content;
    }

    @Override
    public InetSocketAddress sender() {
        return null;
    }

    @Override
    public InetSocketAddress recipient() {
        return recipient;
    }

    @Override
    public int refCnt() {
        return 0;
    }

    @Override
    public AddressedEnvelope<BaseMessage, InetSocketAddress> retain() {
        return this;
    }

    @Override
    public AddressedEnvelope<BaseMessage, InetSocketAddress> retain(int increment) {
        return this;
    }

    @Override
    public AddressedEnvelope<BaseMessage, InetSocketAddress> touch() {
        return this;
    }

    @Override
    public AddressedEnvelope<BaseMessage, InetSocketAddress> touch(Object hint) {
        return this;
    }

    @Override
    public boolean release() {
        return false;
    }

    @Override
    public boolean release(int i) {
        return false;
    }
}
