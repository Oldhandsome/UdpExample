package org.example;

import org.example.protocol.packet.ProtocolPacket;

public class Main {
    public static void main(String[] args) {
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

        while (startIndex < dataBytes || endIndex < dataBytes){
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
