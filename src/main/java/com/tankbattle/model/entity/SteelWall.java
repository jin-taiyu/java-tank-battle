package com.tankbattle.model.entity;

/**
 * 钢墙类，继承自Wall，不可被摧毁
 * 
 * @author Taiyu Jin
 */
public class SteelWall extends Wall {
    /**
     * 构造函数
     * 
     * @param x 初始X坐标
     * @param y 初始Y坐标
     */
    public SteelWall(double x, double y) {
        super(x, y, false); // 钢墙不可被摧毁
    }
}