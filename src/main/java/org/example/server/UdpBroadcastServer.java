package org.example.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.DatagramPacketDecoder;
import io.netty.handler.codec.DatagramPacketEncoder;
import org.example.message.BaseMessage;
import org.example.protocol.BaseMessageEncoder;
import org.example.protocol.ByteArrayEncoder;
import org.example.protocol.ByteBufDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class UdpBroadcastServer {
    private final Logger logger = LoggerFactory.getLogger(UdpBroadcastServer.class);
    private final BaseMessageEncoder baseMessageEncoder = new BaseMessageEncoder();
    private final ByteArrayEncoder byteArrayEncoder = new ByteArrayEncoder();
    private final ByteBufDecoder decoder = new ByteBufDecoder();
    private final int port;
    private volatile NioDatagramChannel channel;

    public UdpBroadcastServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException, SocketException, UnknownHostException {
        int port = 51888; // 本机端口

        UdpBroadcastServer receiver = new UdpBroadcastServer(port);
        receiver.start();
    }

    /**
     * 启动服务器
     */
    public void start() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channelFactory(new ChannelFactory<Channel>() {
                    @Override
                    public Channel newChannel() {
                        return new NioDatagramChannel(InternetProtocolFamily.IPv4);
                    }
                })
                .option(ChannelOption.SO_BROADCAST, true)
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
                });

        if (channel == null || !channel.isActive()) {
            synchronized (UdpServer.class) {
                if (channel == null || !channel.isActive()) {
                    channel = (NioDatagramChannel) bootstrap.bind(port).sync().channel();

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
}
