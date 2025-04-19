package com.tankbattle.model.entity;

import com.tankbattle.model.enums.Direction;

/**
 * 墙体基类，继承自GameObject
 * 
 * @author Taiyu Jin
 */
public abstract class Wall extends GameObject {
    // 墙体是否可摧毁
    protected boolean destructible;
    
    /**
     * 构造函数
     * 
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param destructible 是否可摧毁
     */
    public Wall(double x, double y, boolean destructible) {
        super(x, y, Direction.UP); // 墙体方向不重要，默认为UP
        this.destructible = destructible;
        this.width = 40; // 默认墙体宽度
        this.height = 40; // 默认墙体高度
        this.speed = 0; // 墙体不移动
    }
    
    @Override
    public void update(double deltaTime) {
        // 墙体不需要更新逻辑
    }
    
    @Override
    public void move(double deltaTime) {
        // 墙体不移动，覆盖父类方法
    }
    
    @Override
    public void handleCollision() {
        // 墙体不需要处理碰撞，它不会移动
    }
    
    // Getter 和 Setter 方法
    
    public boolean isDestructible() {
        return destructible;
    }
    
    public void setDestructible(boolean destructible) {
        this.destructible = destructible;
    }
}