package org.example.protocol.packet;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class HandShakePacket {
    /**
     * initial sequence num
     */
    private int initialSequenceNum;

    /**
     * connection request type: 1: regular connection request, 0: rendezvous connection request, -1/-2: response
     */
    private int connectionRequestType;

    /**
     * socket ID
     */
    private int socketId;

}
