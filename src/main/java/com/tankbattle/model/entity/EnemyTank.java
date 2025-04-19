package com.tankbattle.model.entity;

import com.tankbattle.model.enums.Direction;
import java.util.Random;

/**
 * 敌人坦克类，继承自Tank
 * 
 * @author Taiyu Jin
 */
public class EnemyTank extends Tank {
    // AI决策相关
    private double directionChangeTime; // 方向改变的时间间隔
    private double currentDirectionTime; // 当前方向持续时间
    private double decisionTime; // AI决策时间间隔
    private double currentDecisionTime; // 当前决策时间
    private Random random;
    
    /**
     * 构造函数
     * 
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param direction 初始方向
     */
    public EnemyTank(double x, double y, Direction direction) {
        super(x, y, direction);
        this.health = 1; // 敌人坦克生命值
        this.speed = 80; // 敌人坦克速度较慢
        this.shootCooldown = 1.0; // 敌人射击冷却时间较长
        
        // 初始化AI决策参数
        this.random = new Random();
        this.directionChangeTime = 2.0 + random.nextDouble() * 3.0; // 2-5秒随机改变方向
        this.currentDirectionTime = 0;
        this.decisionTime = 0.5 + random.nextDouble() * 1.5; // 0.5-2秒做一次决策
        this.currentDecisionTime = 0;
        this.moving = true; // 敌人坦克默认移动
    }
    
    @Override
    protected Bullet createBullet(double bulletX, double bulletY) {
        return new Bullet(bulletX, bulletY, direction, false); // 敌人子弹
    }
    
    /**
     * 更新敌人坦克AI
     * 
     * @param deltaTime 时间增量
     * @param playerTank 玩家坦克对象，用于追踪
     */
    public void updateAI(double deltaTime, PlayerTank playerTank) {
        if (!alive) return;
        
        // 更新方向改变计时器
        currentDirectionTime += deltaTime;
        if (currentDirectionTime >= directionChangeTime) {
            // 随机改变方向
            changeRandomDirection();
            currentDirectionTime = 0;
            directionChangeTime = 2.0 + random.nextDouble() * 3.0; // 重置方向改变时间
        }
        
        // 更新决策计时器
        currentDecisionTime += deltaTime;
        if (currentDecisionTime >= decisionTime) {
            // 做出决策：有一定概率朝向玩家
            if (random.nextDouble() < 0.3 && playerTank.isAlive()) { // 30%概率追踪玩家
                facePlayer(playerTank);
            }
            
            // 随机决定是否开火
            if (random.nextDouble() < 0.2) { // 20%概率开火
                fire();
            }
            
            currentDecisionTime = 0;
            decisionTime = 0.5 + random.nextDouble() * 1.5; // 重置决策时间
        }
    }
    
    /**
     * 随机改变方向
     */
    private void changeRandomDirection() {
        Direction[] directions = Direction.values();
        setDirection(directions[random.nextInt(directions.length)]);
    }
    
    /**
     * 面向玩家坦克
     * 
     * @param playerTank 玩家坦克对象
     */
    private void facePlayer(PlayerTank playerTank) {
        // 计算与玩家的相对位置
        double dx = playerTank.getX() - this.x;
        double dy = playerTank.getY() - this.y;
        
        // 根据相对位置决定面向方向
        if (Math.abs(dx) > Math.abs(dy)) {
            // 水平距离更大，选择水平方向
            if (dx > 0) {
                setDirection(Direction.RIGHT);
            } else {
                setDirection(Direction.LEFT);
            }
        } else {
            // 垂直距离更大，选择垂直方向
            if (dy > 0) {
                setDirection(Direction.DOWN);
            } else {
                setDirection(Direction.UP);
            }
        }
    }
    
    /**
     * 获取射击冷却时间
     * 
     * @return 射击冷却时间
     */
    public double getShootCooldown() {
        return shootCooldown;
    }
    
    /**
     * 设置射击冷却时间
     * 
     * @param shootCooldown 射击冷却时间
     */
    public void setShootCooldown(double shootCooldown) {
        this.shootCooldown = shootCooldown;
    }
}