package com.tankbattle.model.enums;

/**
 * 道具类型枚举
 * 
 * @author Taiyu Jin
 */
public enum ItemType {
    SHIELD,  // 护盾，使玩家坦克短暂无敌
    SPEED,   // 速度提升，使玩家坦克移动速度加快
    POWER,   // 火力增强，使玩家坦克可以发射增强子弹
    LIFE,    // 增加生命，玩家坦克生命值+1
    BOMB;    // 全场爆炸，摧毁场上所有敌人坦克
    
    /**
     * 获取道具图片文件名
     * 
     * @return 图片文件名
     */
    public String getImageFileName() {
        switch (this) {
            case SHIELD:
                return "item_shield.svg";
            case SPEED:
                return "item_speed.svg";
            case POWER:
                return "item_power.svg";
            case LIFE:
                return "item_life.svg";
            case BOMB:
                return "item_bomb.svg";
            default:
                return "item_default.svg";
        }
    }
}