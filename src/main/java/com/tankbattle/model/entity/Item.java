package com.tankbattle.model.entity;

import com.tankbattle.model.enums.Direction;
import com.tankbattle.model.enums.ItemType;

/**
 * 游戏道具基类
 * 
 * @author Taiyu Jin
 */
public class Item extends GameObject {
    
    // 道具类型
    protected ItemType type;
    
    // 道具持续时间（秒），对于即时生效的道具(如加血)设为0
    protected double duration;
    
    // 道具显示在地图上的剩余时间
    protected double remainingDisplayTime;
    
    // 道具在地图上显示的最大时间
    private static final double MAX_DISPLAY_TIME = 10.0; // 10秒后消失
    
    /**
     * 构造函数
     * 
     * @param x 道具X坐标
     * @param y 道具Y坐标
     * @param type 道具类型
     */
    public Item(double x, double y, ItemType type) {
        super(x, y, Direction.UP); // 道具没有方向，使用默认值
        this.type = type;
        this.width = 30; // 道具默认宽度
        this.height = 30; // 道具默认高度
        this.remainingDisplayTime = MAX_DISPLAY_TIME;
        
        // 根据道具类型设置持续时间
        switch (type) {
            case SHIELD:
                this.duration = 5.0; // 护盾持续5秒
                break;
            case SPEED:
                this.duration = 7.0; // 速度提升持续7秒
                break;
            case POWER:
                this.duration = 10.0; // 火力增强持续10秒
                break;
            case LIFE:
            case BOMB:
            default:
                this.duration = 0; // 即时生效的道具
                break;
        }
    }
    
    @Override
    public void update(double deltaTime) {
        // 更新道具在地图上显示的剩余时间
        if (alive) {
            remainingDisplayTime -= deltaTime;
            if (remainingDisplayTime <= 0) {
                setAlive(false); // 时间到，道具消失
            }
        }
    }
    
    /**
     * 应用道具效果到玩家坦克
     * 
     * @param playerTank 玩家坦克
     * @return 是否成功应用
     */
    public boolean applyEffect(PlayerTank playerTank) {
        if (playerTank == null || !playerTank.isAlive() || !this.isAlive()) {
            return false;
        }
        
        switch (type) {
            case SHIELD:
                playerTank.activateShield(duration);
                break;
            case SPEED:
                playerTank.activateSpeedBoost(duration);
                break;
            case POWER:
                playerTank.activateFirePower(duration);
                break;
            case LIFE:
                playerTank.addLife();
                break;
            case BOMB:
                // 全场爆炸效果将在GameModel中处理
                return true;
            default:
                return false;
        }
        
        return true;
    }
    
    // Getter和Setter方法
    
    public ItemType getType() {
        return type;
    }
    
    public double getDuration() {
        return duration;
    }
    
    public double getRemainingDisplayTime() {
        return remainingDisplayTime;
    }
}