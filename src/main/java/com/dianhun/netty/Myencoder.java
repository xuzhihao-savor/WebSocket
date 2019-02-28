package com.dianhun.netty;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.MessageToByteEncoder;

public class Myencoder extends LengthFieldPrepender {
    private static final int HEADER_SIZE=6;
    private int length;
    private  short id=10001;
    public Myencoder(int lengthFieldLength) {
        super(lengthFieldLength);
    }

    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf in, ByteBuf out) throws Exception {
        System.out.println("encode");
        length=in.readableBytes()+HEADER_SIZE;
        out.writeByte(length);
        out.writeByte(this.id);
        out.writeBytes(in);


    }
}
