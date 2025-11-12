package com.portscanner.service;


import java.util.stream.LongStream;

/**
 * IP 范围生成器
 */
public class IpRangeGenerator {

    public static LongStream getIpStream(String startIp, String endIp) {
        long start = ipToLong(startIp);
        long end = ipToLong(endIp);
        return LongStream.rangeClosed(start, end);
    }

    public static long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        long result = 0;
        for (String part : parts) {
            result = result * 256 + Integer.parseInt(part);
        }
        return result;
    }

    public static String longToIp(long ip) {
        return String.format("%d.%d.%d.%d",
                (ip >> 24) & 0xFF,
                (ip >> 16) & 0xFF,
                (ip >> 8) & 0xFF,
                ip & 0xFF);
    }
}