package org.example.protocol.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.protocol.packet.ControlPacket;
import org.example.protocol.packet.DataPacket;
import org.example.util.ByteBufUtils;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 按照最大个数进行确认；当需要确认的包的个数到达最大个数后，发送【确认】
 * 按照时间进行确认；当确认的队列不为空但不够时，且距离上一次的确认超过一个阈值，则发送【确认】
 */
public class DataPacketAcknowledgeHelper extends ChannelInboundHandlerAdapter implements PckAckMsgReceiver, Runnable {

    /**
     * 最大的确认的包的数量
     */
    private final int MAX_PACKETS_LENGTH = 20;
    /**
     * 最大的时间间隔
     */
    private final int MAX_TIME_INTERVAL = 2000;
    /**
     * 最大的时间间隔的单位
     */
    private final TimeUnit MAX_TIME_INTERVAL_UNIT = TimeUnit.MILLISECONDS;
    /**
     * 需要发送确认的队列
     */
    private final ArrayDeque<Integer> confirmedPackets = new ArrayDeque<>();
    /**
     * 锁
     */
    private final ReentrantLock reentrantLock = new ReentrantLock();

    @Override
    public Queue<Integer> getConfirmPackets() {
        return confirmedPackets;
    }

    @Override
    public int getConfirmedPacketsLength() {
        return confirmedPackets.size();
    }

    @Override
    public void addConfirmedPacket(int sequenceNum) {
        if (reentrantLock.tryLock()) {
            confirmedPackets.add(sequenceNum);

            if (confirmedPackets.size() >= MAX_PACKETS_LENGTH) {
                sendControlPacket();
            }

            reentrantLock.unlock();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        if (msg instanceof DataPacket dataPacket) {
            addConfirmedPacket(dataPacket.getSeqNum());
        }
    }

    @Override
    public void run() {
        sendControlPacket();
    }

    public void sendControlPacket() {
        if (reentrantLock.tryLock()) {
            // 1.立即发送确认的包
            ByteBuf byteBuf = ByteBufUtils.buffer();
            // 第一个位置写0；方便后续写入长度
            byteBuf.writeInt(0);

            int size = 0;
            Integer packetNum;
            while ((packetNum = confirmedPackets.poll()) != null) {
                byteBuf.writeInt(packetNum);
                size++;
            }
            byteBuf.setInt(0, size);


            reentrantLock.unlock();
        }
    }
}
