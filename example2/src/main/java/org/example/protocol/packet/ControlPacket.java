package org.example.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.ToString;
import org.example.util.ByteBufUtils;

@Data
@ToString
public class ControlPacket extends ProtocolPacket {

    public static final int CONTROL_PACKET_TYPE_HANDSHAKE = 0;
    public static final int CONTROL_PACKET_TYPE_KEEP_ALIVE = 1;
    public static final int CONTROL_PACKET_TYPE_ACKNOWLEDGEMENT = 2;
    public static final int CONTROL_PACKET_TYPE_NEGATIVE_ACKNOWLEDGE = 3;
    public static final int CONTROL_PACKET_TYPE_ACKNOWLEDGEMENT_OF_ACKNOWLEDGEMENT = 6;

    public static final int HAND_SHAKE_TYPE_REGULAR_CONNECTION_REQUEST = 1;
    public static final int HAND_SHAKE_TYPE_RENDEZVOUS_CONNECTION_REQUEST = 0;
    public static final int HAND_SHAKE_TYPE_RESPONSE_1 = -1;
    public static final int HAND_SHAKE_TYPE_RESPONSE_2 = -2;

    private int additionalInfo;

    private int controlPacketType;

    private ByteBuf control;

    public ControlPacket() {
    }

    /**
     * 构建ack数据包
     *
     * @param confirmedNum The ACK sequence number
     * @param expectNum    expectNum The sequence number to which (but not include) all the previous packets have beed received
     */
    public static ControlPacket acknowledge(int confirmedNum, int expectNum) {
        ControlPacket controlPacket = null;
        return controlPacket;
    }

    /**
     * 构建handShake数据包
     *
     * @param initialSeqNum  起始的序列号
     * @param connectionType handShake 的类型
     * @param socketId       client的socketId
     */
    public static ControlPacket handShake(int initialSeqNum, int connectionType, int socketId) {
        ControlPacket controlPacket = new ControlPacket();

        controlPacket.setAdditionalInfo(0);
        controlPacket.setControlPacketType(CONTROL_PACKET_TYPE_HANDSHAKE);
        {
            ByteBuf byteBuf = ByteBufUtils.buffer();
            byteBuf.writeInt(initialSeqNum);
            byteBuf.writeInt(connectionType);
            byteBuf.writeInt(socketId);

            controlPacket.setControl(byteBuf);
        }

        return controlPacket;
    }

    @Override
    public int getPacketType() {
        return ProtocolPacket.PACKET_TYPE_CONTROL;
    }

    @Override
    public void serialize(ByteBuf out) {
        // header
        int bytes = 0;
        // 1 bit ——》packetType
        bytes += this.getPacketType();
        // 15 bits ——》controlType
        bytes = bytes << 15;
        // 16 bits ——》Reserved
        bytes += this.getControlPacketType();
        bytes = bytes << 16;
        out.writeInt(bytes);

        // 32 bits ——》additionalInfo
        out.writeInt(this.getAdditionalInfo());

//        // 32 bits ——》ip地址
//        out.writeInt(IpUtil.toInt(this.getIpAddress()));
//        // 32 bits ——》port
//        out.writeInt(this.getPort());
        // 32 bits ——》destination socket id
        out.writeInt(this.getDestinationSocketId());
        // 32 bits ——》 数据包的数据体长度
        int length = this.getControl().readableBytes();
        out.writeInt(length);

        // control
        // 数据包的体
        out.writeBytes(this.getControl());
    }

    @Override
    public void deserialize(ByteBuf in) {
        // header
        // 32 bits ——》sequenceNum
        int controlTypeAndReserved = in.readInt();
        // 16 bits ——》Reserved
        controlTypeAndReserved = controlTypeAndReserved >>> 16;
        // 15 bits ——》controlType
        controlPacketType = controlTypeAndReserved & 0x7FFF;

        // 32 bits ——》additionalInfo
        additionalInfo = in.readInt();

//        // 32 bits ——》ip地址
//        ipAddress = IpUtil.fromInt(in.readInt());
//        // 32 bits ——》port
//        port = in.readInt();
//        // 32 bits ——》destination socket id
        destinationSocketId = in.readInt();
        // 32 bits ——》 数据包的数据体长度
        int length = in.readInt();

        // control
        // 数据包的体
        control = ByteBufUtils.buffer(length);
        in.readBytes(control, length);
    }
}
