package org.example.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.example.protocol.packet.ControlPacket;
import org.example.protocol.packet.DataPacket;
import org.example.protocol.packet.ProtocolPacket;
import org.example.protocol.packet.msg.HandShake;
import org.example.protocol.packet.msg.Message;
import org.example.protocol.serializer.ISerializer;
import org.example.protocol.serializer.JsonSerializer;
import org.example.util.ByteBufUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

class ClientEncoders {

    private final static AtomicInteger sequenceIdGenerator;

    static {
        Random random = new Random();
        sequenceIdGenerator = new AtomicInteger(random.nextInt(1000));
    }

    public static int getCurrentSequenceId() {
        return sequenceIdGenerator.get();
    }

    /**
     * 序列化 ProtocolPacket 到 ByteBuf
     */
    public static class ProtocolPacketEncoder extends MessageToMessageEncoder<ProtocolPacket> {

        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, ProtocolPacket msg, List<Object> list) throws Exception {
            ByteBuf out = ByteBufUtils.buffer();
            msg.serialize(out);
            list.add(out);
        }
    }

    /**
     * 客户端发起建立连接（序列化 HandShake -> DefaultAddressedEnvelope）
     */
    public static class HandShakeEncoder extends MessageToMessageEncoder<HandShake> {

        private final Logger logger = LoggerFactory.getLogger(HandShakeEncoder.class);

        private final InetSocketAddress remoteAddress;

        public HandShakeEncoder(InetSocketAddress remoteAddress) {
            this.remoteAddress = remoteAddress;
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, HandShake msg, List<Object> out) throws Exception {
            int startSequenceNum = getCurrentSequenceId();
            ControlPacket controlPacket = ControlPacket.handShake(
                    startSequenceNum,
                    ControlPacket.HAND_SHAKE_TYPE_REGULAR_CONNECTION_REQUEST,
                    0
            );
            controlPacket.setDestinationSocketId(0);

            logger.debug("ControlPacket {}", controlPacket);

            out.add(new DefaultAddressedEnvelope<>(controlPacket, remoteAddress));
        }
    }

    /**
     * 客户端发送数据（序列化 Message -> DefaultAddressedEnvelope）
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
            byte[] messageBytes = serializer.serialize(msg);
            int messageCode = msg.getMessageCode();

            logger.debug("DataPacket code {}, length {}", messageCode, messageBytes.length);

            DataPacket dataPacket;
            if (messageBytes.length < DataPacket.MAX_PACKET_DATA_SIZE) {
                dataPacket = new DataPacket();
                dataPacket.setSeqNum(sequenceIdGenerator.incrementAndGet());
                dataPacket.setDestinationSocketId(0);
//                dataPacket.setIpAddress(remoteAddress.getAddress().getHostAddress());
//                dataPacket.setPort(remoteAddress.getPort());
                // 发送的是一条完整的信息
                dataPacket.setMsgType(DataPacket.DATA_PACKET_TYPE_SOLO_PACKET);
                dataPacket.setMsgNum(0);

                ByteBuf data = ByteBufUtils.buffer(messageBytes.length + 1);// 追加一个字节 的 messageCode
                {
                    data.writeByte(messageCode);
                    data.writeBytes(messageBytes);
                }
                dataPacket.setData(data);

                logger.debug("DataPacket {}, length {}", dataPacket, messageBytes.length);

                out.add(new DefaultAddressedEnvelope<>(dataPacket, remoteAddress));
            } else {
                // 拆包次数
                int times = (int) Math.ceil((float) messageBytes.length / DataPacket.MAX_PACKET_DATA_SIZE);
                // 保证当前的sequenceNum是连续的
                while (true) {
                    int current = sequenceIdGenerator.get();
                    int expectValue = current + times;
                    if (sequenceIdGenerator.compareAndSet(current, expectValue)) {

                        int startIndex = 0, endIndex = DataPacket.MAX_PACKET_DATA_SIZE;
                        int msgNum = 0;
                        // 将长度超过限制的包拆成多个
                        while (startIndex < messageBytes.length || endIndex < messageBytes.length) {
                            int length = Math.min(endIndex, messageBytes.length) - startIndex;

                            {
                                dataPacket = new DataPacket();
                                dataPacket.setSeqNum(current + msgNum);
                                dataPacket.setDestinationSocketId(0);
//                                dataPacket.setIpAddress(remoteAddress.getAddress().getHostAddress());
//                                dataPacket.setPort(remoteAddress.getPort());
                                // 按照位置发送不同的MessageType
                                if (endIndex == DataPacket.MAX_PACKET_DATA_SIZE) {
                                    dataPacket.setMsgType(DataPacket.DATA_PACKET_TYPE_FIRST_PACKET);
                                } else if (endIndex >= messageBytes.length) {
                                    dataPacket.setMsgType(DataPacket.DATA_PACKET_TYPE_LAST_PACKET);
                                } else {
                                    dataPacket.setMsgType(DataPacket.DATA_PACKET_TYPE_MIDDLE_PACKET);
                                }
                                dataPacket.setMsgNum(msgNum);

                                ByteBuf data = ByteBufUtils.buffer(length + 1); // 追加一个字节 的 messageCode
                                {
                                    data.writeByte(messageCode);
                                    data.writeBytes(messageBytes, startIndex, length);
                                }
                                dataPacket.setData(data);

                                logger.debug("DataPacket {}, length {}", dataPacket, length);

                                out.add(new DefaultAddressedEnvelope<>(dataPacket, remoteAddress));
                            }

                            msgNum += 1;
                            startIndex = endIndex;
                            endIndex += DataPacket.MAX_PACKET_DATA_SIZE;
                        }

                        break;
                    }
                }
            }
        }
    }
}
