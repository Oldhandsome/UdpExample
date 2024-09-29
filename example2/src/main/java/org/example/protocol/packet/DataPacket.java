package org.example.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.ToString;
import org.example.util.ByteBufUtils;
import org.example.util.IpUtil;

@Data
@ToString
public class DataPacket extends ProtocolPacket {
    public static final int DATA_PACKET_TYPE_SOLO_PACKET = 3;
    public static final int DATA_PACKET_TYPE_FIRST_PACKET = 2;
    public static final int DATA_PACKET_TYPE_LAST_PACKET = 1;
    public static final int DATA_PACKET_TYPE_MIDDLE_PACKET = 0;

    public static final int MAX_SEQUENCE_NUM = 0x7FFFFFFF;

    public static final int MAX_MESSAGE_NUM = 0x3FFFFFFF;

    public final static int MAX_PACKET_DATA_SIZE = 20;

    /**
     * sequence number
     */
    protected int seqNum;

    /**
     * message number
     */
    private int msgNum;

    /**
     * message type
     */
    private int msgType;

    /**
     * packet data
     */
    private ByteBuf data;


    @Override
    public int getPacketType() {
        return ProtocolPacket.PACKET_TYPE_DATA;
    }

    @Override
    public void serialize(ByteBuf out) {
        // header
        int bytes = 0;
        // 1 bit ——》packetType
        bytes += this.getPacketType();
        bytes = bytes << 31;
        bytes += this.getSeqNum();
        // 31 bits ——》sequenceNum
        out.writeInt(bytes);

        bytes = 0;
        // 2 bits ——》messageType
        bytes += this.getMsgType();
        // 30 bits ——》 messageNum
        bytes = bytes << 30;
        bytes += this.getMsgNum();
        out.writeInt(bytes);

        // 32 bits ——》ip地址
        out.writeInt(IpUtil.toInt(this.getIpAddress()));
        // 32 bits ——》port
        out.writeInt(this.getPort());
        // 32 bits ——》 数据包的数据体长度
        int length = this.getData().readableBytes();
        out.writeInt(length);

        // data
        // 数据包的体
        out.writeBytes(this.getData());
    }

    @Override
    public void deserialize(ByteBuf in) {
        // header
        // 32 bits ——》sequenceNum
        seqNum = in.readInt() & DataPacket.MAX_SEQUENCE_NUM;

        int messageTypeAndNum = in.readInt();
        // 30 bits ——》 messageNum
        msgNum = messageTypeAndNum & DataPacket.MAX_MESSAGE_NUM;
        // 2 bits ——》messageType
        msgType = messageTypeAndNum >>> 30; // 这里用的是无符号右移
        // 32 bits ——》ip地址
        ipAddress = IpUtil.fromInt(in.readInt());
        // 32 bits ——》port
        port = in.readInt();
        // 32 bits ——》 数据包的数据体长度
        int length = in.readInt();

        // data
        // 数据包的体
        data = ByteBufUtils.buffer(length);
        in.readBytes(data, length);
    }
}
