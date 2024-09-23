package org.example;

import io.netty.util.NetUtil;

import java.net.*;
import java.util.Arrays;
import java.util.Enumeration;

public class Main {
    public static void main(String[] args) throws SocketException, UnknownHostException {
        InetAddress localhost = NetUtil.LOCALHOST;
        System.out.println(localhost);
        NetworkInterface loopbackIf = NetUtil.LOOPBACK_IF;
        System.out.println(loopbackIf);
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            System.out.println(networkInterface.getName());
            System.out.println(networkInterface.getDisplayName());
            System.out.println("-----------");
        }

        NetworkInterface concreteNi = NetworkInterface.getByInetAddress(InetAddress.getByName("192.168.121.178"));
        System.out.println(concreteNi);
        System.out.println(concreteNi.getName());
        System.out.println(concreteNi.getDisplayName());

        // 网络地址
        Enumeration<InetAddress> inetAddresses = concreteNi.getInetAddresses();
        while (inetAddresses.hasMoreElements()){
            InetAddress inetAddress = inetAddresses.nextElement();
            System.out.println(inetAddress);
            System.out.println(Arrays.toString(inetAddress.getAddress()));
            System.out.println(inetAddress.getHostAddress());
            System.out.println(inetAddress.getHostName());
            System.out.println(inetAddress.getCanonicalHostName());
            System.out.println("**********************");
        }

        // 网卡地址 包含 网络地址、广播地址、子网掩码等信息
        
        for (InterfaceAddress interfaceAddress : concreteNi.getInterfaceAddresses()) {
            System.out.println(interfaceAddress);
            System.out.println(interfaceAddress.getAddress());
            System.out.println(interfaceAddress.getBroadcast());
            System.out.println(interfaceAddress.getNetworkPrefixLength());

            System.out.println("++++++++++++++");
        }

    }
}
