package com.healthx.util;

import android.util.Log;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 日期时间工具类，提供日期时间相关的工具方法
 */
public class DateTimeUtils {
    
    private static final String TAG = "DateTimeUtils";
    
    // 标准格式定义 - 旧格式保留，为保持向后兼容
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // 新的标准格式定义 - 符合规范
    // API交互用格式
    public static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter API_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter API_DATETIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    // UI展示用格式
    public static final DateTimeFormatter UI_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter UI_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter UI_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // 额外的解析格式 - 用于灵活解析
    private static final DateTimeFormatter ISO_WITHOUT_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter STANDARD_WITH_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter STANDARD_WITHOUT_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // java.util.Date 格式化器
    private static final SimpleDateFormat DATE_SDF = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat TIME_SDF = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATETIME_SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    
    // ISO格式化器
    private static final SimpleDateFormat ISO_DATE_SDF = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat ISO_DATETIME_SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
    private static final SimpleDateFormat ISO_DATETIME_NO_MS_SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    
    static {
        // 设置时区为UTC，用于API交互
        DATE_SDF.setTimeZone(TimeZone.getTimeZone("UTC"));
        TIME_SDF.setTimeZone(TimeZone.getTimeZone("UTC"));
        DATETIME_SDF.setTimeZone(TimeZone.getTimeZone("UTC"));
        ISO_DATE_SDF.setTimeZone(TimeZone.getTimeZone("UTC"));
        ISO_DATETIME_SDF.setTimeZone(TimeZone.getTimeZone("UTC"));
        ISO_DATETIME_NO_MS_SDF.setTimeZone(TimeZone.getTimeZone("UTC"));
        // UI展示用本地时区
    }
    
    /**
     * 将LocalDate格式化为字符串 (API格式)
     */
    public static String formatDateForApi(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(API_DATE_FORMAT);
    }
    
    /**
     * 将LocalDate格式化为字符串 (UI显示)
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(UI_DATE_FORMAT);
    }
    
    /**
     * 将LocalTime格式化为字符串 (API格式)
     */
    public static String formatTimeForApi(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(API_TIME_FORMAT);
    }
    
    /**
     * 将LocalTime格式化为字符串 (UI显示)
     */
    public static String formatTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(UI_TIME_FORMAT);
    }
    
    /**
     * 将LocalDateTime格式化为日期字符串
     */
    public static String formatToDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(UI_DATE_FORMAT);
    }
    
    /**
     * 将LocalDateTime格式化为时间字符串
     */
    public static String formatToTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(UI_TIME_FORMAT);
    }
    
    /**
     * 将LocalDateTime格式化为日期时间字符串 (API格式)
     */
    public static String formatDateTimeForApi(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(API_DATETIME_FORMAT);
    }
    
    /**
     * 将LocalDateTime格式化为日期时间字符串 (UI显示)
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(UI_DATETIME_FORMAT);
    }
    
    /**
     * 解析日期字符串为LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, API_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            Log.e(TAG, "解析日期失败: " + dateStr, e);
            return null;
        }
    }
    
    /**
     * 解析时间字符串为LocalTime
     */
    public static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        try {
            // 首先尝试使用API格式解析
            return LocalTime.parse(timeStr, API_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            try {
                // 如果失败，尝试使用UI格式解析
                return LocalTime.parse(timeStr, UI_TIME_FORMAT);
            } catch (DateTimeParseException e2) {
                Log.e(TAG, "解析时间失败: " + timeStr, e2);
                return null;
            }
        }
    }
    
    /**
     * 解析日期时间字符串为LocalDateTime (使用标准格式)
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            Log.e(TAG, "解析日期时间失败: " + dateTimeStr, e);
            return parseFlexibleDateTime(dateTimeStr);
        }
    }
    
    /**
     * 获取今日开始时间
     */
    public static LocalDateTime getStartOfDay() {
        return LocalDate.now().atStartOfDay();
    }
    
    /**
     * 获取今日结束时间
     */
    public static LocalDateTime getEndOfDay() {
        return LocalDate.now().atTime(LocalTime.MAX);
    }
    
    /**
     * 获取指定日期的开始时间
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }
    
    /**
     * 获取指定日期的结束时间
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }
    
    /**
     * 计算两个日期时间之间的分钟差
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toMinutes();
    }
    
    /**
     * 格式化分钟为小时和分钟字符串
     */
    public static String formatMinutesToHoursAndMinutes(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%d小时%d分钟", hours, mins);
    }
    
    /**
     * 智能解析日期时间字符串，支持多种格式
     * 
     * 支持的格式:
     * - ISO标准格式: 
     *   - yyyy-MM-dd'T'HH:mm:ss (不带毫秒)
     *   - yyyy-MM-dd'T'HH:mm:ss.SSS (标准3位毫秒)
     *   - yyyy-MM-dd'T'HH:mm:ss.SSSSSS (任意长度毫秒)
     * - 标准格式: 
     *   - yyyy-MM-dd HH:mm:ss (带秒)
     *   - yyyy-MM-dd HH:mm (不带秒)
     * - 日期格式: yyyy-MM-dd
     * 
     * @param dateTimeStr 日期时间字符串
     * @return 解析后的LocalDateTime对象，解析失败返回null
     */
    public static LocalDateTime parseFlexibleDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        
        try {
            // 1. 尝试直接使用标准ISO解析器（支持任意长度毫秒）
            if (dateTimeStr.contains("T")) {
                try {
                    return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
                } catch (DateTimeParseException e) {
                    // 可能是不带毫秒的ISO格式，继续下一步尝试
                    Log.d(TAG, "ISO标准解析失败，尝试其他格式: " + dateTimeStr);
                }
            }
            
            // 2. 按照格式模式尝试解析
            if (dateTimeStr.contains("T")) {
                // ISO格式 (yyyy-MM-dd'T'HH:mm:ss...)
                if (dateTimeStr.contains(".")) {
                    // 可能是带毫秒的ISO格式，细分为几种情况
                    int dotIndex = dateTimeStr.indexOf('.');
                    int endIndex = dateTimeStr.length();
                    
                    // 调整毫秒位数为3位（标准）
                    String adjustedStr = dateTimeStr;
                    int msLength = endIndex - dotIndex - 1;
                    
                    if (msLength > 3) {
                        // 截断过长的毫秒部分
                        adjustedStr = dateTimeStr.substring(0, dotIndex + 4); // 只保留3位毫秒
                    } else if (msLength < 3) {
                        // 补齐不足的毫秒部分
                        StringBuilder sb = new StringBuilder(dateTimeStr);
                        for (int i = 0; i < 3 - msLength; i++) {
                            sb.append('0');
                        }
                        adjustedStr = sb.toString();
                    }
                    
                    try {
                        return LocalDateTime.parse(adjustedStr, API_DATETIME_FORMAT);
                    } catch (DateTimeParseException e) {
                        Log.d(TAG, "调整毫秒后解析仍失败: " + adjustedStr);
                        // 继续尝试其他方式
                    }
                } else {
                    // 不带毫秒的ISO格式 (yyyy-MM-dd'T'HH:mm:ss)
                    try {
                        return LocalDateTime.parse(dateTimeStr, ISO_WITHOUT_MS);
                    } catch (DateTimeParseException e) {
                        Log.d(TAG, "ISO不带毫秒解析失败: " + dateTimeStr);
                    }
                }
            } else if (dateTimeStr.contains(" ")) {
                // 标准格式 (yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd HH:mm)
                if (dateTimeStr.length() > 16) { // 包含秒
                    try {
                        return LocalDateTime.parse(dateTimeStr, STANDARD_WITH_SECONDS);
                    } catch (DateTimeParseException e) {
                        Log.d(TAG, "标准带秒格式解析失败: " + dateTimeStr);
                    }
                } else { // 不包含秒
                    try {
                        return LocalDateTime.parse(dateTimeStr, STANDARD_WITHOUT_SECONDS);
                    } catch (DateTimeParseException e) {
                        Log.d(TAG, "标准不带秒格式解析失败: " + dateTimeStr);
                    }
                }
            } else {
                // 尝试作为纯日期解析 (yyyy-MM-dd)
                try {
                    return LocalDate.parse(dateTimeStr, API_DATE_FORMAT).atStartOfDay();
                } catch (DateTimeParseException e) {
                    Log.d(TAG, "纯日期格式解析失败: " + dateTimeStr);
                }
            }
            
            // 3. 最后兜底：尝试直接用标准解析器解析，可能会抛出异常
            return LocalDateTime.parse(dateTimeStr);
            
        } catch (Exception e) {
            // 如果所有尝试都失败，记录错误并返回null
            Log.e(TAG, "所有尝试都失败，解析日期时间失败: " + dateTimeStr + ", 错误: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 获取指定天数之前的日期
     * @param days 天数
     * @return 指定天数之前的日期
     */
    public static Date getDateBefore(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return calendar.getTime();
    }
    
    /**
     * 检查日期是否为今天
     * @param date 要检查的日期
     * @return 如果是今天则返回true
     */
    public static boolean isToday(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR);
    }
    
    /**
     * 检查日期是否为昨天
     * @param date 要检查的日期
     * @return 如果是昨天则返回true
     */
    public static boolean isYesterday(Date date) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        return yesterday.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && yesterday.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR);
    }
    
    /**
     * 检查两个日期是否为同一天
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @return 如果是同一天则返回true
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * 格式化Date为日期字符串 (yyyy-MM-dd)
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return DATE_SDF.format(date);
    }
    
    /**
     * 格式化Date为时间字符串 (HH:mm)
     * @param date 日期
     * @return 格式化后的时间字符串
     */
    public static String formatTime(Date date) {
        if (date == null) {
            return "";
        }
        return TIME_SDF.format(date);
    }
    
    /**
     * 格式化Date为日期时间字符串 (yyyy-MM-dd HH:mm)
     * @param date 日期
     * @return 格式化后的日期时间字符串
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return DATETIME_SDF.format(date);
    }
    
    /**
     * 将时间戳转换为ISO格式字符串，用于与后端API交互
     * 遵循规范：yyyy-MM-dd'T'HH:mm:ss
     * 注意：不使用毫秒，因为后端LocalDateTime无法解析带毫秒的格式
     */
    public static String timestampToIsoString(long timestamp) {
        if (timestamp <= 0) {
            return null;
        }
        
        synchronized (ISO_DATETIME_NO_MS_SDF) {
            return ISO_DATETIME_NO_MS_SDF.format(new Date(timestamp));
        }
    }
    
    /**
     * 将ISO格式字符串转换为时间戳
     * 支持多种格式的ISO字符串解析
     */
    public static long isoStringToTimestamp(String isoString) {
        if (isoString == null || isoString.isEmpty()) {
            return 0;
        }
        
        try {
            // 根据字符串特征选择合适的解析格式
            if (isoString.contains("T")) {
                // ISO格式
                if (isoString.contains(".")) {
                    // 带毫秒
                    synchronized (ISO_DATETIME_SDF) {
                        return ISO_DATETIME_SDF.parse(isoString).getTime();
                    }
                } else {
                    // 不带毫秒
                    synchronized (ISO_DATETIME_NO_MS_SDF) {
                        return ISO_DATETIME_NO_MS_SDF.parse(isoString).getTime();
                    }
                }
            } else if (isoString.contains(" ")) {
                // 普通日期时间格式 (yyyy-MM-dd HH:mm:ss)
                synchronized (DATETIME_SDF) {
                    return DATETIME_SDF.parse(isoString).getTime();
                }
            } else {
                // 纯日期格式 (yyyy-MM-dd)
                synchronized (ISO_DATE_SDF) {
                    return ISO_DATE_SDF.parse(isoString).getTime();
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, "解析ISO日期时间字符串失败: " + isoString, e);
            return 0;
        }
    }
    
    /**
     * 格式化日期为UI显示格式
     */
    public static String formatDateForUI(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (DATE_SDF) {
            return DATE_SDF.format(date);
        }
    }
    
    /**
     * 格式化时间为UI显示格式
     */
    public static String formatTimeForUI(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (TIME_SDF) {
            return TIME_SDF.format(date);
        }
    }
    
    /**
     * 格式化日期时间为UI显示格式
     */
    public static String formatDateTimeForUI(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (DATETIME_SDF) {
            return DATETIME_SDF.format(date);
        }
    }
    
    /**
     * 将时间戳格式化为UI显示格式
     */
    public static String formatTimestampForUI(long timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        return formatDateTimeForUI(new Date(timestamp));
    }
    
    /**
     * 获取日期的开始时间（0点0分0秒）
     */
    public static Date startOfDay(Date date) {
        if (date == null) {
            return null;
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        return cal.getTime();
    }
    
    /**
     * 获取日期的结束时间（23点59分59秒999毫秒）
     */
    public static Date endOfDay(Date date) {
        if (date == null) {
            return null;
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        
        return cal.getTime();
    }
    
    /**
     * 增加或减少天数
     */
    public static Date addDays(Date date, int days) {
        if (date == null) {
            return null;
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        
        return cal.getTime();
    }
    
    /**
     * 增加或减少月份
     */
    public static Date addMonths(Date date, int months) {
        if (date == null) {
            return null;
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, months);
        
        return cal.getTime();
    }
} 