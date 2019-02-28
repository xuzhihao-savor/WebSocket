package com.dianhun.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;


public class RoomServer {
    private final String host;
    private final int port;
    public RoomServer(){
        this(0);
    }
    public RoomServer(int port){
         this("localhost",port);
    }
    public RoomServer(String host, int port){
        this.host=host;
        this.port=port;
    }
    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(); //EventLoopGroup()的使用方式
        try {
            ServerBootstrap b = new ServerBootstrap();// ServerBootstrap
            b.group(bossGroup, workerGroup);   //前者用来处理accept事件，后者用于处理已经建立的连接的io
            b.channel(NioServerSocketChannel.class);   //用它来建立新accept的连接
            b.childHandler(new ChannelInitializer<SocketChannel>(){

                @Override     //当新连接accept的时候，这个方法会调用
                protected void initChannel(SocketChannel ch) throws Exception {
                    // TODO Auto-generated method stub
                    ch.pipeline().addLast("handshake", new WebSocketServerProtocolHandler("", "", true));  //websocket的handler部分定义的，它会自己处理握手等操作
                    ch.pipeline().addLast(new MyWebSocketHandler());


                }

            });
            ChannelFuture f = b.bind(8080).sync();    //在所有的网卡上监听这个端口
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
