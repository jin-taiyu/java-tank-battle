package com.tankbattle.model.save;

import java.io.Serializable;
import java.util.Date;

/**
 * 游戏存档类，用于保存游戏状态
 * 
 * @author Taiyu Jin
 */
public class GameSave implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String saveName;
    private Date saveDate;
    private int levelNumber;
    private int score;
    private int playerLives;
    private boolean hasPowerUp;
    private boolean hasShield;
    private boolean hasSpeedBoost;
    
    // 存档元数据
    private int gameVersion;
    
    /**
     * 默认构造函数
     */
    public GameSave() {
        this.saveDate = new Date();
        this.gameVersion = 1; // 当前游戏版本
    }
    
    /**
     * 创建游戏存档
     * 
     * @param saveName 存档名称
     * @param levelNumber 关卡编号
     * @param score 得分
     * @param playerLives 玩家生命值
     */
    public GameSave(String saveName, int levelNumber, int score, int playerLives) {
        this();
        this.saveName = saveName;
        this.levelNumber = levelNumber;
        this.score = score;
        this.playerLives = playerLives;
    }
    
    // Getter和Setter方法
    
    public String getSaveName() {
        return saveName;
    }
    
    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }
    
    public Date getSaveDate() {
        return saveDate;
    }
    
    public void setSaveDate(Date saveDate) {
        this.saveDate = saveDate;
    }
    
    public int getLevelNumber() {
        return levelNumber;
    }
    
    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public int getPlayerLives() {
        return playerLives;
    }
    
    public void setPlayerLives(int playerLives) {
        this.playerLives = playerLives;
    }
    
    public boolean isHasPowerUp() {
        return hasPowerUp;
    }
    
    public void setHasPowerUp(boolean hasPowerUp) {
        this.hasPowerUp = hasPowerUp;
    }
    
    public boolean isHasShield() {
        return hasShield;
    }
    
    public void setHasShield(boolean hasShield) {
        this.hasShield = hasShield;
    }
    
    public boolean isHasSpeedBoost() {
        return hasSpeedBoost;
    }
    
    public void setHasSpeedBoost(boolean hasSpeedBoost) {
        this.hasSpeedBoost = hasSpeedBoost;
    }
    
    public int getGameVersion() {
        return gameVersion;
    }
    
    public void setGameVersion(int gameVersion) {
        this.gameVersion = gameVersion;
    }
    
    @Override
    public String toString() {
        return String.format("存档: %s | 关卡: %d | 得分: %d | 日期: %s", 
                saveName, levelNumber, score, saveDate.toString());
    }
}
