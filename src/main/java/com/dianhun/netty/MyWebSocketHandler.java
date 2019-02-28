package com.dianhun.netty;

import battleapp.netproto.Msg;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;
import java.util.Date;

/**
 * 接收/处理
 */
public class MyWebSocketHandler extends SimpleChannelInboundHandler<Object> {
    private WebSocketServerHandshaker handshaker;
    public static final String WEB_SOCKET_URL="ws://192.168.0.9:11117/ws";
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg)
            throws Exception {
        // TODO Auto-generated method stub

        WebSocketFrame frame = (WebSocketFrame)msg;
        ByteBuf buf = frame.content();  //真正的数据是放在buf里面的


        String aa = buf.toString(Charset.forName("utf-8"));  //将数据按照utf-8的方式转化为字符串
        System.out.println(aa);
        WebSocketFrame out = new TextWebSocketFrame(aa);  //创建一个websocket帧，将其发送给客户端
        ctx.pipeline().writeAndFlush(out).addListener(new ChannelFutureListener(){


            public void operationComplete(ChannelFuture future)
                    throws Exception {
                // TODO Auto-generated method stub
                ctx.pipeline().close();  //从pipeline上面关闭的时候，会关闭底层的chanel，而且会从eventloop上面取消注册
            }

        });

    }

    protected void channelRead0(ChannelHandlerContext context, Object msg) throws Exception {


    }
    private void handWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame){
        //判断是否是关闭websocket的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
        }
        //判断是否是ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        //判断是否是二进制消息，如果是二进制消息，抛出异常
        if( ! (frame instanceof TextWebSocketFrame) ){
            System.out.println("目前我们不支持二进制消息");
            throw new RuntimeException("【"+this.getClass().getName()+"】不支持消息");
        }
        //返回应答消息
        //获取客户端向服务端发送的消息
        String request = ((TextWebSocketFrame) frame).text();
        System.out.println("服务端收到客户端的消息====>>>" + request);
        TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString()+ ctx.channel().isActive()    + " ===>>> " + request);
        //服务端向每个连接上来的客户端群发消息
        NettyConfig.group.writeAndFlush(tws);
    }
    private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req){
        if (!req.getDecoderResult().isSuccess()
                || ! ("websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req,
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                WEB_SOCKET_URL, null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        }else{
            handshaker.handshake(ctx.channel(), req);
        }
    }

    // 服务端向客户端响应消息
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req,
                                  DefaultFullHttpResponse res){
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        //服务端向客户端发送数据
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyConfig.group.add(ctx.channel());

        System.out.println("客户端和服务端链接开启");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyConfig.group.remove(ctx.channel());
        System.out.println("客户端与服务端链接关闭");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


}
