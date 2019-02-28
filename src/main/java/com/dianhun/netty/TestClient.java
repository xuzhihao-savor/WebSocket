package com.dianhun.netty;

import battleapp.netproto.Msg;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.net.InetSocketAddress;

public class TestClient {
    private final String host;
    private final int port;
    public TestClient() {
        this(0);
    }

    public TestClient(int port) {
        this("localhost", port);
    }

    public TestClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group) // 注册线程池
                    .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
                    .remoteAddress(new InetSocketAddress(this.host, this.port)) // 绑定连接端口和host信息
                    .handler(new ChannelInitializer<SocketChannel>() { // 绑定连接初始化器
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("connected...");
                            ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            //服务器端接收的是客户端RequestUser对象，所以这边将接收对象进行解码生产实列
                            ch.pipeline().addLast(new ProtobufDecoder(Msg.SC_NotifyGameInit.getDefaultInstance()));
                            ch.pipeline().addLast(new Myencoder(4));
                            //Google Protocol Buffers编码器
                            ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            //Google Protocol Buffers编码器
                            ch.pipeline().addLast(new ProtobufEncoder());
                            ch.pipeline().addLast(new TestClientHandler());
                        }
                    });
            System.out.println("created..");

            ChannelFuture cf = b.connect().sync(); // 异步连接服务器
            System.out.println("connected..."); // 连接完成

            cf.channel().closeFuture().sync(); // 异步等待关闭连接channel
            System.out.println("closed.."); // 关闭完成
        } finally {
            group.shutdownGracefully().sync(); // 释放线程池资源
        }
    }
}
