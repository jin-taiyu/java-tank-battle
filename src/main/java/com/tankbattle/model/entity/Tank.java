package com.tankbattle.model.entity;

import com.tankbattle.model.enums.Direction;

/**
 * 坦克基类，继承自GameObject
 * 
 * @author Taiyu Jin
 */
public abstract class Tank extends GameObject {
    // 坦克生命值
    protected int health;
    
    // 坦克是否正在移动
    protected boolean moving;
    
    // 射击冷却时间
    protected double shootCooldown;
    protected double currentCooldown;
    
    // 加速度相关参数
    protected double currentSpeed; // 当前实际速度
    protected double acceleration; // 加速度
    protected double deceleration; // 减速度
    
    // 游戏区域宽高（用于边界检查）
    private static final int GAME_WIDTH = 800;
    private static final int GAME_HEIGHT = 600;
    
    /**
     * 构造函数
     * 
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param direction 初始方向
     */
    public Tank(double x, double y, Direction direction) {
        super(x, y, direction);
        this.moving = false;
        this.health = 1;
        this.shootCooldown = 0.5; // 默认射击冷却时间为0.5秒
        this.currentCooldown = 0;
        this.speed = 100; // 最大速度
        this.currentSpeed = 0; // 初始实际速度为0
        this.acceleration = 350; // 加速度，单位为像素/秒²
        this.deceleration = 350; // 减速度，单位为像素/秒²
        this.width = 40; // 默认宽度
        this.height = 40; // 默认高度
    }
    
    @Override
    public void update(double deltaTime) {
        // 更新射击冷却
        if (currentCooldown > 0) {
            currentCooldown -= deltaTime;
        }
        
        // 更新坦克速度
        if (alive) {
            if (moving) {
                // 如果正在移动，逐渐加速到最大速度
                currentSpeed = Math.min(currentSpeed + acceleration * deltaTime, speed);
            } else {
                // 如果不在移动，逐渐减速到0
                currentSpeed = Math.max(currentSpeed - deceleration * deltaTime, 0);
            }
            
            // 只要还有速度，就继续移动（即使已经停止按键）
            if (currentSpeed > 0) {
                move(deltaTime);
            }
        }
    }
    
    /**
     * 重写移动方法，增加边界检查
     * 
     * @param deltaTime 时间增量
     */
    @Override
    public void move(double deltaTime) {
        if (!alive) return;
        
        // 先保存当前位置，如果移动后超出边界可以恢复
        double oldX = x;
        double oldY = y;
        
        // 使用当前实际速度计算新位置，而不是最大速度
        double newX = x + direction.getDx() * currentSpeed * deltaTime;
        double newY = y + direction.getDy() * currentSpeed * deltaTime;
        
        // 边界检查
        if (newX < 0) {
            newX = 0;
            // 撞墙时减速
            currentSpeed *= 0.8;
        } else if (newX > GAME_WIDTH - width) {
            newX = GAME_WIDTH - width;
            // 撞墙时减速
            currentSpeed *= 0.8;
        }
        
        if (newY < 0) {
            newY = 0;
            // 撞墙时减速
            currentSpeed *= 0.8;
        } else if (newY > GAME_HEIGHT - height) {
            newY = GAME_HEIGHT - height;
            // 撞墙时减速
            currentSpeed *= 0.8;
        }
        
        // 更新坦克位置
        x = newX;
        y = newY;
    }
    
    /**
     * 碰撞处理
     * 重写父类的方法，添加更合理的碰撞响应
     */
    @Override
    public void handleCollision() {
        // 根据坦克当前方向，向相反方向后退一小段距离
        double backupDistance = 2.0; // 固定的后退距离，避免后退过多
        
        // 根据方向调整坐标
        switch (direction) {
            case UP:
                y += backupDistance;
                break;
            case DOWN:
                y -= backupDistance;
                break;
            case LEFT:
                x += backupDistance;
                break;
            case RIGHT:
                x -= backupDistance;
                break;
        }
        
        // 确保坦克不会移出游戏区域
        if (x < 0) {
            x = 0;
        } else if (x > GAME_WIDTH - width) {
            x = GAME_WIDTH - width;
        }
        
        if (y < 0) {
            y = 0;
        } else if (y > GAME_HEIGHT - height) {
            y = GAME_HEIGHT - height;
        }
    }
    
    /**
     * 坦克射击方法
     * 
     * @return 如果可以射击，返回一个新的子弹对象；否则返回null
     */
    public Bullet fire() {
        if (currentCooldown <= 0 && alive) {
            // 重置冷却时间
            currentCooldown = shootCooldown;
            
            // 计算子弹的初始位置（从坦克中心点发射）
            double bulletX = x + width / 2.0 - 5; // 假设子弹宽度为10
            double bulletY = y + height / 2.0 - 5; // 假设子弹高度为10
            
            // 根据坦克方向调整子弹位置，使其从坦克炮口发射
            switch (direction) {
                case UP:
                    bulletY -= height / 2.0;
                    break;
                case DOWN:
                    bulletY += height / 2.0;
                    break;
                case LEFT:
                    bulletX -= width / 2.0;
                    break;
                case RIGHT:
                    bulletX += width / 2.0;
                    break;
            }
            
            // 创建并返回子弹对象
            return createBullet(bulletX, bulletY);
        }
        return null;
    }
    
    /**
     * 创建子弹的抽象方法，由子类实现
     * 
     * @param bulletX 子弹X坐标
     * @param bulletY 子弹Y坐标
     * @return 子弹对象
     */
    protected abstract Bullet createBullet(double bulletX, double bulletY);
    
    /**
     * 坦克被击中时调用
     */
    public void hit() {
        health--;
        if (health <= 0) {
            setAlive(false);
        }
    }
    
    // Getter 和 Setter 方法
    
    public boolean isMoving() {
        return moving;
    }
    
    public void setMoving(boolean moving) {
        this.moving = moving;
    }
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
    }
}