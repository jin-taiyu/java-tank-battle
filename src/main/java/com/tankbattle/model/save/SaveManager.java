package com.tankbattle.model.save;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 游戏存档管理器，负责保存和加载游戏存档
 * 
 * @author Taiyu Jin
 */
public class SaveManager {
    private static SaveManager instance;
    
    private static final String SAVE_DIRECTORY = "saves";
    private static final String FILE_EXTENSION = ".sav";
    
    /**
     * 私有构造函数，单例模式
     */
    private SaveManager() {
        // 确保存档目录存在
        ensureSaveDirectoryExists();
    }
    
    /**
     * 获取存档管理器实例
     * 
     * @return 存档管理器实例
     */
    public static synchronized SaveManager getInstance() {
        if (instance == null) {
            instance = new SaveManager();
        }
        return instance;
    }
    
    /**
     * 确保存档目录存在
     */
    private void ensureSaveDirectoryExists() {
        File dir = new File(SAVE_DIRECTORY);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.println("无法创建存档目录: " + SAVE_DIRECTORY);
            }
        }
    }
    
    /**
     * 保存游戏
     * 
     * @param save 游戏存档
     * @return 是否保存成功
     */
    public boolean saveGame(GameSave save) {
        ensureSaveDirectoryExists();
        
        // 如果没有提供存档名称，自动生成一个
        if (save.getSaveName() == null || save.getSaveName().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            save.setSaveName("TankBattle_" + sdf.format(new Date()));
        }
        
        String filePath = SAVE_DIRECTORY + File.separator + save.getSaveName() + FILE_EXTENSION;
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(save);
            System.out.println("游戏已保存到: " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("保存游戏失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 加载游戏
     * 
     * @param saveName 存档名称
     * @return 游戏存档对象
     */
    public GameSave loadGame(String saveName) {
        String filePath = SAVE_DIRECTORY + File.separator + saveName + FILE_EXTENSION;
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            GameSave save = (GameSave) ois.readObject();
            System.out.println("已加载游戏: " + save.getSaveName());
            return save;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("加载游戏失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取所有存档
     * 
     * @return 存档列表
     */
    public List<GameSave> getAllSaves() {
        List<GameSave> saves = new ArrayList<>();
        File dir = new File(SAVE_DIRECTORY);
        
        if (!dir.exists() || !dir.isDirectory()) {
            return saves;
        }
        
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(FILE_EXTENSION));
        if (files == null) {
            return saves;
        }
        
        for (File file : files) {
            String saveName = file.getName().substring(0, file.getName().length() - FILE_EXTENSION.length());
            GameSave save = loadGame(saveName);
            if (save != null) {
                saves.add(save);
            }
        }
        
        return saves;
    }
    
    /**
     * 删除存档
     * 
     * @param saveName 存档名称
     * @return 是否删除成功
     */
    public boolean deleteSave(String saveName) {
        String filePath = SAVE_DIRECTORY + File.separator + saveName + FILE_EXTENSION;
        File file = new File(filePath);
        
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("已删除存档: " + saveName);
            } else {
                System.err.println("无法删除存档: " + saveName);
            }
            return deleted;
        }
        
        return false;
    }
    
    /**
     * 检查存档是否存在
     * 
     * @param saveName 存档名称
     * @return 是否存在
     */
    public boolean saveExists(String saveName) {
        String filePath = SAVE_DIRECTORY + File.separator + saveName + FILE_EXTENSION;
        return new File(filePath).exists();
    }
}
