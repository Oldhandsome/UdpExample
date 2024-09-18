package org.example.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * 仅用于传递的消息是byte[]的编码器
 */
public class ByteArrayEncoder extends MessageToMessageEncoder<byte[]> {

    private final UnpooledByteBufAllocator unpooledByteBufAllocator = new UnpooledByteBufAllocator(true);

    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] msg, List<Object> out) throws Exception {
        ByteBuf byteBuf = unpooledByteBufAllocator.buffer();
        // 2个字节 -》类型
        // 数据包的头
        byteBuf.writeShort(ByteBufDecoder.TYPE_BYTE_ARRAY);
        // 数据包的体
        byteBuf.writeBytes(msg);

        out.add(byteBuf);
    }
}
