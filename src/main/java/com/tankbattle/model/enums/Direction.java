package com.tankbattle.model.enums;

/**
 * 方向枚举，用于坦克和子弹的移动方向
 * 
 * @author Taiyu Jin
 */
public enum Direction {
    /**
     * 上方向
     */
    UP,
    
    /**
     * 右方向
     */
    RIGHT,
    
    /**
     * 下方向
     */
    DOWN,
    
    /**
     * 左方向
     */
    LEFT;
    
    /**
     * 获取方向对应的X轴移动值
     * 
     * @return X轴移动值
     */
    public int getDx() {
        switch (this) {
            case RIGHT:
                return 1;
            case LEFT:
                return -1;
            default:
                return 0;
        }
    }
    
    /**
     * 获取方向对应的Y轴移动值
     * 
     * @return Y轴移动值
     */
    public int getDy() {
        switch (this) {
            case DOWN:
                return 1;
            case UP:
                return -1;
            default:
                return 0;
        }
    }
    
    /**
     * 获取方向对应的角度值（用于旋转图像）
     * 
     * @return 角度值
     */
    public double getAngle() {
        switch (this) {
            case RIGHT:
                return 90;
            case DOWN:
                return 180;
            case LEFT:
                return 270;
            default: // UP
                return 0;
        }
    }
}