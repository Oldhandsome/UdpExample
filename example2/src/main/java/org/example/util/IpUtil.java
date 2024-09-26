package org.example.util;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.regex.Pattern;

public class IpUtil {
    private static final String IPV4_REGEX =
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    private static final Pattern IPv4_PATTERN = Pattern.compile(IPV4_REGEX);

    /**
     * 将ip转换成一个32位的整数
     *
     * @param ipAddress IP地址
     */
    public static long toLong(String ipAddress) {
        long result = 0;
        for (String i : ipAddress.split("\\.")) {
            result = Integer.parseInt(i) + (result << 8);
        }
        return result;
    }

    /**
     * 将数字转化ip地址
     */
    public static String fromLong(long ipAddress) {
        String[] array = new String[]{null, null, null, null};
        array[3] = String.valueOf(ipAddress & 255);
        array[2] = String.valueOf((ipAddress >> 8) & 255);
        array[1] = String.valueOf((ipAddress >> 16) & 255);
        array[0] = String.valueOf((ipAddress >> 24) & 255);
        return String.join(".", array);
    }

    public static boolean isValid(String ip) {
        return IPv4_PATTERN.matcher(ip).matches();
    }


    /**
     * 将ip转换成一个32位的整数（可能是负数）
     *
     * @param ipAddress IP地址
     */
    public static int toInt(String ipAddress) {
        int result = 0;
        for (String i : ipAddress.split("\\.")) {
            result = Integer.parseInt(i) + (result << 8);
        }
        return result;
    }

    public static String fromInt(int ipAddress){
        String[] array = new String[]{null, null, null, null};
        array[3] = String.valueOf(ipAddress & 255);
        array[2] = String.valueOf((ipAddress >> 8) & 255);
        array[1] = String.valueOf((ipAddress >> 16) & 255);
        array[0] = String.valueOf((ipAddress >> 24) & 255);
        return String.join(".", array);
    }

    public static String getLocalIpAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    public static void main(String[] args) throws UnknownHostException {
        System.out.println(IpUtil.isValid("219.239.110.138"));
        System.out.println("----------------");
        long ipAddressLong = IpUtil.toLong("219.239.110.138");// 3689901706
        System.out.println(ipAddressLong); // 3689901706
        System.out.println(IpUtil.fromLong(3689901706L));

        System.out.println("*****************");
        int ipAddressInt = IpUtil.toInt("219.239.110.138"); // -605065590
        System.out.println(ipAddressInt);
        System.out.println(IpUtil.fromInt(ipAddressInt));

        System.out.println(UUID.randomUUID());
        System.out.println(System.currentTimeMillis());

        System.out.println(InetAddress.getLocalHost().getHostAddress());
    }
}
