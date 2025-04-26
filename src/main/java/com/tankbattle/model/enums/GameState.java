package com.tankbattle.model.enums;

/**
 * 游戏状态枚举
 * 
 * @author Taiyu Jin
 */
public enum GameState {
    /**
     * 主菜单状态
     */
    MENU,
    
    /**
     * 游戏运行状态
     */
    RUNNING,
    
    /**
     * 游戏暂停状态
     */
    PAUSED,
    
    /**
     * 游戏胜利状态
     */
    VICTORY,
    
    /**
     * 游戏失败状态
     */
    GAME_OVER,
    
    /**
     * 关卡完成状态
     */
    LEVEL_COMPLETE
}
