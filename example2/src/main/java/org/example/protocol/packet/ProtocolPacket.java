package org.example.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public abstract class ProtocolPacket {
    public static final int PACKET_TYPE_DATA = 0;

    public static final int PACKET_TYPE_CONTROL = 1;

    /**
     * destination ip address
     */
    protected String ipAddress;

    /**
     * destination port
     */
    protected int port;

    /**
     * 返回PacketType
     */
    public abstract int getPacketType();

    /**
     * 序列化
     */
    public abstract void serialize(ByteBuf out);

    /**
     * 反序列化
     */
    public abstract void deserialize(ByteBuf in);
}