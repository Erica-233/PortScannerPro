package com.portscanner.ui;

import com.portscanner.model.ScanResult;
import com.portscanner.service.IpRangeGenerator;
import com.portscanner.service.PortScannerService;
import com.portscanner.util.LoggerUtil;
import com.portscanner.util.Validator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 控制台用户界面 - 视图层
 */
public class ConsoleUI {

    private final Scanner scanner = new Scanner(System.in);
    private final PortScannerService scannerService;

    public ConsoleUI() {
        this.scannerService = new PortScannerService(100);
        printBanner();
    }

    public void start() {
        printWelcome();

        int mode = chooseMode();
        String startIp = "127.0.0.1", endIp = "127.0.0.1";
        if (mode == 2) {
            startIp = inputIp("起始IP");
            endIp = inputIp("结束IP");
        }

        int startPort = inputPort("起始端口", 1, 65535);
        int endPort = inputPort("结束端口", startPort, 65535);

        startScanning(startIp, endIp, startPort, endPort);
        scannerService.shutdown();
    }

    private void startScanning(String startIp, String endIp, int startPort, int endPort) {
        long totalStart = System.currentTimeMillis();
        AtomicInteger totalOpen = new AtomicInteger(0);
        AtomicInteger totalClosed = new AtomicInteger(0);

        IpRangeGenerator.getIpStream(startIp, endIp).forEach(ipNum -> {
            String ip = IpRangeGenerator.longToIp(ipNum);
            System.out.println("\n正在扫描: " + ip);
            LoggerUtil.info("开始扫描 IP: " + ip);

            List<ScanResult> results = scannerService.scanHost(ip, startPort, endPort);
            for (ScanResult r : results) {
                if (r.isOpen()) {
                    System.out.println("  " + r);
                    totalOpen.incrementAndGet();  // 线程安全递增
                } else {
                    totalClosed.incrementAndGet();
                }
            }
        });

        printSummary(totalStart, totalOpen.get(), totalClosed.get());
        printSecurityWarning();
    }

    // === UI 辅助方法 ===

    private void printBanner() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("banner.txt")) {
            if (is != null) {
                new BufferedReader(new InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))
                        .lines()
                        .forEach(System.out::println);
            } else {
                System.out.println("=== PortScanner Pro v1.0 ===");
            }
        } catch (Exception e) {
            System.out.println("=== PortScanner Pro v1.0 ===");
        }
    }

    private void printWelcome() {
        System.out.println("欢迎使用专业端口扫描器");
        System.out.println("仅限授权安全自查使用");
    }

    private int chooseMode() {
        System.out.print("扫描模式 [1.本机 2.IP范围]: ");
        return Validator.readInt(scanner, 1, 2);
    }

    private String inputIp(String prompt) {
        while (true) {
            System.out.print("输入" + prompt + ": ");
            String ip = scanner.nextLine().trim();
            if (Validator.isValidIp(ip)) return ip;
            System.out.println("IP格式错误！请重新输入。");
        }
    }

    private int inputPort(String prompt, int min, int max) {
        System.out.print(prompt + " (" + min + "-" + max + "): ");
        return Validator.readInt(scanner, min, max);
    }

    private void printSummary(long startTime, int open, int closed) {
        long time = System.currentTimeMillis() - startTime;
        System.out.println("\n" + "=".repeat(50));
        System.out.println("扫描完成！耗时: " + String.format("%.2f", time / 1000.0) + "秒");
        System.out.println("开放端口: " + open + " | 关闭端口: " + closed);
        System.out.println("日志已保存至: logs/scan_log.txt");
        System.out.println("=".repeat(50));
    }

    private void printSecurityWarning() {
        System.out.println("\n法律声明：本工具仅用于授权安全测试，禁止非法扫描。");
        System.out.println("请确保您已获得目标系统明确授权。");
    }
}