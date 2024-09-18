package org.example.protocol.packet;

import io.netty.channel.AddressedEnvelope;

import java.net.InetSocketAddress;

/**
 * 用于封装byte[]
 */
public class ByteArrayPacket implements AddressedEnvelope<byte[], InetSocketAddress> {

    private final byte[] content;

    private final InetSocketAddress recipient;

    /**
     * @param content 带传递的消息
     * @param recipient 接受者的地址
     * */
    public ByteArrayPacket(byte[] content, InetSocketAddress recipient) {
        this.content = content;
        this.recipient = recipient;
    }

    @Override
    public byte[] content() {
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
    public AddressedEnvelope<byte[], InetSocketAddress> retain() {
        return this;
    }

    @Override
    public AddressedEnvelope<byte[], InetSocketAddress> retain(int increment) {
        return this;
    }

    @Override
    public AddressedEnvelope<byte[], InetSocketAddress> touch() {
        return this;
    }

    @Override
    public AddressedEnvelope<byte[], InetSocketAddress> touch(Object hint) {
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
