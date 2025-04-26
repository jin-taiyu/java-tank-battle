package com.tankbattle.view;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.tankbattle.model.save.GameSave;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.input.KeyEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.Glow;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.Reflection;
import javafx.scene.shape.Rectangle;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.util.Duration;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import com.tankbattle.model.AudioManager;
import com.tankbattle.model.GameModel;
import com.tankbattle.model.ResourceManager;
import com.tankbattle.model.entity.Bullet;
import com.tankbattle.model.entity.EnemyTank;
import com.tankbattle.model.entity.PlayerTank;
import com.tankbattle.model.entity.Wall;
import com.tankbattle.model.entity.BrickWall;
import com.tankbattle.model.entity.SteelWall;
import com.tankbattle.model.entity.Item;
import com.tankbattle.model.enums.Direction;
import com.tankbattle.model.enums.GameState;
import com.tankbattle.model.enums.ItemType;

/**
 * 游戏视图类，负责渲染游戏界面
 * 
 * @author Taiyu Jin
 */
public class GameView {
    // JavaFX组件
    private Stage stage;
    private Scene gameScene;
    private Scene menuScene;
    private Scene gameOverScene;
    private Scene victoryScene;
    private Scene pauseScene;
    private Scene helpScene;
    private Scene levelCompleteScene;
    private Scene musicSettingsScene;
    private Canvas gameCanvas;
    private GraphicsContext gc;
    
    // 游戏模型
    private GameModel gameModel;
    
    // 资源管理器
    private ResourceManager resourceManager;
    
    // 音频管理器
    private AudioManager audioManager;
    
    // 游戏区域大小
    private static final int GAME_WIDTH = 800;
    private static final int GAME_HEIGHT = 600;
    
    // 特效
    private Glow glowEffect;
    private DropShadow shadowEffect;
    
    // 添加HUD元素
    private VBox hudPanel;
    private ImageView heartIcon;
    private Text scoreText;
    private Text levelText;
    private Text enemiesText;
    private HBox statusEffectsBox;
    
    // 在类成员区域添加标志位，避免重复播放
    private boolean levelCompleteAudioPlayed = false;
    // 游戏结束音乐播放标志，避免多次播放
    private boolean gameOverAudioPlayed = false;
    
    /**
     * 构造函数
     * 
     * @param stage 主舞台
     * @param gameModel 游戏模型
     */
    public GameView(Stage stage, GameModel gameModel) {
        this.stage = stage;
        this.gameModel = gameModel;
        
        // 获取资源管理器和音频管理器
        this.resourceManager = ResourceManager.getInstance();
        this.audioManager = AudioManager.getInstance();
        
        // 初始化游戏画布
        gameCanvas = new Canvas(GAME_WIDTH, GAME_HEIGHT);
        gc = gameCanvas.getGraphicsContext2D();
        
        // 初始化特效
        glowEffect = new Glow(0.8);
        shadowEffect = new DropShadow(10, Color.CYAN);
        
        // 创建HUD界面
        createHudPanel();
        
        // 创建游戏场景 - 添加布局以包含游戏画布和HUD
        BorderPane gamePane = new BorderPane();
        gamePane.setCenter(gameCanvas);
        
        // 使用StackPane将HUD叠加在游戏画布上
        StackPane gameStackPane = new StackPane();
        gameStackPane.getChildren().addAll(gameCanvas, hudPanel);
        
        // 设置HUD位置在左上角
        StackPane.setAlignment(hudPanel, Pos.TOP_LEFT);
        gamePane.setCenter(gameStackPane);
        
        gameScene = new Scene(gamePane, GAME_WIDTH, GAME_HEIGHT);
        
        // 创建菜单场景
        createMenuScene();
        
        // 创建游戏结束场景
        createGameOverScene();
        
        // 创建胜利场景
        createVictoryScene();
        
        // 创建暂停场景
        createPauseScene();
        
        // 创建帮助场景
        createHelpScene();
        
        // 创建音乐设置场景
        createMusicSettingsScene();
        
        // 创建关卡完成场景
        createLevelCompleteScene();
    }
    
    /**
     * 创建HUD面板
     */
    private void createHudPanel() {
        // 创建HUD主面板
        hudPanel = new VBox(10);
        hudPanel.setPadding(new Insets(10));
        hudPanel.setAlignment(Pos.TOP_LEFT);
        hudPanel.setMaxWidth(200);
        hudPanel.setMouseTransparent(true); // 确保HUD不会拦截游戏操作
        
        // 创建半透明背景
        Rectangle hudBackground = new Rectangle(180, 240);
        hudBackground.setFill(Color.rgb(0, 0, 0, 0.6));
        hudBackground.setArcWidth(15);
        hudBackground.setArcHeight(15);
        hudBackground.setStroke(Color.rgb(100, 100, 100, 0.8));
        hudBackground.setStrokeWidth(2);
        
        // 创建标题
        Text hudTitle = new Text("游戏状态");
        hudTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        hudTitle.setFill(Color.WHITE);
        
        // 添加分隔线
        Rectangle divider = new Rectangle(160, 2);
        divider.setFill(Color.rgb(150, 150, 150, 0.8));
        
        // 创建得分面板
        HBox scoreBox = new HBox(10);
        scoreBox.setAlignment(Pos.CENTER_LEFT);
        
        // 得分图标
        Rectangle scoreIcon = new Rectangle(18, 18);
        scoreIcon.setFill(Color.YELLOW);
        scoreIcon.setArcWidth(5);
        scoreIcon.setArcHeight(5);
        
        // 得分文本
        scoreText = new Text();
        scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        scoreText.setFill(Color.WHITE);
        scoreText.textProperty().bind(gameModel.scoreProperty().asString("得分: %d"));
        
        scoreBox.getChildren().addAll(scoreIcon, scoreText);
        
        // 创建生命面板
        HBox livesBox = new HBox(10);
        livesBox.setAlignment(Pos.CENTER_LEFT);
        
        // 心形图标
        heartIcon = new ImageView(resourceManager.getImage("player_tank_up"));
        heartIcon.setFitWidth(20);
        heartIcon.setFitHeight(20);
        
        // 生命文本 - 将在render方法中更新
        Text livesText = new Text("生命: ");
        livesText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        livesText.setFill(Color.WHITE);
        
        livesBox.getChildren().addAll(heartIcon, livesText);
        
        // 创建关卡面板
        HBox levelBox = new HBox(10);
        levelBox.setAlignment(Pos.CENTER_LEFT);
        
        // 关卡图标
        Rectangle levelIcon = new Rectangle(18, 18);
        levelIcon.setFill(Color.LIGHTBLUE);
        levelIcon.setArcWidth(5);
        levelIcon.setArcHeight(5);
        
        // 关卡文本
        levelText = new Text();
        levelText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        levelText.setFill(Color.WHITE);
        levelText.textProperty().bind(gameModel.levelProperty().asString("关卡: %d"));
        
        levelBox.getChildren().addAll(levelIcon, levelText);
        
        // 创建敌人面板
        HBox enemiesBox = new HBox(10);
        enemiesBox.setAlignment(Pos.CENTER_LEFT);
        
        // 敌人图标
        ImageView enemyIcon = new ImageView(resourceManager.getImage("enemy_tank_down"));
        enemyIcon.setFitWidth(20);
        enemyIcon.setFitHeight(20);
        
        // 敌人文本
        enemiesText = new Text();
        enemiesText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        enemiesText.setFill(Color.WHITE);
        enemiesText.textProperty().bind(gameModel.remainingEnemiesProperty().asString("剩余: %d"));
        
        enemiesBox.getChildren().addAll(enemyIcon, enemiesText);
        
        // 创建状态效果面板
        VBox effectsPanel = new VBox(8);
        effectsPanel.setAlignment(Pos.CENTER_LEFT);
        
        // 状态效果标题
        Text effectsTitle = new Text("道具状态");
        effectsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        effectsTitle.setFill(Color.WHITE);
        
        // 状态效果容器
        statusEffectsBox = new HBox(8);
        statusEffectsBox.setAlignment(Pos.CENTER_LEFT);
        statusEffectsBox.setPadding(new Insets(5, 0, 0, 0));
        
        effectsPanel.getChildren().addAll(effectsTitle, statusEffectsBox);
        
        // 创建HUD内容面板
        VBox hudContent = new VBox(12);
        hudContent.setAlignment(Pos.TOP_LEFT);
        hudContent.setPadding(new Insets(10));
        hudContent.getChildren().addAll(
            hudTitle,
            divider,
            scoreBox,
            livesBox,
            levelBox,
            enemiesBox,
            effectsPanel
        );
        
        // 将内容添加到HUD面板
        StackPane hudPane = new StackPane();
        hudPane.getChildren().addAll(hudBackground, hudContent);
        hudPanel.getChildren().add(hudPane);
    }
    
    /**
     * 更新HUD状态效果区域
     */
    private void updateStatusEffects() {
        statusEffectsBox.getChildren().clear();
        
        PlayerTank playerTank = gameModel.getPlayerTank();
        if (playerTank == null || !playerTank.isAlive()) {
            return;
        }
        
        // 添加护盾效果
        if (playerTank.hasShield()) {
            statusEffectsBox.getChildren().add(createStatusEffectIcon(
                "item_shield", 
                Color.CYAN, 
                String.format("护盾: %.1fs", playerTank.getShieldTime())
            ));
        }
        
        // 添加速度提升效果
        if (playerTank.hasSpeedBoost()) {
            statusEffectsBox.getChildren().add(createStatusEffectIcon(
                "item_speed", 
                Color.GREEN, 
                String.format("速度: %.1fs", playerTank.getSpeedBoostTime())
            ));
        }
        
        // 添加火力增强效果
        if (playerTank.hasPowerUp()) {
            statusEffectsBox.getChildren().add(createStatusEffectIcon(
                "item_power", 
                Color.RED, 
                String.format("火力: %.1fs", playerTank.getPowerUpTime())
            ));
        }
    }
    
    /**
     * 创建状态效果图标
     * 
     * @param imageName 图标图像名称
     * @param color 提示颜色
     * @param tooltip 提示文本
     * @return 图标容器
     */
    private StackPane createStatusEffectIcon(String imageName, Color color, String tooltip) {
        // 创建图标
        ImageView icon = new ImageView(resourceManager.getImage(imageName));
        icon.setFitWidth(22);
        icon.setFitHeight(22);
        
        // 创建背景
        Rectangle background = new Rectangle(30, 30);
        background.setFill(Color.rgb(40, 40, 40, 0.6));
        background.setArcWidth(5);
        background.setArcHeight(5);
        background.setStroke(color);
        background.setStrokeWidth(2);
        
        // 创建工具提示
        javafx.scene.control.Tooltip toolTip = new javafx.scene.control.Tooltip(tooltip);
        javafx.scene.control.Tooltip.install(icon, toolTip);
        
        // 组合图标和背景
        StackPane iconPane = new StackPane(background, icon);
        
        // 添加发光效果
        Glow iconGlow = new Glow(0.5);
        icon.setEffect(iconGlow);
        
        return iconPane;
    }
    
    /**
     * 创建主菜单场景
     */
    private void createMenuScene() {
        // 创建主界面布局
        StackPane menuRoot = new StackPane();
        
        // 创建背景 - 使用矩形填充并添加渐变效果
        Rectangle background = new Rectangle(GAME_WIDTH, GAME_HEIGHT);
        background.setFill(Color.rgb(20, 20, 40)); // 深蓝色背景
        
        // 添加坦克装饰 - 在背景上放置坦克图像
        ImageView tankUp = new ImageView(resourceManager.getImage("player_tank_up"));
        tankUp.setFitWidth(80);
        tankUp.setFitHeight(80);
        tankUp.setTranslateX(-GAME_WIDTH / 3);
        tankUp.setTranslateY(-GAME_HEIGHT / 4);
        tankUp.setRotate(15);
        tankUp.setOpacity(0.6);
        
        ImageView tankRight = new ImageView(resourceManager.getImage("player_tank_right"));
        tankRight.setFitWidth(80);
        tankRight.setFitHeight(80);
        tankRight.setTranslateX(GAME_WIDTH / 3);
        tankRight.setTranslateY(GAME_HEIGHT / 4);
        tankRight.setRotate(-15);
        tankRight.setOpacity(0.6);
        
        ImageView enemyTank = new ImageView(resourceManager.getImage("enemy_tank_down"));
        enemyTank.setFitWidth(60);
        enemyTank.setFitHeight(60);
        enemyTank.setTranslateX(GAME_WIDTH / 4);
        enemyTank.setTranslateY(-GAME_HEIGHT / 3);
        enemyTank.setRotate(-10);
        enemyTank.setOpacity(0.6);
        
        // 创建标题文本
        Text titleText = new Text("坦克大战");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 70));
        titleText.setFill(Color.WHITE);
        titleText.setStroke(Color.BLACK);
        titleText.setStrokeWidth(2);
        
        // 添加反射效果使标题更有质感
        Reflection reflection = new Reflection();
        reflection.setFraction(0.3);
        reflection.setTopOpacity(0.5);
        reflection.setBottomOpacity(0);
        titleText.setEffect(reflection);
        
        // 设置标题位置在顶部居中
        StackPane titlePane = new StackPane(titleText);
        titlePane.setPadding(new Insets(30, 0, 0, 0));
        titlePane.setAlignment(Pos.TOP_CENTER);
        
        // 创建菜单选项面板
        VBox menuOptionsBox = new VBox(15);
        menuOptionsBox.setAlignment(Pos.CENTER);
        menuOptionsBox.setPadding(new Insets(10));
        menuOptionsBox.setMaxWidth(400);
        
        // 创建关卡选择界面
        HBox levelSelectorBox = new HBox(20);
        levelSelectorBox.setAlignment(Pos.CENTER);
        levelSelectorBox.setPadding(new Insets(10));
        
        // 添加"选择关卡"标签
        Label levelLabel = new Label("选择关卡:");
        levelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        levelLabel.setTextFill(Color.WHITE);
        
        // 添加关卡选择按钮
        HBox levelButtonsBox = new HBox(10);
        levelButtonsBox.setAlignment(Pos.CENTER);
        
        // 添加五个关卡按钮
        for (int i = 1; i <= 5; i++) {
            final int level = i;
            Button levelButton = createStyledButton(String.valueOf(i), 40, 40);
            
            // 选中当前选择的关卡
            if (gameModel.getSelectedLevel() == level) {
                levelButton.setStyle(levelButton.getStyle() + "-fx-background-color: #4CAF50;");
            }
            
            levelButton.setOnAction(e -> {
                try {
                    audioManager.playSoundEffect("button_click");
                } catch (Exception ex) {
                    System.err.println("播放按钮音效失败: " + ex.getMessage());
                }
                
                // 更新选中的关卡
                gameModel.setSelectedLevel(level);
                
                // 更新UI，刷新当前关卡选择界面
                createMenuScene();
                showMainMenu();
            });
            
            levelButtonsBox.getChildren().add(levelButton);
        }
        
        levelSelectorBox.getChildren().addAll(levelLabel, levelButtonsBox);
        
        // 创建开始游戏按钮
        Button startButton = createStyledButton("开始游戏", 200, 50);
        startButton.setOnAction(e -> {
            try {
                audioManager.playSoundEffect("button_click");
            } catch (Exception ex) {
                System.err.println("播放按钮音效失败: " + ex.getMessage());
            }
            startGame();
        });
        
        // 创建加载游戏按钮
        Button loadButton = createStyledButton("加载游戏", 200, 50);
        loadButton.setOnAction(e -> {
            try {
                audioManager.playSoundEffect("button_click");
            } catch (Exception ex) {
                System.err.println("播放按钮音效失败: " + ex.getMessage());
            }
            loadGame();
        });
        
        // 创建玩法说明按钮 (减小宽度)
        Button helpButton = createStyledButton("玩法说明", 95, 40);
        helpButton.setOnAction(e -> {
            try {
                audioManager.playSoundEffect("button_click");
            } catch (Exception ex) {
                System.err.println("播放按钮音效失败: " + ex.getMessage());
            }
            showHelpScene();
        });
        
        // 创建音乐设置按钮 (减小宽度)
        Button musicButton = createStyledButton("音乐设置", 95, 40);
        musicButton.setOnAction(e -> {
            try { audioManager.playSoundEffect("button_click"); } catch (Exception ex) { System.err.println("播放按钮音效失败: " + ex.getMessage()); }
            showMusicSettingsScene();
        });
        
        // 将玩法说明和音乐设置按钮放入水平布局
        HBox utilityButtonsBox = new HBox(10); // 10像素的间距
        utilityButtonsBox.setAlignment(Pos.CENTER);
        utilityButtonsBox.getChildren().addAll(helpButton, musicButton);
        
        // 创建退出游戏按钮
        Button exitButton = createStyledButton("退出游戏", 200, 50);
        exitButton.setOnAction(e -> {
            try {
                audioManager.playSoundEffect("button_click");
            } catch (Exception ex) {
                System.err.println("播放按钮音效失败: " + ex.getMessage());
            }
            System.exit(0);
        });
        
        // 添加按钮到菜单选项面板 (使用新的水平布局替代单独的按钮)
        menuOptionsBox.getChildren().addAll(levelSelectorBox, startButton, loadButton, utilityButtonsBox, exitButton);
        
        // 创建半透明面板作为菜单选项的背景
        Rectangle menuBg = new Rectangle(400, 320); // 适当减小高度，因为现在按钮占用的空间更少
        menuBg.setFill(Color.rgb(0, 0, 0, 0.7));
        menuBg.setArcWidth(20);
        menuBg.setArcHeight(20);
        menuBg.setStroke(Color.GRAY);
        menuBg.setStrokeWidth(2);
        
        // 组合菜单背景和选项
        StackPane menuPane = new StackPane(menuBg, menuOptionsBox);
        
        // 将所有元素添加到主布局
        menuRoot.getChildren().addAll(background, tankUp, tankRight, enemyTank, titlePane, menuPane);
        
        // 创建场景
        menuScene = new Scene(menuRoot, GAME_WIDTH, GAME_HEIGHT);
    }
    
    /**
     * 创建风格化按钮
     * 
     * @param text 按钮文本
     * @param width 按钮宽度
     * @param height 按钮高度
     * @return 风格化的按钮
     */
    private Button createStyledButton(String text, double width, double height) {
        Button button = new Button(text);
        button.setPrefSize(width, height);
        
        // 设置基本样式
        button.setStyle(
            "-fx-background-color: #3a3a3a;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-color: #555555;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 5px;"
        );
        
        // 添加鼠标悬停效果
        button.setOnMouseEntered(e -> {
            button.setStyle(button.getStyle() + "-fx-background-color: #4a4a4a;");
            button.setEffect(new Glow(0.3));
        });
        
        // 移除鼠标悬停效果
        button.setOnMouseExited(e -> {
            // 还原基本样式
            button.setStyle(
                "-fx-background-color: #3a3a3a;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 16px;" +
                "-fx-background-radius: 5px;" +
                "-fx-border-color: #555555;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 5px;"
            );
            
            // 如果是选中的关卡按钮，保持突出显示
            if (text.length() == 1 && Integer.parseInt(text) == gameModel.getSelectedLevel()) {
                button.setStyle(button.getStyle() + "-fx-background-color: #4CAF50;");
            }
            
            button.setEffect(null);
        });
        
        // 添加按下效果
        button.setOnMousePressed(e -> {
            button.setStyle(button.getStyle() + "-fx-background-color: #2a2a2a;");
        });
        
        button.setOnMouseReleased(e -> {
            button.setStyle(button.getStyle() + "-fx-background-color: #4a4a4a;");
        });
        
        return button;
    }
    
    /**
     * 创建游戏结束场景
     */
    private void createGameOverScene() {
        // 创建主界面布局
        StackPane gameOverRoot = new StackPane();
        
        // 创建背景
        Rectangle background = new Rectangle(GAME_WIDTH, GAME_HEIGHT);
        background.setFill(Color.rgb(40, 20, 20)); // 暗红色背景表示失败
        
        // 添加装饰元素 - 破碎的坦克图像
        ImageView brokenTank = new ImageView(resourceManager.getImage("player_tank_up"));
        brokenTank.setFitWidth(100);
        brokenTank.setFitHeight(100);
        brokenTank.setTranslateX(-GAME_WIDTH / 4);
        brokenTank.setTranslateY(GAME_HEIGHT / 4);
        brokenTank.setRotate(45); // 倾斜表示损坏
        brokenTank.setOpacity(0.4);
        
        // 创建游戏结束文本
        Text gameOverText = new Text("游戏结束");
        gameOverText.setFont(Font.font("Arial", FontWeight.BOLD, 70));
        gameOverText.setFill(Color.RED);
        gameOverText.setStroke(Color.BLACK);
        gameOverText.setStrokeWidth(2);
        
        // 添加发光效果
        DropShadow glow = new DropShadow();
        glow.setColor(Color.DARKRED);
        glow.setRadius(15);
        gameOverText.setEffect(glow);
        
        // 创建得分文本
        Text scoreText = new Text();
        scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        scoreText.setFill(Color.WHITE);
        scoreText.textProperty().bind(gameModel.scoreProperty().asString("最终得分: %d"));
        
        // 创建中央内容布局，包含标题和按钮
        VBox contentBox = new VBox(40); // 增加间距，修复重叠问题
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(20));
        
        // 设置标题位置在顶部居中
        VBox textBox = new VBox(20);
        textBox.setAlignment(Pos.CENTER);
        textBox.getChildren().addAll(gameOverText, scoreText);
        
        // 创建按钮面板
        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMaxWidth(300);
        
        // 创建重新开始按钮
        Button restartButton = createStyledButton("重新开始", 200, 50);
        restartButton.setOnAction(e -> {
            try {
                audioManager.playSoundEffect("button_click");
            } catch (Exception ex) {
                System.err.println("播放按钮音效失败: " + ex.getMessage());
            }
            startGame();
        });
        
        // 创建返回主菜单按钮
        Button menuButton = createStyledButton("返回主菜单", 200, 50);
        menuButton.setOnAction(e -> {
            try {
                audioManager.playSoundEffect("button_click");
            } catch (Exception ex) {
                System.err.println("播放按钮音效失败: " + ex.getMessage());
            }
            showMainMenu();
        });
        
        // 添加按钮到面板
        buttonBox.getChildren().addAll(restartButton, menuButton);
        
        // 创建半透明面板作为按钮的背景
        Rectangle buttonBg = new Rectangle(300, 150);
        buttonBg.setFill(Color.rgb(0, 0, 0, 0.7));
        buttonBg.setArcWidth(20);
        buttonBg.setArcHeight(20);
        buttonBg.setStroke(Color.DARKRED);
        buttonBg.setStrokeWidth(2);
        
        // 组合按钮和背景
        StackPane buttonPane = new StackPane(buttonBg, buttonBox);
        
        // 将文本框和按钮面板添加到中央内容布局
        contentBox.getChildren().addAll(textBox, buttonPane);
        
        // 将所有元素添加到主布局
        gameOverRoot.getChildren().addAll(background, brokenTank, contentBox);
        
        // 创建场景
        gameOverScene = new Scene(gameOverRoot, GAME_WIDTH, GAME_HEIGHT);
    }
    
    /**
     * 创建胜利场景
     */
    private void createVictoryScene() {
        // 创建主界面布局
        StackPane victoryRoot = new StackPane();
        
        // 创建背景
        Rectangle background = new Rectangle(GAME_WIDTH, GAME_HEIGHT);
        background.setFill(Color.rgb(20, 40, 20)); // 暗绿色背景表示胜利
        
        // 添加装饰元素 - 胜利的坦克图像
        ImageView victorTank = new ImageView(resourceManager.getImage("player_tank_up"));
        victorTank.setFitWidth(100);
        victorTank.setFitHeight(100);
        victorTank.setTranslateX(GAME_WIDTH / 4);
        victorTank.setTranslateY(GAME_HEIGHT / 4);
        victorTank.setOpacity(0.8);
        
        // 创建胜利文本
        Text victoryText = new Text("胜利！");
        victoryText.setFont(Font.font("Arial", FontWeight.BOLD, 70));
        victoryText.setFill(Color.LIGHTGREEN);
        victoryText.setStroke(Color.BLACK);
        victoryText.setStrokeWidth(2);
        
        // 添加光晕效果
        Glow glow = new Glow(0.8);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.GREEN);
        shadow.setRadius(15);
        shadow.setInput(glow);
        victoryText.setEffect(shadow);
        
        // 创建得分文本
        Text scoreText = new Text();
        scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        scoreText.setFill(Color.WHITE);
        scoreText.textProperty().bind(gameModel.scoreProperty().asString("最终得分: %d"));
        
        // 设置标题位置在顶部居中
        VBox textBox = new VBox(20);
        textBox.setAlignment(Pos.CENTER);
        textBox.getChildren().addAll(victoryText, scoreText);
        
        // 创建按钮面板
        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMaxWidth(300);
        buttonBox.setTranslateY(100);
        
        // 创建下一关按钮
        Button nextLevelButton = createStyledButton("下一关", 200, 50);
        nextLevelButton.setOnAction(e -> {
            try {
                audioManager.playSoundEffect("button_click");
            } catch (Exception ex) {
                System.err.println("播放按钮音效失败: " + ex.getMessage());
            }
            nextLevel();
        });
        
        // 创建返回主菜单按钮
        Button menuButton = createStyledButton("返回主菜单", 200, 50);
        menuButton.setOnAction(e -> {
            try {
                audioManager.playSoundEffect("button_click");
            } catch (Exception ex) {
                System.err.println("播放按钮音效失败: " + ex.getMessage());
            }
            showMainMenu();
        });
        
        // 添加按钮到面板
        buttonBox.getChildren().addAll(nextLevelButton, menuButton);
        
        // 创建半透明面板作为按钮的背景
        Rectangle buttonBg = new Rectangle(300, 150);
        buttonBg.setFill(Color.rgb(0, 0, 0, 0.7));
        buttonBg.setArcWidth(20);
        buttonBg.setArcHeight(20);
        buttonBg.setStroke(Color.DARKGREEN);
        buttonBg.setStrokeWidth(2);
        
        // 组合按钮和背景
        StackPane buttonPane = new StackPane(buttonBg, buttonBox);
        
        // 将所有元素添加到主布局
        victoryRoot.getChildren().addAll(background, victorTank, textBox, buttonPane);
        
        // 创建场景
        victoryScene = new Scene(victoryRoot, GAME_WIDTH, GAME_HEIGHT);
    }
    
    /**
     * 创建暂停场景
     */
    private void createPauseScene() {
        // 创建主界面布局
        StackPane pauseRoot = new StackPane();
        
        // 创建背景
        Rectangle background = new Rectangle(GAME_WIDTH, GAME_HEIGHT);
        background.setFill(Color.rgb(30, 30, 30, 0.7)); // 半透明黑色背景
        
        // 创建暂停文本
        Text pauseText = new Text("游戏暂停");
        pauseText.setFont(Font.font("Arial", FontWeight.BOLD, 70));
        pauseText.setFill(Color.WHITE);
        pauseText.setStroke(Color.BLACK);
        pauseText.setStrokeWidth(2);
        
        // 添加阴影效果
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.BLUE);
        shadow.setRadius(10);
        pauseText.setEffect(shadow);
        
        // 创建按钮面板 - 不再设置translateY，改由布局控制位置
        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20)); // 添加内边距确保按钮不会太靠近边缘
        
        // 创建继续游戏按钮
        Button resumeButton = createStyledButton("继续游戏", 200, 50);
        resumeButton.setOnAction(e -> {
            try {
                audioManager.playSoundEffect("button_click");
            } catch (Exception ex) {
                System.err.println("播放按钮音效失败: " + ex.getMessage());
            }
            resumeGame();
        });
        
        // 创建返回主菜单按钮
        Button menuButton = createStyledButton("返回主菜单", 200, 50);
        menuButton.setOnAction(e -> {
            try {
                audioManager.playSoundEffect("button_click");
            } catch (Exception ex) {
                System.err.println("播放按钮音效失败: " + ex.getMessage());
            }
            showMainMenu();
        });
        
        // 添加按钮到面板
        buttonBox.getChildren().addAll(resumeButton, menuButton);
        
        // 创建半透明面板作为按钮的背景 - 使用适合内容的尺寸
        Rectangle buttonBg = new Rectangle(240, 160); // 调整尺寸以更好地匹配按钮尺寸
        buttonBg.setFill(Color.rgb(0, 0, 0, 0.8));
        buttonBg.setArcWidth(20);
        buttonBg.setArcHeight(20);
        buttonBg.setStroke(Color.GRAY);
        buttonBg.setStrokeWidth(2);
        
        // 组合按钮和背景
        StackPane buttonPane = new StackPane(buttonBg, buttonBox);
        
        // 将文本和按钮面板组合到一个VBox中
        VBox contentBox = new VBox(30);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().addAll(pauseText, buttonPane);
        
        // 将所有元素添加到主布局
        pauseRoot.getChildren().addAll(background, contentBox);
        
        // 创建场景
        pauseScene = new Scene(pauseRoot, GAME_WIDTH, GAME_HEIGHT);
        
        // 移除对键盘监听，确保仅通过按钮恢复/退出
        pauseScene.setOnKeyPressed(null);
    }
    
    /**
     * 显示主菜单
     */
    public void showMainMenu() {
        try {
            // 停止游戏音乐，播放菜单音乐
            audioManager.stopBackgroundMusic();
            // 尝试播放音乐，但如果失败也继续执行
            if (!audioManager.playBackgroundMusic("audio/menu_bgm.wav", true)) {
                System.err.println("菜单背景音乐加载失败，游戏将继续运行");
            }
        } catch (Exception e) {
            System.err.println("播放菜单音乐失败: " + e.getMessage());
            // 出现异常时禁用所有音频
            audioManager.disableAllAudio();
        }
        
        stage.setScene(menuScene);
        gameModel.setGameState(GameState.MENU);
    }
    
    /**
     * 显示游戏场景
     */
    public void showGameScene() {
        // 重置游戏结束音乐播放标志
        gameOverAudioPlayed = false;
        levelCompleteAudioPlayed = false;
        try {
            // 播放游戏音乐
            audioManager.stopBackgroundMusic();
            // 尝试播放音乐，但如果失败也继续执行
            if (!audioManager.playBackgroundMusic("audio/game_bgm.wav", true)) {
                System.err.println("游戏背景音乐加载失败，游戏将继续运行");
            }
        } catch (Exception e) {
            System.err.println("播放游戏音乐失败: " + e.getMessage());
            // 出现异常时禁用所有音频
            audioManager.disableAllAudio();
        }
        
        stage.setScene(gameScene);
    }
    
    /**
     * 显示游戏结束场景
     */
    public void showGameOverScene() {
        if (gameModel.getGameState() == GameState.GAME_OVER) {
            if (!gameOverAudioPlayed) {
                try {
                    // 播放游戏结束音效
                    audioManager.playSoundEffect("game_over");
                    
                    // 停止游戏音乐，播放游戏结束音乐
                    audioManager.stopBackgroundMusic();
                    if (!audioManager.playBackgroundMusic("audio/gameover_bgm.wav", true)) {
                        System.err.println("游戏结束背景音乐加载失败，游戏将继续运行");
                    }
                } catch (Exception e) {
                    System.err.println("播放游戏结束音效/音乐失败: " + e.getMessage());
                    audioManager.disableAllAudio();
                }
                gameOverAudioPlayed = true;
            }
            stage.setScene(gameOverScene);
        }
    }
    
    /**
     * 显示胜利场景
     */
    public void showVictoryScene() {
        if (gameModel.getGameState() == GameState.VICTORY) {
            try {
                // 播放胜利音效
                audioManager.playSoundEffect("victory");
                
                // 停止游戏音乐，播放胜利音乐
                audioManager.stopBackgroundMusic();
                // 尝试播放音乐，但如果失败也继续执行
                if (!audioManager.playBackgroundMusic("audio/victory_bgm.wav", true)) {
                    System.err.println("胜利背景音乐加载失败，游戏将继续运行");
                }
            } catch (Exception e) {
                System.err.println("播放胜利音效/音乐失败: " + e.getMessage());
                // 出现异常时禁用所有音频
                audioManager.disableAllAudio();
            }
            
            stage.setScene(victoryScene);
        }
    }
    
    /**
     * 显示关卡完成场景
     */
    public void showLevelCompleteScene() {
        if (gameModel.getGameState() == GameState.LEVEL_COMPLETE) {
            if (!levelCompleteAudioPlayed) {
                try {
                    audioManager.playSoundEffect("victory");
                    audioManager.stopBackgroundMusic();
                    if (!audioManager.playBackgroundMusic("audio/menu_bgm.wav", true)) {
                        System.err.println("关卡完成背景音乐加载失败");
                    }
                } catch (Exception e) {
                    System.err.println("播放关卡完成音频失败: " + e.getMessage());
                }
                levelCompleteAudioPlayed = true;
            }
            stage.setScene(levelCompleteScene);
        }
    }
    
    /**
     * 显示暂停场景
     */
    public void showPauseScene() {
        if (gameModel.getGameState() == GameState.PAUSED) {
            stage.setScene(pauseScene);
        }
    }
    
    /**
     * 创建帮助场景（包含玩法说明和键位说明）
     */
    private void createHelpScene() {
        // 创建主界面布局
        StackPane helpRoot = new StackPane(); 
        
        // 创建背景
        Rectangle background = new Rectangle(GAME_WIDTH, GAME_HEIGHT);
        background.setFill(Color.rgb(20, 40, 60));
        
        // 创建帮助标题
        Text helpTitle = new Text("游戏说明");
        helpTitle.setFont(Font.font("Arial", FontWeight.BOLD, 46));
        helpTitle.setFill(Color.WHITE);
        helpTitle.setStroke(Color.BLACK);
        helpTitle.setStrokeWidth(2);
        
        // 添加光晕效果
        Glow glow = new Glow(0.6);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.LIGHTBLUE);
        shadow.setRadius(15);
        shadow.setInput(glow);
        helpTitle.setEffect(shadow);
        
        // 创建玩法说明区域
        VBox gameplayBox = new VBox(25);
        gameplayBox.setAlignment(Pos.CENTER_LEFT);
        gameplayBox.setPadding(new Insets(15));
        gameplayBox.setMaxWidth(GAME_WIDTH - 140);
        
        // 创建玩法说明标题
        Text gameplayTitle = new Text("玩法说明");
        gameplayTitle.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        gameplayTitle.setFill(Color.LIGHTGREEN);
        gameplayTitle.setStroke(Color.BLACK);
        gameplayTitle.setStrokeWidth(1);
        
        // 创建玩法说明内容，增加列表项之间的间距，优化嵌套列表格式
        VBox gameplayTextBox = new VBox(8);
        gameplayTextBox.setAlignment(Pos.CENTER_LEFT);
        
        // 主列表项 - 基本玩法
        Text item1 = new Text("• 控制玩家坦克消灭所有敌方坦克以获得胜利");
        Text item2 = new Text("• 坦克被敌人子弹击中时，将减少一条生命");
        Text item3 = new Text("• 生命耗尽时游戏结束");
        Text item4 = new Text("• 每消灭一辆敌方坦克会获得分数");
        
        // 道具说明标题
        Text itemsTitle = new Text("• 地图上会随机出现各种道具：");
        
        // 子列表项 - 道具说明 (使用更明显的缩进和不同的前缀)
        HBox shieldBox = new HBox(10);
        shieldBox.setAlignment(Pos.CENTER_LEFT);
        Rectangle shieldBullet = new Rectangle(6, 6);
        shieldBullet.setFill(Color.CYAN);
        Text shieldText = new Text(" 护盾：临时无敌状态");
        shieldBox.getChildren().addAll(new Text("    "), shieldBullet, shieldText);
        
        HBox speedBox = new HBox(10);
        speedBox.setAlignment(Pos.CENTER_LEFT);
        Rectangle speedBullet = new Rectangle(6, 6);
        speedBullet.setFill(Color.GREEN);
        Text speedText = new Text(" 加速：提高坦克移动速度");
        speedBox.getChildren().addAll(new Text("    "), speedBullet, speedText);
        
        HBox powerBox = new HBox(10);
        powerBox.setAlignment(Pos.CENTER_LEFT);
        Rectangle powerBullet = new Rectangle(6, 6);
        powerBullet.setFill(Color.RED);
        Text powerText = new Text(" 火力：增强子弹威力");
        powerBox.getChildren().addAll(new Text("    "), powerBullet, powerText);
        
        HBox lifeBox = new HBox(10);
        lifeBox.setAlignment(Pos.CENTER_LEFT);
        Rectangle lifeBullet = new Rectangle(6, 6);
        lifeBullet.setFill(Color.PINK);
        Text lifeText = new Text(" 生命：增加一条生命");
        lifeBox.getChildren().addAll(new Text("    "), lifeBullet, lifeText);
        
        HBox bombBox = new HBox(10);
        bombBox.setAlignment(Pos.CENTER_LEFT);
        Rectangle bombBullet = new Rectangle(6, 6);
        bombBullet.setFill(Color.YELLOW);
        Text bombText = new Text(" 炸弹：摧毁屏幕上所有敌方坦克");
        bombBox.getChildren().addAll(new Text("    "), bombBullet, bombText);
        
        // 其他列表项
        Text item5 = new Text("• 砖墙可以被子弹摧毁，钢墙无法摧毁");
        Text item6 = new Text("• 按ESC键可以返回主菜单");
        
        // 设置所有文本的样式
        for (Text text : new Text[]{item1, item2, item3, item4, itemsTitle, 
                                   shieldText, speedText, powerText, lifeText, bombText, 
                                   item5, item6}) {
            text.setFont(Font.font("Arial", 18));
            text.setFill(Color.WHITE);
        }
        
        // 将所有元素添加到列表中
        gameplayTextBox.getChildren().addAll(
            item1, item2, item3, item4, itemsTitle,
            shieldBox, speedBox, powerBox, lifeBox, bombBox,
            item5, item6
        );
        
        gameplayBox.getChildren().addAll(gameplayTitle, gameplayTextBox);
        
        // 创建键位说明区域
        VBox controlsBox = new VBox(30);
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(15));
        controlsBox.setMaxWidth(GAME_WIDTH - 120);
        
        // 创建键位说明标题
        Text controlsTitle = new Text("键位说明");
        controlsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 28)); // 与玩法说明标题一致
        controlsTitle.setFill(Color.LIGHTYELLOW);
        controlsTitle.setStroke(Color.BLACK);
        controlsTitle.setStrokeWidth(1);
        
        // 创建键位说明内容（使用HBox水平布局）
        HBox controlsContentBox = new HBox(60);
        controlsContentBox.setAlignment(Pos.CENTER);
        controlsContentBox.setMaxWidth(GAME_WIDTH - 160);
        
        // 移动键位说明
        VBox movementBox = new VBox(20);
        movementBox.setAlignment(Pos.CENTER);
        movementBox.setMinWidth(180);
        movementBox.setMaxWidth(200);
        
        Text movementTitle = new Text("移动控制");
        movementTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        movementTitle.setFill(Color.WHITE);
        
        GridPane movementGrid = new GridPane();
        movementGrid.setHgap(10);
        movementGrid.setVgap(10);
        movementGrid.setAlignment(Pos.CENTER);
        movementGrid.setPadding(new Insets(15, 0, 15, 0));
        
        // 上
        StackPane upKey = createKeyRect("W");
        movementGrid.add(upKey, 1, 0);
        
        // 左
        StackPane leftKey = createKeyRect("A");
        movementGrid.add(leftKey, 0, 1);
        
        // 下
        StackPane downKey = createKeyRect("S");
        movementGrid.add(downKey, 1, 1);
        
        // 右
        StackPane rightKey = createKeyRect("D");
        movementGrid.add(rightKey, 2, 1);
        
        Text moveDescription = new Text("或者使用方向键↑↓←→");
        moveDescription.setFont(Font.font("Arial", 16));
        moveDescription.setFill(Color.WHITE);
        
        movementBox.getChildren().addAll(movementTitle, movementGrid, moveDescription);
        
        // 功能键位说明
        VBox actionBox = new VBox(20);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setMinWidth(180);
        actionBox.setMaxWidth(200);
        
        Text actionTitle = new Text("功能控制");
        actionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        actionTitle.setFill(Color.WHITE);
        
        // 创建一个GridPane来统一功能控制的布局，与移动控制保持一致
        GridPane actionGrid = new GridPane();
        actionGrid.setHgap(10);
        actionGrid.setVgap(15);
        actionGrid.setAlignment(Pos.CENTER);
        actionGrid.setPadding(new Insets(15, 0, 15, 0)); // 与移动控制相同的padding
        
        // 发射子弹
        StackPane spaceKey = createKeyRect("空格");
        Text fireText = new Text("发射子弹");
        fireText.setFont(Font.font("Arial", 16));
        fireText.setFill(Color.WHITE);
        
        // 暂停游戏
        StackPane escKey = createKeyRect("ESC");
        Text pauseText = new Text("暂停游戏");
        pauseText.setFont(Font.font("Arial", 16));
        pauseText.setFill(Color.WHITE);
        
        // 添加到GridPane，第一列是按键，第二列是说明文本
        actionGrid.add(spaceKey, 0, 0);
        actionGrid.add(fireText, 1, 0);
        actionGrid.add(escKey, 0, 1);
        actionGrid.add(pauseText, 1, 1);
        
        actionBox.getChildren().addAll(actionTitle, actionGrid);
        
        // 添加移动和功能键位说明到水平布局
        controlsContentBox.getChildren().addAll(movementBox, actionBox);
        
        // 将标题和统计信息位置添加到键位说明区域
        controlsBox.getChildren().addAll(controlsTitle, controlsContentBox);
        
        // 在底部添加ESC键使用提示
        Text escHint = new Text("按下ESC键也可直接返回主菜单");
        escHint.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        escHint.setFill(Color.LIGHTBLUE);
        escHint.setOpacity(0.8);

        // 创建返回按钮
        Button backButton = createStyledButton("返回主菜单", 200, 50);
        backButton.setOnAction(e -> {
            try {
                audioManager.playSoundEffect("button_click");
            } catch (Exception ex) {
                System.err.println("播放按钮音效失败: " + ex.getMessage());
            }
            showMainMenu();
        });
        
        // 为按钮创建单独的容器，添加ESC提示和按钮
        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 5, 0));
        buttonBox.getChildren().addAll(escHint, backButton);
        
        // 组合全部内容，调整各部分间距
        VBox helpContent = new VBox();
        helpContent.setAlignment(Pos.CENTER);
        helpContent.setPadding(new Insets(20));
        helpContent.setSpacing(25); // 使用setSpacing统一设置间距
        helpContent.getChildren().addAll(helpTitle, gameplayBox, controlsBox, buttonBox);
        
        // 创建可滚动面板，防止内容溢出，确保内容在黑色背景框内
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(helpContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // 隐藏水平滚动条
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // 根据需要显示垂直滚动条
        
        // 设置滚动面板的样式，使其透明并适应内部黑色背景框
        scrollPane.setStyle(
            "-fx-background: transparent; " +
            "-fx-background-color: transparent; " + 
            "-fx-padding: 0; " + 
            "-fx-border-width: 0; " +
            "-fx-control-inner-background: transparent;"
        );
        scrollPane.setPannable(true); // 允许鼠标拖动
        
        // 设置滚动面板的大小，使其与背景矩形大小一致，小一点以确保内容不会溢出
        scrollPane.setPrefSize(GAME_WIDTH - 100, GAME_HEIGHT - 100);
        scrollPane.setMaxSize(GAME_WIDTH - 100, GAME_HEIGHT - 100);
        
        // 确保内容默认显示在顶部
        scrollPane.setVvalue(0);
        
        // 创建半透明面板作为背景
        Rectangle helpBg = new Rectangle(GAME_WIDTH - 80, GAME_HEIGHT - 80);
        helpBg.setFill(Color.rgb(0, 0, 0, 0.7));
        helpBg.setArcWidth(20);
        helpBg.setArcHeight(20);
        helpBg.setStroke(Color.LIGHTBLUE);
        helpBg.setStrokeWidth(2);
        
        // 组合内容和背景
        StackPane contentPane = new StackPane();
        contentPane.getChildren().addAll(helpBg, scrollPane);
        
        // 将所有元素添加到主布局
        helpRoot.getChildren().addAll(background, contentPane);
        
        // 创建场景并添加键盘事件处理
        helpScene = new Scene(helpRoot, GAME_WIDTH, GAME_HEIGHT);
        
        // 添加ESC键返回主菜单功能
        helpScene.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                try {
                    audioManager.playSoundEffect("button_click");
                } catch (Exception ex) {
                    System.err.println("播放按钮音效失败: " + ex.getMessage());
                }
                showMainMenu();
            }
        });
    }
    
    /**
     * 为键位说明创建键盘按键矩形
     * 
     * @param key 按键文本
     * @return 样式化的按键矩形
     */
    private StackPane createKeyRect(String key) {
        Rectangle keyRect = new Rectangle(50, 50);
        keyRect.setFill(Color.rgb(60, 60, 60));
        keyRect.setArcWidth(10);
        keyRect.setArcHeight(10);
        keyRect.setStroke(Color.LIGHTGRAY);
        keyRect.setStrokeWidth(2);
        
        Text keyText = new Text(key);
        keyText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        keyText.setFill(Color.WHITE);
        
        StackPane keyPane = new StackPane(keyRect, keyText);
        return keyPane;
    }
    
    /**
     * 显示帮助场景
     */
    public void showHelpScene() {
        try {
            // 停止其他音乐，播放菜单音乐
            audioManager.stopBackgroundMusic();
            // 尝试播放音乐，但如果失败也继续执行
            if (!audioManager.playBackgroundMusic("audio/menu_bgm.wav", true)) {
                System.err.println("菜单背景音乐加载失败");
            }
        } catch (Exception e) {
            System.err.println("播放帮助界面音乐失败: " + e.getMessage());
            // 出现异常时禁用所有音频
            audioManager.disableAllAudio();
        }
        
        stage.setScene(helpScene);
        
        // 确保帮助内容滚动到顶部
        javafx.application.Platform.runLater(() -> {
            // 查找ScrollPane组件并设置其滚动位置为顶部
            findScrollPaneAndScrollToTop(helpScene.getRoot());
        });
    }
    
    /**
     * 递归查找ScrollPane并将其滚动到顶部
     * 
     * @param node 要搜索的节点
     */
    private void findScrollPaneAndScrollToTop(javafx.scene.Node node) {
        if (node instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) node;
            scrollPane.setVvalue(0); // 滚动到顶部
            return;
        }
        
        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                findScrollPaneAndScrollToTop(child);
            }
        }
    }
    
    /**
     * 开始游戏
     */
    private void startGame() {
        try {
            // 播放游戏开始音效，如果失败也继续游戏
            boolean soundPlayed = audioManager.playSoundEffect("game_start");
            if (!soundPlayed) {
                System.err.println("游戏开始音效播放失败，但游戏将继续");
            }
        } catch (Exception e) {
            System.err.println("播放游戏开始音效失败: " + e.getMessage());
            // 禁用所有音频以避免进一步问题
            audioManager.disableAllAudio();
        }
        
        // 无论音频问题如何，都确保游戏继续
        gameModel.initGame();
        showGameScene();
    }
    
    /**
     * 进入下一关
     */
    private void nextLevel() {
        try {
            // 播放游戏开始音效
            boolean soundPlayed = audioManager.playSoundEffect("game_start");
            if (!soundPlayed) {
                System.err.println("游戏开始音效播放失败，但游戏将继续");
            }
        } catch (Exception e) {
            System.err.println("播放游戏开始音效失败: " + e.getMessage());
            // 禁用所有音频以避免进一步问题
            audioManager.disableAllAudio();
        }
        
        // 无论音频问题如何，都确保游戏继续
        gameModel.nextLevel();
        showGameScene();
    }
    
    /**
     * 恢复游戏
     */
    private void resumeGame() {
        gameModel.resumeGame();
        showGameScene();
    }
    
    /**
     * 渲染游戏画面
     */
    public void render() {
        // 清空画布
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        // 绘制墙体
        for (Wall wall : gameModel.getWalls()) {
            Image wallImage;
            if (wall instanceof BrickWall) {
                wallImage = resourceManager.getImage("brick_wall");
            } else if (wall instanceof SteelWall) {
                wallImage = resourceManager.getImage("steel_wall");
            } else {
                continue;
            }
            
            gc.drawImage(wallImage, wall.getX(), wall.getY(), wall.getWidth(), wall.getHeight());
        }
        
        // 绘制道具
        for (Item item : gameModel.getItems()) {
            String imageName;
            switch (item.getType()) {
                case SHIELD:
                    imageName = "item_shield";
                    break;
                case SPEED:
                    imageName = "item_speed";
                    break;
                case POWER:
                    imageName = "item_power";
                    break;
                case LIFE:
                    imageName = "item_life";
                    break;
                case BOMB:
                    imageName = "item_bomb";
                    break;
                default:
                    imageName = "item_default";
            }
            
            Image itemImage = resourceManager.getImage(imageName);
            if (itemImage != null) {
                // 使道具闪烁，使其更加醒目
                if (System.currentTimeMillis() % 1000 < 800) {
                    gc.setEffect(glowEffect);
                    gc.drawImage(itemImage, item.getX(), item.getY(), item.getWidth(), item.getHeight());
                    gc.setEffect(null);
                }
            } else {
                // 如果没有找到道具图片，绘制占位符
                gc.setFill(Color.YELLOW);
                gc.fillRect(item.getX(), item.getY(), item.getWidth(), item.getHeight());
            }
        }
        
        // 绘制玩家坦克
        PlayerTank playerTank = gameModel.getPlayerTank();
        if (playerTank != null && playerTank.isAlive()) {
            // 获取坦克图像
            String directionStr;
            switch (playerTank.getDirection()) {
                case UP:
                    directionStr = "up";
                    break;
                case RIGHT:
                    directionStr = "right";
                    break;
                case DOWN:
                    directionStr = "down";
                    break;
                case LEFT:
                    directionStr = "left";
                    break;
                default:
                    directionStr = "up";
            }
            
            Image tankImage = resourceManager.getImage("player_tank_" + directionStr);
            
            // 如果玩家处于无敌状态，闪烁显示
            if (playerTank.isInvincible() && System.currentTimeMillis() % 500 < 250) {
                // 在无敌状态闪烁时不绘制坦克
            } else {
                // 根据玩家特殊状态添加不同的视觉效果
                if (playerTank.hasShield()) {
                    // 护盾效果 - 蓝色阴影
                    gc.setEffect(shadowEffect);
                    gc.drawImage(tankImage, playerTank.getX(), playerTank.getY(), 
                                playerTank.getWidth(), playerTank.getHeight());
                    gc.setEffect(null);
                }
                else if (playerTank.hasSpeedBoost()) {
                    // 速度提升效果 - 绿色轮廓
                    gc.drawImage(tankImage, playerTank.getX(), playerTank.getY(), 
                                playerTank.getWidth(), playerTank.getHeight());
                    
                    gc.setStroke(Color.GREEN);
                    gc.setLineWidth(2);
                    gc.strokeRect(playerTank.getX(), playerTank.getY(), 
                                playerTank.getWidth(), playerTank.getHeight());
                }
                else if (playerTank.hasPowerUp()) {
                    // 火力增强效果 - 红色光晕
                    Glow redGlow = new Glow(0.5);
                    gc.setEffect(redGlow);
                    gc.drawImage(tankImage, playerTank.getX(), playerTank.getY(), 
                                playerTank.getWidth(), playerTank.getHeight());
                    gc.setEffect(null);
                }
                else {
                    // 正常状态
                    gc.drawImage(tankImage, playerTank.getX(), playerTank.getY(), 
                                playerTank.getWidth(), playerTank.getHeight());
                }
            }
        }
        
        // 绘制敌人坦克
        for (EnemyTank enemyTank : gameModel.getEnemyTanks()) {
            // 获取敌人坦克图像
            String directionStr;
            switch (enemyTank.getDirection()) {
                case UP:
                    directionStr = "up";
                    break;
                case RIGHT:
                    directionStr = "right";
                    break;
                case DOWN:
                    directionStr = "down";
                    break;
                case LEFT:
                    directionStr = "left";
                    break;
                default:
                    directionStr = "up";
            }
            
            Image tankImage = resourceManager.getImage("enemy_tank_" + directionStr);
            gc.drawImage(tankImage, enemyTank.getX(), enemyTank.getY(), 
                        enemyTank.getWidth(), enemyTank.getHeight());
        }
        
        // 绘制子弹
        for (Bullet bullet : gameModel.getBullets()) {
            Image bulletImage = resourceManager.getImage("bullet");
            
            // 如果是增强子弹，添加特效并绘制更大
            if (bullet.isPowered()) {
                gc.setEffect(glowEffect);
                gc.drawImage(bulletImage, bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight());
                gc.setEffect(null);
            } else {
                gc.drawImage(bulletImage, bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight());
            }
        }
        
        // 更新HUD信息
        updateHUD();
    }
    
    /**
     * 更新HUD信息
     */
    private void updateHUD() {
        PlayerTank playerTank = gameModel.getPlayerTank();
        
        // 更新生命值文本
        if (playerTank != null) {
            Text livesText = (Text) ((HBox) ((VBox) ((StackPane) hudPanel.getChildren().get(0)).getChildren().get(1)).getChildren().get(3)).getChildren().get(1);
            livesText.setText("生命: " + playerTank.getLives());
        }
        
        // 更新状态效果
        updateStatusEffects();
    }
    
    /**
     * 设置键盘事件处理
     * 
     * @param onKeyPressed 按键按下事件处理器
     * @param onKeyReleased 按键释放事件处理器
     */
    public void setKeyHandlers(javafx.event.EventHandler<KeyEvent> onKeyPressed, 
                              javafx.event.EventHandler<KeyEvent> onKeyReleased) {
        gameScene.setOnKeyPressed(onKeyPressed);
        gameScene.setOnKeyReleased(onKeyReleased);
    }
    
    /**
     * 创建关卡完成场景
     */
    private void createLevelCompleteScene() {
        // 创建主界面布局
        StackPane levelCompleteRoot = new StackPane();
        
        // 创建背景
        Rectangle background = new Rectangle(GAME_WIDTH, GAME_HEIGHT);
        background.setFill(Color.rgb(30, 50, 80)); // 蓝色背景表示关卡完成
        
        // 添加装饰元素 - 玩家坦克图像
        ImageView playerTank = new ImageView(resourceManager.getImage("player_tank_up"));
        playerTank.setFitWidth(100);
        playerTank.setFitHeight(100);
        playerTank.setTranslateX(-GAME_WIDTH / 4);
        playerTank.setTranslateY(-GAME_HEIGHT / 4);
        playerTank.setOpacity(0.8);
        
        // 添加装饰元素 - 敌人坦克图像（显示为倒下的）
        ImageView enemyTank = new ImageView(resourceManager.getImage("enemy_tank_down"));
        enemyTank.setFitWidth(80);
        enemyTank.setFitHeight(80);
        enemyTank.setTranslateX(GAME_WIDTH / 4);
        enemyTank.setTranslateY(GAME_HEIGHT / 5);
        enemyTank.setRotate(90); // 侧倒表示被击败
        enemyTank.setOpacity(0.6);
        
        // 创建关卡完成文本
        Text levelCompleteText = new Text("关卡完成！");
        levelCompleteText.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        levelCompleteText.setFill(Color.LIGHTBLUE);
        levelCompleteText.setStroke(Color.BLACK);
        levelCompleteText.setStrokeWidth(2);
        
        // 添加光晕效果
        Glow glow = new Glow(0.7);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.DEEPSKYBLUE);
        shadow.setRadius(12);
        shadow.setInput(glow);
        levelCompleteText.setEffect(shadow);
        
        // 创建统计信息文本
        VBox statsBox = new VBox(15);
        statsBox.setAlignment(Pos.CENTER);
        
        // 当前关卡文本
        Text currentLevelText = new Text();
        currentLevelText.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        currentLevelText.setFill(Color.WHITE);
        currentLevelText.textProperty().bind(gameModel.levelProperty().asString("关卡: %d"));
        
        // 得分文本
        Text scoreText = new Text();
        scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        scoreText.setFill(Color.WHITE);
        scoreText.textProperty().bind(gameModel.scoreProperty().asString("当前得分: %d"));
        
        // 将文本添加到统计信息框中
        statsBox.getChildren().addAll(currentLevelText, scoreText);
        
        // 设置标题和统计信息位置
        VBox textBox = new VBox(30);
        textBox.setAlignment(Pos.CENTER);
        textBox.getChildren().addAll(levelCompleteText, statsBox);
         
        // 创建按钮面板
        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMaxWidth(300);
        buttonBox.setPadding(new Insets(20));
        
        // 创建下一关按钮
        Button nextLevelButton = createStyledButton("进入下一关", 200, 50);
        nextLevelButton.setOnAction(e -> {
            try {
                audioManager.playSoundEffect("button_click");
            } catch (Exception ex) {
                System.err.println("播放按钮音效失败: " + ex.getMessage());
            }
            nextLevel();
        });
        
        // 创建保存游戏按钮
        Button saveGameButton = createStyledButton("保存游戏", 200, 50);
        saveGameButton.setOnAction(e -> {
            try {
                audioManager.playSoundEffect("button_click");
                
                // 保存游戏逻辑将在任务3中实现
                saveGame();
            } catch (Exception ex) {
                System.err.println("播放按钮音效或保存游戏失败: " + ex.getMessage());
            }
        });
        
        // 创建返回主菜单按钮
        Button menuButton = createStyledButton("返回主菜单", 200, 50);
        menuButton.setOnAction(e -> {
            try {
                audioManager.playSoundEffect("button_click");
            } catch (Exception ex) {
                System.err.println("播放按钮音效失败: " + ex.getMessage());
            }
            showMainMenu();
        });
        
        // 添加按钮到面板
        buttonBox.getChildren().addAll(nextLevelButton, saveGameButton, menuButton);
        
        // 创建半透明面板作为按钮的背景
        Rectangle buttonBg = new Rectangle(300, 200); // 增加高度以适应新按钮
        buttonBg.setFill(Color.rgb(0, 0, 0, 0.7));
        buttonBg.setArcWidth(20);
        buttonBg.setArcHeight(20);
        buttonBg.setStroke(Color.LIGHTBLUE);
        
        // 组合按钮和背景
        StackPane buttonPane = new StackPane(buttonBg, buttonBox);
        
        // 使用 VBox 对文本和按钮进行垂直布局
        VBox mainBox = new VBox(60, textBox, buttonPane);
        mainBox.setAlignment(Pos.CENTER);
        
        // 将所有装饰元素和新的布局添加到主布局
        levelCompleteRoot.getChildren().clear();
        levelCompleteRoot.getChildren().addAll(background, playerTank, enemyTank, mainBox);
        
        // 创建场景
        levelCompleteScene = new Scene(levelCompleteRoot, GAME_WIDTH, GAME_HEIGHT);
        
        // 移除对键盘监听
        levelCompleteScene.setOnKeyPressed(null);
    }
    
    /**
     * 保存游戏状态
     */
    private void saveGame() {
        // 创建一个文本输入对话框
        TextInputDialog dialog = new TextInputDialog("存档_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        dialog.setTitle("保存游戏");
        dialog.setHeaderText("请输入存档名称");
        dialog.setContentText("存档名称:");
        
        // 获取用户输入
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String saveName = result.get().trim();
            
            // 调用GameModel的saveGame方法保存游戏
            boolean success = gameModel.saveGame(saveName);
            
            // 显示保存结果
            Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setTitle("保存游戏");
            alert.setHeaderText(success ? "保存成功" : "保存失败");
            alert.setContentText(success ? "游戏进度已保存！" : "保存游戏时出现错误，请重试。");
            alert.showAndWait();
        }
    }
    
    /**
     * 加载游戏存档
     */
    private void loadGame() {
        // 获取所有存档
        List<GameSave> saves = gameModel.getAllSaves();
        
        if (saves.isEmpty()) {
            // 如果没有存档，显示提示信息
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("加载游戏");
            alert.setHeaderText("没有可用的存档");
            alert.setContentText("请先创建一个游戏存档。");
            alert.showAndWait();
            return;
        }
        
        // 创建存档选择对话框
        ChoiceDialog<GameSave> dialog = new ChoiceDialog<>(saves.get(0), saves);
        dialog.setTitle("加载游戏");
        dialog.setHeaderText("请选择一个存档");
        dialog.setContentText("存档:");
        
        // 设置单元格工厂，自定义显示存档信息
        dialog.getItems().setAll(saves);
        
        // 获取用户选择
        Optional<GameSave> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            GameSave selectedSave = result.get();
            
            // 调用GameModel的loadGame方法加载游戏
            boolean success = gameModel.loadGame(selectedSave.getSaveName());
            
            if (success) {
                // 直接更新游戏界面状态
                showGameScene();
                
                // 播放游戏开始音效
                try {
                    audioManager.playSoundEffect("game_start");
                } catch (Exception ex) {
                    System.err.println("播放音效失败: " + ex.getMessage());
                }
            } else {
                // 显示加载失败信息
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("加载游戏");
                alert.setHeaderText("加载失败");
                alert.setContentText("无法加载选定的存档，请重试。");
                alert.showAndWait();
            }
        }
    }
    
    /**
     * 创建音乐设置场景
     */
    private void createMusicSettingsScene() {
        StackPane root = new StackPane();
        Rectangle background = new Rectangle(GAME_WIDTH, GAME_HEIGHT);
        background.setFill(Color.rgb(20, 40, 60));
        
        Text title = new Text("背景音乐设置");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 46));
        title.setFill(Color.WHITE);
        title.setStroke(Color.BLACK);
        title.setStrokeWidth(2);
        Glow glow = new Glow(0.6);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.LIGHTBLUE);
        shadow.setRadius(15);
        shadow.setInput(glow);
        title.setEffect(shadow);
        
        Label sliderLabel = new Label("音量:");
        sliderLabel.setTextFill(Color.WHITE);
        // 使用全局音量初始化滑块（0-100）
        double initialVol = audioManager.getGlobalVolume() * 100;
        Slider volumeSlider = new Slider(0, 100, initialVol);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(25);
        volumeSlider.setBlockIncrement(5);
        HBox sliderBox = new HBox(10, sliderLabel, volumeSlider);
        sliderBox.setAlignment(Pos.CENTER);
 
        // 滑块监听：统一设置全局音量，0即静音，100最大音量
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue() / 100.0;
            audioManager.setGlobalVolume(vol);
        });
        
        Button backButton = createStyledButton("返回主菜单", 200, 50);
        backButton.setOnAction(e -> {
            try { audioManager.playSoundEffect("button_click"); } catch (Exception ex) { System.err.println("播放按钮音效失败: " + ex.getMessage()); }
            showMainMenu();
        });
        
        VBox content = new VBox(30, title, sliderBox, backButton);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        
        Rectangle panelBg = new Rectangle(GAME_WIDTH - 80, GAME_HEIGHT - 80);
        panelBg.setFill(Color.rgb(0, 0, 0, 0.7));
        panelBg.setArcWidth(20);
        panelBg.setArcHeight(20);
        panelBg.setStroke(Color.LIGHTBLUE);
        
        StackPane panelPane = new StackPane(panelBg, content);
        root.getChildren().addAll(background, panelPane);
        
        musicSettingsScene = new Scene(root, GAME_WIDTH, GAME_HEIGHT);
        musicSettingsScene.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                try { audioManager.playSoundEffect("button_click"); } catch (Exception ex) { System.err.println("播放按钮音效失败: " + ex.getMessage()); }
                showMainMenu();
            }
        });
    }
    
    /**
     * 显示音乐设置场景
     */
    public void showMusicSettingsScene() {
        stage.setScene(musicSettingsScene);
        gameModel.setGameState(GameState.MENU);
    }
}