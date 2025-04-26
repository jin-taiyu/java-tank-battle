package com.tankbattle.model.level;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * 关卡加载器，用于从文件加载关卡配置
 * 
 * @author Taiyu Jin
 */
public class LevelLoader {
    
    private static final String DEFAULT_LEVELS_DIRECTORY = "levels";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * 从JSON文件加载单个关卡配置
     * 
     * @param filePath JSON文件路径
     * @return 关卡配置对象
     * @throws IOException 文件读取错误
     * @throws JsonSyntaxException JSON解析错误
     */
    public static LevelConfig loadLevelFromJson(String filePath) throws IOException, JsonSyntaxException {
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, LevelConfig.class);
        }
    }
    
    /**
     * 加载所有关卡配置
     * 
     * @param directory 关卡目录
     * @return 关卡配置列表
     */
    public static List<LevelConfig> loadAllLevels(String directory) {
        List<LevelConfig> levels = new ArrayList<>();
        File dir = new File(directory);
        
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("关卡目录不存在: " + directory);
            return createDefaultLevels();
        }
        
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null || files.length == 0) {
            System.err.println("关卡目录中没有JSON文件: " + directory);
            return createDefaultLevels();
        }
        
        for (File file : files) {
            try {
                LevelConfig level = loadLevelFromJson(file.getAbsolutePath());
                levels.add(level);
                System.out.println("成功加载关卡: " + level.getLevelName());
            } catch (Exception e) {
                System.err.println("加载关卡失败: " + file.getName() + ", 错误: " + e.getMessage());
            }
        }
        
        // 如果没有成功加载任何关卡，返回默认关卡
        if (levels.isEmpty()) {
            return createDefaultLevels();
        }
        
        // 按关卡编号排序
        levels.sort((a, b) -> Integer.compare(a.getLevelNumber(), b.getLevelNumber()));
        return levels;
    }
    
    /**
     * 加载所有关卡配置，使用默认目录
     * 
     * @return 关卡配置列表
     */
    public static List<LevelConfig> loadAllLevels() {
        return loadAllLevels(DEFAULT_LEVELS_DIRECTORY);
    }
    
    /**
     * 创建默认关卡配置并保存到文件
     * 
     * @return 默认关卡配置列表
     */
    public static List<LevelConfig> createDefaultLevels() {
        List<LevelConfig> defaultLevels = new ArrayList<>();
        
        // 创建5个默认关卡
        for (int i = 1; i <= 5; i++) {
            LevelConfig level = createDefaultLevel(i);
            defaultLevels.add(level);
            
            // 尝试将默认关卡保存到文件
            try {
                saveDefaultLevel(level);
            } catch (IOException e) {
                System.err.println("保存默认关卡失败: " + e.getMessage());
            }
        }
        
        return defaultLevels;
    }
    
    /**
     * 创建单个默认关卡配置
     * 
     * @param levelNumber 关卡编号
     * @return 默认关卡配置
     */
    private static LevelConfig createDefaultLevel(int levelNumber) {
        LevelConfig level = new LevelConfig();
        level.setLevelNumber(levelNumber);
        level.setLevelName("第" + levelNumber + "关");
        level.setEnemyTankCount(10 + (levelNumber - 1) * 2); // 关卡越高，敌人越多
        
        // 设置玩家出生点
        LevelConfig.PlayerSpawnConfig playerSpawn = new LevelConfig.PlayerSpawnConfig(400, 500);
        level.setPlayerSpawn(playerSpawn);
        
        // 设置敌人出生点
        List<LevelConfig.EnemySpawnConfig> enemySpawns = new ArrayList<>();
        enemySpawns.add(new LevelConfig.EnemySpawnConfig(100, 100));
        enemySpawns.add(new LevelConfig.EnemySpawnConfig(400, 100));
        enemySpawns.add(new LevelConfig.EnemySpawnConfig(700, 100));
        level.setEnemySpawns(enemySpawns);
        
        // 根据关卡级别设置墙体
        List<LevelConfig.WallConfig> walls = new ArrayList<>();
        
        // 基础墙体
        walls.add(new LevelConfig.WallConfig(250, 300, 40, 40, "brick"));
        walls.add(new LevelConfig.WallConfig(290, 300, 40, 40, "brick"));
        walls.add(new LevelConfig.WallConfig(330, 300, 40, 40, "brick"));
        walls.add(new LevelConfig.WallConfig(450, 300, 40, 40, "brick"));
        walls.add(new LevelConfig.WallConfig(490, 300, 40, 40, "brick"));
        walls.add(new LevelConfig.WallConfig(530, 300, 40, 40, "brick"));
        
        // 中间的墙
        walls.add(new LevelConfig.WallConfig(370, 250, 40, 40, "steel"));
        walls.add(new LevelConfig.WallConfig(410, 250, 40, 40, "steel"));
        
        // 根据关卡级别增加墙体
        for (int i = 0; i < levelNumber; i++) {
            walls.add(new LevelConfig.WallConfig(200 + i * 100, 200, 40, 40, "steel"));
            walls.add(new LevelConfig.WallConfig(200 + i * 100, 400, 40, 40, "brick"));
        }
        
        level.setWalls(walls);
        
        return level;
    }
    
    /**
     * 保存默认关卡到文件
     * 
     * @param level 关卡配置
     * @throws IOException 文件写入错误
     */
    private static void saveDefaultLevel(LevelConfig level) throws IOException {
        File dir = new File(DEFAULT_LEVELS_DIRECTORY);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("无法创建关卡目录: " + DEFAULT_LEVELS_DIRECTORY);
            }
        }
        
        String filePath = DEFAULT_LEVELS_DIRECTORY + "/level_" + level.getLevelNumber() + ".json";
        String json = gson.toJson(level);
        Files.write(Paths.get(filePath), json.getBytes());
    }
}
