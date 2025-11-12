# PortScannerPro

Java 端口扫描器，支持并发扫描、日志记录、输入校验。仅用于授权安全自查！

## 功能
- 本机 / IP 范围扫描
- 端口范围检测（1-65535）
- 线程池并发（100 线程）
- 日志输出（logs/scan_log.txt）
- UML 类图：![](https://gitee.com/Erica114/typoary-image/raw/master/20251112112909173.png)

## 技术栈
- Java 17 + Maven
- 架构：MVC（Model-Service-UI-Util）

## 运行
```bash
mvn compile exec:java -Dexec.mainClass="com.portscanner.App"
