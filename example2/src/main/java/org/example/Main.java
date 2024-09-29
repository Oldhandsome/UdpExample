package org.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import org.example.util.ByteBufUtils;

import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        testCompositeByteBuf();



    }

    public static void testCompositeByteBuf() {
        String string = "海城的雨摊开了难过，回应着情绪我却不说，只要它别坠落，在你孤单眼眸，你冰冷的唇咽下脆弱";

        byte[] dataBytes = string.getBytes(StandardCharsets.UTF_8);
        CompositeByteBuf compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();

        System.out.println(dataBytes.length);

        int startIndex = 0;
        int endIndex = 20;
        int MAX_PACKET_DATA_SIZE = 20;
        int which = 0;

        while (startIndex < dataBytes.length || endIndex < dataBytes.length) {
            int length = Math.min(endIndex, dataBytes.length) - startIndex;
            System.out.println(startIndex + " " + length);
            ByteBuf buffer = ByteBufUtils.buffer(length);
            buffer.writeBytes(dataBytes, startIndex, length);

            compositeByteBuf.addComponent(true, which++, buffer);

            startIndex = endIndex;
            endIndex += MAX_PACKET_DATA_SIZE;
        }
        System.out.println("*****************");
        System.out.println(compositeByteBuf.readableBytes());
//        byte[] readableBytes = new byte[compositeByteBuf.readableBytes()];
//        for (ByteBuf byteBuf : compositeByteBuf) {
//            byteBuf.getBytes(0, readableBytes);
//        }
//        System.out.println(new String(readableBytes, StandardCharsets.UTF_8));


        System.out.println("-------------");
        byte[] readableBytes = new byte[compositeByteBuf.readableBytes()];
        for (int i = 0; i < compositeByteBuf.numComponents(); i++) {
            System.out.println(compositeByteBuf.component(i).readableBytes());
        }

        System.out.println("1234");
        compositeByteBuf.getBytes(compositeByteBuf.readerIndex(), readableBytes);
        System.out.println(new String(readableBytes, StandardCharsets.UTF_8));

        System.out.println(compositeByteBuf.toString(0, dataBytes.length, StandardCharsets.UTF_8));

    }

    public static void testWhile() {

        int times = (int) Math.ceil((float) 10 / 3);

        System.out.println(times);

        int MAX_PACKET_DATA_SIZE = 20;
        int startIndex = 0;
        int endIndex = MAX_PACKET_DATA_SIZE;
        int dataBytes = 100;
//        do {
//            if(startIndex == endIndex)
//                break;
//
//            System.out.println(startIndex + " " + endIndex);
//            System.out.println("12345");
//
//            startIndex = endIndex;
//            endIndex += MAX_PACKET_DATA_SIZE;
//            if(endIndex > dataBytes){
//                endIndex = dataBytes;
//            }
//        } while (endIndex <= dataBytes);

//        while (endIndex <= dataBytes) {
//            if (startIndex == endIndex)
//                break;
//            System.out.println(startIndex + " " + endIndex);
//            startIndex = endIndex;
//            endIndex += MAX_PACKET_DATA_SIZE;
//            if (endIndex > dataBytes) {
//                endIndex = dataBytes;
//            }
//        }

        while (startIndex < dataBytes || endIndex < dataBytes) {
            System.out.println(startIndex + " " + Math.min(endIndex, dataBytes));
            System.out.println(startIndex + " " + endIndex);

            // 发送的是一条完整的信息
            if (endIndex == MAX_PACKET_DATA_SIZE) {
                System.out.println("first");
            } else if (endIndex >= dataBytes) {
                System.out.println("last");
            } else {
                System.out.println("middle");
            }
            System.out.println("*************");

            startIndex = endIndex;
            endIndex += MAX_PACKET_DATA_SIZE;
        }
    }
}
