package com.tankbattle.model.entity;

import com.tankbattle.model.enums.Direction;

/**
 * 玩家坦克类，继承自Tank
 * 
 * @author Taiyu Jin
 */
public class PlayerTank extends Tank {
    // 玩家生命数
    private int lives;
    
    // 无敌时间（复活后短暂无敌）
    private double invincibleTime;
    private boolean invincible;
    
    // 道具相关属性
    private boolean hasShield;         // 是否有护盾
    private double shieldTime;         // 护盾剩余时间
    
    private boolean hasSpeedBoost;     // 是否有速度提升
    private double speedBoostTime;     // 速度提升剩余时间
    private double normalSpeed;        // 正常速度
    
    private boolean hasPowerUp;        // 是否有火力增强
    private double powerUpTime;        // 火力增强剩余时间
    
    /**
     * 构造函数
     * 
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param direction 初始方向
     */
    public PlayerTank(double x, double y, Direction direction) {
        super(x, y, direction);
        this.lives = 3; // 默认3条命
        this.health = 1; // 一次击中就会损失一条命
        this.speed = 150; // 玩家坦克速度
        this.normalSpeed = this.speed; // 保存正常速度
        this.shootCooldown = 0.3; // 玩家射击冷却时间较短
        this.invincibleTime = 0;
        this.invincible = false;
        
        // 初始化道具状态
        this.hasShield = false;
        this.shieldTime = 0;
        this.hasSpeedBoost = false;
        this.speedBoostTime = 0;
        this.hasPowerUp = false;
        this.powerUpTime = 0;
    }
    
    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        
        // 更新无敌状态
        if (invincible) {
            invincibleTime -= deltaTime;
            if (invincibleTime <= 0) {
                invincible = false;
            }
        }
        
        // 更新护盾状态
        if (hasShield) {
            shieldTime -= deltaTime;
            if (shieldTime <= 0) {
                hasShield = false;
            }
        }
        
        // 更新速度提升状态
        if (hasSpeedBoost) {
            speedBoostTime -= deltaTime;
            if (speedBoostTime <= 0) {
                hasSpeedBoost = false;
                this.speed = normalSpeed; // 恢复正常速度
            }
        }
        
        // 更新火力增强状态
        if (hasPowerUp) {
            powerUpTime -= deltaTime;
            if (powerUpTime <= 0) {
                hasPowerUp = false;
            }
        }
    }
    
    @Override
    protected Bullet createBullet(double bulletX, double bulletY) {
        Bullet bullet = new Bullet(bulletX, bulletY, direction, true); // 玩家子弹
        
        // 如果有火力增强，提升子弹伤害和大小
        if (hasPowerUp) {
            bullet.setPowered(true);
            bullet.setSpeed(bullet.getSpeed() * 1.2); // 增强子弹速度
        }
        
        return bullet;
    }
    
    /**
     * 玩家坦克被击中
     */
    @Override
    public void hit() {
        // 如果有护盾或处于无敌状态，不受伤害
        if (!invincible && !hasShield) {
            lives--;
            setAlive(false);
        }
    }
    
    /**
     * 玩家坦克复活
     * 
     * @param x 复活X坐标
     * @param y 复活Y坐标
     * @param direction 复活方向
     */
    public void respawn(double x, double y, Direction direction) {
        if (lives > 0) {
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.health = 1;
            this.setAlive(true);
            
            // 设置短暂无敌状态
            this.invincible = true;
            this.invincibleTime = 3.0; // 3秒无敌时间
            
            // 重置道具状态
            this.hasShield = false;
            this.shieldTime = 0;
            this.hasSpeedBoost = false;
            this.speedBoostTime = 0;
            this.hasPowerUp = false;
            this.powerUpTime = 0;
            this.speed = normalSpeed;
        }
    }
    
    /**
     * 激活护盾效果
     * 
     * @param duration 持续时间
     */
    public void activateShield(double duration) {
        this.hasShield = true;
        this.shieldTime = duration;
    }
    
    /**
     * 激活速度提升效果
     * 
     * @param duration 持续时间
     */
    public void activateSpeedBoost(double duration) {
        this.hasSpeedBoost = true;
        this.speedBoostTime = duration;
        this.speed = normalSpeed * 1.5; // 提升50%速度
    }
    
    /**
     * 激活火力增强效果
     * 
     * @param duration 持续时间
     */
    public void activateFirePower(double duration) {
        this.hasPowerUp = true;
        this.powerUpTime = duration;
    }
    
    /**
     * 增加生命值
     */
    public void addLife() {
        this.lives++;
    }
    
    // Getter 和 Setter 方法
    
    public int getLives() {
        return lives;
    }
    
    public void setLives(int lives) {
        this.lives = lives;
    }
    
    public boolean isInvincible() {
        return invincible || hasShield; // 无敌状态或有护盾都视为无敌
    }
    
    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
    }
    
    public double getInvincibleTime() {
        return invincibleTime;
    }
    
    public void setInvincibleTime(double invincibleTime) {
        this.invincibleTime = invincibleTime;
    }
    
    public boolean hasShield() {
        return hasShield;
    }
    
    public double getShieldTime() {
        return shieldTime;
    }
    
    public boolean hasSpeedBoost() {
        return hasSpeedBoost;
    }
    
    public double getSpeedBoostTime() {
        return speedBoostTime;
    }
    
    public boolean hasPowerUp() {
        return hasPowerUp;
    }
    
    public double getPowerUpTime() {
        return powerUpTime;
    }
    
    /**
     * 检查玩家是否有火力增强
     * @return 是否有火力增强
     */
    public boolean isPowered() {
        return hasPowerUp;
    }
    
    /**
     * 设置玩家火力增强状态
     * @param powered 是否有火力增强
     */
    public void setPowered(boolean powered) {
        this.hasPowerUp = powered;
    }
    
    /**
     * 检查玩家是否有护盾
     * @return 是否有护盾
     */
    public boolean isShielded() {
        return hasShield;
    }
    
    /**
     * 设置玩家护盾状态
     * @param shielded 是否有护盾
     */
    public void setShielded(boolean shielded) {
        this.hasShield = shielded;
    }
    
    /**
     * 检查玩家是否有速度提升
     * @return 是否有速度提升
     */
    public boolean isSpeedBoosted() {
        return hasSpeedBoost;
    }
    
    /**
     * 设置玩家速度提升状态
     * @param speedBoosted 是否有速度提升
     */
    public void setSpeedBoosted(boolean speedBoosted) {
        this.hasSpeedBoost = speedBoosted;
    }
}