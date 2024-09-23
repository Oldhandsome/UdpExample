package org.example.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.example.message.BaseMessage;
import org.example.protocol.serializer.ISerializer;
import org.example.protocol.serializer.JsonSerializer;
import org.example.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 仅用于传递的消息是BaseMessage的编码器
 */
@ChannelHandler.Sharable
public class BaseMessageEncoder extends MessageToMessageEncoder<BaseMessage> {
    private final UnpooledByteBufAllocator unpooledByteBufAllocator = new UnpooledByteBufAllocator(true);
    private final ISerializer serializer = JsonSerializer.getInstance();
    private final Logger logger = LoggerFactory.getLogger(ByteBufDecoder.class);

    /**
     * 编码
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, BaseMessage msg, List<Object> out) throws Exception {
        ByteBuf byteBuf = unpooledByteBufAllocator.buffer();
        // 4个字节——》ip地址
        byteBuf.writeInt(IpUtil.toInt(IpUtil.getLocalIpAddress()));
        // 2个字节——》数据类型
        byteBuf.writeShort(MessageType.TYPE_BASE_MESSAGE);
        // 2个字节——》数据包的数据体长度
        byte[] bytes = serializer.serialize(msg).getBytes(StandardCharsets.UTF_8);
        byteBuf.writeShort(bytes.length);
        // 数据包的体
        byteBuf.writeBytes(bytes);

        out.add(byteBuf);
    }
}
