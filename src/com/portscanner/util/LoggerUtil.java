package com.portscanner.util;


import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * 日志工具
 */
public class LoggerUtil {
    private static final Logger LOGGER = Logger.getLogger("PortScanner");

    static {
        try {
            // 自动创建 logs 目录
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("logs"));
            FileHandler fh = new FileHandler("logs/scan_log.txt", true);
            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
            LOGGER.setUseParentHandlers(false);
        } catch (Exception e) {
            System.err.println("日志初始化失败: " + e.getMessage());
        }
    }

    public static void info(String msg) {
        LOGGER.info(msg);
    }
}