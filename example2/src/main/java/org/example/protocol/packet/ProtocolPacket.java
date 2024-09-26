package org.example.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ProtocolPacket {
    public static final int MESSAGE_TYPE_SOLO_PACKET = 3;
    public static final int MESSAGE_TYPE_FIRST_PACKET = 2;
    public static final int MESSAGE_TYPE_LAST_PACKET = 1;
    public static final int MESSAGE_TYPE_MIDDLE_PACKET = 0;

    public static final int MAX_MESSAGE_NUM = 0x3FFFFFFF;
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
    private int messageType;

    /**
     * destination ip address
     */
    private String ipAddress;

    /**
     * destination port
     */
    private int port;

    /**
     * packet data
     */
    private ByteBuf data;
}