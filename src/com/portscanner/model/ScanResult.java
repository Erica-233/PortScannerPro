package com.portscanner.model;
/**
 * 扫描结果实体类
 */
public class ScanResult {
    private final String ip;
    private final int port;
    private final boolean isOpen;
    private final long responseTimeMs;

    public ScanResult(String ip, int port, boolean isOpen, long responseTimeMs) {
        this.ip = ip;
        this.port = port;
        this.isOpen = isOpen;
        this.responseTimeMs = responseTimeMs;
    }

    // Getters
    public String getIp() { return ip; }
    public int getPort() { return port; }
    public boolean isOpen() { return isOpen; }
    public long getResponseTimeMs() { return responseTimeMs; }

    @Override
    public String toString() {
        return String.format("[%s:%d] %s (响应: %dms)",
                ip, port, isOpen ? "开放" : "关闭", responseTimeMs);
    }
}