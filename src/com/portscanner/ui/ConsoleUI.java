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
 * 控制台用户界面 - 终极修复版
 * 功能：强制打印端口状态 + 调试信息 + 自动交换 IP + 防 null
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

            // 自动交换 IP 顺序
            long startNum = IpRangeGenerator.ipToLong(startIp);
            long endNum = IpRangeGenerator.ipToLong(endIp);
            if (startNum > endNum) {
                System.out.println("检测到起始IP大于结束IP，自动交换顺序...");
                String temp = startIp;
                startIp = endIp;
                endIp = temp;
            }
        }

        int startPort = inputPort("起始端口", 1, 65535);
        int endPort = inputPort("结束端口", startPort, 65535);

        // 耗时警告
        int totalPorts = endPort - startPort + 1;
        if (totalPorts > 1000) {
            System.out.println("警告：将扫描 " + totalPorts + " 个端口，可能耗时较长。");
            System.out.print("继续？(y/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.println("扫描已取消。");
                scannerService.shutdown();
                return;
            }
        }

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

            // 关键调试：打印结果数量
            System.out.println("调试: 收到 " + results.size() + " 个扫描结果");

            if (results.isEmpty()) {
                System.out.println("  [警告] 没有收到任何结果！");
            }

            for (ScanResult r : results) {
                if (r == null) {
                    System.out.println("  [错误] 结果为 null！");
                    continue;
                }

                // 强制打印每个端口状态
                System.out.println("  " + r.toString());  // 强制调用 toString()

                if (r.isOpen()) {
                    totalOpen.incrementAndGet();
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
            System.out.println("IP格式错误！请重新输入（示例: 192.168.1.1）。");
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