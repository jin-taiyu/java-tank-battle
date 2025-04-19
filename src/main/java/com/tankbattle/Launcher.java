package com.tankbattle;

/**
 * 游戏启动器类
 * 用于处理跨平台启动问题，简化从打包后JAR的启动
 */
public class Launcher {
    
    /**
     * 主入口点
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 直接调用TankBattleApp的main方法
        TankBattleApp.main(args);
    }
}