package com.tankbattle.model;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * 音频管理器类，负责加载和播放游戏音频
 * 
 * @author Taiyu Jin
 */
public class AudioManager {
    // 音效集合
    private Map<String, AudioClip> soundEffects;
    
    // 背景音乐播放器
    private MediaPlayer bgmPlayer;
    
    // 音频开关
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    
    // 错误跟踪
    private boolean hasLoadingError = false;
    
    // 单例实例
    private static AudioManager instance;
    
    /**
     * 获取AudioManager单例实例
     * 
     * @return AudioManager实例
     */
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    /**
     * 私有构造函数
     */
    private AudioManager() {
        soundEffects = new HashMap<>();
        try {
            initAudio();
        } catch (Exception e) {
            System.err.println("音频初始化失败: " + e.getMessage());
            hasLoadingError = true;
            // 设置为false以防止进一步尝试加载音频
            soundEnabled = false;
            musicEnabled = false;
        }
    }
    
    /**
     * 初始化音频资源
     */
    private void initAudio() {
        // 预加载音效 - 使用懒加载，只在需要时才加载
        // 不在构造函数中尝试加载所有音效
    }
    
    /**
     * 加载音效
     * 
     * @param name 音效名称
     * @param path 音效路径
     * @return 是否成功加载
     */
    private boolean loadSoundEffect(String name, String path) {
        try {
            URL url = null;
            
            // 尝试多种加载方式
            // 1. 直接从根路径加载
            url = AudioManager.class.getResource("/" + path);
            
            // 2. 使用类加载器
            if (url == null) {
                url = getClass().getClassLoader().getResource(path);
            }
            
            // 3. 尝试从模块路径加载
            if (url == null) {
                url = getClass().getResource("/com/tankbattle/" + path);
            }
            
            if (url != null) {
                AudioClip clip = new AudioClip(url.toString());
                soundEffects.put(name, clip);
                return true;
            } else {
                System.out.println("未找到音频资源: " + path);
                return false;
            }
        } catch (Exception e) {
            System.out.println("音效加载失败: " + path + ", 错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 播放音效
     * 
     * @param name 音效名称
     * @return 是否成功播放
     */
    public boolean playSoundEffect(String name) {
        if (!soundEnabled || hasLoadingError) return false;
        
        try {
            AudioClip clip = soundEffects.get(name);
            if (clip == null) {
                // 懒加载 - 首次使用时加载
                String resourcePath = "audio/" + name + ".wav";
                boolean loaded = loadSoundEffect(name, resourcePath);
                if (!loaded) {
                    // 如果加载失败，记录但继续游戏
                    return false;
                }
                clip = soundEffects.get(name);
            }
            
            if (clip != null) {
                clip.play();
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("播放音效失败: " + name + ", 错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 播放背景音乐
     * 
     * @param path 音乐路径
     * @param loop 是否循环
     * @return 是否成功播放
     */
    public boolean playBackgroundMusic(String path, boolean loop) {
        if (!musicEnabled || hasLoadingError) return false;
        
        try {
            // 停止当前播放的音乐
            stopBackgroundMusic();
            
            URL url = getClass().getClassLoader().getResource(path);
            // 尝试其他加载方式
            if (url == null) {
                url = AudioManager.class.getResource("/" + path);
            }
            if (url == null) {
                url = getClass().getResource("/com/tankbattle/" + path);
            }
            if (url != null) {
                try {
                    Media media = new Media(url.toString());
                    bgmPlayer = new MediaPlayer(media);
                    
                    // 设置错误处理
                    bgmPlayer.setOnError(() -> {
                        System.out.println("背景音乐播放错误: " + bgmPlayer.getError());
                        stopBackgroundMusic(); // 确保完全停止和释放
                    });
                    
                    // 设置完成事件处理，防止音乐意外停止
                    bgmPlayer.setOnEndOfMedia(() -> {
                        if (loop) {
                            bgmPlayer.seek(javafx.util.Duration.ZERO);
                            bgmPlayer.play();
                        } else {
                            stopBackgroundMusic();
                        }
                    });
                    
                    if (loop) {
                        bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                    }
                    bgmPlayer.setVolume(0.6);
                    // 直接在 JavaFX 线程上播放，无需额外线程
                    Platform.runLater(() -> {
                        if (bgmPlayer != null) {
                            bgmPlayer.play();
                        }
                    });
                    return true;
                } catch (Exception e) {
                    System.out.println("背景音乐创建失败: " + path + ", 错误: " + e.getMessage());
                    return false;
                }
            } else {
                System.out.println("背景音乐文件不存在: " + path);
                return false;
            }
        } catch (Exception e) {
            System.out.println("背景音乐加载失败: " + path + ", 错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 停止背景音乐
     */
    public void stopBackgroundMusic() {
        try {
            if (bgmPlayer != null) {
                bgmPlayer.stop();
                bgmPlayer.dispose();
                bgmPlayer = null;
                
                // 强制垃圾回收，确保旧的音频资源被释放
                System.gc();
            }
        } catch (Exception e) {
            System.out.println("停止背景音乐失败: " + e.getMessage());
            // 即使出错，也确保播放器被置为null
            bgmPlayer = null;
        }
    }
    
    /**
     * 设置音效开关
     * 
     * @param enabled 是否启用
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    /**
     * 设置音乐开关
     * 
     * @param enabled 是否启用
     */
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled && bgmPlayer != null) {
            bgmPlayer.stop();
        } else if (enabled && bgmPlayer != null) {
            bgmPlayer.play();
        }
    }
    
    /**
     * 获取音效开关状态
     * 
     * @return 是否启用音效
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    /**
     * 获取音乐开关状态
     * 
     * @return 是否启用音乐
     */
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    /**
     * 禁用所有音频
     * 在检测到致命音频问题时使用
     */
    public void disableAllAudio() {
        soundEnabled = false;
        musicEnabled = false;
        hasLoadingError = true;
        stopBackgroundMusic();
    }
}