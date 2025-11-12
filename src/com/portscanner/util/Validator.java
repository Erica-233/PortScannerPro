package com.portscanner.util;


import java.util.Scanner;

/**
 * 输入校验工具
 */
public class Validator {

    public static boolean isValidIp(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        try {
            for (String p : parts) {
                int n = Integer.parseInt(p);
                if (n < 0 || n > 255) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static int readInt(Scanner sc, int min, int max) {
        while (true) {
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v >= min && v <= max) return v;
                System.out.printf("请输入 %d~%d: ", min, max);
            } catch (Exception e) {
                System.out.print("无效输入，请重试: ");
            }
        }
    }
}