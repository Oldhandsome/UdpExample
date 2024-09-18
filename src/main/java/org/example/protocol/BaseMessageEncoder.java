package org.example.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.example.message.BaseMessage;
import org.example.protocol.serializer.ISerializer;
import org.example.protocol.serializer.JsonSerializer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 仅用于传递的消息是BaseMessage的编码器
 * */
@ChannelHandler.Sharable
public class BaseMessageEncoder extends MessageToMessageEncoder<BaseMessage> {

    private final UnpooledByteBufAllocator unpooledByteBufAllocator = new UnpooledByteBufAllocator(true);

    private final ISerializer serializer = JsonSerializer.getInstance();

    /**
     * 编码
     * */
    @Override
    protected void encode(ChannelHandlerContext ctx, BaseMessage msg, List<Object> out) throws Exception {
        ByteBuf byteBuf = unpooledByteBufAllocator.buffer();
        // 2个字节 -》类型
        // 数据包的头
        byteBuf.writeShort(ByteBufDecoder.TYPE_BASE_MESSAGE);
        // 数据包的体
        byteBuf.writeBytes(serializer.serialize(msg).getBytes(StandardCharsets.UTF_8));

        out.add(byteBuf);
    }


}
