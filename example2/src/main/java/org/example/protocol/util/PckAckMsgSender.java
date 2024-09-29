package org.example.protocol.util;

import org.example.protocol.packet.DataPacket;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sender
 * 1.Encoder 添加待确认消息的列表（ProtocolPakcetEncoder）（定时器定时扫描，未确认的消息重新发送）
 * 2.Decoder 处理已确认的消息列表（ControlPacketDecoder）
 */
public interface PckAckMsgSender {
    /**
     * 待确认的报文
     */
    Map<Integer, DataPacket> unconfirmedPackets = new ConcurrentHashMap<>();

    /**
     * 确认一个Packet
     */
    default void confirmPacket(int sequenceNum) {
        if (unconfirmedPackets.containsKey(sequenceNum)) {
            synchronized (unconfirmedPackets) {
                unconfirmedPackets.remove(sequenceNum);
            }
        }
    }

    /**
     * 确认多个Packet
     */
    default void confirmPackets(List<Integer> sequenceNum) {
        synchronized (unconfirmedPackets) {
            for (int seq : sequenceNum) {
                unconfirmedPackets.remove(seq);
            }
        }
    }

    /**
     * 添加一个待确认的Packet
     */
    default void addUnconfirmedPacket(DataPacket dataPacket) {
        unconfirmedPackets.put(dataPacket.getSeqNum(), dataPacket);
    }

}
