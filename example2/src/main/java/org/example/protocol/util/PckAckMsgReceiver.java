package org.example.protocol.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Receiver
 * 1.Decoder 添加已确认的消息列表（在DataPacketDecoder）(定时器定时扫描，发送确认的ControlPacket)
 */
public interface PckAckMsgReceiver {

    /**
     * 已确认的报文
     */
    List<Integer> confirmedPackets = new ArrayList<>();

    default void addConfirmedPacket(int sequenceNum) {
        confirmedPackets.add(sequenceNum);
    }

}
