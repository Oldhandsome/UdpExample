package org.example.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.ToString;
import org.example.util.ByteBufUtils;
import org.example.util.IpUtil;

@Data
@ToString
public class ControlPacket extends ProtocolPacket {

    public static final int CONTROL_PACKET_TYPE_HANDSHAKE = 0;
    public static final int CONTROL_PACKET_TYPE_ACKNOWLEDGEMENT = 2;
    public static final int CONTROL_PACKET_TYPE_NEGATIVE_ACKNOWLEDGE = 3;
    public static final int CONTROL_PACKET_TYPE_ACKNOWLEDGEMENT_OF_ACKNOWLEDGEMENT = 6;

    private int additionalInfo;

    private int controlPacketType;

    private ByteBuf control;

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

        // 32 bits ——》ip地址
        out.writeInt(IpUtil.toInt(this.getIpAddress()));
        // 32 bits ——》port
        out.writeInt(this.getPort());
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

        // 32 bits ——》ip地址
        ipAddress = IpUtil.fromInt(in.readInt());
        // 32 bits ——》port
        port = in.readInt();
        // 32 bits ——》 数据包的数据体长度
        int length = in.readInt();

        // control
        // 数据包的体
        control = ByteBufUtils.buffer(length);
        in.readBytes(control, length);
    }
}
