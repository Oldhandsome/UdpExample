package org.example.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.DatagramPacketDecoder;
import io.netty.handler.codec.DatagramPacketEncoder;
import io.netty.util.NetUtil;
import org.example.message.BaseMessage;
import org.example.protocol.BaseMessageEncoder;
import org.example.protocol.ByteArrayEncoder;
import org.example.protocol.ByteBufDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;

/**
 * UDP服务器
 */
public class UdpServer {
    private final Logger logger = LoggerFactory.getLogger(UdpServer.class);
    private final BaseMessageEncoder baseMessageEncoder = new BaseMessageEncoder();
    private final ByteArrayEncoder byteArrayEncoder = new ByteArrayEncoder();
    private final ByteBufDecoder decoder = new ByteBufDecoder();
    private final InetSocketAddress address;
    private volatile NioDatagramChannel channel;

    public UdpServer(InetSocketAddress address) {
        this.address = address;
    }

    public static void main(String[] args) throws InterruptedException {
        UdpServer udpServer = new UdpServer(new InetSocketAddress("239.8.8.1", 51888));
        udpServer.startServer();
    }

    /**
     * 启动服务器
     */
    public void startServer() throws InterruptedException {
        NetworkInterface networkInterface = NetUtil.LOOPBACK_IF;

        ChannelFuture channelFuture = new Bootstrap().group(new NioEventLoopGroup()).channelFactory(new ChannelFactory<Channel>() {
            @Override
            public Channel newChannel() {
                return new NioDatagramChannel(InternetProtocolFamily.IPv4);
            }
        }).handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            public void initChannel(NioDatagramChannel ch) throws Exception {
                ch.pipeline().addLast("Decoder-MooooooN", new DatagramPacketDecoder(decoder)).addLast("BaseMessageEncoder-MooooooN", new DatagramPacketEncoder<>(baseMessageEncoder)).addLast("ByteArrayEncoder-MooooooN", new DatagramPacketEncoder<>(byteArrayEncoder)).addLast(new ChannelInboundHandlerAdapter() {
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
        }).bind(address.getPort());

        if (channel == null || !channel.isActive()) {
            synchronized (UdpServer.class) {
                if (channel == null || !channel.isActive()) {
                    channel = (NioDatagramChannel) channelFuture.sync().channel();
                    channel.joinGroup(address, networkInterface).sync();
                    logger.debug("server start {}!", channel.isActive());
                    channel.closeFuture().await();
                }
            }
        }
    }

    /**
     * 终止服务器
     */
    public void endServer() throws InterruptedException {
        if (channel == null || !channel.isActive()) {
            synchronized (UdpServer.class) {
                if (channel == null || !channel.isActive()) {
                    channel.close().sync();
                    channel.eventLoop().shutdownGracefully().sync();

                    logger.debug("server stop {}!", channel.isActive());

                    channel = null;
                }
            }
        }
    }
}
