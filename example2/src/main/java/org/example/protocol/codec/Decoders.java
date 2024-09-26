package org.example.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.example.protocol.packet.ProtocolPacket;
import org.example.protocol.packet.msg.Message;
import org.example.protocol.serializer.ISerializer;
import org.example.protocol.serializer.JsonSerializer;
import org.example.util.ByteBufUtils;
import org.example.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class Decoders {

    /**
     * 反序列化 ByteBuf -> protocolPacket
     */
    public static class ByteBufDecoder extends MessageToMessageDecoder<ByteBuf> {

        private final Logger logger = LoggerFactory.getLogger(ByteBufDecoder.class);

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            // header
            // 32 bits ——》sequenceNum
            int sequenceNum = in.readInt();
            // 2 bits ——》messageType
            // 30 bits ——》 messageNum
            int messageTypeAndNum = in.readInt();
            int messageNum = messageTypeAndNum & ProtocolPacket.MAX_MESSAGE_NUM;
            int messageType = messageTypeAndNum >>> 30; // 这里用的是无符号右移
            // 32 bits ——》ip地址
            String ipAddress = IpUtil.fromInt(in.readInt());
            // 32 bits ——》port
            int port = in.readInt();
            // 32 bits ——》 数据包的数据体长度
            int length = in.readInt();

            ByteBuf buffer = ByteBufUtils.buffer(length);
            in.readBytes(buffer, length);

            ProtocolPacket protocolPacket = new ProtocolPacket();
            protocolPacket.setData(buffer);
            protocolPacket.setSeqNum(sequenceNum);
            protocolPacket.setMessageType(messageType);
            protocolPacket.setMsgNum(messageNum);
            protocolPacket.setIpAddress(ipAddress);
            protocolPacket.setPort(port);

            out.add(protocolPacket);
        }
    }

    /**
     * 反序列化 ProtocolPacket -> 其他所有类型
     */
    public static class ProtocolPacketDecoder extends MessageToMessageDecoder<ProtocolPacket> {

        private final ISerializer serializer = JsonSerializer.getInstance();

        private final Logger logger = LoggerFactory.getLogger(ProtocolPacketDecoder.class);

        @Override
        protected void decode(ChannelHandlerContext ctx, ProtocolPacket msg, List<Object> out) throws Exception {

            switch (msg.getMessageType()) {
                case ProtocolPacket.MESSAGE_TYPE_SOLO_PACKET:
                    ByteBuf byteBuf = msg.getData();
                    int dataType = byteBuf.readByte();

                    int length = byteBuf.readableBytes();
                    byte[] dataBytes = new byte[length];
                    byteBuf.readBytes(dataBytes);

                    String body = new String(dataBytes, StandardCharsets.UTF_8);

                    logger.debug("received msg: {}", body);

                    out.add(serializer.deserialize(body, Message.getClassByCode(dataType)));
                    break;
                case ProtocolPacket.MESSAGE_TYPE_FIRST_PACKET:
                    break;
                case ProtocolPacket.MESSAGE_TYPE_MIDDLE_PACKET:
                    break;
                case ProtocolPacket.MESSAGE_TYPE_LAST_PACKET:
                    break;
            }
        }
    }
}
