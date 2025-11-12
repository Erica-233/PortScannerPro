package com.portscanner;

import com.portscanner.ui.ConsoleUI;

/**
 * 程序入口 - 软件主类
 */
public class App {
    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}