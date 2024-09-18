package org.example.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.example.message.BaseMessage;
import org.example.protocol.serializer.ISerializer;
import org.example.protocol.serializer.JsonSerializer;

import java.nio.charset.Charset;
import java.util.List;

@ChannelHandler.Sharable
public class ByteBufDecoder extends MessageToMessageDecoder<ByteBuf> {

    /**
     * 传递的消息类型是BaseMessage
     * */
    public final static short TYPE_BASE_MESSAGE = 1;

    /**
     * 传递的消息类型是byte[]
     * */
    public final static short TYPE_BYTE_ARRAY = 2;

    private final ISerializer serializer = JsonSerializer.getInstance();

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 2个字节 -》类型
        short i = byteBuf.readShort();

        int length = byteBuf.readableBytes();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        switch (i) {
            case TYPE_BASE_MESSAGE:
                String jsonString = new String(bytes, Charset.defaultCharset());
                list.add(serializer.deserialize(jsonString, BaseMessage.class));
                break;
            case TYPE_BYTE_ARRAY:
                list.add(bytes);
                break;
        }
    }
}
