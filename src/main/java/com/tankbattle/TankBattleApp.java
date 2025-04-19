package com.tankbattle;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import com.tankbattle.controller.GameController;
import com.tankbattle.view.GameView;
import com.tankbattle.model.GameModel;
import com.tankbattle.model.AudioManager;
import com.tankbattle.model.ResourceManager;

/**
 * 坦克大战游戏主应用类
 * 
 * @author Taiyu Jin
 */
public class TankBattleApp extends Application {
    
    private GameController gameController;
    private GameView gameView;
    private GameModel gameModel;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // 提前初始化资源管理器，确保资源已加载
            ResourceManager.getInstance();
            
            // 音频管理器使用懒加载方式，减少启动时的加载问题
            AudioManager.getInstance();
            
            // 初始化游戏模型、视图和控制器
            gameModel = new GameModel();
            gameView = new GameView(primaryStage, gameModel);
            gameController = new GameController(gameModel, gameView);
            
            // 设置窗口标题
            primaryStage.setTitle("坦克大战");
            
            // 显示主菜单
            gameView.showMainMenu();
            
            // 显示窗口
            primaryStage.show();
            
            // 设置未捕获异常处理器
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                System.err.println("未捕获异常：" + throwable.getMessage());
                throwable.printStackTrace();
                
                // 在JavaFX线程中显示错误对话框
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("错误");
                    alert.setHeaderText("游戏运行时发生错误");
                    alert.setContentText("错误详情: " + throwable.getMessage());
                    alert.showAndWait();
                });
            });
        } catch (Exception e) {
            System.err.println("游戏启动失败: " + e.getMessage());
            e.printStackTrace();
            
            // 显示错误对话框
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("启动错误");
            alert.setHeaderText("游戏无法启动");
            alert.setContentText("错误详情: " + e.getMessage());
            alert.showAndWait();
            
            // 退出应用
            Platform.exit();
        }
    }
    
    @Override
    public void stop() {
        try {
            // 确保在应用关闭时释放资源
            AudioManager audioManager = AudioManager.getInstance();
            if (audioManager != null) {
                audioManager.stopBackgroundMusic();
            }
            
            // 这里可以添加其他需要在关闭时执行的清理操作
        } catch (Exception e) {
            System.err.println("应用关闭时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 游戏入口点
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            // 启动JavaFX应用
            launch(args);
        } catch (Exception e) {
            System.err.println("主方法发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}