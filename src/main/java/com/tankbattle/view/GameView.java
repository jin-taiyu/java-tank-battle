package com.tankbattle.view;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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
        
        // 添加按钮到菜单选项面板
        menuOptionsBox.getChildren().addAll(levelSelectorBox, startButton, exitButton);
        
        // 创建半透明面板作为菜单选项的背景
        Rectangle menuBg = new Rectangle(400, 250);
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
            try {
                // 播放游戏结束音效
                audioManager.playSoundEffect("game_over");
                
                // 停止游戏音乐，播放游戏结束音乐
                audioManager.stopBackgroundMusic();
                // 尝试播放音乐，但如果失败也继续执行
                if (!audioManager.playBackgroundMusic("audio/gameover_bgm.wav", true)) {
                    System.err.println("游戏结束背景音乐加载失败，游戏将继续运行");
                }
            } catch (Exception e) {
                System.err.println("播放游戏结束音效/音乐失败: " + e.getMessage());
                // 出现异常时禁用所有音频
                audioManager.disableAllAudio();
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
     * 显示暂停场景
     */
    public void showPauseScene() {
        if (gameModel.getGameState() == GameState.PAUSED) {
            stage.setScene(pauseScene);
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
}