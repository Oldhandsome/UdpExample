package org.example.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.example.message.BaseMessage;
import org.example.protocol.serializer.ISerializer;
import org.example.protocol.serializer.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;

@ChannelHandler.Sharable
public class ByteBufDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final ISerializer serializer = JsonSerializer.getInstance();
    private final Logger logger = LoggerFactory.getLogger(ByteBufDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 4个字节——》ip地址
        int ipAddress = byteBuf.readInt();
        // 2个字节——》数据类型
        short type = byteBuf.readShort();
        // 2个字节——》数据包的数据体长度
        short length = byteBuf.readShort();

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        switch (type) {
            case MessageType.TYPE_BASE_MESSAGE:
                String jsonString = new String(bytes, Charset.defaultCharset());
                list.add(serializer.deserialize(jsonString, BaseMessage.class));
                break;
            case MessageType.TYPE_BYTE_ARRAY:
                list.add(bytes);
                break;
        }
    }
}
