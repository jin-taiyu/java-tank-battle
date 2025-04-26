package com.tankbattle.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.tankbattle.model.entity.EnemyTank;
import com.tankbattle.model.entity.PlayerTank;
import com.tankbattle.model.entity.Tank;
import com.tankbattle.model.entity.Bullet;
import com.tankbattle.model.entity.Wall;
import com.tankbattle.model.entity.BrickWall;
import com.tankbattle.model.entity.SteelWall;
import com.tankbattle.model.entity.GameObject;
import com.tankbattle.model.entity.Item;
import com.tankbattle.model.enums.Direction;
import com.tankbattle.model.enums.GameState;
import com.tankbattle.model.enums.ItemType;
import com.tankbattle.model.level.LevelConfig;
import com.tankbattle.model.level.LevelManager;
import com.tankbattle.model.save.GameSave;
import com.tankbattle.model.save.SaveManager;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * 游戏模型类，包含游戏的核心数据和逻辑
 * 
 * @author Taiyu Jin
 */
public class GameModel {
    // 游戏状态
    private GameState gameState;
    
    // 游戏对象
    private PlayerTank playerTank;
    private List<EnemyTank> enemyTanks;
    private List<Bullet> bullets;
    private List<Wall> walls;
    private List<Item> items;
    
    // 游戏数据 (使用JavaFX属性以支持绑定)
    private IntegerProperty score = new SimpleIntegerProperty(0);
    private IntegerProperty level = new SimpleIntegerProperty(1);
    private IntegerProperty selectedLevel = new SimpleIntegerProperty(1); // 添加所选关卡属性
    private IntegerProperty remainingEnemies = new SimpleIntegerProperty(0);
    private Random random;
    
    // 游戏区域大小
    private static final int GAME_WIDTH = 800;
    private static final int GAME_HEIGHT = 600;
    
    // 网格大小
    private static final int GRID_SIZE = 40;
    
    // 道具生成概率相关
    private static final double ITEM_DROP_CHANCE = 0.3; // 敌人被击败时有30%几率掉落道具
    private double itemSpawnTimer = 0;
    private static final double ITEM_SPAWN_INTERVAL = 15.0; // 每15秒尝试自动生成一个道具
    
    // 关卡和存档管理器
    private LevelManager levelManager;
    private SaveManager saveManager;
    
    /**
     * 构造函数
     */
    public GameModel() {
        this.gameState = GameState.MENU;
        this.random = new Random();
        
        // 初始化游戏对象列表
        this.enemyTanks = new ArrayList<>();
        this.bullets = new ArrayList<>();
        this.walls = new ArrayList<>();
        this.items = new ArrayList<>();
        
        // 初始化关卡管理器和存档管理器
        this.levelManager = LevelManager.getInstance();
        this.saveManager = SaveManager.getInstance();
    }
    
    /**
     * 初始化游戏
     */
    public void initGame() {
        // 重置游戏数据
        this.score.set(0);
        this.level.set(selectedLevel.get());
        
        // 初始化玩家坦克
        this.playerTank = new PlayerTank(GAME_WIDTH / 2, GAME_HEIGHT - GRID_SIZE * 2, Direction.UP);
        
        // 清空并初始化敌人坦克、子弹、墙体和道具
        this.enemyTanks.clear();
        this.bullets.clear();
        this.walls.clear();
        this.items.clear();
        
        // 重置道具生成计时器
        this.itemSpawnTimer = 0;
        
        // 设置关卡管理器的当前关卡
        levelManager.setCurrentLevelByNumber(level.get());
        
        // 加载关卡
        loadLevel(level.get());
        
        // 设置游戏状态为运行中
        this.gameState = GameState.RUNNING;
    }
    
    /**
     * 加载关卡
     * 
     * @param levelNumber 关卡编号
     */
    public void loadLevel(int levelNumber) {
        // 清空现有敌人、子弹和道具
        this.enemyTanks.clear();
        this.bullets.clear();
        this.items.clear();
        this.walls.clear();
        
        // 从关卡管理器获取关卡配置
        LevelConfig levelConfig = levelManager.getCurrentLevel();
        if (levelConfig == null) {
            // 如果没有找到关卡配置，使用默认配置
            generateDefaultLevel(levelNumber);
            return;
        }
        
        // 设置关卡敌人数量
        this.remainingEnemies.set(levelConfig.getEnemyTankCount());
        
        // 初始生成的敌人数量
        int initialEnemies = Math.min(4, this.remainingEnemies.get());
        
        // 加载墙体
        loadWalls(levelConfig);
        
        // 设置玩家出生点
        if (levelConfig.getPlayerSpawn() != null) {
            this.playerTank.setX(levelConfig.getPlayerSpawn().getX());
            this.playerTank.setY(levelConfig.getPlayerSpawn().getY());
        }
        
        // 生成敌人坦克
        for (int i = 0; i < initialEnemies; i++) {
            if (levelConfig.getEnemySpawns() != null && !levelConfig.getEnemySpawns().isEmpty()) {
                // 使用配置中的敌人出生点
                int index = i % levelConfig.getEnemySpawns().size();
                LevelConfig.EnemySpawnConfig spawnConfig = levelConfig.getEnemySpawns().get(index);
                spawnEnemyTank(spawnConfig.getX(), spawnConfig.getY());
            } else {
                // 使用默认出生点
                spawnEnemyTank();
            }
        }
        
        // 如果是第2关以上，在关卡开始时生成一个道具
        if (levelNumber >= 2) {
            spawnRandomItem();
        }
    }
    
    /**
     * 从关卡配置加载墙体
     * 
     * @param levelConfig 关卡配置
     */
    private void loadWalls(LevelConfig levelConfig) {
        if (levelConfig.getWalls() == null || levelConfig.getWalls().isEmpty()) {
            // 如果没有墙体配置，使用默认墙体
            generateWalls(levelConfig.getLevelNumber());
            return;
        }
        
        // 根据配置创建墙体
        for (LevelConfig.WallConfig wallConfig : levelConfig.getWalls()) {
            Wall wall;
            if ("steel".equals(wallConfig.getType())) {
                wall = new SteelWall(wallConfig.getX(), wallConfig.getY());
            } else {
                // 默认为砖墙
                wall = new BrickWall(wallConfig.getX(), wallConfig.getY());
            }
            wall.setWidth(wallConfig.getWidth() > 0 ? wallConfig.getWidth() : GRID_SIZE);
            wall.setHeight(wallConfig.getHeight() > 0 ? wallConfig.getHeight() : GRID_SIZE);
            walls.add(wall);
        }
    }
    
    /**
     * 生成默认关卡配置（兼容旧版本）
     * 
     * @param level 关卡编号
     */
    private void generateDefaultLevel(int level) {
        // 设置关卡敌人数量（随关卡增加）
        this.remainingEnemies.set(10 + (level - 1) * 2);
        
        // 初始生成的敌人数量
        int initialEnemies = Math.min(4, this.remainingEnemies.get());
        
        // 生成敌人坦克
        for (int i = 0; i < initialEnemies; i++) {
            spawnEnemyTank();
        }
        
        // 根据关卡生成不同的墙体布局
        generateWalls(level);
    }
    
    /**
     * 进入下一关
     */
    public void nextLevel() {
        // 使用关卡管理器切换到下一关
        LevelConfig nextLevelConfig = levelManager.nextLevel();
        
        if (nextLevelConfig != null) {
            // 增加关卡
            this.level.set(this.level.get() + 1);
            
            // 重置玩家坦克位置
            this.playerTank.respawn(GAME_WIDTH / 2, GAME_HEIGHT - GRID_SIZE * 2, Direction.UP);
            
            // 加载新关卡
            loadLevel(this.level.get());
            
            // 播放关卡开始音效
            AudioManager.getInstance().playSoundEffect("game_start");
            
            // 设置游戏状态为运行中
            this.gameState = GameState.RUNNING;
        } else {
            // 如果没有下一关，则游戏胜利
            this.gameState = GameState.VICTORY;
            AudioManager.getInstance().stopBackgroundMusic();
            AudioManager.getInstance().playBackgroundMusic("victory_bgm.wav", true);
        }
    }
    
    /**
     * 生成敌人坦克
     */
    public void spawnEnemyTank() {
        // 敌人坦克生成点
        int[] spawnPoints = {GRID_SIZE, GAME_WIDTH / 2, GAME_WIDTH - GRID_SIZE * 2};
        
        // 随机选择生成点
        int x = spawnPoints[random.nextInt(spawnPoints.length)];
        int y = GRID_SIZE;
        
        // 检查生成点是否有碰撞
        boolean collision;
        int attempts = 0;
        final int MAX_ATTEMPTS = 10; // 最大尝试次数，防止无限循环
        
        do {
            collision = false;
            for (Tank tank : enemyTanks) {
                if (tank != null && Math.abs(tank.getX() - x) < GRID_SIZE * 2 && Math.abs(tank.getY() - y) < GRID_SIZE * 2) {
                    collision = true;
                    break;
                }
            }
            
            if (collision) {
                x = spawnPoints[random.nextInt(spawnPoints.length)];
                attempts++;
                
                // 如果多次尝试失败，强制在一个位置生成
                if (attempts >= MAX_ATTEMPTS) {
                    x = 0; // 使用左上角作为最后的选择
                    y = 0;
                    collision = false; // 强制退出循环
                    System.out.println("警告：敌人坦克生成点尝试次数过多，强制生成");
                }
            }
        } while (collision);
        
        // 根据关卡难度创建不同类型的敌人坦克
        Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
        EnemyTank enemyTank = new EnemyTank(x, y, direction);
        
        // 根据关卡提升敌人坦克属性
        int currentLevel = level.get();
        if (currentLevel >= 2) {
            enemyTank.setSpeed(enemyTank.getSpeed() * (1.0 + currentLevel * 0.1)); // 每关速度提升10%
        }
        if (currentLevel >= 3) {
            enemyTank.setShootCooldown(enemyTank.getShootCooldown() * 0.8); // 第3关开始射速提升20%
        }
        
        enemyTanks.add(enemyTank);
    }
    
    /**
     * 在指定位置生成敌人坦克
     * 
     * @param x 坦克X坐标
     * @param y 坦克Y坐标
     */
    public void spawnEnemyTank(int x, int y) {
        // 根据关卡难度创建不同类型的敌人坦克
        Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
        EnemyTank enemyTank = new EnemyTank(x, y, direction);
        
        // 根据关卡提升敌人坦克属性
        int currentLevel = level.get();
        if (currentLevel >= 2) {
            enemyTank.setSpeed(enemyTank.getSpeed() * (1.0 + currentLevel * 0.1)); // 每关速度提升10%
        }
        if (currentLevel >= 3) {
            enemyTank.setShootCooldown(enemyTank.getShootCooldown() * 0.8); // 第3关开始射速提升20%
        }
        
        enemyTanks.add(enemyTank);
    }
    
    /**
     * 生成墙体
     * 
     * @param level 关卡编号，用于生成不同的墙体布局
     */
    private void generateWalls(int level) {
        walls.clear();
        
        switch (level) {
            case 1: // 第一关 - 简单布局
                generateLevel1Walls();
                break;
            case 2: // 第二关 - 迷宫布局
                generateLevel2Walls();
                break;
            case 3: // 第三关 - 复杂布局
                generateLevel3Walls();
                break;
            default: // 更高关卡使用随机墙体
                generateRandomWalls();
                break;
        }
    }
    
    /**
     * 生成第一关墙体 - 简单布局
     */
    private void generateLevel1Walls() {
        // 左侧墙体
        for (int i = 0; i < 5; i++) {
            walls.add(new BrickWall(GRID_SIZE * 3, GRID_SIZE * (i + 2)));
        }
        
        // 右侧墙体
        for (int i = 0; i < 5; i++) {
            walls.add(new BrickWall(GAME_WIDTH - GRID_SIZE * 4, GRID_SIZE * (i + 2)));
        }
        
        // 中央钢墙
        for (int i = 0; i < 3; i++) {
            walls.add(new SteelWall(GAME_WIDTH / 2 - GRID_SIZE / 2, GAME_HEIGHT / 2 - GRID_SIZE + i * GRID_SIZE));
        }
        
        // 玩家基地周围的墙
        walls.add(new BrickWall(GAME_WIDTH / 2 - GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 3));
        walls.add(new BrickWall(GAME_WIDTH / 2, GAME_HEIGHT - GRID_SIZE * 3));
        walls.add(new BrickWall(GAME_WIDTH / 2 + GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 3));
        walls.add(new BrickWall(GAME_WIDTH / 2 - GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 4));
        walls.add(new BrickWall(GAME_WIDTH / 2 + GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 4));
    }
    
    /**
     * 生成第二关墙体 - 迷宫布局
     */
    private void generateLevel2Walls() {
        // 上方横向墙体
        for (int i = 0; i < 8; i++) {
            walls.add(new BrickWall(GRID_SIZE * (i + 2), GRID_SIZE * 3));
        }
        
        for (int i = 0; i < 8; i++) {
            walls.add(new BrickWall(GRID_SIZE * (i + 11), GRID_SIZE * 3));
        }
        
        // 中央迷宫
        // 横向墙
        for (int i = 0; i < 4; i++) {
            walls.add(new BrickWall(GRID_SIZE * (i + 4), GAME_HEIGHT / 2));
        }
        
        for (int i = 0; i < 4; i++) {
            walls.add(new BrickWall(GRID_SIZE * (i + 13), GAME_HEIGHT / 2));
        }
        
        // 纵向墙
        for (int i = 0; i < 3; i++) {
            walls.add(new BrickWall(GRID_SIZE * 7, GAME_HEIGHT / 2 - GRID_SIZE * (i + 1)));
        }
        
        for (int i = 0; i < 3; i++) {
            walls.add(new BrickWall(GAME_WIDTH - GRID_SIZE * 8, GAME_HEIGHT / 2 - GRID_SIZE * (i + 1)));
        }
        
        // 钢墙屏障
        for (int i = 0; i < 3; i++) {
            walls.add(new SteelWall(GRID_SIZE * (i + 4), GAME_HEIGHT - GRID_SIZE * 7));
            walls.add(new SteelWall(GAME_WIDTH - GRID_SIZE * (i + 5), GAME_HEIGHT - GRID_SIZE * 7));
        }
        
        // 玩家基地周围的墙
        walls.add(new SteelWall(GAME_WIDTH / 2 - GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 3));
        walls.add(new SteelWall(GAME_WIDTH / 2, GAME_HEIGHT - GRID_SIZE * 3));
        walls.add(new SteelWall(GAME_WIDTH / 2 + GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 3));
        walls.add(new SteelWall(GAME_WIDTH / 2 - GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 4));
        walls.add(new SteelWall(GAME_WIDTH / 2 + GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 4));
    }
    
    /**
     * 生成第三关墙体 - 复杂布局
     */
    private void generateLevel3Walls() {
        // 上方复杂墙体
        for (int i = 0; i < 10; i += 2) {
            walls.add(new SteelWall(GRID_SIZE * (i + 1), GRID_SIZE * 2));
            walls.add(new BrickWall(GRID_SIZE * (i + 2), GRID_SIZE * 2));
        }
        
        // 中间走廊
        for (int i = 0; i < 5; i++) {
            walls.add(new BrickWall(GRID_SIZE * 5, GRID_SIZE * (i + 4)));
            walls.add(new BrickWall(GAME_WIDTH - GRID_SIZE * 6, GRID_SIZE * (i + 4)));
        }
        
        // 中央复杂区域
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if ((i + j) % 2 == 0) {
                    walls.add(new SteelWall(GAME_WIDTH / 2 - GRID_SIZE + i * GRID_SIZE, 
                                           GAME_HEIGHT / 2 - GRID_SIZE + j * GRID_SIZE));
                } else {
                    walls.add(new BrickWall(GAME_WIDTH / 2 - GRID_SIZE + i * GRID_SIZE, 
                                           GAME_HEIGHT / 2 - GRID_SIZE + j * GRID_SIZE));
                }
            }
        }
        
        // 下方障碍
        for (int i = 0; i < 8; i++) {
            if (i % 3 != 1) { // 留下通道
                walls.add(new SteelWall(GRID_SIZE * (i + 2), GAME_HEIGHT - GRID_SIZE * 6));
                walls.add(new SteelWall(GAME_WIDTH - GRID_SIZE * (i + 3), GAME_HEIGHT - GRID_SIZE * 6));
            }
        }
        
        // 玩家基地周围的墙 - 全部使用钢墙
        walls.add(new SteelWall(GAME_WIDTH / 2 - GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 3));
        walls.add(new SteelWall(GAME_WIDTH / 2, GAME_HEIGHT - GRID_SIZE * 3));
        walls.add(new SteelWall(GAME_WIDTH / 2 + GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 3));
        walls.add(new SteelWall(GAME_WIDTH / 2 - GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 4));
        walls.add(new SteelWall(GAME_WIDTH / 2 + GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 4));
    }
    
    /**
     * 生成随机墙体 - 用于更高级别的关卡
     */
    private void generateRandomWalls() {
        // 确保基地周围有保护
        walls.add(new SteelWall(GAME_WIDTH / 2 - GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 3));
        walls.add(new SteelWall(GAME_WIDTH / 2, GAME_HEIGHT - GRID_SIZE * 3));
        walls.add(new SteelWall(GAME_WIDTH / 2 + GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 3));
        walls.add(new SteelWall(GAME_WIDTH / 2 - GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 4));
        walls.add(new SteelWall(GAME_WIDTH / 2 + GRID_SIZE, GAME_HEIGHT - GRID_SIZE * 4));
        
        // 随机生成砖墙
        int brickWallCount = 40 + random.nextInt(20); // 40-60个砖墙
        for (int i = 0; i < brickWallCount; i++) {
            int x = (random.nextInt((GAME_WIDTH / GRID_SIZE) - 2) + 1) * GRID_SIZE;
            int y = (random.nextInt((GAME_HEIGHT / GRID_SIZE) - 6) + 1) * GRID_SIZE;
            
            // 避免在玩家出生点附近生成
            if (Math.abs(x - GAME_WIDTH / 2) < GRID_SIZE * 2 && 
                Math.abs(y - (GAME_HEIGHT - GRID_SIZE * 2)) < GRID_SIZE * 2) {
                continue;
            }
            
            walls.add(new BrickWall(x, y));
        }
        
        // 随机生成钢墙
        int steelWallCount = 10 + random.nextInt(10); // 10-20个钢墙
        for (int i = 0; i < steelWallCount; i++) {
            int x = (random.nextInt((GAME_WIDTH / GRID_SIZE) - 2) + 1) * GRID_SIZE;
            int y = (random.nextInt((GAME_HEIGHT / GRID_SIZE) - 6) + 1) * GRID_SIZE;
            
            // 避免在玩家出生点附近生成
            if (Math.abs(x - GAME_WIDTH / 2) < GRID_SIZE * 2 && 
                Math.abs(y - (GAME_HEIGHT - GRID_SIZE * 2)) < GRID_SIZE * 2) {
                continue;
            }
            
            walls.add(new SteelWall(x, y));
        }
    }
    
    /**
     * 生成随机道具
     */
    public void spawnRandomItem() {
        ItemType[] itemTypes = ItemType.values();
        ItemType randomType = itemTypes[random.nextInt(itemTypes.length)];
        
        // 随机位置，避开墙体和坦克
        int x, y;
        boolean validPosition;
        int attempts = 0;
        final int MAX_ATTEMPTS = 20;
        
        do {
            validPosition = true;
            x = random.nextInt(GAME_WIDTH - GRID_SIZE * 2) + GRID_SIZE;
            y = random.nextInt(GAME_HEIGHT - GRID_SIZE * 4) + GRID_SIZE;
            
            // 检查是否与墙体重叠
            for (Wall wall : walls) {
                if (Math.abs(wall.getX() - x) < GRID_SIZE && Math.abs(wall.getY() - y) < GRID_SIZE) {
                    validPosition = false;
                    break;
                }
            }
            
            // 检查是否与坦克重叠
            if (validPosition) {
                if (playerTank != null && 
                    Math.abs(playerTank.getX() - x) < GRID_SIZE && Math.abs(playerTank.getY() - y) < GRID_SIZE) {
                    validPosition = false;
                }
            }
            
            if (validPosition) {
                for (EnemyTank tank : enemyTanks) {
                    if (Math.abs(tank.getX() - x) < GRID_SIZE && Math.abs(tank.getY() - y) < GRID_SIZE) {
                        validPosition = false;
                        break;
                    }
                }
            }
            
            attempts++;
            if (attempts >= MAX_ATTEMPTS) {
                // 如果尝试多次仍找不到合适位置，则不生成道具
                return;
            }
        } while (!validPosition);
        
        // 创建道具并添加到列表
        Item item = new Item(x, y, randomType);
        items.add(item);
    }
    
    /**
     * 触发全场爆炸道具效果
     */
    private void triggerBombEffect() {
        // 移除所有敌人坦克
        int tanksDestroyed = enemyTanks.size();
        enemyTanks.clear();
        
        // 增加分数
        score.set(score.get() + tanksDestroyed * 100);
        
        // 减少剩余敌人数量
        remainingEnemies.set(Math.max(0, remainingEnemies.get() - tanksDestroyed));
        
        // 播放爆炸音效
        AudioManager.getInstance().playSoundEffect("tank_explosion");
    }
    
    /**
     * 更新游戏状态
     * 
     * @param deltaTime 时间增量
     */
    public void update(double deltaTime) {
        if (gameState != GameState.RUNNING) {
            return;
        }
        
        // 更新玩家坦克
        playerTank.update(deltaTime);
        
        // 更新敌人坦克
        for (EnemyTank enemyTank : new ArrayList<>(enemyTanks)) {
            enemyTank.update(deltaTime);
            
            // 敌人AI行为
            enemyTank.updateAI(deltaTime, playerTank);
            
            // 随机发射子弹
            if (random.nextDouble() < 0.01) {
                Bullet bullet = enemyTank.fire();
                if (bullet != null) {
                    bullets.add(bullet);
                }
            }
        }
        
        // 更新子弹
        for (Bullet bullet : new ArrayList<>(bullets)) {
            bullet.update(deltaTime);
            
            // 检查子弹是否超出边界
            if (bullet.getX() < 0 || bullet.getX() > GAME_WIDTH || 
                bullet.getY() < 0 || bullet.getY() > GAME_HEIGHT) {
                bullets.remove(bullet);
                continue;
            }
            
            // 检查子弹与坦克碰撞
            checkBulletTankCollisions(bullet);
            
            // 检查子弹与墙体碰撞
            checkBulletWallCollisions(bullet);
        }
        
        // 更新道具
        for (Item item : new ArrayList<>(items)) {
            item.update(deltaTime);
            
            // 移除已经消失的道具
            if (!item.isAlive()) {
                items.remove(item);
                continue;
            }
            
            // 检查玩家是否拾取道具
            if (playerTank.isAlive() && checkCollision(playerTank, item)) {
                // 应用道具效果
                if (item.getType() == ItemType.BOMB) {
                    // 全场爆炸特殊处理
                    triggerBombEffect();
                } else {
                    // 其他道具直接应用到玩家
                    item.applyEffect(playerTank);
                }
                
                // 播放道具拾取音效
                AudioManager.getInstance().playSoundEffect("button_click");
                
                // 移除已使用的道具
                items.remove(item);
            }
        }
        
        // 检查坦克与墙体碰撞
        checkTankWallCollisions();
        
        // 检查坦克与坦克碰撞
        checkTankTankCollisions();
        
        // 检查游戏胜利或失败条件
        checkGameConditions();
        
        // 如果敌人数量不足，生成新敌人
        if (enemyTanks.size() < 4 && remainingEnemies.get() > 0) {
            spawnEnemyTank();
            remainingEnemies.set(remainingEnemies.get() - 1);
        }
        
        // 更新道具生成计时器
        itemSpawnTimer += deltaTime;
        if (itemSpawnTimer >= ITEM_SPAWN_INTERVAL) {
            itemSpawnTimer = 0;
            // 随机决定是否生成道具
            if (random.nextDouble() < 0.5 && items.size() < 3) { // 限制同时存在的道具数量
                spawnRandomItem();
            }
        }
    }
    
    /**
     * 检查子弹与坦克碰撞
     * 
     * @param bullet 子弹对象
     */
    private void checkBulletTankCollisions(Bullet bullet) {
        // 检查子弹与玩家坦克碰撞
        if (!bullet.isFromPlayer() && playerTank.isAlive() && checkCollision(bullet, playerTank)) {
            bullets.remove(bullet);
            playerTank.hit();
            
            // 播放坦克爆炸音效
            AudioManager.getInstance().playSoundEffect("tank_explosion");
            
            // 检查玩家是否失败
            if (!playerTank.isAlive()) {
                // 播放坦克爆炸音效
                AudioManager.getInstance().playSoundEffect("tank_explosion");
                
                if (playerTank.getLives() <= 0) {
                    // 游戏结束，播放游戏结束音乐
                    AudioManager.getInstance().stopBackgroundMusic();
                    AudioManager.getInstance().playBackgroundMusic("gameover_bgm.wav", true);
                    gameState = GameState.GAME_OVER;
                } else {
                    // 重生玩家坦克
                    playerTank.respawn(GAME_WIDTH / 2, GAME_HEIGHT - GRID_SIZE * 2, Direction.UP);
                }
            }
            return;
        }
        
        // 检查子弹与敌人坦克碰撞
        if (bullet.isFromPlayer()) {
            for (EnemyTank enemyTank : new ArrayList<>(enemyTanks)) {
                if (checkCollision(bullet, enemyTank)) {
                    bullets.remove(bullet);
                    
                    // 播放坦克爆炸音效
                    AudioManager.getInstance().playSoundEffect("tank_explosion");
                    
                    // 移除敌人坦克
                    enemyTanks.remove(enemyTank);
                    
                    // 增加分数
                    score.set(score.get() + 100);
                    
                    // 随机掉落道具
                    if (random.nextDouble() < ITEM_DROP_CHANCE) {
                        ItemType[] itemTypes = ItemType.values();
                        ItemType randomType = itemTypes[random.nextInt(itemTypes.length)];
                        Item item = new Item(enemyTank.getX(), enemyTank.getY(), randomType);
                        items.add(item);
                    }
                    
                    return;
                }
            }
        }
    }
    
    /**
     * 检查子弹与墙体碰撞
     * 
     * @param bullet 子弹对象
     */
    private void checkBulletWallCollisions(Bullet bullet) {
        for (Wall wall : new ArrayList<>(walls)) {
            if (checkCollision(bullet, wall)) {
                bullets.remove(bullet);
                
                // 播放子弹击中墙体的音效
                AudioManager.getInstance().playSoundEffect("bullet_hit");
                
                // 如果是砖墙，则摧毁，增强子弹可以摧毁钢墙
                if (wall instanceof BrickWall || (bullet.isPowered() && bullet.isFromPlayer())) {
                    walls.remove(wall);
                }
                
                return;
            }
        }
    }
    
    // [其他方法保持原样...]

    /**
     * 检查坦克与墙体碰撞
     */
    private void checkTankWallCollisions() {
        // 检查玩家坦克与墙体碰撞
        if (playerTank != null && playerTank.isAlive()) {
            boolean collision = false;
            for (Wall wall : new ArrayList<>(walls)) {
                if (wall != null && checkCollision(playerTank, wall)) {
                    collision = true;
                    break;
                }
            }
            
            if (collision) {
                playerTank.handleCollision();
            }
            
            // 强制检查边界 - 确保玩家坦克不会移出游戏区域
            enforceBoundary(playerTank);
        }
        
        // 检查敌人坦克与墙体碰撞
        for (EnemyTank enemyTank : new ArrayList<>(enemyTanks)) {
            if (enemyTank != null && enemyTank.isAlive()) {
                boolean collision = false;
                for (Wall wall : new ArrayList<>(walls)) {
                    if (wall != null && checkCollision(enemyTank, wall)) {
                        collision = true;
                        break;
                    }
                }
                
                if (collision) {
                    enemyTank.handleCollision();
                }
                
                // 强制检查边界 - 确保敌人坦克不会移出游戏区域
                enforceBoundary(enemyTank);
            }
        }
    }
    
    /**
     * 确保游戏对象在游戏边界内
     * 
     * @param obj 游戏对象
     */
    private void enforceBoundary(GameObject obj) {
        if (obj == null || !obj.isAlive()) {
            return;
        }
        
        double x = obj.getX();
        double y = obj.getY();
        boolean modified = false;
        
        // 检查X坐标边界
        if (x < 0) {
            x = 0;
            modified = true;
        } else if (x > GAME_WIDTH - obj.getWidth()) {
            x = GAME_WIDTH - obj.getWidth();
            modified = true;
        }
        
        // 检查Y坐标边界
        if (y < 0) {
            y = 0;
            modified = true;
        } else if (y > GAME_HEIGHT - obj.getHeight()) {
            y = GAME_HEIGHT - obj.getHeight();
            modified = true;
        }
        
        // 如果位置被修改，则更新对象坐标
        if (modified) {
            obj.setX(x);
            obj.setY(y);
        }
    }
    
    /**
     * 检查坦克与坦克碰撞
     */
    private void checkTankTankCollisions() {
        // 检查玩家坦克与敌人坦克碰撞
        if (playerTank != null && playerTank.isAlive()) {
            for (EnemyTank enemyTank : new ArrayList<>(enemyTanks)) {
                if (enemyTank != null && enemyTank.isAlive() && checkCollision(playerTank, enemyTank)) {
                    playerTank.handleCollision();
                    enemyTank.handleCollision();
                }
            }
        }
        
        // 检查敌人坦克之间的碰撞
        List<EnemyTank> tankList = new ArrayList<>(enemyTanks);
        for (int i = 0; i < tankList.size(); i++) {
            EnemyTank tank1 = tankList.get(i);
            if (tank1 == null || !tank1.isAlive()) continue;
            
            for (int j = i + 1; j < tankList.size(); j++) {
                EnemyTank tank2 = tankList.get(j);
                if (tank2 == null || !tank2.isAlive()) continue;
                
                if (checkCollision(tank1, tank2)) {
                    tank1.handleCollision();
                    tank2.handleCollision();
                }
            }
        }
    }
    
    /**
     * 检查游戏胜利或失败条件
     */
    private void checkGameConditions() {
        // 检查游戏胜利条件
        if (enemyTanks.isEmpty() && remainingEnemies.get() <= 0) {
            // 播放胜利音效
            AudioManager.getInstance().playSoundEffect("victory");
            
            if (level.get() < 5) { // 增加到5个关卡
                // 进入关卡完成状态，而不是直接进入下一关
                gameState = GameState.LEVEL_COMPLETE;
            } else {
                // 游戏全部通关，播放胜利音乐
                AudioManager.getInstance().stopBackgroundMusic();
                AudioManager.getInstance().playBackgroundMusic("victory_bgm.wav", true);
                gameState = GameState.VICTORY;
            }
        }
        
        // 检查游戏失败条件
        if (!playerTank.isAlive() && playerTank.getLives() <= 0) {
            gameState = GameState.GAME_OVER;
        }
    }
    
    /**
     * 检查两个游戏对象之间的碰撞
     * 
     * @param obj1 游戏对象1
     * @param obj2 游戏对象2
     * @return 是否碰撞
     */
    private boolean checkCollision(GameObject obj1, GameObject obj2) {
        // 增加空检查，防止NullPointerException
        if (obj1 == null || obj2 == null) {
            return false;
        }
        
        // 确保对象有效并处于活动状态
        if (!obj1.isAlive() || !obj2.isAlive()) {
            return false;
        }
        
        // 基本的AABB碰撞检测
        return obj1.getX() < obj2.getX() + obj2.getWidth() &&
               obj1.getX() + obj1.getWidth() > obj2.getX() &&
               obj1.getY() < obj2.getY() + obj2.getHeight() &&
               obj1.getY() + obj1.getHeight() > obj2.getY();
    }
    
    /**
     * 玩家坦克发射子弹
     */
    public void playerFire() {
        if (playerTank.isAlive() && gameState == GameState.RUNNING) {
            Bullet bullet = playerTank.fire();
            if (bullet != null) {
                bullets.add(bullet);
                // 播放发射子弹音效
                AudioManager.getInstance().playSoundEffect("tank_fire");
            }
        }
    }
    
    /**
     * 移动玩家坦克
     * 
     * @param direction 方向
     * @param moving 是否移动
     */
    public void movePlayerTank(Direction direction, boolean moving) {
        if (playerTank.isAlive() && gameState == GameState.RUNNING) {
            playerTank.setDirection(direction);
            playerTank.setMoving(moving);
            
            // 播放坦克移动音效
            if (moving) {
                AudioManager.getInstance().playSoundEffect("tank_move");
            }
        }
    }
    
    /**
     * 暂停游戏
     */
    public void pauseGame() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.PAUSED;
        }
    }
    
    /**
     * 恢复游戏
     */
    public void resumeGame() {
        if (gameState == GameState.PAUSED) {
            gameState = GameState.RUNNING;
        }
    }
    
    /**
     * 重置游戏
     */
    public void resetGame() {
        initGame();
    }
    
    /**
     * 保存游戏
     * 
     * @param saveName 存档名称，如果为null则自动生成
     * @return 是否保存成功
     */
    public boolean saveGame(String saveName) {
        // 创建游戏存档对象
        GameSave gameSave = new GameSave();
        
        // 设置存档名称
        gameSave.setSaveName(saveName);
        
        // 保存游戏核心数据
        gameSave.setLevelNumber(level.get());
        gameSave.setScore(score.get());
        gameSave.setPlayerLives(playerTank.getLives());
        
        // 保存玩家状态
        gameSave.setHasPowerUp(playerTank.isPowered());
        gameSave.setHasShield(playerTank.isShielded());
        gameSave.setHasSpeedBoost(playerTank.isSpeedBoosted());
        
        // 使用存档管理器保存游戏
        return saveManager.saveGame(gameSave);
    }
    
    /**
     * 加载游戏
     * 
     * @param saveName 存档名称
     * @return 是否加载成功
     */
    public boolean loadGame(String saveName) {
        // 使用存档管理器加载游戏
        GameSave gameSave = saveManager.loadGame(saveName);
        
        if (gameSave == null) {
            return false;
        }
        
        // 恢复游戏核心数据
        this.level.set(gameSave.getLevelNumber());
        this.score.set(gameSave.getScore());
        
        // 初始化并设置玩家坦克
        this.playerTank = new PlayerTank(GAME_WIDTH / 2, GAME_HEIGHT - GRID_SIZE * 2, Direction.UP);
        this.playerTank.setLives(gameSave.getPlayerLives());
        
        // 恢复玩家状态
        if (gameSave.isHasPowerUp()) {
            playerTank.setPowered(true);
        }
        if (gameSave.isHasShield()) {
            playerTank.setShielded(true);
        }
        if (gameSave.isHasSpeedBoost()) {
            playerTank.setSpeedBoosted(true);
        }
        
        // 清空并初始化敌人坦克、子弹、墙体和道具
        this.enemyTanks.clear();
        this.bullets.clear();
        this.walls.clear();
        this.items.clear();
        
        // 重置道具生成计时器
        this.itemSpawnTimer = 0;
        
        // 设置关卡管理器的当前关卡
        levelManager.setCurrentLevelByNumber(level.get());
        
        // 加载关卡
        loadLevel(level.get());
        
        // 设置游戏状态为运行中
        this.gameState = GameState.RUNNING;
        
        return true;
    }
    
    /**
     * 获取所有存档
     * 
     * @return 存档列表
     */
    public List<GameSave> getAllSaves() {
        return saveManager.getAllSaves();
    }
    
    // Getter方法
    
    public List<Item> getItems() {
        return items;
    }
    
    // JavaFX属性的Getter方法
    
    public IntegerProperty scoreProperty() {
        return score;
    }
    
    public IntegerProperty levelProperty() {
        return level;
    }
    
    public IntegerProperty remainingEnemiesProperty() {
        return remainingEnemies;
    }
    
    public IntegerProperty selectedLevelProperty() {
        return selectedLevel;
    }
    
    public int getSelectedLevel() {
        return selectedLevel.get();
    }
    
    public void setSelectedLevel(int selectedLevel) {
        this.selectedLevel.set(selectedLevel);
    }
    
    // Getter 和 Setter 方法
    
    public GameState getGameState() {
        return gameState;
    }
    
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
    
    public PlayerTank getPlayerTank() {
        return playerTank;
    }
    
    public List<EnemyTank> getEnemyTanks() {
        return enemyTanks;
    }
    
    public List<Bullet> getBullets() {
        return bullets;
    }
    
    public List<Wall> getWalls() {
        return walls;
    }
    
    public int getScore() {
        return score.get();
    }
    
    public void setScore(int score) {
        this.score.set(score);
    }
    
    public int getLevel() {
        return level.get();
    }
    
    public void setLevel(int level) {
        this.level.set(level);
    }
    
    public int getRemainingEnemies() {
        return remainingEnemies.get();
    }
    
    public void setRemainingEnemies(int remainingEnemies) {
        this.remainingEnemies.set(remainingEnemies);
    }
    
    public static int getGameWidth() {
        return GAME_WIDTH;
    }
    
    public static int getGameHeight() {
        return GAME_HEIGHT;
    }
    
    public static int getGridSize() {
        return GRID_SIZE;
    }
}