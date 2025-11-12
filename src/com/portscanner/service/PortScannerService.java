package com.portscanner.service;

import com.portscanner.model.ScanResult;
import com.portscanner.util.LoggerUtil;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 核心扫描服务 - 业务逻辑层
 */
public class PortScannerService {

    private final ExecutorService executor;
    private static final int TIMEOUT_MS = 1000;

    public PortScannerService(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    /**
     * 扫描单个IP的所有端口
     */
    public List<ScanResult> scanHost(String ip, int startPort, int endPort) {
        List<ScanResult> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(endPort - startPort + 1);

        for (int port = startPort; port <= endPort; port++) {
            final int p = port;
            executor.submit(() -> {
                long start = System.nanoTime();
                boolean open = isPortOpen(ip, p);
                long timeMs = (System.nanoTime() - start) / 1_000_000;
                results.add(new ScanResult(ip, p, open, timeMs));
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return results;
    }

    /**
     * TCP 连接测试
     */
    private boolean isPortOpen(String ip, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), TIMEOUT_MS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}