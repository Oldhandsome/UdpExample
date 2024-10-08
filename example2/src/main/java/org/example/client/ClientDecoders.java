package org.example.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.ToString;
import org.example.protocol.packet.ControlPacket;
import org.example.protocol.packet.DataPacket;
import org.example.protocol.packet.ProtocolPacket;
import org.example.protocol.packet.msg.Message;
import org.example.protocol.serializer.ISerializer;
import org.example.protocol.serializer.JsonSerializer;
import org.example.util.ByteBufUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ClientDecoders {

    private static int socketId;

    /**
     * 反序列化 ByteBuf -> protocolPacket
     */
    public static class ByteBufDecoder extends MessageToMessageDecoder<ByteBuf> {

        private final Logger logger = LoggerFactory.getLogger(ByteBufDecoder.class);

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            int header = in.getInt(0); // 注意这里用的是 get
            int packetType = header >>> 31;
            logger.debug("received a packet!");
            switch (packetType) {
                case ProtocolPacket.PACKET_TYPE_DATA:
                    DataPacket dataPacket = new DataPacket();
                    dataPacket.deserialize(in);

                    logger.debug("received data packet: {}", dataPacket);

                    out.add(dataPacket);

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

    public static class ControlPacketDecoder extends ChannelInboundHandlerAdapter {

        private final Logger logger = LoggerFactory.getLogger(ClientDecoders.ControlPacketDecoder.class);

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);
            if (msg instanceof ControlPacket controlPacket) {
                int controlPacketType = controlPacket.getControlPacketType();

                switch (controlPacketType) {
                    case ControlPacket.CONTROL_PACKET_TYPE_HANDSHAKE: {
                        ByteBuf control = controlPacket.getControl();
                        int startSeq = control.readInt();
                        int connectionType = control.readInt();
                        int socketId = control.readInt();

                        logger.debug("client connection type: {}, socket id: {}", connectionType, socketId);

                        if (connectionType == ControlPacket.HAND_SHAKE_TYPE_RESPONSE_1) {
                            ClientDecoders.socketId = socketId;
                        }

                        break;
                    }
                    case ControlPacket.CONTROL_PACKET_TYPE_ACKNOWLEDGEMENT: {

                        break;
                    }
                }
            }

        }
    }

    /**
     * 处理DataPacket（反序列化 DataPacket -> 其他所有类型）
     */
    public static class DataPacketDecoder extends MessageToMessageDecoder<DataPacket> {

        private final Logger logger = LoggerFactory.getLogger(DataPacketDecoder.class);

        /**
         * Message的反序列化
         */
        private final ISerializer serializer = JsonSerializer.getInstance();

        /**
         * 未组装的ByteBuf集合【键：起始的SequenceNum，值：待组装的ByteBuf】
         */
        private final Map<Integer, FullDataPacket> unassembledByteBufMapper = new ConcurrentHashMap<>();

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);
            // 发送消息的确认
//            DataPacket dataPacket = (DataPacket) msg;
//            ControlPacket controlPacket = ControlPacket.acknowledge(dataPacket.getSeqNum(), );
//            ctx.writeAndFlush(controlPacket);
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, DataPacket dataPacket, List<Object> out) throws Exception {
            switch (dataPacket.getMsgType()) {
                case DataPacket.DATA_PACKET_TYPE_SOLO_PACKET: {
                    int messageCode = dataPacket.getData().readByte();

                    int length = dataPacket.getData().readableBytes();
                    byte[] messageBytes = new byte[length];
                    dataPacket.getData().readBytes(messageBytes);

                    Message message = serializer.deserialize(messageBytes, Message.getClassByCode(messageCode));

                    out.add(message);

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
                        int readerIndex = fullDataPacket.getCompositeByteBuf().readerIndex();
                        byte[] messageBytes = new byte[length];
                        fullDataPacket.getCompositeByteBuf().getBytes(readerIndex, messageBytes);

                        Message message = serializer.deserialize(messageBytes, Message.getClassByCode(messageCode));

                        out.add(message);

                        ReferenceCountUtil.release(fullDataPacket.getCompositeByteBuf());

                        unassembledByteBufMapper.remove(fullDataPacket.getSeqNum());
                    }
                }
                break;
            }
        }
    }

    /**
     * 对于DataPacket太大，需要使用该类进行包装
     */
    @Getter
    @ToString
    public static class FullDataPacket {
        /**
         * 集合
         */
        private final CompositeByteBuf compositeByteBuf;
        /**
         * start sequence number
         */
        protected int seqNum;
        /**
         * current received packets
         */
        private volatile int counts;
        /**
         * the max msg num
         */
        private int maxMsgNumber;

        public FullDataPacket(int seqNum) {
            this.compositeByteBuf = ByteBufUtils.compositeByteBuf();
            this.seqNum = seqNum;
        }

        /**
         * 数组长度最多是16，可能会抛出异常，这是 UnpooledByteBufAllocator的问题
         */
        public void addDataPacket(DataPacket dataPacket) {
            synchronized (compositeByteBuf) {
                switch (dataPacket.getMsgType()) {
                    case DataPacket.DATA_PACKET_TYPE_FIRST_PACKET,
                         DataPacket.DATA_PACKET_TYPE_MIDDLE_PACKET:
                        compositeByteBuf.addComponent(true, dataPacket.getMsgNum(), dataPacket.getData());
                        break;
                    case DataPacket.DATA_PACKET_TYPE_LAST_PACKET:
                        maxMsgNumber = dataPacket.getMsgNum();
                        compositeByteBuf.addComponent(true, dataPacket.getMsgNum(), dataPacket.getData());
                        break;
                }
                counts += 1; // 放在最后【写屏障】
            }
        }

        /**
         * 判断当前的Packet是否接受完毕
         */
        public boolean isComplete() {
            if (maxMsgNumber == 0)
                return false;
            return maxMsgNumber == counts - 1;
        }
    }

}
