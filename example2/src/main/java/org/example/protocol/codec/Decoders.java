package org.example.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import org.example.protocol.packet.ControlPacket;
import org.example.protocol.packet.DataPacket;
import org.example.protocol.packet.FullDataPacket;
import org.example.protocol.packet.ProtocolPacket;
import org.example.protocol.packet.msg.Message;
import org.example.protocol.serializer.ISerializer;
import org.example.protocol.serializer.JsonSerializer;
import org.example.protocol.util.PckAckMsgReceiver;
import org.example.protocol.util.PckAckMsgSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Decoders {

    /**
     * 反序列化 ByteBuf -> protocolPacket
     */
    public static class ByteBufDecoder extends MessageToMessageDecoder<ByteBuf> implements PckAckMsgReceiver {

        private final Logger logger = LoggerFactory.getLogger(ByteBufDecoder.class);

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            int header = in.getInt(0); // 注意这里用的是 get
            int packetType = header >>> 31;
            switch (packetType) {
                case ProtocolPacket.PACKET_TYPE_DATA:
                    DataPacket dataPacket = new DataPacket();
                    dataPacket.deserialize(in);

                    logger.debug("received data packet: {}", dataPacket);

                    out.add(dataPacket);

                    addConfirmedPacket(dataPacket.getSeqNum());

                    break;
                case ProtocolPacket.PACKET_TYPE_CONTROL:
                    ControlPacket controlPacket = new ControlPacket();
                    controlPacket.deserialize(in);

                    logger.debug("received control packet: {}", controlPacket);

                    out.add(controlPacket);
                    break;
            }
        }
    }

    /**
     * 反序列化 DataPacket -> 其他所有类型
     */
    public static class DataPacketDecoder extends MessageToMessageDecoder<DataPacket> {

        private final ISerializer serializer = JsonSerializer.getInstance();

        private final Logger logger = LoggerFactory.getLogger(DataPacketDecoder.class);

        /**
         * 未组装的ByteBuf集合【键：起始的SequenceNum，值：待组装的ByteBuf】
         */
        private final Map<Integer, FullDataPacket> unassembledByteBufMapper = new ConcurrentHashMap<>();

        @Override
        protected void decode(ChannelHandlerContext ctx, DataPacket dataPacket, List<Object> out) throws Exception {
            switch (dataPacket.getMsgType()) {
                case DataPacket.DATA_PACKET_TYPE_SOLO_PACKET: {
                    int messageCode = dataPacket.getData().readByte();

                    int length = dataPacket.getData().readableBytes();
                    byte[] messageBytes = new byte[length];
                    dataPacket.getData().readBytes(messageBytes);

                    out.add(serializer.deserialize(messageBytes, Message.getClassByCode(messageCode)));

                    ReferenceCountUtil.release(dataPacket.getData());
                }
                break;
                case DataPacket.DATA_PACKET_TYPE_FIRST_PACKET,
                     DataPacket.DATA_PACKET_TYPE_MIDDLE_PACKET,
                     DataPacket.DATA_PACKET_TYPE_LAST_PACKET: {
                    int startSeqNum = dataPacket.getSeqNum() - dataPacket.getMsgNum();
                    int messageCode = dataPacket.getData().readByte();
                    FullDataPacket fullDataPacket = unassembledByteBufMapper.get(startSeqNum);
                    if (fullDataPacket == null) {
                        fullDataPacket = new FullDataPacket(startSeqNum);
                        unassembledByteBufMapper.put(startSeqNum, fullDataPacket);
                    }
                    fullDataPacket.addDataPacket(dataPacket);
                    // 如果接受完成，进行反序列化
                    if (fullDataPacket.isComplete()) {
                        int length = fullDataPacket.getCompositeByteBuf().readableBytes();
                        byte[] messageBytes = new byte[length];
                        fullDataPacket.getCompositeByteBuf().getBytes(fullDataPacket.getCompositeByteBuf().readerIndex(), messageBytes);

                        out.add(serializer.deserialize(messageBytes, Message.getClassByCode(messageCode)));

                        ReferenceCountUtil.release(fullDataPacket.getCompositeByteBuf());

                        unassembledByteBufMapper.remove(fullDataPacket.getSeqNum());
                    }
                }
                break;
            }
        }
    }

    /**
     * 反序列化 ControlPacket -> 其他所有类型
     */
    public static class ControlPacketDecoder extends MessageToMessageDecoder<ControlPacket> implements PckAckMsgSender {

        private final Logger logger = LoggerFactory.getLogger(ControlPacketDecoder.class);

        @Override
        protected void decode(ChannelHandlerContext ctx, ControlPacket msg, List<Object> out) throws Exception {

        }
    }
}
