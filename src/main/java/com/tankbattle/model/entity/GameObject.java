package com.tankbattle.model.entity;

import com.tankbattle.model.enums.Direction;

/**
 * 游戏对象基类，所有游戏实体的父类
 * 
 * @author Taiyu Jin
 */
public abstract class GameObject {
    // 位置坐标
    protected double x;
    protected double y;
    
    // 尺寸
    protected int width;
    protected int height;
    
    // 方向
    protected Direction direction;
    
    // 移动速度
    protected double speed;
    
    // 是否存活
    protected boolean alive;
    
    /**
     * 构造函数
     * 
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param direction 初始方向
     */
    public GameObject(double x, double y, Direction direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.alive = true;
    }
    
    /**
     * 更新游戏对象状态
     * 
     * @param deltaTime 时间增量
     */
    public abstract void update(double deltaTime);
    
    /**
     * 移动游戏对象
     * 
     * @param deltaTime 时间增量
     */
    public void move(double deltaTime) {
        if (alive) {
            x += direction.getDx() * speed * deltaTime;
            y += direction.getDy() * speed * deltaTime;
        }
    }
    
    /**
     * 处理碰撞
     */
    public void handleCollision() {
        // 默认碰撞处理，子类可以重写
        // 只需要后退一小段距离，防止穿透墙体
        double backupDistance = speed * 0.1;
        x -= direction.getDx() * backupDistance;
        y -= direction.getDy() * backupDistance;
    }
    
    // Getter 和 Setter 方法
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
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
    
    public Direction getDirection() {
        return direction;
    }
    
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}