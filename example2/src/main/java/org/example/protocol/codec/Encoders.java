package org.example.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.example.protocol.packet.ProtocolPacket;
import org.example.protocol.packet.msg.Message;
import org.example.protocol.serializer.ISerializer;
import org.example.protocol.serializer.JsonSerializer;
import org.example.util.ByteBufUtils;
import org.example.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Encoders {

    public final static int MAX_PACKET_DATA_SIZE = 10;

    private final static AtomicInteger sequenceIdGenerator;

    static {
        Random random = new Random();
        sequenceIdGenerator = new AtomicInteger(random.nextInt());
    }

    /**
     * 序列化 ProtocolPacket 到 ByteBuf
     */
    public static class ProtocolPacketEncoder extends MessageToMessageEncoder<ProtocolPacket> {
        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, ProtocolPacket msg, List<Object> list) throws Exception {
            ByteBuf out = ByteBufUtils.buffer();
            {
                // header
                // 32 bits ——》sequenceNum
                out.writeInt(msg.getSeqNum());
                int bytes = 0;
                // 2 bits ——》messageType
                bytes += msg.getMessageType();
                // 30 bits ——》 messageNum
                bytes = bytes << 30;
                bytes += msg.getMsgNum();
                out.writeInt(bytes);
                // 32 bits ——》ip地址
                out.writeInt(IpUtil.toInt(msg.getIpAddress()));
                // 32 bits ——》port
                out.writeInt(msg.getPort());
                // 32 bits ——》 数据包的数据体长度
                int length = msg.getData().readableBytes();
                out.writeInt(length);

                // data
                // 数据包的体
                out.writeBytes(msg.getData());
            }

            list.add(out);
        }
    }

    /**
     * 序列化 Message -> ProtocolPacket
     */
    public static class MessageEncoder extends MessageToMessageEncoder<Message> {

        private final Logger logger = LoggerFactory.getLogger(MessageEncoder.class);

        private final ISerializer serializer = JsonSerializer.getInstance();

        private final InetSocketAddress remoteAddress;

        public MessageEncoder(InetSocketAddress remoteAddress) {
            this.remoteAddress = remoteAddress;
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
            logger.debug("send msg: {}", msg);

            byte[] dataBytes = serializer.serialize(msg).getBytes(StandardCharsets.UTF_8);
            int dataType = msg.getCode();

            if (dataBytes.length < MAX_PACKET_DATA_SIZE) {
                ProtocolPacket protocolPacket = new ProtocolPacket();
                protocolPacket.setSeqNum(sequenceIdGenerator.incrementAndGet());
                protocolPacket.setIpAddress(remoteAddress.getAddress().getHostAddress());
                protocolPacket.setPort(remoteAddress.getPort());
                // 发送的是一条完整的信息
                protocolPacket.setMessageType(ProtocolPacket.MESSAGE_TYPE_SOLO_PACKET);
                protocolPacket.setMsgNum(1);

                ByteBuf data = ByteBufUtils.buffer(dataBytes.length + 1);// 追加一个字节 的 dataType
                {
                    data.writeByte(dataType);
                    data.writeBytes(dataBytes);
                }
                protocolPacket.setData(data);
                out.add(new DefaultAddressedEnvelope<>(protocolPacket, remoteAddress));
            } else {
                int startIndex = 0, endIndex = MAX_PACKET_DATA_SIZE;
                int msgNum = 1;
                // 将长度超过限制的包拆成多个
                while (startIndex < dataBytes.length || endIndex < dataBytes.length) {
                    int length = Math.min(endIndex, dataBytes.length) - startIndex;

                    {
                        ProtocolPacket protocolPacket = new ProtocolPacket();
                        protocolPacket.setSeqNum(sequenceIdGenerator.incrementAndGet());
                        protocolPacket.setIpAddress(remoteAddress.getAddress().getHostAddress());
                        protocolPacket.setPort(remoteAddress.getPort());
                        // 按照位置发送不同的MessageType
                        if (endIndex == MAX_PACKET_DATA_SIZE) {
                            protocolPacket.setMessageType(ProtocolPacket.MESSAGE_TYPE_FIRST_PACKET);
                        } else if (endIndex >= dataBytes.length) {
                            protocolPacket.setMessageType(ProtocolPacket.MESSAGE_TYPE_LAST_PACKET);
                        } else {
                            protocolPacket.setMessageType(ProtocolPacket.MESSAGE_TYPE_MIDDLE_PACKET);
                        }
                        protocolPacket.setMsgNum(msgNum);

                        ByteBuf data = ByteBufUtils.buffer(length + 1); // 追加一个字节 的 dataType
                        {
                            data.writeByte(dataType);
                            data.writeBytes(dataBytes, startIndex, length);
                        }
                        protocolPacket.setData(data);
                        out.add(new DefaultAddressedEnvelope<>(protocolPacket, remoteAddress));
                    }

                    msgNum += 1;
                    startIndex = endIndex;
                    endIndex += MAX_PACKET_DATA_SIZE;
                }
            }
        }
    }
}
