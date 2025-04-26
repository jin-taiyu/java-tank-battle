package com.tankbattle.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import com.tankbattle.model.GameModel;
import com.tankbattle.view.GameView;
import com.tankbattle.model.enums.Direction;
import com.tankbattle.model.enums.GameState;

/**
 * 游戏控制器类，负责处理用户输入和游戏逻辑
 * 
 * @author Taiyu Jin
 */
public class GameController {
    // 游戏模型和视图
    private GameModel gameModel;
    private GameView gameView;
    
    // 游戏循环
    private AnimationTimer gameLoop;
    
    // 上一帧时间
    private long lastTime;
    
    /**
     * 构造函数
     * 
     * @param gameModel 游戏模型
     * @param gameView 游戏视图
     */
    public GameController(GameModel gameModel, GameView gameView) {
        this.gameModel = gameModel;
        this.gameView = gameView;
        
        // 设置键盘事件处理
        setupKeyHandlers();
        
        // 初始化游戏循环
        initGameLoop();
    }
    
    /**
     * 设置键盘事件处理
     */
    private void setupKeyHandlers() {
        gameView.setKeyHandlers(this::handleKeyPressed, this::handleKeyReleased);
    }
    
    /**
     * 处理按键按下事件
     * 
     * @param event 键盘事件
     */
    private void handleKeyPressed(KeyEvent event) {
        if (gameModel.getGameState() == GameState.RUNNING) {
            switch (event.getCode()) {
                case W:
                case UP:
                    gameModel.movePlayerTank(Direction.UP, true);
                    break;
                case S:
                case DOWN:
                    gameModel.movePlayerTank(Direction.DOWN, true);
                    break;
                case A:
                case LEFT:
                    gameModel.movePlayerTank(Direction.LEFT, true);
                    break;
                case D:
                case RIGHT:
                    gameModel.movePlayerTank(Direction.RIGHT, true);
                    break;
                case SPACE:
                case J:
                    gameModel.playerFire();
                    break;
                case ESCAPE:
                case P:
                    gameModel.pauseGame();
                    gameView.showPauseScene();
                    break;
                default:
                    break;
            }
        } else if (gameModel.getGameState() == GameState.PAUSED) {
            // 暂停界面仅使用按钮退出，禁用键盘恢复
        }
    }
    
    /**
     * 处理按键释放事件
     * 
     * @param event 键盘事件
     */
    private void handleKeyReleased(KeyEvent event) {
        if (gameModel.getGameState() == GameState.RUNNING) {
            switch (event.getCode()) {
                case W:
                case UP:
                    if (gameModel.getPlayerTank().getDirection() == Direction.UP) {
                        gameModel.movePlayerTank(Direction.UP, false);
                    }
                    break;
                case S:
                case DOWN:
                    if (gameModel.getPlayerTank().getDirection() == Direction.DOWN) {
                        gameModel.movePlayerTank(Direction.DOWN, false);
                    }
                    break;
                case A:
                case LEFT:
                    if (gameModel.getPlayerTank().getDirection() == Direction.LEFT) {
                        gameModel.movePlayerTank(Direction.LEFT, false);
                    }
                    break;
                case D:
                case RIGHT:
                    if (gameModel.getPlayerTank().getDirection() == Direction.RIGHT) {
                        gameModel.movePlayerTank(Direction.RIGHT, false);
                    }
                    break;
                default:
                    break;
            }
        }
    }
    
    /**
     * 初始化游戏循环
     */
    private void initGameLoop() {
        lastTime = System.nanoTime();
        
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // 计算时间增量（秒）
                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                
                // 更新游戏状态
                update(deltaTime);
                
                // 渲染游戏
                render();
            }
        };
        
        // 启动游戏循环
        gameLoop.start();
    }
    
    /**
     * 更新游戏状态
     * 
     * @param deltaTime 时间增量
     */
    private void update(double deltaTime) {
        // 根据游戏状态更新
        switch (gameModel.getGameState()) {
            case RUNNING:
                gameModel.update(deltaTime);
                break;
            case PAUSED:
                gameView.showPauseScene();
                break;
            case VICTORY:
                gameView.showVictoryScene();
                break;
            case GAME_OVER:
                gameView.showGameOverScene();
                break;
            case LEVEL_COMPLETE:
                gameView.showLevelCompleteScene();
                break;
            default:
                break;
        }
    }
    
    /**
     * 渲染游戏
     */
    private void render() {
        if (gameModel.getGameState() == GameState.RUNNING) {
            gameView.render();
        }
    }
}