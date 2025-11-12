package com.portscanner.service;

import com.portscanner.model.ScanResult;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 核心扫描服务 - 完全线程安全
 */
public class PortScannerService {

    private final ExecutorService executor;
    private static final int TIMEOUT_MS = 200;  // 优化：200ms 超时

    public PortScannerService(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    /**
     * 扫描单个主机
     */
    public List<ScanResult> scanHost(String ip, int startPort, int endPort) {
        // 线程安全列表
        List<ScanResult> results = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(endPort - startPort + 1);

        for (int port = startPort; port <= endPort; port++) {
            final int p = port;
            executor.submit(() -> {
                try {
                    long start = System.nanoTime();
                    boolean open = isPortOpen(ip, p);
                    long timeMs = (System.nanoTime() - start) / 1_000_000;

                    // 必须添加结果
                    results.add(new ScanResult(ip, p, open, timeMs));

                } catch (Exception e) {
                    // 防止 null
                    results.add(new ScanResult(ip, p, false, 0));
                } finally {
                    latch.countDown();  // 保证计数
                }
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

    /**
     * 关闭线程池
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}