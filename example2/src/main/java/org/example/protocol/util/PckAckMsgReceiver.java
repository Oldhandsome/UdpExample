package org.example.protocol.util;

import java.util.Queue;

/**
 * Receiver
 * 1.Decoder 添加已确认的消息列表（在DataPacketDecoder）(定时器定时扫描，发送确认的ControlPacket)
 */
public interface PckAckMsgReceiver {

    /**
     * 获取确认队列中的确认序号队列
     */
    Queue<Integer> getConfirmPackets();

    /**
     * 获取确认队列的需要确认的序号个数
     */
    int getConfirmedPacketsLength();

    /**
     * 向确认队列尾部中添加一个序号
     */
    void addConfirmedPacket(int sequenceNum);

}
