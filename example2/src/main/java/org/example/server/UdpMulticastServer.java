package org.example.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.DatagramPacketDecoder;
import io.netty.handler.codec.DatagramPacketEncoder;
import org.example.protocol.packet.msg.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;

/**
 * UDP服务器【组播】
 */
public class UdpMulticastServer {
    private final Logger logger = LoggerFactory.getLogger(UdpMulticastServer.class);
    private final String tcpIp;
    private final String udpIp;
    private final int port;
    private volatile NioDatagramChannel channel;

    public UdpMulticastServer(String tcpIp, String udpIp, int port) {
        this.tcpIp = tcpIp;
        this.udpIp = udpIp;
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException, SocketException, UnknownHostException {
        String tcpIp = "192.168.121.178";// 本机地址
        String udpIp = "225.1.2.2"; // 组播地址
        int port = 51888; // 本机端口

        UdpMulticastServer receiver = new UdpMulticastServer(tcpIp, udpIp, port);
        receiver.start();
    }

    /**
     * 启动服务器
     */
    public void start() throws InterruptedException, UnknownHostException, SocketException {
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName(tcpIp));
        InetAddress inetAddress = InetAddress.getByName(tcpIp);
        InetSocketAddress udpGroupAddress = new InetSocketAddress(udpIp, port);

        Bootstrap bootstrap = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channelFactory(new ChannelFactory<Channel>() {
                    @Override
                    public Channel newChannel() {
                        return new NioDatagramChannel(InternetProtocolFamily.IPv4);
                    }
                })
                .option(ChannelOption.IP_MULTICAST_IF, networkInterface)
                .option(ChannelOption.IP_MULTICAST_ADDR, inetAddress)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                        nioDatagramChannel.pipeline()
                                .addLast("Protocol-Packet-Encoder", new DatagramPacketEncoder<>(new ServerEncoders.ProtocolPacketEncoder()))
                                .addLast("Message-Encoder", new ServerEncoders.MessageEncoder(null))

                                .addLast("ByteBuf-Decoder", new DatagramPacketDecoder(new ServerDecoders.ByteBufDecoder()))
                                .addLast("Control-Packet-Decoder", new ServerDecoders.ControlPacketDecoder())
                                .addLast("Data-Packet-Decoder", new ServerDecoders.DataPacketDecoder())
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        super.channelRead(ctx, msg);
                                        if (msg instanceof Message) {
                                            logger.debug("received base message {}", msg);
                                        }
                                    }
                                });
                    }
                });

        if (channel == null || !channel.isActive()) {
            synchronized (UdpMulticastServer.class) {
                if (channel == null || !channel.isActive()) {
                    channel = (NioDatagramChannel) bootstrap.bind(port).sync().channel();
                    channel.joinGroup(udpGroupAddress, networkInterface).sync(); // join group
                    logger.debug("server start {}!", channel.isActive());
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
            synchronized (UdpMulticastServer.class) {
                if (channel == null || !channel.isActive()) {
                    channel.close().sync();
                    channel.eventLoop().shutdownGracefully().sync();

                    logger.debug("receiver stop {}!", channel.isActive());

                    channel = null;
                }
            }
        }
    }

}
