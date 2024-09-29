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
        String string = "环滁皆山也。其西南诸峰，林壑尤美，望之蔚然而深秀者，琅琊也。山行六七里，渐闻水声潺潺，而泻出于两峰之间者，酿泉也。峰回路转，有亭翼然临于泉上者，醉翁亭也。作亭者谁？山之僧智仙也。名之者谁？太守自谓也。太守与客来饮于此，饮少辄醉，而年又最高，故自号曰醉翁也。醉翁之意不在酒，在乎山水之间也。山水之乐，得之心而寓之酒也。";

        byte[] dataBytes = string.getBytes(StandardCharsets.UTF_8);
        CompositeByteBuf compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();

        System.out.println(compositeByteBuf.maxNumComponents());

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
