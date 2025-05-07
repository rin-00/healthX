package com.healthx.util;

import org.junit.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

import static org.junit.Assert.*;

/**
 * 日期时间工具类的单元测试
 */
public class DateTimeUtilsTest {

    @Test
    public void testFormatDate() {
        LocalDate date = LocalDate.of(2025, 1, 15);
        assertEquals("2025-01-15", DateTimeUtils.formatDate(date));
    }

    @Test
    public void testFormatTime() {
        LocalTime time = LocalTime.of(14, 30);
        assertEquals("14:30", DateTimeUtils.formatTime(time));
    }

    @Test
    public void testFormatDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 15, 14, 30, 45);
        assertEquals("2025-01-15 14:30", DateTimeUtils.formatDateTime(dateTime));
    }

    @Test
    public void testFormatDateTimeForApi() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 15, 14, 30, 45);
        assertEquals("2025-01-15T14:30:45", DateTimeUtils.formatDateTimeForApi(dateTime));
    }

    @Test
    public void testParseFlexibleDateTime() {
        // 测试ISO格式（带T，不带毫秒）
        LocalDateTime result1 = DateTimeUtils.parseFlexibleDateTime("2025-01-15T14:30:45");
        assertNotNull(result1);
        assertEquals(2025, result1.getYear());
        assertEquals(1, result1.getMonthValue());
        assertEquals(15, result1.getDayOfMonth());
        assertEquals(14, result1.getHour());
        assertEquals(30, result1.getMinute());
        assertEquals(45, result1.getSecond());

        // 测试ISO格式（带T，带毫秒）
        LocalDateTime result2 = DateTimeUtils.parseFlexibleDateTime("2025-01-15T14:30:45.123");
        assertNotNull(result2);
        assertEquals(2025, result2.getYear());
        assertEquals(123000000, result2.getNano());

        // 测试标准格式（不带T，带空格）
        LocalDateTime result3 = DateTimeUtils.parseFlexibleDateTime("2025-01-15 14:30:45");
        assertNotNull(result3);
        assertEquals(2025, result3.getYear());
        assertEquals(45, result3.getSecond());

        // 测试简短格式（不带秒）
        LocalDateTime result4 = DateTimeUtils.parseFlexibleDateTime("2025-01-15 14:30");
        assertNotNull(result4);
        assertEquals(2025, result4.getYear());
        assertEquals(0, result4.getSecond());

        // 测试纯日期格式
        LocalDateTime result5 = DateTimeUtils.parseFlexibleDateTime("2025-01-15");
        assertNotNull(result5);
        assertEquals(2025, result5.getYear());
        assertEquals(0, result5.getHour());
        assertEquals(0, result5.getMinute());
        
        // 测试出问题的格式 - 2025-05-04T15:02:36 (不带毫秒的ISO格式)
        LocalDateTime result6 = DateTimeUtils.parseFlexibleDateTime("2025-05-04T15:02:36");
        assertNotNull(result6);
        assertEquals(2025, result6.getYear());
        assertEquals(5, result6.getMonthValue());
        assertEquals(4, result6.getDayOfMonth());
        assertEquals(15, result6.getHour());
        assertEquals(2, result6.getMinute());
        assertEquals(36, result6.getSecond());
        
        // 测试出问题的格式 - 2025-05-03T20:44:11.094 (带非标准长度毫秒的ISO格式)
        LocalDateTime result7 = DateTimeUtils.parseFlexibleDateTime("2025-05-03T20:44:11.094");
        assertNotNull(result7);
        assertEquals(2025, result7.getYear());
        assertEquals(5, result7.getMonthValue());
        assertEquals(3, result7.getDayOfMonth());
        assertEquals(20, result7.getHour());
        assertEquals(44, result7.getMinute());
        assertEquals(11, result7.getSecond());
        assertEquals(94000000, result7.getNano());
        
        // 测试其他毫秒长度格式
        LocalDateTime result8 = DateTimeUtils.parseFlexibleDateTime("2025-05-03T20:44:11.9");
        assertNotNull(result8);
        assertEquals(900000000, result8.getNano());
        
        LocalDateTime result9 = DateTimeUtils.parseFlexibleDateTime("2025-05-03T20:44:11.12345");
        assertNotNull(result9);
        assertEquals(123000000, result9.getNano()); // 应该只保留前3位毫秒
    }

    @Test
    public void testParseInvalidDateTime() {
        // 测试无效格式
        LocalDateTime result = DateTimeUtils.parseFlexibleDateTime("invalid-date");
        assertNull(result);
    }
} 