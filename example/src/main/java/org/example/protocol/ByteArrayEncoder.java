package org.example.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.example.util.IpUtil;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 仅用于传递的消息是byte[]的编码器
 */
@ChannelHandler.Sharable
public class ByteArrayEncoder extends MessageToMessageEncoder<byte[]> {

    private final UnpooledByteBufAllocator unpooledByteBufAllocator = new UnpooledByteBufAllocator(true);

    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] msg, List<Object> out) throws Exception {
        ByteBuf byteBuf = unpooledByteBufAllocator.buffer();
        // 4个字节——》ip地址
        byteBuf.writeInt(IpUtil.toInt(IpUtil.getLocalIpAddress()));
        // 2个字节——》数据类型
        byteBuf.writeShort(MessageType.TYPE_BYTE_ARRAY);
        // 2个字节——》数据包的数据体长度
        byteBuf.writeShort(msg.length);
        // 数据包的体
        byteBuf.writeBytes(msg);

        out.add(byteBuf);
    }
}
