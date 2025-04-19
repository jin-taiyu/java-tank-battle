package com.tankbattle.model.entity;

/**
 * 砖墙类，继承自Wall，可被摧毁
 * 
 * @author Taiyu Jin
 */
public class BrickWall extends Wall {
    /**
     * 构造函数
     * 
     * @param x 初始X坐标
     * @param y 初始Y坐标
     */
    public BrickWall(double x, double y) {
        super(x, y, true); // 砖墙可被摧毁
    }
}