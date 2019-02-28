package com.dianhun.netty;

import io.netty.buffer.ByteBuf;

public class Protocol {
    short id;
    int length;
    ByteBuf protobuf;

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public ByteBuf getProtobuf() {
        return protobuf;
    }

    public void setProtobuf(ByteBuf protobuf) {
        this.protobuf = protobuf;
    }
}
