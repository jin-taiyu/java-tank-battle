package com.tankbattle.model.entity;

import com.tankbattle.model.enums.Direction;

/**
 * 子弹类，继承自GameObject
 * 
 * @author Taiyu Jin
 */
public class Bullet extends GameObject {
    // 子弹是否来自玩家
    private boolean fromPlayer;
    
    // 子弹伤害值
    private int damage;
    
    // 是否为增强子弹（火力道具效果）
    private boolean powered;
    
    /**
     * 构造函数
     * 
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param direction 初始方向
     * @param fromPlayer 是否来自玩家
     */
    public Bullet(double x, double y, Direction direction, boolean fromPlayer) {
        super(x, y, direction);
        this.fromPlayer = fromPlayer;
        this.damage = 1; // 默认伤害值
        this.speed = 300; // 子弹速度比坦克快
        this.width = 10; // 子弹宽度
        this.height = 10; // 子弹高度
        this.powered = false; // 默认不是增强子弹
    }
    
    @Override
    public void update(double deltaTime) {
        if (alive) {
            move(deltaTime);
        }
    }
    
    /**
     * 子弹碰撞处理，子弹碰撞后直接消失
     */
    @Override
    public void handleCollision() {
        setAlive(false);
    }
    
    /**
     * 设置为增强子弹
     * 
     * @param powered 是否为增强子弹
     */
    public void setPowered(boolean powered) {
        this.powered = powered;
        if (powered) {
            this.damage = 2; // 增强子弹伤害值为2
            this.width = 14; // 增强子弹稍大一些
            this.height = 14;
        } else {
            this.damage = 1;
            this.width = 10;
            this.height = 10;
        }
    }
    
    // Getter 和 Setter 方法
    
    public boolean isFromPlayer() {
        return fromPlayer;
    }
    
    public void setFromPlayer(boolean fromPlayer) {
        this.fromPlayer = fromPlayer;
    }
    
    public int getDamage() {
        return damage;
    }
    
    public void setDamage(int damage) {
        this.damage = damage;
    }
    
    public boolean isPowered() {
        return powered;
    }
}