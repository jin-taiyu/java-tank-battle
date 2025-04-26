package com.tankbattle.model.level;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 关卡配置类，定义一个关卡的所有参数
 * 
 * @author Taiyu Jin
 */
public class LevelConfig {
    private int levelNumber;
    private String levelName;
    private int enemyTankCount;
    private List<WallConfig> walls;
    private PlayerSpawnConfig playerSpawn;
    private List<EnemySpawnConfig> enemySpawns;
    private Map<String, Object> additionalProperties;
    
    /**
     * 默认构造函数
     */
    public LevelConfig() {
        this.walls = new ArrayList<>();
        this.enemySpawns = new ArrayList<>();
        this.additionalProperties = new HashMap<>();
    }
    
    /**
     * 创建关卡配置
     * 
     * @param levelNumber 关卡编号
     * @param levelName 关卡名称
     * @param enemyTankCount 敌方坦克数量
     */
    public LevelConfig(int levelNumber, String levelName, int enemyTankCount) {
        this();
        this.levelNumber = levelNumber;
        this.levelName = levelName;
        this.enemyTankCount = enemyTankCount;
    }
    
    // Getter和Setter方法
    
    public int getLevelNumber() {
        return levelNumber;
    }
    
    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }
    
    public String getLevelName() {
        return levelName;
    }
    
    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }
    
    public int getEnemyTankCount() {
        return enemyTankCount;
    }
    
    public void setEnemyTankCount(int enemyTankCount) {
        this.enemyTankCount = enemyTankCount;
    }
    
    public List<WallConfig> getWalls() {
        return walls;
    }
    
    public void setWalls(List<WallConfig> walls) {
        this.walls = walls;
    }
    
    public void addWall(WallConfig wall) {
        this.walls.add(wall);
    }
    
    public PlayerSpawnConfig getPlayerSpawn() {
        return playerSpawn;
    }
    
    public void setPlayerSpawn(PlayerSpawnConfig playerSpawn) {
        this.playerSpawn = playerSpawn;
    }
    
    public List<EnemySpawnConfig> getEnemySpawns() {
        return enemySpawns;
    }
    
    public void setEnemySpawns(List<EnemySpawnConfig> enemySpawns) {
        this.enemySpawns = enemySpawns;
    }
    
    public void addEnemySpawn(EnemySpawnConfig enemySpawn) {
        this.enemySpawns.add(enemySpawn);
    }
    
    public Object getAdditionalProperty(String key) {
        return additionalProperties.get(key);
    }
    
    public void setAdditionalProperty(String key, Object value) {
        this.additionalProperties.put(key, value);
    }
    
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }
    
    /**
     * 墙体配置
     */
    public static class WallConfig {
        private int x;
        private int y;
        private int width;
        private int height;
        private String type; // "brick" or "steel"
        
        public WallConfig() {
        }
        
        public WallConfig(int x, int y, int width, int height, String type) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = type;
        }
        
        public int getX() {
            return x;
        }
        
        public void setX(int x) {
            this.x = x;
        }
        
        public int getY() {
            return y;
        }
        
        public void setY(int y) {
            this.y = y;
        }
        
        public int getWidth() {
            return width;
        }
        
        public void setWidth(int width) {
            this.width = width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public void setHeight(int height) {
            this.height = height;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
    }
    
    /**
     * 玩家出生点配置
     */
    public static class PlayerSpawnConfig {
        private int x;
        private int y;
        
        public PlayerSpawnConfig() {
        }
        
        public PlayerSpawnConfig(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public int getX() {
            return x;
        }
        
        public void setX(int x) {
            this.x = x;
        }
        
        public int getY() {
            return y;
        }
        
        public void setY(int y) {
            this.y = y;
        }
    }
    
    /**
     * 敌人出生点配置
     */
    public static class EnemySpawnConfig {
        private int x;
        private int y;
        
        public EnemySpawnConfig() {
        }
        
        public EnemySpawnConfig(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public int getX() {
            return x;
        }
        
        public void setX(int x) {
            this.x = x;
        }
        
        public int getY() {
            return y;
        }
        
        public void setY(int y) {
            this.y = y;
        }
    }
}
