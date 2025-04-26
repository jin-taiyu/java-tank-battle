package com.tankbattle.model.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 关卡管理器，管理游戏关卡的加载、切换等
 * 
 * @author Taiyu Jin
 */
public class LevelManager {
    private static LevelManager instance;
    
    private List<LevelConfig> levels;
    private int currentLevelIndex = 0;
    
    /**
     * 私有构造函数，单例模式
     */
    private LevelManager() {
        loadLevels();
    }
    
    /**
     * 获取关卡管理器实例
     * 
     * @return 关卡管理器实例
     */
    public static synchronized LevelManager getInstance() {
        if (instance == null) {
            instance = new LevelManager();
        }
        return instance;
    }
    
    /**
     * 加载所有关卡
     */
    private void loadLevels() {
        this.levels = LevelLoader.loadAllLevels();
    }
    
    /**
     * 重新加载关卡
     */
    public void reloadLevels() {
        loadLevels();
    }
    
    /**
     * 获取所有关卡
     * 
     * @return 关卡列表
     */
    public List<LevelConfig> getLevels() {
        return new ArrayList<>(levels);
    }
    
    /**
     * 获取关卡数量
     * 
     * @return 关卡数量
     */
    public int getLevelCount() {
        return levels.size();
    }
    
    /**
     * 获取当前关卡
     * 
     * @return 当前关卡配置
     */
    public LevelConfig getCurrentLevel() {
        if (levels.isEmpty()) {
            return null;
        }
        return levels.get(currentLevelIndex);
    }
    
    /**
     * 设置当前关卡索引
     * 
     * @param index 关卡索引
     * @return 是否设置成功
     */
    public boolean setCurrentLevelIndex(int index) {
        if (index >= 0 && index < levels.size()) {
            this.currentLevelIndex = index;
            return true;
        }
        return false;
    }
    
    /**
     * 通过关卡编号设置当前关卡
     * 
     * @param levelNumber 关卡编号
     * @return 是否设置成功
     */
    public boolean setCurrentLevelByNumber(int levelNumber) {
        Optional<LevelConfig> levelOpt = levels.stream()
                .filter(l -> l.getLevelNumber() == levelNumber)
                .findFirst();
        
        if (levelOpt.isPresent()) {
            int index = levels.indexOf(levelOpt.get());
            return setCurrentLevelIndex(index);
        }
        return false;
    }
    
    /**
     * 获取当前关卡索引
     * 
     * @return 当前关卡索引
     */
    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }
    
    /**
     * 是否有下一关
     * 
     * @return 是否有下一关
     */
    public boolean hasNextLevel() {
        return currentLevelIndex < levels.size() - 1;
    }
    
    /**
     * 切换到下一关
     * 
     * @return 下一关配置，如果没有下一关则返回null
     */
    public LevelConfig nextLevel() {
        if (hasNextLevel()) {
            currentLevelIndex++;
            return getCurrentLevel();
        }
        return null;
    }
    
    /**
     * 重新开始当前关卡
     * 
     * @return 当前关卡配置
     */
    public LevelConfig restartLevel() {
        return getCurrentLevel();
    }
}
