package com.dianhun.netty;

import battleapp.netproto.Msg;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class MatchServer {
    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();    //构建serverbootstrap对象
            b.group(bossGroup, workerGroup);   //设置时间循环对象，前者用来处理accept事件，后者用于处理已经建立的连接的io
            b.channel(NioServerSocketChannel.class);   //用它来建立新accept的连接，用于构造serversocketchannel的工厂类


            b.childHandler(new ChannelInitializer<SocketChannel>() {      //为accept channel的pipeline预添加的inboundhandler
                @Override     //当新连接accept的时候，这个方法会调用
                protected void initChannel(SocketChannel ch) throws Exception {

                    ch.pipeline().addLast(new ReadTimeoutHandler(60));  //如果60秒钟都没有新的数据读取，那么自动关闭
                    //ch.pipeline().addLast(new WriteTimeoutHandler(1));  //写的1秒钟超时
                    ch.pipeline().addLast(new Mydecoder(1024*1024,4,1,0,0,true));
                    ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());  //这里需要将protobuf的数据封装成netty定义的帧，将帧完整的解析出来
                    ch.pipeline().addLast(new ProtobufDecoder(Msg.CS_AskMatchGameInfo.getDefaultInstance()));  //用于将二进制的protobuf数据转化为对象

                }

            });
            //bind方法会创建一个serverchannel，并且会将当前的channel注册到eventloop上面，
            //会为其绑定本地端口，并对其进行初始化，为其的pipeline加一些默认的handler
            ChannelFuture f = b.bind(8000).sync();
            f.channel().closeFuture().sync();  //相当于在这里阻塞，直到serverchannel关闭
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
