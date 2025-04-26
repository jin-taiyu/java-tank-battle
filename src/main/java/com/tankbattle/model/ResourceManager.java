package com.tankbattle.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * SVG资源加载器类，负责加载和管理SVG图像资源
 * 
 * @author Taiyu Jin
 */
public class ResourceManager {
    // SVG图像缓存
    private Map<String, Image> imageCache;
    
    // 资源加载错误跟踪
    private boolean hasLoadingError = false;
    
    // 单例实例
    private static ResourceManager instance;
    
    /**
     * 获取ResourceManager单例实例
     * 
     * @return ResourceManager实例
     */
    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }
    
    /**
     * 私有构造函数
     */
    private ResourceManager() {
        imageCache = new HashMap<>();
        try {
            preloadResources();
        } catch (Exception e) {
            System.err.println("资源预加载失败: " + e.getMessage());
            hasLoadingError = true;
        }
    }
    
    /**
     * 预加载常用资源
     */
    private void preloadResources() {
        // 预加载玩家坦克图像
        loadImage("player_tank_up", "images/player_tank_up.svg", 40, 40);
        loadImage("player_tank_right", "images/player_tank_right.svg", 40, 40);
        loadImage("player_tank_down", "images/player_tank_down.svg", 40, 40);
        loadImage("player_tank_left", "images/player_tank_left.svg", 40, 40);
        
        // 预加载敌人坦克图像
        loadImage("enemy_tank_up", "images/enemy_tank_up.svg", 40, 40);
        loadImage("enemy_tank_right", "images/enemy_tank_right.svg", 40, 40);
        loadImage("enemy_tank_down", "images/enemy_tank_down.svg", 40, 40);
        loadImage("enemy_tank_left", "images/enemy_tank_left.svg", 40, 40);
        
        // 预加载子弹图像
        loadImage("bullet", "images/bullet.svg", 10, 10);
        
        // 预加载墙体图像
        loadImage("brick_wall", "images/brick_wall.svg", 40, 40);
        loadImage("steel_wall", "images/steel_wall.svg", 40, 40);
        
        // 预加载特效图像
        loadImage("explosion", "images/explosion.svg", 40, 40);
        
        // 预加载道具图像
        loadImage("item_shield", "images/item_shield.svg", 30, 30);
        loadImage("item_speed", "images/item_speed.svg", 30, 30);
        loadImage("item_power", "images/item_power.svg", 30, 30);
        loadImage("item_life", "images/item_life.svg", 30, 30);
        loadImage("item_bomb", "images/item_bomb.svg", 30, 30);
        loadImage("item_default", "images/item_default.svg", 30, 30);
        
        // 如果发现有图像加载失败，则为所有缺失的图像创建默认图像
        ensureAllImagesExist();
    }
    
    /**
     * 确保所有需要的图像都存在，如果缺失则创建默认图像
     */
    private void ensureAllImagesExist() {
        // 玩家坦克
        ensureImageExists("player_tank_up", 40, 40, Color.GREEN);
        ensureImageExists("player_tank_right", 40, 40, Color.GREEN);
        ensureImageExists("player_tank_down", 40, 40, Color.GREEN);
        ensureImageExists("player_tank_left", 40, 40, Color.GREEN);
        
        // 敌人坦克
        ensureImageExists("enemy_tank_up", 40, 40, Color.RED);
        ensureImageExists("enemy_tank_right", 40, 40, Color.RED);
        ensureImageExists("enemy_tank_down", 40, 40, Color.RED);
        ensureImageExists("enemy_tank_left", 40, 40, Color.RED);
        
        // 子弹
        ensureImageExists("bullet", 10, 10, Color.YELLOW);
        
        // 墙体
        ensureImageExists("brick_wall", 40, 40, Color.BROWN);
        ensureImageExists("steel_wall", 40, 40, Color.GRAY);
        
        // 特效
        ensureImageExists("explosion", 40, 40, Color.ORANGE);
        
        // 道具
        ensureImageExists("item_shield", 30, 30, Color.CYAN);
        ensureImageExists("item_speed", 30, 30, Color.GREEN);
        ensureImageExists("item_power", 30, 30, Color.RED);
        ensureImageExists("item_life", 30, 30, Color.PURPLE);
        ensureImageExists("item_bomb", 30, 30, Color.ORANGE);
        ensureImageExists("item_default", 30, 30, Color.YELLOW);
    }
    
    /**
     * 确保指定名称的图像存在，如果不存在则创建简单的彩色矩形图像
     * 
     * @param name 图像名称
     * @param width 宽度
     * @param height 高度
     * @param color 颜色
     */
    private void ensureImageExists(String name, int width, int height, Color color) {
        if (!imageCache.containsKey(name) || imageCache.get(name) == null) {
            createSimpleImage(name, width, height, color);
        }
    }
    
    /**
     * 创建简单的彩色矩形图像
     * 
     * @param name 图像名称
     * @param width 宽度
     * @param height 高度
     * @param color 颜色
     */
    private void createSimpleImage(String name, int width, int height, Color color) {
        try {
            Canvas canvas = new Canvas(width, height);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            
            // 绘制矩形
            gc.setFill(color);
            gc.fillRect(0, 0, width, height);
            
            // 绘制边框
            gc.setStroke(color.darker());
            gc.setLineWidth(2);
            gc.strokeRect(0, 0, width, height);
            
            // 如果是坦克，添加简单的形状表示方向
            if (name.contains("tank")) {
                gc.setFill(color.darker());
                
                if (name.contains("up")) {
                    gc.fillRect(width/2 - 2, 0, 4, height/2);
                } else if (name.contains("right")) {
                    gc.fillRect(width/2, height/2 - 2, width/2, 4);
                } else if (name.contains("down")) {
                    gc.fillRect(width/2 - 2, height/2, 4, height/2);
                } else if (name.contains("left")) {
                    gc.fillRect(0, height/2 - 2, width/2, 4);
                }
            }
            
            // 如果是道具，添加特殊标识
            if (name.contains("item_")) {
                gc.setFill(Color.WHITE);
                gc.setFont(javafx.scene.text.Font.font(14));
                String text = "";
                
                if (name.contains("shield")) {
                    text = "S";
                } else if (name.contains("speed")) {
                    text = "F";  // F for Fast
                } else if (name.contains("power")) {
                    text = "P";
                } else if (name.contains("life")) {
                    text = "L";
                } else if (name.contains("bomb")) {
                    text = "B";
                } else {
                    text = "?";
                }
                
                // 计算文本宽度以居中
                javafx.scene.text.Text textNode = new javafx.scene.text.Text(text);
                textNode.setFont(gc.getFont());
                double textWidth = textNode.getLayoutBounds().getWidth();
                double textHeight = textNode.getLayoutBounds().getHeight();
                
                gc.fillText(text, (width - textWidth) / 2, (height + textHeight) / 2);
            }
            
            // 保存为JavaFX图像
            imageCache.put(name, canvas.snapshot(null, null));
            
            System.out.println("已创建简单替代图像: " + name);
        } catch (Exception e) {
            System.err.println("创建简单图像失败: " + name + ", " + e.getMessage());
            
            // 最后的备选方案：创建1x1像素的纯色图像
            imageCache.put(name, createFallbackImage(color));
        }
    }
    
    /**
     * 创建1x1像素的纯色图像作为最后的备选方案
     * 
     * @param color 颜色
     * @return 图像
     */
    private Image createFallbackImage(Color color) {
        int r = (int)(color.getRed() * 255);
        int g = (int)(color.getGreen() * 255);
        int b = (int)(color.getBlue() * 255);
        
        String colorHex = String.format("#%02X%02X%02X", r, g, b);
        return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==");
    }
    
    /**
     * 加载SVG图像资源
     * 
     * @param name 图像名称
     * @param path 图像路径
     * @param width 目标宽度
     * @param height 目标高度
     */
    public void loadImage(String name, String path, int width, int height) {
        try {
            // 从模块资源中加载SVG资源
            InputStream inputStream = null;
            
            // 首先尝试使用模块化方式加载
            inputStream = ResourceManager.class.getResourceAsStream("/" + path);
            
            // 如果失败，尝试常规类加载器方式
            if (inputStream == null) {
                inputStream = getClass().getClassLoader().getResourceAsStream(path);
            }
            
            // 如果仍然失败，尝试使用模块名作为前缀
            if (inputStream == null) {
                inputStream = getClass().getResourceAsStream("/com/tankbattle/" + path);
            }
            
            if (inputStream == null) {
                System.out.println("未找到资源: " + path);
                // 如果找不到SVG资源，创建一个简单的默认图像
                createDefaultImage(name, width, height);
                return;
            }
            
            // 使用Batik将SVG转换为JavaFX图像
            PNGTranscoder transcoder = new PNGTranscoder();
            transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) width);
            transcoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, (float) height);
            
            // 设置转码器输入
            TranscoderInput input = new TranscoderInput(inputStream);
            
            // 设置转码器输出
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outputStream);
            
            // 执行转换
            transcoder.transcode(input, output);
            
            // 创建JavaFX图像
            Image image = new Image(new ByteArrayInputStream(outputStream.toByteArray()));
            
            // 缓存图像
            imageCache.put(name, image);
            
            // 关闭流
            inputStream.close();
            outputStream.close();
        } catch (IOException | TranscoderException e) {
            System.out.println("加载SVG资源失败: " + path + ", 错误: " + e.getMessage());
            
            // 创建默认图像作为备选
            createDefaultImage(name, width, height);
        }
    }
    
    /**
     * 创建简单的默认图像
     * 
     * @param name 图像名称
     * @param width 宽度
     * @param height 高度
     */
    private void createDefaultImage(String name, int width, int height) {
        // 根据对象类型选择默认颜色
        if (name.contains("player_tank")) {
            createSimpleImage(name, width, height, Color.GREEN);
        } else if (name.contains("enemy_tank")) {
            createSimpleImage(name, width, height, Color.RED);
        } else if (name.contains("bullet")) {
            createSimpleImage(name, width, height, Color.YELLOW);
        } else if (name.contains("brick_wall")) {
            createSimpleImage(name, width, height, Color.BROWN);
        } else if (name.contains("steel_wall")) {
            createSimpleImage(name, width, height, Color.GRAY);
        } else if (name.contains("explosion")) {
            createSimpleImage(name, width, height, Color.ORANGE);
        } else if (name.contains("item_")) {
            createSimpleImage(name, width, height, Color.CYAN);
        } else {
            // 其他对象的默认图像（蓝色方块）
            createSimpleImage(name, width, height, Color.BLUE);
        }
    }
    
    /**
     * 获取图像资源
     * 
     * @param name 图像名称
     * @return 图像对象
     */
    public Image getImage(String name) {
        Image image = imageCache.get(name);
        if (image == null) {
            // 如果找不到图像，返回一个紫色方块作为错误指示
            System.err.println("严重错误：尝试获取未加载的图像: " + name);
            createSimpleImage(name, 40, 40, Color.PURPLE);
            image = imageCache.get(name);
        }
        return image;
    }
    
    /**
     * 获取坦克方向对应的图像
     * 
     * @param prefix 前缀（player_tank或enemy_tank）
     * @param direction 方向
     * @return 图像对象
     */
    public Image getTankImage(String prefix, String direction) {
        return getImage(prefix + "_" + direction.toLowerCase());
    }
}