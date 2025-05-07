package com.healthx.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 运动相关常量类，包含各种运动类型的基础卡路里消耗率
 */
public class ExerciseConstants {
    
    // 每分钟卡路里消耗率（千卡/分钟）
    public static final Map<String, Double> CALORIES_PER_MINUTE = new HashMap<>();
    
    static {
        // 有氧运动
        CALORIES_PER_MINUTE.put("步行", 4.0);
        CALORIES_PER_MINUTE.put("跑步", 10.0);
        CALORIES_PER_MINUTE.put("骑自行车", 8.0);
        CALORIES_PER_MINUTE.put("游泳", 9.0);
        CALORIES_PER_MINUTE.put("有氧健身操", 7.5);
        CALORIES_PER_MINUTE.put("跳绳", 11.0);
        CALORIES_PER_MINUTE.put("爬楼梯", 8.5);
        CALORIES_PER_MINUTE.put("慢跑", 8.0);
        CALORIES_PER_MINUTE.put("快走", 5.5);
        
        // 力量训练
        CALORIES_PER_MINUTE.put("哑铃训练", 6.0);
        CALORIES_PER_MINUTE.put("杠铃训练", 7.0);
        CALORIES_PER_MINUTE.put("器械训练", 6.5);
        CALORIES_PER_MINUTE.put("俯卧撑", 7.0);
        CALORIES_PER_MINUTE.put("仰卧起坐", 6.0);
        CALORIES_PER_MINUTE.put("引体向上", 8.0);
        CALORIES_PER_MINUTE.put("深蹲", 7.0);
        
        // 柔韧性训练
        CALORIES_PER_MINUTE.put("瑜伽", 3.0);
        CALORIES_PER_MINUTE.put("普拉提", 5.0);
        CALORIES_PER_MINUTE.put("拉伸运动", 2.5);
        CALORIES_PER_MINUTE.put("太极", 3.0);
        
        // 平衡训练
        CALORIES_PER_MINUTE.put("平衡球训练", 4.0);
        CALORIES_PER_MINUTE.put("单腿站立", 3.0);
        CALORIES_PER_MINUTE.put("平板支撑", 5.0);
        
        // 功能性训练
        CALORIES_PER_MINUTE.put("高强度间歇训练", 12.0);
        CALORIES_PER_MINUTE.put("战绳训练", 10.0);
        CALORIES_PER_MINUTE.put("壶铃训练", 8.0);
        CALORIES_PER_MINUTE.put("核心训练", 7.0);
        
        // 运动项目
        CALORIES_PER_MINUTE.put("篮球", 8.0);
        CALORIES_PER_MINUTE.put("足球", 10.0);
        CALORIES_PER_MINUTE.put("网球", 8.0);
        CALORIES_PER_MINUTE.put("羽毛球", 7.0);
        CALORIES_PER_MINUTE.put("乒乓球", 5.0);
        CALORIES_PER_MINUTE.put("排球", 6.0);
        
        // 其他运动
        CALORIES_PER_MINUTE.put("舞蹈", 7.0);
        CALORIES_PER_MINUTE.put("徒步旅行", 6.0);
        CALORIES_PER_MINUTE.put("冥想", 1.0);
        CALORIES_PER_MINUTE.put("划船", 8.5);
        CALORIES_PER_MINUTE.put("其他", 5.0);
    }
    
    // 强度因子
    public static final double LOW_INTENSITY_FACTOR = 0.8;
    public static final double MEDIUM_INTENSITY_FACTOR = 1.0;
    public static final double HIGH_INTENSITY_FACTOR = 1.3;
    
    /**
     * 根据运动名称、时长和强度计算卡路里消耗
     *
     * @param exerciseName 运动名称
     * @param duration 时长（分钟）
     * @param intensity 强度（低强度、中等强度、高强度）
     * @return 消耗的卡路里
     */
    public static double calculateCaloriesBurned(String exerciseName, int duration, String intensity) {
        // 获取基础每分钟消耗率，默认为5.0
        double baseCaloriesPerMinute = CALORIES_PER_MINUTE.getOrDefault(exerciseName, 5.0);
        
        // 根据强度调整
        double intensityFactor = MEDIUM_INTENSITY_FACTOR; // 默认为中等强度
        
        if ("低强度".equals(intensity)) {
            intensityFactor = LOW_INTENSITY_FACTOR;
        } else if ("高强度".equals(intensity)) {
            intensityFactor = HIGH_INTENSITY_FACTOR;
        }
        
        // 计算总卡路里消耗
        return baseCaloriesPerMinute * duration * intensityFactor;
    }
} 