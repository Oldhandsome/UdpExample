package org.example.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.DatagramPacketDecoder;
import io.netty.handler.codec.DatagramPacketEncoder;
import io.netty.util.NetUtil;
import org.example.message.BaseMessage;
import org.example.message.CommonMessage;
import org.example.protocol.BaseMessageEncoder;
import org.example.protocol.ByteArrayEncoder;
import org.example.protocol.ByteBufDecoder;
import org.example.protocol.packet.BaseMessagePacket;
import org.example.protocol.packet.ByteArrayPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Scanner;

/**
 * UDP服务器【组播】
 */
public class UdpReceiver {
    private final Logger logger = LoggerFactory.getLogger(UdpServer.class);
    private final BaseMessageEncoder baseMessageEncoder = new BaseMessageEncoder();
    private final ByteArrayEncoder byteArrayEncoder = new ByteArrayEncoder();
    private final ByteBufDecoder decoder = new ByteBufDecoder();
    private volatile NioDatagramChannel channel;
    private final String tcpIp;
    private final int port;
    private InetAddress tcpIpAddress;
    private InetSocketAddress udpIpAddress;
    private NetworkInterface networkInterface;

    public UdpReceiver(String tcpIp, String udpIp, int port) throws UnknownHostException, SocketException {
        this.tcpIp = tcpIp;
        this.port = port;
        // udp地址
        this.udpIpAddress = new InetSocketAddress(udpIp, port);
        // 获取网卡
//        networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName(tcpIp));
        networkInterface = NetUtil.LOOPBACK_IF;
        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
        // 获取网卡下的tcp的IPV4地址
        while (addresses.hasMoreElements()) {
            InetAddress address = addresses.nextElement();
            if (address instanceof Inet4Address) {
                tcpIpAddress = address;
                logger.info("网络接口名称：{}", networkInterface.getName());
                logger.info("网卡接口地址：{}", address.getHostAddress());
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, SocketException, UnknownHostException {
//        String tcpIp = "192.168.121.178";// 本机地址
        String tcpIp = "127.0.0.1";// 本机地址
        String udpIp = "225.1.2.2"; // 组播地址
        int port = 9090; // 本机端口

        UdpReceiver receiver = new UdpReceiver(tcpIp, udpIp, port);
        receiver.start();

        Scanner scanner = new Scanner(System.in);
        while (true){
            String input = scanner.next();
            if(input.equals("quit")){
                break;
            }
            CommonMessage commonMessage = new CommonMessage(input);
            receiver.write(commonMessage);
        }

        receiver.stop();
    }

    /**
     * 启动服务器
     */
    public void start() throws InterruptedException, UnknownHostException {
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channelFactory(new ChannelFactory<Channel>() {
                    @Override
                    public Channel newChannel() {
                        return new NioDatagramChannel(InternetProtocolFamily.IPv4);
                    }
                })
                .localAddress(new InetSocketAddress(tcpIpAddress, port))
                .option(ChannelOption.IP_MULTICAST_IF, networkInterface)
                .option(ChannelOption.IP_MULTICAST_ADDR, InetAddress.getByName(tcpIp))
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_RCVBUF, 2048 * 1024)
                .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    public void initChannel(NioDatagramChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("Decoder-MooooooN", new DatagramPacketDecoder(decoder))
                                .addLast("BaseMessageEncoder-MooooooN", new DatagramPacketEncoder<>(baseMessageEncoder))
                                .addLast("ByteArrayEncoder-MooooooN", new DatagramPacketEncoder<>(byteArrayEncoder))
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        super.channelRead(ctx, msg);
                                        if (msg instanceof BaseMessage) {
                                            logger.debug("received base message {}", msg);
                                        } else if (msg instanceof byte[]) {
                                            logger.debug("received byte array {}", new String((byte[]) msg, StandardCharsets.UTF_8));
                                        }
                                    }
                                });
                    }
                }).bind(udpIpAddress.getPort());

        if (channel == null || !channel.isActive()) {
            synchronized (UdpServer.class) {
                if (channel == null || !channel.isActive()) {
                    channel = (NioDatagramChannel) channelFuture.sync().channel();
                    channel.joinGroup(udpIpAddress, networkInterface).sync();
                    logger.debug("receiver start {}!", channel.isActive());
                    channel.closeFuture().await();
                }
            }
        }
    }

    /**
     * 终止服务器
     */
    public void stop() throws InterruptedException {
        if (channel == null || !channel.isActive()) {
            synchronized (UdpServer.class) {
                if (channel == null || !channel.isActive()) {
                    channel.close().sync();
                    channel.eventLoop().shutdownGracefully().sync();

                    logger.debug("receiver stop {}!", channel.isActive());

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
                channel.writeAndFlush(new BaseMessagePacket(msg, udpIpAddress));
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
                channel.writeAndFlush(new ByteArrayPacket(bytes, udpIpAddress));
                return true;
            }
        }
        return false;
    }

    /**
     * 最原始的方法(需要在最开头写入一个short类型的字节类型)
     *
     * @param byteBuf 需要写入的bytebuf
     */
    @Deprecated
    public boolean write(ByteBuf byteBuf) {
        synchronized (this) {
            if (channel != null && channel.isActive()) {
//                ByteBuf buf = new UnpooledByteBufAllocator(true).buffer();
//                buf.writeBytes(bytes);
                channel.writeAndFlush(new DatagramPacket(byteBuf, udpIpAddress));
                return true;
            }
        }
        return false;
    }
}
