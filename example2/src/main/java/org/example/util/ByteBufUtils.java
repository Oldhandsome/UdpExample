package org.example.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class ByteBufUtils {
    private final static UnpooledByteBufAllocator unpooledByteBufAllocator = new UnpooledByteBufAllocator(true);

    public static ByteBuf buffer(int length){
        return unpooledByteBufAllocator.buffer(length, length);
    }

    public static ByteBuf buffer(){
        return unpooledByteBufAllocator.buffer();
    }
}
