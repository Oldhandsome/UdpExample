package org.example.protocol.packet;

import io.netty.buffer.CompositeByteBuf;
import lombok.Getter;
import lombok.ToString;
import org.example.util.ByteBufUtils;


@Getter
@ToString
public class FullDataPacket {

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
