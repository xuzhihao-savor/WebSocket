package com.dianhun.netty;

import battleapp.netproto.Msg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class
Mydecoder extends LengthFieldBasedFrameDecoder  {
 private static final int HEADER_SIZE=6;
 private short id=10001;
 private int length;
 private ByteBuf protobuf;
    public Mydecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if(in ==null){
            return null;
        }
        if (in.readableBytes()<HEADER_SIZE){
            throw new Exception("消息错误");
        }
        length=in.readableBytes();
        if(in.readableBytes()<length){
            throw new Exception("消息错误");
        }
        this.id=in.readByte();
        protobuf = in.readBytes(length);
        return protobuf;
    }
}

