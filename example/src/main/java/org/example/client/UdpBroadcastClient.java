package org.example.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.DatagramPacketDecoder;
import io.netty.handler.codec.DatagramPacketEncoder;
import org.example.message.BaseMessage;
import org.example.message.CommonMessage;
import org.example.protocol.BaseMessageEncoder;
import org.example.protocol.ByteArrayEncoder;
import org.example.protocol.ByteBufDecoder;
import org.example.protocol.packet.BaseMessagePacket;
import org.example.protocol.packet.ByteArrayPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * UDP的客户端【多对多】；
 * 不能使用服务器和客户端不能在同一台设备上，组播是基于路由器之上实现的，要想网络内支持组播，需要有能够管理组播组的路由器或是三层交换机（带部分路由功能的交换机），广播是多播的特例
 */
public class UdpBroadcastClient {
    /**
     * 远程服务器地址
     */
    private final InetSocketAddress remoteAddress;
    private final BaseMessageEncoder baseMessageEncoder = new BaseMessageEncoder();
    private final ByteArrayEncoder byteArrayEncoder = new ByteArrayEncoder();
    private final ByteBufDecoder decoder = new ByteBufDecoder();
    private final Logger logger = LoggerFactory.getLogger(UdpBroadcastClient.class);
    private volatile Channel channel;


    public UdpBroadcastClient(int remotePort) {
        this.remoteAddress = new InetSocketAddress("255.255.255.255", remotePort);
    }

    public static void main(String[] args) throws InterruptedException {
        UdpBroadcastClient udpClient = new UdpBroadcastClient(51888);
        udpClient.connect();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("please input:");
            String input = scanner.next();
            System.out.println("the input of user:" + input);
            if (input.equals("quit")) {
                break;
            }
            if (input.isBlank()) {
                continue;
            }
            CommonMessage commonMessage = new CommonMessage(input);
            udpClient.write(commonMessage);
        }

        udpClient.disconnect();
    }

    /**
     * 与服务器建立连接
     */
    public void connect() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                        nioDatagramChannel.pipeline()
                                .addLast("Decoder-MooooooN", new DatagramPacketDecoder(decoder))
                                .addLast("BaseMessageEncoder-MooooooN", new DatagramPacketEncoder<>(baseMessageEncoder))
                                .addLast("ByteArrayEncoder-MooooooN", new DatagramPacketEncoder<>(byteArrayEncoder))
                                .addLast("ToString-MooooooN", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        super.channelRead(ctx, msg);
                                        logger.info("Server received from client: {}", msg);
                                    }
                                });
                    }
                });

        if (channel == null || !channel.isActive()) {
            synchronized (this) {
                if (channel == null || channel.isActive()) {
                    channel = bootstrap.bind(0).sync().channel();
                }
            }
        }
    }

    /**
     * 与服务器断开连接
     */
    public void disconnect() throws InterruptedException {
        if (channel != null && channel.isActive()) {
            synchronized (this) {
                if (channel != null && channel.isActive()) {
                    channel.close().sync();
                    channel.eventLoop().shutdownGracefully().sync();
                    channel = null;
                }
            }
        }
    }

    /**
     * 使用字节数组，进行发送（会自动封装该类型的数据到 ByteArrayPacket）
     *
     * @param msg 待写入的msg
     */
    public boolean write(BaseMessage msg) {
        synchronized (this) {
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(new BaseMessagePacket(msg, remoteAddress));
                return true;
            }
        }
        return false;
    }

    /**
     * 使用字节数组，进行发送（会自动封装该类型的数据到 ByteArrayPacket）
     *
     * @param bytes 写入的字节数组
     */
    public boolean write(byte[] bytes) {
        synchronized (this) {
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(new ByteArrayPacket(bytes, remoteAddress));
                return true;
            }
        }
        return false;
    }

    /**
     * 最原始的方法(最要在最开头写入一个short类型的字节类型)
     *
     * @param byteBuf 需要写入的bytebuf
     */
    @Deprecated
    public boolean write(ByteBuf byteBuf) {
        synchronized (this) {
            if (channel != null && channel.isActive()) {
//                ByteBuf buf = new UnpooledByteBufAllocator(true).buffer();
//                buf.writeBytes(bytes);
                channel.writeAndFlush(new DatagramPacket(byteBuf, remoteAddress));
                return true;
            }
        }
        return false;
    }

}
